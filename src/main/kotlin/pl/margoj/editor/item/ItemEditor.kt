package pl.margoj.editor.item

import javafx.stage.FileChooser
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.editors.AbstractEditor
import pl.margoj.mrf.item.MargoItem
import pl.margoj.mrf.item.serialization.ItemDeserializer
import pl.margoj.mrf.item.serialization.ItemSerializer

class ItemEditor(editor: MargoJEditor) : AbstractEditor<ItemEditor, MargoItem>(editor, FileChooser.ExtensionFilter("Przedmiot formatu MargoJ (*.mjm)", "*.mjm"), ".mjm")
{
    val serializer = ItemSerializer()
    val deserializer = ItemDeserializer()

    override val currentEditingObject: MargoItem? = null

    lateinit var propertiesRenderer: PropertiesRenderer
        private set

    fun init()
    {
        this.propertiesRenderer = PropertiesRenderer(PropertiesRenderer.DEFAULT_PROPERTIES_RENDERERS)
        this.propertiesRenderer.calculate()
        this.propertiesRenderer.render(this.workspaceController.paneItemPropertiesContainer, "")
    }

    override fun doSave(): ByteArray?
    {
        return null
    }

    override fun updateUndoRedoMenu()
    {
    }

}
