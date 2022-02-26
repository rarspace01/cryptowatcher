package rarspace01.notification

import io.quarkus.mongodb.panache.PanacheMongoRepository
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class SubscriptionRepository : PanacheMongoRepository<Subscription> {
    fun findByTickerAndUser(ticker: String, user: String) = find("{ ticker: \"$ticker\", user: \"$user\"}").firstResult<Subscription>()
    fun findByUser(user: String) = find("user", user).stream<Subscription>()
    fun findByTicker(ticker: String) = find("ticker", ticker).stream<Subscription>()
}
