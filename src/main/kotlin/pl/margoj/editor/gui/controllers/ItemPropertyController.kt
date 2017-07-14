package pl.margoj.editor.gui.controllers

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.StackPane
import pl.margoj.editor.gui.api.CustomController
import java.net.URL
import java.util.ResourceBundle

class ItemPropertyController: CustomController
{
    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
    }

    @FXML
    lateinit var propLabelName: Label

    @FXML
    lateinit var propPaneValueHolder: StackPane
}