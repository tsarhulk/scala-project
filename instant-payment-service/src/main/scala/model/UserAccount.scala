package model

import java.util.UUID

case class UserAccount(id: UUID = UUID.randomUUID(), phoneNumber: Int, money: Int)

case class CreateAccount(phoneNumber: Int, money: Int)
case class UpdatePhoneNumber(id: UUID, phoneNumber: Int)