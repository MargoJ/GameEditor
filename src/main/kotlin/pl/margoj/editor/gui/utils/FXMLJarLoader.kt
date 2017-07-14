package pl.margoj.editor.gui.utils

import javafx.fxml.FXMLLoader
import javafx.scene.Node
import pl.margoj.editor.gui.api.CustomController

class FXMLJarLoader(val path: String)
{
    lateinit var node: Node
        private set

    lateinit var controller: CustomController
        private set

    fun load()
    {
        val loader = FXMLLoader()
        loader.location = FXMLJarLoader::class.java.classLoader.getResource("view/$path.fxml")
        this.node = loader.load()
        this.controller = loader.getController()
    }
}