package rarspace01.notification

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class TickerSubscriptionService(private val subscriptionRepository: SubscriptionRepository) {
    private val subscriptionMap: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun addSubscription(ticker: String, user: String, tickerValue: Double, isLessThan: Boolean) {
        val oldSubscription = subscriptionRepository.findByTickerAndUser(ticker, user) ?: Subscription(ticker = ticker, user = user,value = tickerValue, isLessThan = isLessThan)
        val subscription = oldSubscription.copy(value = tickerValue, isLessThan = isLessThan)
        subscriptionRepository.persist(subscription)
    }

    fun removeSubscription(ticker: String, user: String) {
        subscriptionRepository.findByTickerAndUser(ticker, user)?.also { subscriptionRepository.delete(it) }
    }

    fun removeAllSubscriptions(user: String) {
        subscriptionRepository.findByUser(user).forEach { removeSubscription(it.ticker, it.user) }
    }
}
