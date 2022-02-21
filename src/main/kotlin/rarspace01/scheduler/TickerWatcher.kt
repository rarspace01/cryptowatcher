package rarspace01.scheduler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.dotenv
import io.quarkus.scheduler.Scheduled
import rarspace01.notification.TelegramService
import rarspace01.utilities.HttpHelper
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class TickerWatcher {
    private val counter = AtomicInteger()
    fun get(): Int {
        return counter.get()
    }

    @Inject
    lateinit var telegramService: TelegramService

    @Scheduled(every = "10s")
    fun getTicketUpdate() {
        println("get nomics")
        val apiKey = dotenv()["NOMICS_API_KEY"] ?: ""
        val page = HttpHelper().getPage(
            "https://api.nomics.com/v1/currencies/ticker?key=$apiKey&ids=EWT3&interval=1d&convert=EUR&per-page=100&page=1"
        )
        val objectMapper = ObjectMapper()
        try {
            val jsonNodeRoot = objectMapper.readTree(page)
            jsonNodeRoot.forEach(
                Consumer { jsonNode: JsonNode ->
                    val symbolId = jsonNode["id"].asText()
                    val tickerPrice = jsonNode["price"].asDouble()
                    // consume Ticket & price
                    if (symbolId == "EWT3" && tickerPrice < 0.3) {
                        telegramService.sendMessage("ETW3 below 0.30€")
                    }
                }
            )
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
    }
}
