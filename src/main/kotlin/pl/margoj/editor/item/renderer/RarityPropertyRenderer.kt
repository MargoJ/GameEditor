package pl.margoj.editor.item.renderer

import pl.margoj.mrf.item.ItemRarity
import pl.margoj.mrf.item.properties.special.RarityProperty

class RarityPropertyRenderer : ListPropertyRenderer<ItemRarity, RarityProperty>()
{
    override val propertyType: Class<RarityProperty> = RarityProperty::class.java

    override fun getAllValues(): Array<ItemRarity> = ItemRarity.values()

    override fun getStringRepresentation(value: ItemRarity): String = value.localizedName
}