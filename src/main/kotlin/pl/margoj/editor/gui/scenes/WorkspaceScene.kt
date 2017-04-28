package pl.margoj.editor.gui.scenes

import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene

class WorkspaceScene : CustomScene<CustomController>("workspace")
{
    override fun setup(stage: Stage, scene: Scene, controller: CustomController)
    {
        this.setIcon("icon.png")

        stage.title = "MargoJ Edytor"

        val pane = scene.root as Pane
        stage.minWidth = pane.minWidth
        stage.minHeight = pane.minHeight

        stage.isResizable = true
        stage.sizeToScene()
    }
}