package rarspace01.notification

import org.bson.types.ObjectId

data class Subscription(val id: ObjectId? = null, val ticker: String, val user: String, val value: Double, val isLessThan: Boolean)
