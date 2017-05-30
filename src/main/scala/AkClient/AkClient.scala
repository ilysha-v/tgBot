package AkClient

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

class AkClient()(implicit system: ActorSystem) extends StrictLogging {

  implicit val materializer = ActorMaterializer()
  implicit val ex: ExecutionContext = system.dispatcher

  def startSession(): Future[Response] = {
    ???
  }
}
