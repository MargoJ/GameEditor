package pl.margoj.editor.npc

import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.scene.text.Text
import javafx.stage.FileChooser
import org.apache.commons.lang3.StringUtils
import org.apache.logging.log4j.LogManager
import org.fxmisc.richtext.CodeArea
import pl.margoj.editor.MargoJEditor
import pl.margoj.editor.editors.AbstractEditor
import pl.margoj.editor.gui.utils.QuickAlert
import pl.margoj.mrf.MargoResource
import pl.margoj.mrf.npc.NpcScript
import pl.margoj.mrf.npc.serialization.NpcScriptDeserializer
import pl.margoj.mrf.npc.serialization.NpcScriptSerializer
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.TreeSet
import java.util.concurrent.locks.ReentrantLock

class NpcEditor(editor: MargoJEditor) : AbstractEditor<NpcEditor, NpcScript>(editor, FileChooser.ExtensionFilter("Skrypt NPC formatu MargoJ (*.mjn)", "*.mjn"), ".mjn")
{
    private val logger = LogManager.getLogger(NpcEditor::class.java)
    private var allNpcs: Collection<Text>? = null
    lateinit var codeArea: CodeArea
        private set

    val highlighter = NpcEditorHighlighter()
    val serializer = NpcScriptSerializer()
    val deserializer = NpcScriptDeserializer()

    var lock = ReentrantLock()

    var currentScript: NpcScript? = null
        set(value)
        {
            logger.trace("currentScript = $value")
            if (value != field)
            {
                this.clearUndoRedoHistory()
            }

            field = value

            if (value != null)
            {
                this.codeArea.isDisable = false
                this.updateContent(value.content)
                this.workspaceController.titledPaneNpcEditorTitle.text = "Edytor npc: ${value.id}.mjn"
                this.codeArea.undoManager.forgetHistory()
            }
        }

    override val currentEditingObject: NpcScript? get() = this.currentScript

    fun init(codeArea: CodeArea)
    {
        this.codeArea = codeArea
        codeArea.isDisable = true
        codeArea.undoManager.redoAvailableProperty().addListener { _, _, _ -> this.updateUndoRedoMenu() }
        codeArea.undoManager.undoAvailableProperty().addListener { _, _, _ -> this.updateUndoRedoMenu() }
        this.updateUndoRedoMenu()

        logger.trace("init")

        val KEYWORDS = arrayOf(
                "npc",
                "nazwa", "grafika", "poziom", "potwór", "typ", "płeć",
                "dialog", "opcja", "ustaw", "wykonaj",
                "i", "lub", "oraz", "nie",
                "prawda", "fałsz",
                "jeżeli", "przeciwnie", "dopóki", "każdy", "w",
                "dodaj", "odejmij", "pomnóż", "podziel",
                "posiada", "dodaj", "zabierz", "dodaj", "zabierz", "złoto", "xp"
        )
        val PROPERTY_PATTERN = "[\\p{L}0-9_.]+"

        this.highlighter.registerPattern("comment", "(--[^\n]*)")
        this.highlighter.registerPattern("variable_property", "!$PROPERTY_PATTERN")
        this.highlighter.registerPattern("string", "\"([^\"\\\\]|\\\\.)*\"")
        this.highlighter.registerPattern("custom_property", "@$PROPERTY_PATTERN")
        this.highlighter.registerPattern("system_property", "%$PROPERTY_PATTERN")
        this.highlighter.registerPattern("keyword", "\\b(${KEYWORDS.joinToString("|")})\\b")
        this.highlighter.registerPattern("number", "\\d+")

        this.highlighter.compile()

        this.updateUndoRedoMenu()
    }

    fun updateContent(text: String)
    {
        this.codeArea.replaceText(0, this.codeArea.text.length, text)
    }

    fun updateNpcsView()
    {
        logger.trace("updateNpcsView()")

        if (this.editor.currentResourceBundle == null)
        {
            this.allNpcs = null
            this.updateNpcsViewElement()
            return
        }

        val sortedSet = TreeSet<Text> { o1, o2 -> o1.text.compareTo(o2.text); }

        this.editor.currentResourceBundle!!.getResourcesByCategory(MargoResource.Category.NPC_SCRIPTS).forEach { view ->
            val text = Text(view.id)

            text.onMouseClicked = EventHandler {
                if (it.clickCount == 2)
                {
                    this.loadFromBundle(view.id, this.editor.currentResourceBundle!!.loadResource(view)!!)
                }
            }

            sortedSet.add(text)
        }

        this.allNpcs = sortedSet
        this.updateNpcsViewElement()
    }

    fun updateNpcsViewElement()
    {
        logger.trace("updateNpcsViewElement()")

        if (this.allNpcs == null)
        {
            this.workspaceController.listNpcList.items = FXCollections.singletonObservableList(Text("Nie załadowano żadnego zasobu"))
            return
        }

        val visibleScripts = FXCollections.observableList(ArrayList<Text>(this.allNpcs!!.size))

        this.allNpcs!!.filterTo(visibleScripts) { StringUtils.containsIgnoreCase(it.text, this.workspaceController.fieldSearchNpc.text) }

        this.workspaceController.listNpcList.items = visibleScripts
    }

    fun loadFromBundle(id: String, input: InputStream)
    {
        logger.trace("loadFromBundle($input)")

        if (this.currentScript != null && this.touched && !this.askForSave())
        {
            return
        }

        this.deserializer.fileName = id
        this.currentScript = this.deserializer.deserialize(input)
        this.save = { this.editor.addNpcScriptToBundle(this.currentScript!!) }

        QuickAlert.create().information().header("Załadowano zasób").content("Zasób został załadowany poprawnie").showAndWait()
        this.workspaceController.tabPane.selectionModel.select(this.workspaceController.tabNpcEditor)
    }

    override fun doSave(): ByteArray?
    {
        logger.trace("doSave()")

        val script = this.currentScript ?: return null
        this.currentScript!!.content = this.codeArea.text
        return this.serializer.serialize(script)
    }

    override fun openFile(file: File)
    {
        logger.trace("openFile(${file.absolutePath})")

        FileInputStream(file).use { input ->
            this.deserializer.fileName = file.nameWithoutExtension
            this.currentScript = this.deserializer.deserialize(input)
            this.saveFile = file
            this.save = this::saveWithDialog
        }
    }

    override fun canUndo(): Boolean
    {
        return this.codeArea.undoManager.isUndoAvailable
    }

    override fun canRedo(): Boolean
    {
        return this.codeArea.undoManager.isRedoAvailable
    }

    override fun undo(): Boolean
    {
        return this.codeArea.undoManager.undo()
    }

    override fun redo(): Boolean
    {
        return this.codeArea.undoManager.redo()
    }

    override fun updateUndoRedoMenu()
    {
        this.workspaceController.menuNpcUndo.isDisable = !this.canUndo()
        this.workspaceController.menuNpcRedo.isDisable = !this.canRedo()
        this.touch()
    }
}
