package repository

import model._

import java.util.UUID
import scala.concurrent.Future

class UserAccountRepositoryDb extends UserAccountRepository{
  override def get(id: UUID): Future[UserAccount] = ???

  override def list(): Future[Seq[UserAccount]] = ???

  override def create(user: CreateAccount): Future[UserAccount] = ???

  override def delete(id: UUID): Future[Unit] = ???

  override def find(id: UUID): Future[Option[UserAccount]] = ???

  override def updatePhoneNumber(user: UpdatePhoneNumber): Future[Option[UserAccount]] = ???

  override def refillMoney(transaction: RefillMoney): Future[Either[String, UserAccount]] = ???

  override def withdrawMoney(transaction: WithdrawMoney): Future[Either[String, UserAccount]] = ???

  override def moneyTransfer(transaction: MoneyTransfer): Future[Either[String, ChangeMoneyResult]] = ???
}
