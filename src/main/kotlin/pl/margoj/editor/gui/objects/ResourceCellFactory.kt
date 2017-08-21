package pl.margoj.editor.gui.objects

import javafx.scene.control.ContextMenu
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.text.Text
import javafx.util.Callback
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.ResourceText

class ResourceCellFactory(private val editor: MargoJEditor) : Callback<ListView<Text>, ListCell<Text>>
{
    override fun call(param: ListView<Text>): ListCell<Text>
    {
        return Cell()
    }

    private inner class Cell : ListCell<Text>()
    {
        override fun updateItem(item: Text?, empty: Boolean)
        {
            super.updateItem(item, empty)
            graphic = item

            if (item is ResourceText)
            {
                val view = item.view
                val menu = ContextMenu()
                val loadItem = MenuItem("Załaduj zasób")
                val deleteItem = MenuItem("Usuń zasób")

                loadItem.setOnAction { editor.loadResource(view) }
                deleteItem.setOnAction { editor.deleteResource(view) }

                menu.items.addAll(loadItem, deleteItem)

                this.contextMenu = menu

                this.setOnMouseClicked { event ->
                    if (event.clickCount == 2)
                    {
                        editor.loadResource(view)
                    }
                }
            }
            else
            {
                contextMenu = null
                onMouseClicked = null
            }
        }
    }
}
