package rarspace01.scheduler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.cdimascio.dotenv.dotenv
import io.quarkus.scheduler.Scheduled
import rarspace01.notification.Subscription
import rarspace01.notification.SubscriptionRepository
import rarspace01.notification.TelegramService
import rarspace01.ticker.Ticker
import rarspace01.utilities.HttpHelper
import java.util.concurrent.atomic.AtomicInteger
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class TickerWatcher(private val subscriptionRepository: SubscriptionRepository) {
    private val counter = AtomicInteger()
    fun get(): Int {
        return counter.get()
    }

    @Inject
    lateinit var telegramService: TelegramService

    @Scheduled(every = "10s")
    fun getTicketUpdate() {
        val ticker = subscriptionRepository.findAll().stream<Subscription>().map { it.ticker }.toList().toSet()
        println("get nomics")
        val tickerIds = ticker.joinToString(separator = ",")
        val apiKey = dotenv {ignoreIfMissing = true}["NOMICS_API_KEY"] ?: ""
        val url = "https://api.nomics.com/v1/currencies/ticker?key=$apiKey&ids=$tickerIds&interval=1d&convert=EUR&per-page=100&page=1"
        println(url)
        val page = HttpHelper().getPage(
            url
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
