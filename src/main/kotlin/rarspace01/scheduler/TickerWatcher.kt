package rarspace01.scheduler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.dotenv
import io.quarkus.scheduler.Scheduled
import rarspace01.notification.TelegramService
import rarspace01.ticker.Ticker
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
        val apiKey = dotenv {ignoreIfMissing = true}["NOMICS_API_KEY"] ?: ""
        val page = HttpHelper().getPage(
            "https://api.nomics.com/v1/currencies/ticker?key=$apiKey&ids=EWT3&interval=1d&convert=EUR&per-page=100&page=1"
        )
        val objectMapper = ObjectMapper()
        try {
            val jsonNodeRoot = objectMapper.readTree(page)
            jsonNodeRoot.map {
                    val symbolId = it["id"].asText()
                    val tickerPrice = it["price"].asDouble()
                    Ticker(tickerHandle = symbolId, tickerValue = tickerPrice)
                }
                //check each ticker for subscription hit

        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
    }
}
