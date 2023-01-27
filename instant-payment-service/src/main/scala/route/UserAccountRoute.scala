package route

import model._
import repository._

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce._
import io.circe.generic.auto._
import java.util.UUID

class UserAccountRoute (repository: UserAccountRepository) extends FailFastCirceSupport {
  def route: Route = {
    (path("users") & get) { //список всех пользователей
      val list = repository.list()
      complete(list)
    } ~
      path("user") { //создаём новый счет
        (post & entity(as[CreateAccount])) { newAccount =>
          complete(repository.create(newAccount))
        }
      } ~
      path("user" / JavaUUID) { id => //выдача идентификатора пользователя
        get {
          complete(repository.get(id))
        }
      } ~
      path("user") { //обновляем номер телефона пользователя
        (put & entity(as[UpdatePhoneNumber])) {
          updateNumberPhone =>
            complete(repository.updatePhoneNumber(updateNumberPhone))
        }
      } ~
      path("user" / JavaUUID) { id => //удаляем счет
        delete {
          complete(repository.delete(id))
        }
      } ~
      (put & path("user" / "money" / "refill") & parameters("id".as[UUID], "money".as[Int])) {
        (id, money) => // пополняем счёт
          onSuccess(repository.refillMoney(RefillMoney(id, money))) {
            case Right(value) => complete(value)
            case Left(s) => complete(StatusCodes.NotAcceptable, s)
          }
      } ~
      (put & path("user" / "money" / "withdraw") & parameters("id".as[UUID], "money".as[Int])) {
        (id, money) => // списываем деньги со счёта
          onSuccess(repository.withdrawMoney(WithdrawMoney(id, money))) {
            case Right(value) => complete(value)
            case Left(s) => complete(StatusCodes.NotAcceptable, s)
          }
      }
  }
}
