import route._
import repository._
import db.InitDb
import akka.http.scaladsl.Http
import akka.actor.ActorSystem
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce._
import slick.jdbc.PostgresProfile

import scala.concurrent.ExecutionContextExecutor
import slick.jdbc.PostgresProfile.api._

object ServiceApp extends App with FailFastCirceSupport {
  implicit private val system: ActorSystem = ActorSystem("UserApp")
  implicit private val ec: ExecutionContextExecutor = system.dispatcher
  implicit private val db: PostgresProfile.backend.Database = Database.forConfig("database.postgres")

  val repository = new UserAccountRepositoryDb
  val helloRoute = new HelloRoute().route
  val userRoute = new UserAccountRoute(repository).route
  val transactionRoute = new MoneyTransactionRoute(repository).route

  new InitDb().init()

  Http().newServerAt("0.0.0.0", port = 8080).bind(helloRoute ~ userRoute ~ transactionRoute)
  println("Сервер запущен")
}
