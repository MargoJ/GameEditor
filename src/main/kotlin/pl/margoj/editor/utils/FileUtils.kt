package pl.margoj.editor.utils

import org.apache.commons.lang3.Validate
import org.apache.logging.log4j.LogManager
import java.io.File

object FileUtils
{
    private val logger = LogManager.getLogger(FileUtils::class.java)

    val PROGRAM_DIRECTORY = getProgramData()

    val TILESETS_DIRECTORY = PROGRAM_DIRECTORY + "tilesets" + File.separator
    val MOUNT_DIRECTORY = PROGRAM_DIRECTORY + "mounts" + File.separator
    val TEMP_DIRECTORY = PROGRAM_DIRECTORY + "temp" + File.separator
    val LOGS_DIRECTORY = PROGRAM_DIRECTORY + "logs" + File.separator
    val LAST_USED_FILE = File(PROGRAM_DIRECTORY, "last-bundles.txt")

    fun ensureDirectoryCreationIfAvailable(directory: String)
    {
        logger.trace("ensureDirectoryCreationIfAvailable($directory)")

        val file = File(directory)
        if (!file.exists())
        {
            Validate.isTrue(file.mkdirs(), "Couldn't create directory " + directory)
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