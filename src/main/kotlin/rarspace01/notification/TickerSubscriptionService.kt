package rarspace01.notification

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class TickerSubscriptionService(private val subscriptionRepository: SubscriptionRepository) {

    fun addSubscription(ticker: String, user: String, tickerValue: Double, isLessThan: Boolean) {
        var subscription = Subscription()
        subscription.ticker = ticker
        subscription.user = user
        subscription.value = tickerValue
        subscription.isLessThan = isLessThan
        println("persisting subscription: $subscription")
        subscriptionRepository.persist(subscription)
    }

    fun removeSubscription(ticker: String, user: String) {
        subscriptionRepository.findByTickerAndUser(ticker, user)?.also { subscriptionRepository.delete(it) }
    }

    fun removeAllSubscriptions(user: String) {
        subscriptionRepository.findByUser(user).forEach { removeSubscription(it.ticker, it.user) }
    }
}
