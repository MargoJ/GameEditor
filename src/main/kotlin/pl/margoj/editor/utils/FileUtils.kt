package pl.margoj.editor.utils

import org.apache.commons.lang3.Validate
import java.io.File

object FileUtils
{
    val PROGRAM_DIRECTORY = getProgramData()

    val TILESETS_DIRECTORY = PROGRAM_DIRECTORY + "tilesets" + File.separator
    val MOUNT_DIRECTORY = PROGRAM_DIRECTORY + "mounts" + File.separator
    val TEMP_DIRECTORY = PROGRAM_DIRECTORY + "temp" + File.separator
    val LAST_USED_FILE = File(PROGRAM_DIRECTORY, "last-bundles.txt")

    fun ensureDirectoryCreationIfAvailable(directory: String)
    {
        val file = File(directory)
        if (!file.exists())
        {
            Validate.isTrue(file.mkdir(), "Couldn't create directory " + directory)
        }
        if (!file.isDirectory)
        {
            throw IllegalStateException(directory + " is a file, not a directory")
        }
    }

    private fun getProgramData(): String
    {
        if (System.getProperty("os.name").toLowerCase().contains("win"))
        {
            return System.getenv("AppData") + File.separator + "MargoJEditor" + File.separator
        }
        else
        {
            return System.getProperty("user.home") + File.separator + ".MargoJEditor" + File.separator
        }
    }
}