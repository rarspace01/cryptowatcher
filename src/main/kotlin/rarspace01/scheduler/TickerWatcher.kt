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
        val allSubscriptions = subscriptionRepository.findAll().stream<Subscription>().toList()
        val ticker = allSubscriptions.map { it.ticker }.toList().toSet()
        if(ticker.isEmpty()) return
        println("get nomics")
        val tickerIds = ticker.joinToString(separator = ",")
        val apiKey = dotenv { ignoreIfMissing = true }["NOMICS_API_KEY"] ?: ""
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
                Ticker(tickerHandle = symbolId, tickerValue = tickerPrice).also {
                    println(it)
                }
            }.forEach { nomicsTicker ->
                val subscriptionsForTicker = allSubscriptions.filter { it.ticker == nomicsTicker.tickerHandle }
                // is update less alarm
                subscriptionsForTicker.filter { it.isLessThan && nomicsTicker.tickerValue < it.value }.forEach {
                    telegramService.sendMessage(it.user, "${it.ticker} is below ${it.value}")
                }
                // is update more alarm
                subscriptionsForTicker.filter { !it.isLessThan && nomicsTicker.tickerValue > it.value }.forEach {
                    telegramService.sendMessage(it.user, "${it.ticker} is above ${it.value}")
                }
            }
            // check each ticker for subscription hit
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
    }
}
