package rarspace01.scheduler

import io.quarkus.scheduler.Scheduled
import rarspace01.notification.TelegramService
import rarspace01.notification.TickerSubscriptionService
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class TelegramMessageWatcher {

    val subscriptionRegex = Regex("([A-Za-z0-9]+)\\s([<>]+)\\s([0-9.,]+)")

    @Inject
    lateinit var telegramService: TelegramService

    @Inject
    lateinit var tickerSubscriptionService: TickerSubscriptionService

    @Scheduled(every = "5s")
    fun checkForNewMessages() {
        println("get TG messages")
        telegramService.getNewMessages().forEach { message ->
            if (message.message == "/start") {
                telegramService.sendMessage(message.chatId, "you are now subscribed! Add token to watch with `/add TokenId`  you can stop anytime with `/stop`")
            } else if (message.message.startsWith("/add")) {
                val messageFromUser = message.message.replace("/add ", "")
                subscriptionRegex.findAll(messageFromUser).iterator().forEach {
                    if (it.groups.size == 3) {
                        val ticker = it.groups[1]?.value ?: ""
                        val isLessThan = it.groups[2]?.value == "<"
                        val tickerValue = it.groups[3]?.value?.toDoubleOrNull() ?: -1.0
                        tickerSubscriptionService.addSubscription(ticker, message.chatId, tickerValue, isLessThan)
                    } else {
                        telegramService.sendMessage(message.chatId, "please use correct format. check /help")
                    }
                }
            } else if (message.message.startsWith("/stop")) {
                tickerSubscriptionService.removeAllSubscriptions(message.chatId)
            } else if (message.message.startsWith("/help")) {
                telegramService.sendMessage(
                    message.chatId,
                    "`/stop` stops all subscriptions\n" +
                        "`/add BTC < 0.5` - subscribe to ticker when below 0.50€\n" +
                        "`/stop BTC` - remove subscriptions to ticker"
                )
            } else {
                println(message.message + "@" + message.chatId)
                telegramService.sendMessage(message.chatId, "message not yet supported. use /help for commands")
            }
        }
    }
}
