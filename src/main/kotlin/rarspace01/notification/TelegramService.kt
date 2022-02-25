package rarspace01.notification

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.dotenv
import rarspace01.configuration.Configuration
import rarspace01.configuration.ConfigurationRepository
import rarspace01.utilities.HttpHelper
import java.net.URLEncoder
import java.nio.charset.Charset
import javax.enterprise.context.ApplicationScoped
import kotlin.math.max

@ApplicationScoped
class TelegramService(private val configurationRepository: ConfigurationRepository) {
    private val telegramApiKey = dotenv { ignoreIfMissing = true }["TELEGRAM_API_KEY"] ?: ""

    private var offset: Long = getOffsetFromDatabase()

    private val subscriberList = mutableSetOf<String>()

    private fun getOffsetFromDatabase() = configurationRepository.findAll().firstResult<Configuration>()?.offset ?: 0

    private fun saveOffsetToDatabase(offset: Long) {
        val configuration = configurationRepository.findAll().firstResult() ?: Configuration(offset = offset)
        configurationRepository.persist(configuration)
    }

    fun getNewMessages(): List<Message> {

        val url = "https://api.telegram.org/bot$telegramApiKey/getUpdates?offset=$offset"
        val page = HttpHelper().getPage(url)
        val objectMapper = ObjectMapper()
        return try {
            val jsonNodeRoot = objectMapper.readTree(page)
            if (jsonNodeRoot != null && !jsonNodeRoot.get("result").isNull) {
                jsonNodeRoot.get("result").mapNotNull {
                    if (it["message"] == null || it["message"]["chat"] == null || it["message"]["chat"]["id"] == null || it["update_id"] == null) {
                        null
                    } else {
                        val chatId = it["message"]["chat"]["id"].asText()
                        val message = it["message"]["text"].asText()
                        val updateId = it["update_id"].asLong()
                        Message(
                            chatId,
                            message,
                            updateId
                        )
                    }
                }.filter { it.updateId > offset }
                    .onEach {
                        if (it.message != "/stop") {
                            subscriberList.add(it.chatId)
                        } else {
                            subscriberList.remove(it.chatId)
                        }

                        offset = max(offset, it.updateId)
                        saveOffsetToDatabase(offset)
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
        HttpHelper().getPage(
            "https://api.telegram.org/bot$telegramApiKey/sendMessage?chat_id=$chatId&text=${
            URLEncoder.encode(
                text,
                Charset.defaultCharset()
            )
            }"
        )
    }

}
