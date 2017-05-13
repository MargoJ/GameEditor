package pl.margoj.editor.gui.utils

import javafx.scene.control.Alert
import javafx.scene.control.ScrollPane
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.stage.Modality
import javafx.stage.Stage
import pl.margoj.editor.gui.scenes.DialogScene

object FXUtils
{

    private fun isValidNumber(str: String, negative: Boolean): Boolean
    {
        val chars = str.toCharArray()
        if (chars.isEmpty())
        {
            return false
        }
        return (0..str.length - 1).none { !(chars[it] == '-' && it == 0 && negative) && !Character.isDigit(chars[it]) }
    }

    fun makeNumberField(field: TextField, negative: Boolean)
    {
        if (!isValidNumber(field.text, negative))
        {
            field.text = "0"
        }

        field.textProperty().addListener { _, oldValue, newValue ->
            if(newValue.isEmpty())
            {
                field.text = newValue
            }
            else if (!isValidNumber(newValue, negative))
            {
                field.text = oldValue
            }
        }
    }

    fun setAlertIcon(alert: Alert, icon: Image)
    {
        (alert.dialogPane.scene.window as Stage).icons.setAll(icon)
    }

    fun loadDialog(resource: String, title: String, owner: Stage? = null, data: Any? = null)
    {
        val scene = DialogScene("dialog/" + resource, title, data)
        val stage = Stage()
        if (owner != null)
        {
            stage.initModality(Modality.WINDOW_MODAL)
            stage.initOwner(owner)
        }

        scene.stage = stage
        scene.load()
    }

    fun showMultipleErrorsAlert(header: String, errors: List<String>)
    {
        if(errors.isEmpty())
        {
            return
        }
        QuickAlert.create().error().header(header).content("Popraw następujące błedy: \n - " + errors.joinToString("\n - ")).showAndWait()
    }

    fun pannableOnRightclick(event: MouseEvent, pane: ScrollPane): Boolean
    {
        if (event.button != MouseButton.SECONDARY)
        {
            return false
        }
        if (event.eventType == MouseEvent.MOUSE_PRESSED)
        {
            pane.isPannable = true
        }
        else if (event.eventType == MouseEvent.MOUSE_RELEASED)
        {
            pane.isPannable = false
        }
        return true
    }
}
