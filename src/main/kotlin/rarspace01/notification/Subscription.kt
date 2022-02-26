package rarspace01.notification

import io.quarkus.mongodb.panache.PanacheMongoEntity
import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.types.ObjectId

@MongoEntity
data class Subscription(val id: ObjectId? = null, val ticker: String, val user: String, val value: Double, val isLessThan: Boolean) : PanacheMongoEntity()
