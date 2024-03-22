package club.pisquad.uiharu.plugins

import club.pisquad.uiharu.plugins.echo.Echo
import club.pisquad.uiharu.plugins.stealpfp.StealPfp
import io.ktor.server.application.*
import kotlinx.coroutines.runBlocking

fun Application.loadPlugins() {
    runBlocking {
        PluginManager.loadPlugin(Echo)
        PluginManager.loadPlugin(StealPfp)
    }
}