package rarspace01.configuration

import io.quarkus.mongodb.panache.PanacheMongoEntity
import io.quarkus.mongodb.panache.common.MongoEntity
import org.bson.types.ObjectId

@MongoEntity
data class ServiceConfiguration(val id: ObjectId? = null, val offset: Long) : PanacheMongoEntity()
