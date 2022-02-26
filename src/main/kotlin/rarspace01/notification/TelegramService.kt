package rarspace01.notification

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.dotenv
import rarspace01.configuration.ServiceConfiguration
import rarspace01.configuration.ServiceConfigurationRepository
import rarspace01.utilities.HttpHelper
import java.net.URLEncoder
import java.nio.charset.Charset
import javax.enterprise.context.ApplicationScoped
import kotlin.math.max

@ApplicationScoped
class TelegramService(private val serviceConfigurationRepository: ServiceConfigurationRepository) {
    private val telegramApiKey = dotenv { ignoreIfMissing = true }["TELEGRAM_API_KEY"] ?: ""

    private fun getOffsetFromDatabase(): Long {
        return serviceConfigurationRepository.findAll().firstResult<ServiceConfiguration>()?.offset
            ?: 0L
    }

    private fun saveOffsetToDatabase(offset: Long) {
        var serviceConfiguration = serviceConfigurationRepository.findAll().firstResultOptional<ServiceConfiguration>().orElse(ServiceConfiguration())
        serviceConfiguration.offset = offset
        serviceConfigurationRepository.persistOrUpdate(serviceConfiguration)
    }

    fun getNewMessages(): List<Message> {
        var offset = getOffsetFromDatabase()

        val url = "https://api.telegram.org/bot$telegramApiKey/getUpdates?offset=$offset"
        println("$url")
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
