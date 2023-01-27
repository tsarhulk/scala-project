package route

import model._
import repository._

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import de.heikoseeberger.akkahttpcirce._
import io.circe.generic.auto._

class MoneyTransactionRoute (repository: UserAccountRepository) extends FailFastCirceSupport {
  def route: Route = path("transaction") {
    (put & entity(as[MoneyTransfer])) {
      moneyTransfer =>
      onSuccess(repository.moneyTransfer(moneyTransfer)) {
        case Right(value) => complete(value)
        case Left(s) => complete(StatusCodes.NotAcceptable, s)
      }
    }
  }
}
