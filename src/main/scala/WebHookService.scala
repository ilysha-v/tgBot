import tgBot.akClient.AkClient
import tgBot.actors.RouterActor
import tgBot.TelegramRejectionHandler
import tgBot.tgClient.{TelegramApi, TelegramJsonProtocol, TelegramUpdate}
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.{HttpApp, Route}
import com.typesafe.scalalogging.StrictLogging

class WebHookService(
  telegramApi: TelegramApi,
  akClient: AkClient
)(implicit system: ActorSystem)
  extends HttpApp with StrictLogging {
  import SprayJsonSupport._
  import TelegramJsonProtocol._

  system.actorOf(Props[TimerActor](new TimerActor(telegramApi)))
  val routerRef = system.actorOf(Props[RouterActor](new RouterActor(akClient, telegramApi)))
  implicit val ex = system.dispatcher

  def route: Route =
    handleRejections(TelegramRejectionHandler.apply) {
      pathPrefix("api") {
        get {
          path("health") {
            complete("ok")
          }
        } ~
          post {
            path("update") {
              entity(as[TelegramUpdate]) { content =>
                logger.debug(s"Got message from ${content.message.from.first_name}: ${content.message.text}")
                routerRef ! content
                complete("ok")
              }
            }
          }
      }
    }

  override protected def postHttpBindingFailure(ex: Throwable): Unit = {
    logger.error(s"The server could not be started", ex)
  }
}
