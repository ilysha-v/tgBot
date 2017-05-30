package TgClient

import TgBot.Config
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.util.ByteString
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

class TelegramApi(config: Config)(implicit system: ActorSystem) extends StrictLogging {
  import SprayJsonSupport._
  import TelegramJsonProtocol._
  import spray.json._

  private def getUrl(method: String) = s"https://api.telegram.org/bot${config.token}/$method"
  implicit val materializer = ActorMaterializer()
  implicit val ex: ExecutionContext = system.dispatcher

  def getWebHookInfo(): Future[WebHookInfo] = {
    val request = HttpRequest(uri = getUrl("getWebhookInfo"))
    Http()
      .singleRequest(request)
      .flatMap(x => Unmarshal(x.entity).to[TelegramApiResponse[WebHookInfo]])
      .map { response =>
        response.result
      }
  }

  def setWebHookInfo(): Future[Boolean] = {
    val payload = HttpEntity.Strict(
      contentType = ContentTypes.`application/json`,
      data = ByteString(SetWebHookPayload(config.webhookPath).toJson.compactPrint)
    )

    val request = HttpRequest(
      uri = getUrl("setWebhook"),
      method = HttpMethods.POST,
      entity = payload
    )

    Http()
      .singleRequest(request)
      .flatMap(x => Unmarshal(x.entity).to[TelegramApiResponse[Boolean]])
      .map(x => x.result)
  }

  def sendMessage(message: ResponseMessage): Future[Boolean] = {
    val payload = HttpEntity.Strict(
      contentType = ContentTypes.`application/json`,
      data = ByteString(message.toJson.compactPrint)
    )

    val request = HttpRequest(
      uri = getUrl("sendMessage"),
      method = HttpMethods.POST,
      entity = payload
    )

    Http()
      .singleRequest(request)
      .flatMap(x => Unmarshal(x.entity).to[TelegramApiResponse[Boolean]])
      .map(x => x.result)
  }
}
