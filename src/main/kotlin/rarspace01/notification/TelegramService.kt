package rarspace01.notification

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.dotenv
import rarspace01.utilities.HttpHelper
import java.net.URLEncoder
import java.nio.charset.Charset
import javax.enterprise.context.ApplicationScoped
import kotlin.math.max

@ApplicationScoped
class TelegramService {
    private val telegramApiKey = dotenv {ignoreIfMissing = true}["TELEGRAM_API_KEY"] ?: ""

    private var offset: Long? = null

    private val subscriberList = mutableSetOf<String>()

    fun getNewMessages(): List<Message> {

        val url = "https://api.telegram.org/bot$telegramApiKey/getUpdates" + (if (offset != null) "?offset=$offset" else "")
        println("$url - check Messages")
        val page = HttpHelper().getPage(url)
        val objectMapper = ObjectMapper()
        return try {
            val jsonNodeRoot = objectMapper.readTree(page)
            if (jsonNodeRoot != null && !jsonNodeRoot.get("result").isNull) {
                jsonNodeRoot.get("result").mapNotNull {
                    if (it["message"].isNull || it["update_id"].isNull) {
                    null
                    }
                    val chatId = it["message"]["chat"]["id"].asText()
                    val message = it["message"]["text"].asText()
                    val updateId = it["update_id"].asLong()
                    Message(
                        chatId,
                        message,
                        updateId
                    )
                }.filter { it.updateId > (offset ?: 0) }
                    .onEach {
                        if (it.message != "/stop") {
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
