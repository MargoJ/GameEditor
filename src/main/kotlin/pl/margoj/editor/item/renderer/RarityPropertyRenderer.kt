package pl.margoj.editor.item.renderer

import javafx.scene.control.ComboBox
import pl.margoj.mrf.item.ItemRarity
import pl.margoj.mrf.item.properties.RarityProperty

class RarityPropertyRenderer : ItemPropertyRenderer<ItemRarity, RarityProperty, ComboBox<String>>()
{
    override fun getPropertyType(): Class<RarityProperty> = RarityProperty::class.java

    override fun createNode(property: RarityProperty): ComboBox<String>
    {
        val box = ComboBox<String>()
        box.maxWidth = Double.POSITIVE_INFINITY

        for (value in ItemRarity.values())
        {
            box.items.add(value.localizedName)
        }

        box.selectionModel.select(0)

        return box
    }

    override fun update(property: RarityProperty, node: ComboBox<String>, value: ItemRarity)
    {
        node.selectionModel.select(value.localizedName)
    }

    override fun validate(property: RarityProperty, name: String, string: String): Boolean
    {
        return true
    }
}