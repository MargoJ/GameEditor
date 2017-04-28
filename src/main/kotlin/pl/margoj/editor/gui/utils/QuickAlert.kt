package pl.margoj.editor.gui.utils

import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.control.TextArea
import javafx.scene.image.Image
import org.apache.commons.lang3.exception.ExceptionUtils

object QuickAlert
{
    fun create(): QuickAlertBuilder
    {
        return QuickAlertBuilder()
    }

    class QuickAlertBuilder internal constructor()
    {
        private val alert = Alert(AlertType.NONE)

        init
        {
            this.icon("icon.png")
        }

        fun information(): QuickAlertBuilder
        {
            alert.alertType = AlertType.INFORMATION
            alert.title = "Informacja"
            return this
        }

        fun warning(): QuickAlertBuilder
        {
            alert.alertType = AlertType.WARNING
            alert.title = "Uwaga!"
            return this
        }

        fun confirmation(): QuickAlertBuilder
        {
            alert.alertType = AlertType.CONFIRMATION
            alert.title = "Uwaga!"
            return this
        }

        fun error(): QuickAlertBuilder
        {
            alert.alertType = AlertType.ERROR
            alert.title = "Błąd!"
            return this
        }

        fun exception(e: Throwable): QuickAlertBuilder
        {
            val textArea = TextArea(ExceptionUtils.getStackTrace(e))
            textArea.isEditable = false
            textArea.isWrapText = true

            textArea.maxWidth = java.lang.Double.MAX_VALUE
            textArea.maxHeight = java.lang.Double.MAX_VALUE

            this.error().header("Wystąpił nieoczekiwany błąd")
            alert.dialogPane.expandableContent = textArea
            alert.dialogPane.minWidth = 700.0

            return this
        }

        fun header(header: String): QuickAlertBuilder
        {
            alert.headerText = header
            return this
        }

        fun content(content: String): QuickAlertBuilder
        {
            alert.contentText = content
            return this
        }

        fun icon(icon: String): QuickAlertBuilder
        {
            FXUtils.setAlertIcon(this.alert, Image(QuickAlert::class.java.classLoader.getResourceAsStream(icon)))
            return this
        }

        fun buttonTypes(vararg types: ButtonType): QuickAlertBuilder
        {
            this.alert.buttonTypes.setAll(*types)
            return this
        }

        fun show()
        {
            alert.show()
        }

        fun showAndWait(): ButtonType
        {
            return alert.showAndWait().orElse(null)
        }
    }
}
