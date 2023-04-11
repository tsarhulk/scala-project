package repository

import model._
import db.UserDb._
import slick.jdbc.PostgresProfile.api._

import java.util.UUID
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

class UserAccountRepositoryDb(implicit val ec: ExecutionContext, db: Database) extends UserAccountRepository{
  override def get(id: UUID): Future[UserAccount] =
    db.run(users.filter(i => i.id === id).result.head)

  override def list(): Future[Seq[UserAccount]] =
    db.run(users.result)

  override def create(user: CreateAccount): Future[UserAccount] = {
    val newUser = UserAccount(phoneNumber = user.phoneNumber, money = user.money)
    for {
      _ <- db.run(users += newUser)
      res <- get(newUser.id)
    } yield res
  }

  override def delete(id: UUID): Future[Unit] =
    db.run(users.filter(_.id === id).delete).map(_ => ())

  override def find(id: UUID): Future[Option[UserAccount]] =
    db.run(users.filter(i => i.id === id).result.headOption)

  override def updatePhoneNumber(user: UpdatePhoneNumber): Future[Option[UserAccount]] = {
    for {
      _ <- db.run {
        users.filter(_.id === user.id).map(_.phoneNumber).update(user.phoneNumber)
      }
      res <- find(user.id)
    } yield res
  }

  def transactionMoney(transaction: Transaction): Future[Either[String, UserAccount]] = {
    val query = users.filter(_.id === transaction.id).map(_.money)
    for {
      startMoney <- db.run(query.result.headOption)
      buffer = transaction.money
      updateMoney = startMoney.map {
        startMoney =>
          if (startMoney < buffer)
            Left("На вашем счете недостаточно средств для проведения данной операции")
          else
            Right(startMoney + buffer)}.getOrElse(Left("Элемент не найден"))

      future = updateMoney.map(money => db.run {query.update(money)})
      match {
        case Right(future) => future.map(Right(_))
        case Left(s) => Future.successful(Left(s))
      }

      updated <- future
      tmp_res <- find(transaction.id)
      res = updated.map(_ => tmp_res.get)
    } yield res
  }

  override def refillMoney(transaction: RefillMoney): Future[Either[String, UserAccount]] =
    transactionMoney(Transaction(transaction.id, transaction.money))

  override def withdrawMoney(transaction: WithdrawMoney): Future[Either[String, UserAccount]] =
    transactionMoney(Transaction(transaction.id, -transaction.money))

  private val categoryCashback = Map(
    0 -> 0.00, //кэшбэк на переводы
    1 -> 0.01, //кэшбэк на все покупки
    2 -> 0.05, //повышенный кэшбэк на выбранные категории (например, спорттовары, красота и прочее)
    3 -> 0.1, //повышенный кэшбэк у партнёров
    4 -> 0.15 //кэшбэк дня
  )
  private val defaultCashback = 0.00

  override def moneyTransfer(transaction: MoneyTransfer): Future[Either[String, ChangeMoneyResult]] = {
    val sender = users.filter(_.id === transaction.senderId).map(_.money) //отправитель
    val recipient = users.filter(_.id === transaction.recipientId).map(_.money) //получатель

    for {
      senderOpt <- db.run(sender.result.headOption)
      recipientOpt <- db.run(recipient.result.headOption)

      buffer = transaction.money

      //кешбэк
      cashbackMoney = (
        buffer.toDouble * categoryCashback.getOrElse(transaction.category, defaultCashback)).toInt
      bufferWithCashback = buffer - cashbackMoney

      senderUpd = senderOpt.map {
        senderMoney => {
          if (senderMoney >= bufferWithCashback)
            Right(senderMoney - bufferWithCashback)
          else
            Left("На вашем счете недостаточно средств для проведения данной операции")}}.getOrElse(Left("Элемент не найден"))

      recipientUpd = recipientOpt.map {
        recipientMoney => {
          Right(recipientMoney + buffer)}}.getOrElse(Left("Элемент не найден"))

      senderFuture =
        senderUpd.map(money => db.run(sender.update(money)))

      match {
        case Right(_) =>
          recipientUpd.map(money => db.run(recipient.update(money)))
          match {
            case Right(future) => future.map(Right(_))
            case Left(s) => Future.successful(Left(s))
          }
        case Left(s) => Future.successful(Left(s))
      }
      updated <- senderFuture
      res <- find(transaction.senderId)
    } yield updated.map(_ => ChangeMoneyResult(transaction.senderId, res.get.money))
  }
}
