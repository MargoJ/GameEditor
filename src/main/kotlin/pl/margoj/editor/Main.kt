package pl.margoj.editor

import javafx.application.Application
import javafx.stage.Stage
import joptsimple.OptionParser
import pl.margoj.editor.gui.scenes.WorkspaceScene
import java.util.Arrays

var EDITOR_DEBUGGING: Boolean = false

fun main(args: Array<String>)
{
    val optionParser = OptionParser()

    optionParser.acceptsAll(Arrays.asList("help", "?"), "Wyświetla tą pomoc")
    optionParser.acceptsAll(Arrays.asList("debug", "d"), "Włącza tryb debuggowania")

    try
    {
        val options = optionParser.parse(*args)

        if (options.has("?"))
        {
            optionParser.printHelpOn(System.out)
            return
        }

        if (options.has("debug"))
        {
            EDITOR_DEBUGGING = true
        }
    }
    catch (e: Exception)
    {
        e.printStackTrace()
        return
    }

    Application.launch(EditorApplication::class.java, *args)
}

class EditorApplication : Application()
{
    override fun start(primaryStage: Stage)
    {
        val scene = WorkspaceScene()

        scene.stage = primaryStage
        scene.load()
    }
}