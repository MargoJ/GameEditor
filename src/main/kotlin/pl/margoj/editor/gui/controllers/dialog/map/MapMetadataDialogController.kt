package pl.margoj.editor.gui.controllers.dialog.map

import javafx.beans.binding.Bindings
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ChoiceBox
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.gui.api.CustomController
import pl.margoj.editor.gui.api.CustomScene
import pl.margoj.editor.map.actions.MetadataUndoRedo
import pl.margoj.mrf.map.MargoMap
import pl.margoj.mrf.map.metadata.ismain.IsMain
import pl.margoj.mrf.map.metadata.istown.IsTown
import pl.margoj.mrf.map.metadata.parentmap.ParentMap
import pl.margoj.mrf.map.metadata.pvp.MapPvP
import pl.margoj.mrf.map.metadata.respawnmap.RespawnMap
import pl.margoj.mrf.map.metadata.welcome.WelcomeMessage
import java.net.URL
import java.util.ResourceBundle

class MapMetadataDialogController : CustomController
{
    private val options: Map<MapPvP, String> = hashMapOf(
            Pair(MapPvP.NO_PVP, "PvP wyłączone"),
            Pair(MapPvP.CONDITIONAL, "PvP za zgodą"),
            Pair(MapPvP.UNCONDITIONAL, "PvP bezwarunkowe"),
            Pair(MapPvP.ARENAS, "Areny")
    )

    @FXML
    lateinit var fieldWelcomeMessage: TextField

    @FXML
    lateinit var fieldParentMap: TextField

    @FXML
    lateinit var fieldRespawnMap: TextField

    @FXML
    lateinit var choicePvP: ChoiceBox<String>

    @FXML
    lateinit var toggleMain: ToggleButton

    @FXML
    lateinit var toggleTown: ToggleButton

    @FXML
    lateinit var buttonConfirm: Button

    lateinit var scene: CustomScene<*>

    lateinit var map: MargoMap

    override fun preInit(scene: CustomScene<*>)
    {
        this.scene = scene
    }

    override fun loadData(data: Any)
    {
        this.map = data as MargoMap

        this.choicePvP.items.setAll(this.options.values)
        this.choicePvP.selectionModel.select(this.options[this.map.getMetadata(MapPvP::class.java)])

        this.fieldWelcomeMessage.text = this.map.getMetadata(WelcomeMessage::class.java).value
        this.fieldParentMap.text = this.map.getMetadata(ParentMap::class.java).value
        this.fieldRespawnMap.text = this.map.getMetadata(RespawnMap::class.java).value

        this.toggleMain.isSelected = this.map.getMetadata(IsMain::class.java).value
        this.toggleTown.isSelected = this.map.getMetadata(IsTown::class.java).value
    }

    override fun initialize(location: URL?, resources: ResourceBundle?)
    {
        this.toggleMain.textProperty().bind(Bindings.`when`(this.toggleMain.selectedProperty()).then("Główna").otherwise("Poboczna"))
        this.toggleTown.textProperty().bind(Bindings.`when`(this.toggleTown.selectedProperty()).then("Tak").otherwise("Nie"))
        this.fieldParentMap.disableProperty().bind(Bindings.`when`(this.toggleMain.selectedProperty()).then(true).otherwise(false))

        this.buttonConfirm.onAction = EventHandler {
            var anyChanges = false

            val oldMeta = this.map.metadata

            val newPvP = this.textToOption(this.choicePvP.selectionModel.selectedItem)
            if (map.getMetadata(MapPvP::class.java) != newPvP)
            {
                map.setMetadata(newPvP)
                anyChanges = true
            }

            val newWelcome = this.fieldWelcomeMessage.text
            if (map.getMetadata(WelcomeMessage::class.java).value != newWelcome)
            {
                map.setMetadata(WelcomeMessage(newWelcome))
                anyChanges = true
            }

            val newParentMap = this.fieldParentMap.text
            if (map.getMetadata(ParentMap::class.java).value != newParentMap)
            {
                map.setMetadata(ParentMap(newParentMap))
                anyChanges = true
            }

            val newRespawnMap = this.fieldRespawnMap.text
            if (map.getMetadata(RespawnMap::class.java).value != newRespawnMap)
            {
                map.setMetadata(RespawnMap(newRespawnMap))
                anyChanges = true
            }

            val newIsMain = this.toggleMain.isSelected
            if (map.getMetadata(IsMain::class.java).value != newIsMain)
            {
                map.setMetadata(IsMain(newIsMain))
                anyChanges = true
            }


            val newIsTown = this.toggleTown.isSelected
            if (map.getMetadata(IsTown::class.java).value != newIsTown)
            {
                map.setMetadata(IsTown(newIsTown))
                anyChanges = true
            }

            if (anyChanges)
            {
                val mapEditor = MargoJEditor.INSTANCE.mapEditor
                mapEditor.addUndoAction(MetadataUndoRedo(oldMeta, map.metadata))
            }

            this.scene.stage.close()
        }
    }

    private fun textToOption(option: String): MapPvP
    {
        return this.options.entries.stream().filter { it.value == option }.map { it.key }.findAny().get()
    }
}