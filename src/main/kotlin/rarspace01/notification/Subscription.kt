package rarspace01.notification

import io.quarkus.mongodb.panache.kotlin.PanacheMongoEntity

class Subscription : PanacheMongoEntity() {
    lateinit var ticker: String
    lateinit var user: String
    var value: Double = 0.0
    var isLessThan: Boolean = false

    fun toPrint(): String = "$ticker - $user  - ${if (isLessThan) "<" else ">"} $value "
}
