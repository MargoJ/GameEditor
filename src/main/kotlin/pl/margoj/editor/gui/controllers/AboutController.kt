package pl.margoj.editor.gui.controllers

import javafx.fxml.FXML
import javafx.scene.control.Hyperlink
import pl.margoj.editor.gui.api.CustomController
import java.awt.Desktop
import java.net.URI
import java.net.URL
import java.util.ResourceBundle

class AboutController : CustomController
{
    @FXML
    private val linkGithub: Hyperlink? = null

    override fun initialize(location: URL, resources: ResourceBundle?)
    {
        this.linkGithub!!.setOnAction {
            Desktop.getDesktop().browse(URI("https://github.com/MargoJ/GameEditor"))
        }
    }
}
