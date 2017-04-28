package pl.margoj.editor.gui.objects

import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.util.Callback

class WithImageCellFactory(val map: Map<String, Image>) : Callback<ListView<String>, ListCell<String>>
{
    override fun call(param: ListView<String>?): ListCell<String>
    {
        return Cell()
    }

    private inner class Cell : ListCell<String>()
    {
        override fun updateItem(item: String?, empty: Boolean)
        {
            super.updateItem(item, empty)

            if (empty)
            {
                graphic = null
                text = null
            }
            else
            {
                val image = this@WithImageCellFactory.map[item]
                graphic = if (image != null) ImageView(image) else null
                text = item
            }
        }
    }
}