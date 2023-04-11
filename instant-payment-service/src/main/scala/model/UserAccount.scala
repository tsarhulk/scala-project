package model

import java.util.UUID

case class UserAccount(id: UUID = UUID.randomUUID(), phoneNumber: String, money: Int)

case class CreateAccount(phoneNumber: String, money: Int)
case class UpdatePhoneNumber(id: UUID, phoneNumber: String)
