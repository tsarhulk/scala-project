package db

import model.UserAccount
import slick.jdbc.PostgresProfile.api._

import java.util.UUID

object UserDb {
  //case class UserAccount(id: UUID = UUID.randomUUID(), phoneNumber: Int, money: Int)
  class Users(tag: Tag) extends Table[UserAccount](tag, "users") {
    val id = column[UUID]("id", O.PrimaryKey)
    val phoneNumber = column[String]("phoneNumber")
    val money = column[Int]("money")

    def * = (id, phoneNumber, money) <> ((UserAccount.apply _).tupled, UserAccount.unapply)
  }

  val users = TableQuery[Users]
}
