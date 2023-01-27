package repository

import model._

import java.util.UUID
import scala.concurrent.Future

trait UserAccountRepository {
  def get(id: UUID): Future[UserAccount]
  def list(): Future[Seq[UserAccount]]
  def create(user: CreateAccount): Future[UserAccount]
  def find(id: UUID): Future[Option[UserAccount]]
  def updatePhoneNumber(user: UpdatePhoneNumber): Future[Option[UserAccount]]
  def delete(id: UUID): Future[Unit]
  def refillMoney(transaction: RefillMoney): Future[Either[String, UserAccount]]
  def withdrawMoney(transaction: WithdrawMoney): Future[Either[String, UserAccount]]
  def moneyTransfer(transaction: MoneyTransfer): Future[Either[String, ChangeMoneyResult]]

}
