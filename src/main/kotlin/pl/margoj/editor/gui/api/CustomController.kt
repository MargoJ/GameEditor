package pl.margoj.editor.gui.api

import javafx.fxml.Initializable

interface CustomController : Initializable
{
    fun loadData(data: Any)
    {
    }

    fun preInit(scene: CustomScene<*>)
    {
    }
}