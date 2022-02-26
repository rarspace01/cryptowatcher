package rarspace01.configuration

import io.quarkus.mongodb.panache.kotlin.PanacheMongoEntity

class ServiceConfiguration : PanacheMongoEntity() {
    var offset: Long = 0L
}
