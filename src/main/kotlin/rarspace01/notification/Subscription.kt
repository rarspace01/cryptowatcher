package rarspace01.notification

import io.quarkus.mongodb.panache.PanacheMongoEntity
import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

@MongoEntity
data class Subscription @BsonCreator constructor(
    @BsonProperty val id: ObjectId? = null,
    @BsonProperty val ticker: String,
    @BsonProperty val user: String,
    @BsonProperty val value: Double,
    @BsonProperty val isLessThan: Boolean
) : PanacheMongoEntity()
