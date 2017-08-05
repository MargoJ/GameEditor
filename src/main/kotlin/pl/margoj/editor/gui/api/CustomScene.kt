package pl.margoj.editor.gui.api

import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import pl.margoj.editor.gui.utils.FXMLJarLoader
import pl.margoj.editor.gui.utils.FXUtils

abstract class CustomScene<T : CustomController>(val resource: String, val data: Any? = null)
{
    private val logger = LogManager.getLogger(this.javaClass)
    private var loaded = false

    lateinit var stage: Stage
    lateinit var scene: Scene
        private set
    lateinit var controller: T
        private set

    abstract fun setup(stage: Stage, scene: Scene, controller: T)

    @Suppress("UNCHECKED_CAST")
    fun load()
    {
        logger.trace("load()")
        val loader = FXMLJarLoader(this.resource)
        loader.load()

        this.scene = Scene(loader.node as Pane)
        this.controller = loader.controller as T

        this.controller.preInit(this)

        if (data != null)
        {
            this.controller.loadData(data)
        }

        this.setup(stage, scene, controller)

        this.stage.scene = this.scene
        this.stage.show()
        this.loaded = true
    }

    open fun cleanup()
    {
        logger.trace("cleanup()")
        this.stage.hide()
        this.loaded = false
    }

    fun loadAnother(another: CustomScene<*>)
    {
        logger.trace("loadAnother($another)")
        this.cleanup()
        another.stage = this.stage
        another.load()
    }

    protected fun setIcon(iconName: String)
    {
        logger.trace("setIcon($iconName)")
        FXUtils.setStageIcon(this.stage, iconName)
    }
}