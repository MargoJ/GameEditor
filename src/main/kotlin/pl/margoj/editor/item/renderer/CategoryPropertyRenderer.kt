package pl.margoj.editor.item.renderer

import javafx.scene.control.ComboBox
import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.properties.CategoryProperty

class CategoryPropertyRenderer : ItemPropertyRenderer<ItemCategory, CategoryProperty, ComboBox<String>>()
{
    override fun getPropertyType(): Class<CategoryProperty> = CategoryProperty::class.java

    override fun createNode(property: CategoryProperty): ComboBox<String>
    {
        val box = ComboBox<String>()
        box.maxWidth = Double.POSITIVE_INFINITY

        for (value in ItemCategory.values())
        {
            box.items.add(value.localizedName)
        }

        box.selectionModel.select(0)

        return box
    }

    override fun update(property: CategoryProperty, node: ComboBox<String>, value: ItemCategory)
    {
        node.selectionModel.select(value.localizedName)
    }

    override fun validate(property: CategoryProperty, name: String, string: String): Boolean
    {
        return true
    }
}