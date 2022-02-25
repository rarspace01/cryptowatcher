package rarspace01.configuration

import io.quarkus.mongodb.panache.PanacheMongoRepository
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class ServiceConfigurationRepository : PanacheMongoRepository<ServiceConfiguration>
