package tgBot

import akka.http.scaladsl.server.{MalformedRequestContentRejection, RejectionHandler}
import com.typesafe.scalalogging.StrictLogging
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._

object TelegramRejectionHandler extends StrictLogging {
  def apply: RejectionHandler = {
    RejectionHandler.newBuilder()
      .handle {
        case MalformedRequestContentRejection(msg, ex) =>
          logger.error(s"Bad request: $msg", ex)
          complete((BadRequest, "Bad request"))
      }.result()
  }

}
