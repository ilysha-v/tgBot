package tgBot.storage

import tgBot.akClient.SessionId
import tgBot.tgClient.ChatId

trait Storage {
  def getSessionId(chatId: ChatId): Option[SessionId]

  def saveSessionId(chatId: ChatId, sessionId: SessionId)
}

// todo should use redis or something like for it
class MemoryStorage extends Storage {
  val storage = new scala.collection.concurrent.TrieMap[ChatId, SessionId]

  override def getSessionId(chatId: ChatId): Option[SessionId] = {
    storage.get(chatId)
  }

  override def saveSessionId(chatId: ChatId, sessionId: SessionId): Unit = {
    storage.put(chatId, sessionId)
  }
}
