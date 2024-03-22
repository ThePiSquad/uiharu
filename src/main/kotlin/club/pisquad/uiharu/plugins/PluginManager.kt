package club.pisquad.uiharu.plugins

interface Plugin {
    val name: String
    val description: String
    val document: String

    suspend fun initiate() {}
}

object PluginManager {
    private val plugins: MutableMap<String, Plugin> = mutableMapOf()

    suspend fun loadPlugin(plugin: Plugin) {
        LOGGER.debug("Loading plugin ${plugin.name}")
        plugin.initiate()
        plugins[plugin.name] = plugin
    }
}