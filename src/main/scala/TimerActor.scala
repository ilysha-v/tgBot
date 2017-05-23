import TgClient.TelegramApi
import akka.actor.{Actor, Props}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class TimerActor(telegramApi: TelegramApi) extends Actor {
  implicit val system = context.system
  implicit val ec: ExecutionContext = system.dispatcher
  val subscriptionActor = system.actorOf(Props[SubscriptionActor](new SubscriptionActor(telegramApi)))

  val scheduler = system.scheduler.schedule(10 seconds, 5 seconds) {
    subscriptionActor ! SubscriptionActor.Check
  }

  def receive: Receive = {
    case Terminate =>
      scheduler.cancel()
      subscriptionActor ! Terminate
  }
}
