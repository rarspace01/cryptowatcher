package rarspace01.configuration

import org.bson.types.ObjectId

data class Configuration(val id: ObjectId? = null, val offset: Long)
