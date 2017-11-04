package pl.margoj.editor.item.renderer

import javafx.beans.binding.Bindings
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.utils.FXUtils
import pl.margoj.editor.item.PropertyChangeUndoRedo
import pl.margoj.mrf.item.ItemProperties
import pl.margoj.mrf.item.properties.special.TeleportProperty

class TeleportPropertyRenderer : ItemPropertyRenderer<TeleportProperty.Teleport, TeleportProperty, TeleportPropertyRenderer.TeleportNode>()
{
    private val changeCache = hashMapOf<Node, String>()

    override val propertyType: Class<TeleportProperty> = TeleportProperty::class.java

    override fun createNode(property: TeleportProperty): TeleportNode
    {
        return TeleportNode(property)
    }

    override fun update(property: TeleportProperty, node: TeleportNode, value: TeleportProperty.Teleport)
    {
        node.mapInput.text = value.map
        node.coordsInput.isSelected = value.customCoords
        node.xInput.text = value.x.toString()
        node.yInput.text = value.y.toString()
    }

    override fun convert(property: TeleportProperty, node: TeleportNode): TeleportProperty.Teleport
    {
        return TeleportProperty.Teleport(
                node.mapInput.text, node.coordsInput.isSelected, node.xInput.text.toInt(), node.yInput.text.toInt()
        )
    }

    inner class TeleportNode(val property: TeleportProperty) : HBox()
    {
        val mapInput: TextField
        val xInput: TextField
        val yInput: TextField
        val coordsInput: ToggleButton

        init
        {
            val label1 = this.label("Mapa: ")
            this.mapInput = this.input()
            this.coordsInput = ToggleButton()
            val label2 = this.label("X: ")
            this.xInput = this.input()
            val label3 = this.label("Y: ")
            this.yInput = this.input()

            this.mapInput.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
            HBox.setHgrow(this.mapInput, Priority.ALWAYS)

            this.coordsInput.setMinSize(150.0, 0.0)
            HBox.setMargin(this.coordsInput, Insets(0.0, 0.0, 0.0, 5.0))
            HBox.setHgrow(this.coordsInput, Priority.NEVER)

            FXUtils.makeNumberField(this.xInput, false)
            FXUtils.makeNumberField(this.yInput, false)

            this.coordsInput.textProperty().bind(Bindings.`when`(this.coordsInput.selectedProperty()).then("Własne koordynaty").otherwise("Domyślny spawn"))
            this.xInput.disableProperty().bind(this.coordsInput.selectedProperty().not())
            this.yInput.disableProperty().bind(this.coordsInput.selectedProperty().not())

            this.children.setAll(label1, this.mapInput, this.coordsInput, label2, this.xInput, label3, this.yInput)

            fun current() = MargoJEditor.INSTANCE.itemEditor.currentItem!![ItemProperties.TELEPORT]

            addPropertyRenderer(this.mapInput, changeCache, this.property, { val current = current(); TeleportProperty.Teleport(it, current.customCoords, current.x, current.y) }, { it.map })
            addPropertyRenderer(this.xInput, changeCache, this.property, { val current = current(); TeleportProperty.Teleport(current.map, current.customCoords, it.toInt(), current.y) }, { it.x.toString() })
            addPropertyRenderer(this.yInput, changeCache, this.property, { val current = current(); TeleportProperty.Teleport(current.map, current.customCoords, current.x, it.toInt()) }, { it.y.toString() })

            this.coordsInput.setOnAction {
                val itemEditor = MargoJEditor.INSTANCE.itemEditor
                val oldValue = itemEditor.currentItem!![property]
                itemEditor.currentItem!![property] = this@TeleportPropertyRenderer.convert(property, this)
                itemEditor.addUndoAction(PropertyChangeUndoRedo(property, oldValue, itemEditor.currentItem!![property]))
            }
        }

        private fun label(text: String): Label
        {
            val label = Label(text)
            label.setPrefSize(Region.USE_COMPUTED_SIZE, Double.MAX_VALUE)
            label.alignment = Pos.CENTER
            label.padding = Insets(0.0, 5.0, 0.0, 5.0)
            HBox.setHgrow(label, Priority.NEVER)
            return label
        }

        private fun input(): TextField
        {
            val label = TextField()
            label.setMinSize(80.0, 0.0)
            label.setPrefSize(0.0, Double.MAX_VALUE)
            HBox.setHgrow(label, Priority.NEVER)
            return label
        }
    }
}