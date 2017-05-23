import TgBot.Config
import TgClient.TelegramApi
import akka.actor.ActorSystem
import akka.http.scaladsl.settings.ServerSettings
import com.typesafe.config.ConfigFactory
import pureconfig._

object Service {
  def main(args: Array[String]): Unit = {

    loadConfig[Config] match {
      case Right(config) =>
        implicit val system = ActorSystem()
        val tgApi = new TelegramApi(config)

        new WebHookService(tgApi)
          .startServer("0.0.0.0", config.apiPort, ServerSettings(ConfigFactory.load), system)
      case Left(l) => throw new RuntimeException(l.toList.mkString(" "))
    }
  }
}
