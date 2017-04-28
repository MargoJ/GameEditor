package pl.margoj.editor.gui.scenes

import javafx.scene.Scene
import javafx.stage.Stage
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene

class DialogScene(resource: String, val title: String, data: Any? = null) : CustomScene<CustomController>(resource, data)
{
    override fun setup(stage: Stage, scene: Scene, controller: CustomController)
    {
        this.setIcon("icon.png")
        stage.title = this.title
        stage.isResizable = false
        stage.sizeToScene()
    }
}