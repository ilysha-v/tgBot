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
      .flatMap(x => Unmarshal(x.entity).to[Response])
  }

  def getPossibleCharacters(session: SessionInfo): Future[CharactersResponse] = {
    // todo check for useless params
    lazy val url = s"$baseUrl/ws/list?session=${session.id.value}&signature=${session.signature.value}&step=${session.step.value}&size=1&max_pic_width=246&max_pic_height=294&pref_photos=OK-FR&mode_question=0"
//    http://api-ru1.akinator.com/ws/list?session=2168&signature=247858744&step=11&size=2&max_pic_width=246&max_pic_height=294&pref_photos=OK-FR&mode_question=0
//    {"completion":"OK","parameters":{"elements":[{"element":{"id":"6578","name":"\u041f\u0443\u0442\u0438\u043d-\u043a\u0440\u0430\u0431","id_base":"1269562","proba":"0.942501","description":"\u0438\u043d\u0442\u0435\u0440\u043d\u0435\u0442-\u043c\u0435\u043c","valide_contrainte":"1","ranking":"797","minibase_addable":"0","relative_id":"-1","pseudo":"none","picture_path":"partenaire\/q\/1269562__1022946386.jpg","absolute_picture_path":"http:\/\/photos.clarinea.fr\/BL_6_ru\/600\/partenaire\/q\/1269562__1022946386.jpg"}},{"element":{"id":"11614","name":"\u0412\u043b\u0430\u0434\u0438\u043c\u0438\u0440 \u0412\u043b\u0430\u0434\u0438\u043c\u0438\u0440\u043e\u0432\u0438\u0447 \u041f\u0443\u0442\u0438\u043d","id_base":"1327273","proba":"0.0384788","description":"\u043f\u0440\u0435\u0437\u0438\u0434\u0435\u043d\u0442 \u0420\u0424","valide_contrainte":"1","ranking":"309","minibase_addable":"0","relative_id":"-1","pseudo":"\u0432\u043e\u0432\u0430","picture_path":"partenaire\/v\/1327273__915292369.jpg","absolute_picture_path":"http:\/\/photos.clarinea.fr\/BL_6_ru\/partenaire\/v\/1327273__915292369.jpg"}}],"NbObjetsPertinents":"2"}}

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
