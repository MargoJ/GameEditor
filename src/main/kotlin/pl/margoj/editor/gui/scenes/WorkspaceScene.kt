package pl.margoj.editor.gui.scenes

import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene

class WorkspaceScene : CustomScene<CustomController>("workspace")
{
    private val logger = LogManager.getLogger(this::javaClass)

    override fun setup(stage: Stage, scene: Scene, controller: CustomController)
    {
        logger.trace("setup(stage = $stage, scene = $scene, controller = $controller)")

        this.setIcon("icon.png")

        stage.title = "MargoJ Edytor"

        val pane = scene.root as Pane
        stage.minWidth = pane.minWidth
        stage.minHeight = pane.minHeight

        stage.isResizable = true
        stage.sizeToScene()
    }
}