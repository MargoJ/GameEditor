package pl.margoj.editor.item.renderer

import pl.margoj.mrf.item.ItemCategory
import pl.margoj.mrf.item.properties.CategoryProperty

class CategoryPropertyRenderer : ListPropertyRenderer<ItemCategory, CategoryProperty>()
{
    override val propertyType: Class<CategoryProperty> = CategoryProperty::class.java

    override fun getAllValues(): Array<ItemCategory> = ItemCategory.values()

    override fun getStringRepresentation(value: ItemCategory): String = value.localizedName
}