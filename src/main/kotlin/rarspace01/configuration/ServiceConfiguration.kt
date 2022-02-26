package rarspace01.configuration

import io.quarkus.mongodb.panache.PanacheMongoEntity
import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import org.bson.types.ObjectId

@MongoEntity
data class ServiceConfiguration @BsonCreator constructor(
    @BsonProperty val id: ObjectId? = null,
    @BsonProperty val offset: Long = 0L
) : PanacheMongoEntity()
