package rarspace01.scheduler

import io.quarkus.scheduler.Scheduled
import rarspace01.notification.TelegramService
import rarspace01.notification.TickerSubscriptionService
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

@ApplicationScoped
class TelegramMessageWatcher {

    @Inject
    lateinit var telegramService: TelegramService

    @Inject
    lateinit var tickerSubscriptionService: TickerSubscriptionService

    @Scheduled(every = "5s")
    fun checkForNewMessages() {
        println("get TG messages")
        telegramService.getNewMessages().forEach {
            if (it.message == "/start") {
                telegramService.sendMessage(it.chatId, "you are now subscribed! Add token to watch with `/add TokenId`  you can stop anytime with `/stop`")
            } else if (it.message.startsWith("/add")) {
                tickerSubscriptionService.addSubscription(it.message.replace("/add ", ""), it.chatId)
            } else if (it.message.startsWith("/stop")) {
                tickerSubscriptionService.removeAllSubscriptions(it.chatId)
            } else if (it.message.startsWith("/help")) {
                telegramService.sendMessage(
                    it.chatId,
                    "`/stop` stops all subscriptions\n" +
                        "`/add BTC < 0.5` - subscribe to ticker when below 0.50€\n" +
                        "`/stop BTC` - remove subscriptions to ticker"
                )
            } else {
                println(it.message + "@" + it.chatId)
                telegramService.sendMessage(it.chatId, "message not yet supported. use /help for commands")
            }
        }
    }
}
