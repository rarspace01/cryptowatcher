package rarspace01.notification

import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class TickerSubscriptionService {
    private val subscriptionMap: MutableMap<String, MutableList<String>> = mutableMapOf()

    fun addSubscription(ticker: String, user: String) {
        subscriptionMap[ticker] = subscriptionMap.getOrDefault(ticker, mutableListOf()).apply {
            this.add(user)
        }
    }

    fun removeSubscription(ticker: String, user: String) {
        subscriptionMap[ticker]?.remove(user)
    }

    fun removeAllSubscriptions(user: String) {
        subscriptionMap.keys.forEach { removeSubscription(it, user) }
    }
}
