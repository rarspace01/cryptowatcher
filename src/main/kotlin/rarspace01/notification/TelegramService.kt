package rarspace01.notification

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import rarspace01.utilities.HttpHelper
import java.net.URLEncoder
import java.nio.charset.Charset
import javax.enterprise.context.ApplicationScoped
import kotlin.math.max

@ApplicationScoped
class TelegramService {
    private val telegramApiKey = "5234041452:AAHRi9tV3cvqSH-1sAUEfvzw9AW7MbbpFxw"

    private var offset: Long? = null

    private val subscriberList = mutableSetOf<String>()

    fun getNewMessages(): List<Message> {

        val url = "https://api.telegram.org/bot$telegramApiKey/getUpdates" + (if (offset != null) "?offset=$offset" else "")
        val page = HttpHelper().getPage(url)
        val objectMapper = ObjectMapper()
        return try {
            val jsonNodeRoot = objectMapper.readTree(page)
            if (jsonNodeRoot != null && !jsonNodeRoot.get("result").isNull) {
                jsonNodeRoot.get("result").map {
                    Message(it["message"]["chat"]["id"].asText(), it["message"]["text"].asText(), it["update_id"].asLong())
                }.filter { it.updateId > (offset ?: 0) }
                    .onEach {
                        if (it.message != "stop") {
                            subscriberList.add(it.chatId)
                        } else {
                            subscriberList.remove(it.chatId)
                        }

                        offset = max(offset ?: 0, it.updateId)
                    }
            } else {
                emptyList()
            }
        } catch (e: JsonProcessingException) {
            emptyList()
        }
    }

    fun sendMessage(chatId: String, text: String) {
        val params = mapOf("chat_id" to chatId, "text" to text)
        val page = HttpHelper().getPage(
            "https://api.telegram.org/bot$telegramApiKey/sendMessage?chat_id=$chatId&text=${
            URLEncoder.encode(
                text,
                Charset.defaultCharset()
            )
            }"
        )
    }

    fun sendMessage(text: String) {
        subscriberList.forEach {
            sendMessage(it, text)
        }
    }
}