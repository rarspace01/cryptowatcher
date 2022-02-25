package rarspace01.configuration

import org.bson.types.ObjectId

data class ServiceConfiguration(val id: ObjectId? = null, val offset: Long)
