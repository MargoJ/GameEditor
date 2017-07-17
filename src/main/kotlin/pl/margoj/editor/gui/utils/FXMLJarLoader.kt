package pl.margoj.editor.gui.utils

import javafx.fxml.FXMLLoader
import javafx.scene.Node
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.gui.api.CustomController

class FXMLJarLoader(val path: String)
{
    private val logger = LogManager.getLogger(FXMLJarLoader::javaClass)

    lateinit var node: Node
        private set

    lateinit var controller: CustomController
        private set

    fun load()
    {
        logger.trace("load()")
        val loader = FXMLLoader()
        val location = "view/$path.fxml"
        logger.debug("Loading view: $location")
        loader.location = FXMLJarLoader::class.java.classLoader.getResource(location)
        this.node = loader.load()
        this.controller = loader.getController()

        logger.debug("node=${this.node}, controller=${this.controller}")
    }
}