package tgBot.akClient

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging
import tgBot.SessionInfo

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
      s"$baseUrl/ws/answer?session=${session.id.value}&signature=${session.signature.value}&step=${session.step.value}&answer=$answerCode"

    logger.debug(s"Ak request: $url")

    val request = HttpRequest(
      uri = url,
      method = HttpMethods.GET
    )

    Http()
      .singleRequest(request)
      .flatMap{ x =>
        logger.debug(x.entity.toString)
        Unmarshal(x.entity).to[Response]
      }
  }

  def getPossibleCharacters(session: SessionInfo): Future[CharactersResponse] = {
    // todo check for useless params
    lazy val url = s"$baseUrl/ws/list?session=${session.id.value}&signature=${session.signature.value}&step=${session.step.value}&size=1&max_pic_width=246&max_pic_height=294&pref_photos=OK-FR&mode_question=0"

    val request = HttpRequest(
      uri = url,
      method = HttpMethods.GET
    )

    logger.debug(s"Trying to get characters lost from ak: $url")

    Http()
      .singleRequest(request)
      .flatMap(x => Unmarshal(x.entity).to[CharactersResponse])
  }
}
