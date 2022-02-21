package rarspace01.notification

data class Subscription(val ticker: String, val user: String, val value: Double, val isLessThan: Boolean)
