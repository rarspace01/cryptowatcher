package rarspace01

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
        val inputStream = this.javaClass.classLoader.getResourceAsStream("version.properties")
        val properties = Properties().apply {
            this.load(inputStream)
        }
        val appVersion = properties.getProperty("version", "0")
        println("appVersion:" + appVersion)
        return appVersion
    }
}
