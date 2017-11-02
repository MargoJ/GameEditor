package pl.margoj.editor.item.renderer

import javafx.scene.control.ToggleButton
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.item.PropertyChangeUndoRedo
import pl.margoj.mrf.item.properties.ProfessionRequirementProperty

class ProfessionRequirementPropertyRenderer
    : ItemPropertyRenderer<ProfessionRequirementProperty.ProfessionRequirement, ProfessionRequirementProperty, ProfessionRequirementPropertyRenderer.RequirementNode>()
{
    override val propertyType: Class<ProfessionRequirementProperty> = ProfessionRequirementProperty::class.java

    override fun createNode(property: ProfessionRequirementProperty): RequirementNode
    {
        return RequirementNode(property)
    }

    override fun update(property: ProfessionRequirementProperty, node: RequirementNode, value: ProfessionRequirementProperty.ProfessionRequirement)
    {
        node.warrior.isSelected = value.warrior
        node.paladin.isSelected = value.paladin
        node.bladedancer.isSelected = value.bladedancer
        node.mage.isSelected = value.mage
        node.hunter.isSelected = value.hunter
        node.tracker.isSelected = value.tracker
    }

    override fun convert(property: ProfessionRequirementProperty, node: RequirementNode): ProfessionRequirementProperty.ProfessionRequirement
    {
        return ProfessionRequirementProperty.ProfessionRequirement(
                node.warrior.isSelected, node.paladin.isSelected, node.bladedancer.isSelected,
                node.mage.isSelected, node.hunter.isSelected, node.tracker.isSelected
        )
    }

    inner class RequirementNode(property: ProfessionRequirementProperty) : HBox()
    {
        val warrior = ToggleButton("Wojownik")
        val paladin = ToggleButton("Paladyn")
        val bladedancer = ToggleButton("Tancerz ostrzy")
        val mage = ToggleButton("Mag")
        val hunter = ToggleButton("Łowca")
        val tracker = ToggleButton("Tropiciel")

        init
        {
            this.children.setAll(this.warrior, this.paladin, this.bladedancer, this.mage, this.hunter, this.tracker)

            for (child in this.children)
            {
                HBox.setHgrow(child, Priority.ALWAYS)
                child as ToggleButton
                child.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE)
                child.setOnAction {
                    val itemEditor = MargoJEditor.INSTANCE.itemEditor
                    val oldValue = itemEditor.currentItem!![property]
                    itemEditor.currentItem!![property] = this@ProfessionRequirementPropertyRenderer.convert(property, this)
                    itemEditor.addUndoAction(PropertyChangeUndoRedo(property, oldValue, itemEditor.currentItem!![property]))
                }
            }

            this.spacing = 5.0
        }
    }
}