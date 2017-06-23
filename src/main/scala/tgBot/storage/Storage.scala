package tgBot.storage

import tgBot.akClient.{SessionId, Signature, Step}
import tgBot.tgClient.ChatId

case class SessionInfo(id: SessionId, step: Step, signature: Signature)

trait Storage {
  def getSession(chatId: ChatId): Option[SessionInfo]

  def saveSession(chatId: ChatId, sessionInfo: SessionInfo): Unit

  def removeSession(chatId: ChatId): Unit
}

// todo should use redis or something like for it
class MemoryStorage extends Storage {
  val storage = new scala.collection.concurrent.TrieMap[ChatId, SessionInfo]

  override def getSession(chatId: ChatId): Option[SessionInfo] = {
    storage.get(chatId)
  }

  override def saveSession(chatId: ChatId, sessionInfo: SessionInfo): Unit = {
    storage.put(chatId, sessionInfo)
  }

  override def removeSession(chatId: ChatId): Unit = {
    storage.remove(chatId)
  }
}
