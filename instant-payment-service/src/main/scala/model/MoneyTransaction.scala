package model

import java.util.UUID

// проведение транзакции
case class Transaction(id: UUID, money: Int)

// пополнение счёта
case class RefillMoney(id: UUID, money: Int)

// снятие денег со счёта
case class WithdrawMoney(id: UUID, money: Int)

// перевод со счёта на счёт (отправитель, получатель, сумма перевода, категория)
case class MoneyTransfer(senderId: UUID, recipientId: UUID, money: Int, category: Int = 0)

// результат - изменение счёта отправителя
case class ChangeMoneyResult(senderId: UUID, moneyResult: Int)
