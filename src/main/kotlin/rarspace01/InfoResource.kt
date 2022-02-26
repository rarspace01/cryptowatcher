package rarspace01

import rarspace01.utilities.EnvironmentService
import java.util.Properties
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/info")
class InfoResource {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun info(): String {
        val appVersion = EnvironmentService().getAppVersion()
        println("appVersion:$appVersion")
        return appVersion
    }
}
