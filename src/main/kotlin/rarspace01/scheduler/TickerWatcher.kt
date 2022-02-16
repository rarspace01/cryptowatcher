package rarspace01.scheduler

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.quarkus.scheduler.Scheduled
import rarspace01.notification.TelegramService
import rarspace01.utilities.HttpHelper
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import io.github.cdimascio.dotenv.dotenv


@ApplicationScoped
class TickerWatcher {
    private val counter = AtomicInteger()
    fun get(): Int {
        return counter.get()
    }

    @Inject
    lateinit var telegramService: TelegramService

    @Scheduled(every = "10s")
    fun increment() {
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
                    if(symbolId == "EWT3" && tickerPrice<0.3) {
                        telegramService.sendMessage("ETW3 below 0.5€")
                    }
                }
            )
        } catch (e: JsonProcessingException) {
            e.printStackTrace()
        }
        // convert to object
        // get price
        // if price < limit alert
        // alert subscriber of token
    }

//    @Scheduled(every = "10s")
//    fun takleTGMessages() {
//        println("get TGM")
//        telegramService!!.getNewMessages().forEach(
//            Consumer { (chatId, text): Message ->
//                telegramService!!.sendMessage(
//                    chatId, text
//                )
//            }
//        )
//    }
}
