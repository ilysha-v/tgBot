package tgBot.akClient

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import tgBot.storage.SessionInfo

import scala.concurrent.{ExecutionContext, Future}

class AkClient()(implicit system: ActorSystem) extends StrictLogging {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import AkJsonProtocol._

  implicit val materializer = ActorMaterializer()
  implicit val ex: ExecutionContext = system.dispatcher // should use own execution context, not from akka

  val baseUrl = "http://api-ru1.akinator.com" // todo to config

  def startSession(): Future[InitialResponse] = {
    lazy val url = s"$baseUrl/ws/new_session?partner=1"

    val request = HttpRequest(
      uri = url,
      method = HttpMethods.GET
    )

    Http()
      .singleRequest(request)
      .flatMap(x => Unmarshal(x.entity).to[InitialResponse])
  }

  def sendResponse(answerCode: Int, session: SessionInfo): Future[Response] = {
    lazy val url =
      s"$baseUrl/ws/answer?session={${session.id}}&signature=${session.signature}&step=${session.step}&answer=$answerCode"

    val request = HttpRequest(
      uri = url,
      method = HttpMethods.GET
    )

    Http()
      .singleRequest(request)
      .flatMap(x => Unmarshal(x.entity).to[Response])
  }
}
