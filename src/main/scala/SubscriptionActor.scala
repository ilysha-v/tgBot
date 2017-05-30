import SubscriptionActor.Check
import TgClient.TelegramApi
import akka.actor.Actor
import com.typesafe.scalalogging.StrictLogging

import scala.concurrent.{ExecutionContext, Future}

class SubscriptionActor(telegramApi: TelegramApi) extends Actor with StrictLogging {

  implicit val ec: ExecutionContext = context.system.dispatcher

  def receive: Receive = {
    case Check =>
      context.become(checking)
      (for {
        needToSubscribe <- subscriptionNeeded()
        if needToSubscribe
        _ = logger.warn("Subscription not found. Trying to subscribe")
        subscriptionResult = telegramApi.setWebHookInfo()
      } yield subscriptionResult).onComplete { _ => context.become(receive) }
  }

  def checking: Receive = {
    case Check =>
  }

  private def subscriptionNeeded(): Future[Boolean] = {
    val hookStatus = telegramApi.getWebHookInfo()
    hookStatus.map { info =>
      info.url match {
        case Some(x) => false // todo verify url?
        case None => true
      }
    }
  }
}

object SubscriptionActor {
  case class Check()
}
