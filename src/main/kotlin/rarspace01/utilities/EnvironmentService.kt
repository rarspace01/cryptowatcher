package rarspace01.utilities

import java.util.Properties

class EnvironmentService {
    fun getAppVersion(): String = run {
        val inputStream = this.javaClass.classLoader.getResourceAsStream("version.properties")
        val properties = Properties().apply {
            this.load(inputStream)
        }
        val appVersion = properties.getProperty("version", "0")
        return appVersion
    }
}