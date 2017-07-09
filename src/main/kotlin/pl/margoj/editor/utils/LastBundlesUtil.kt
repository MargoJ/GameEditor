package pl.margoj.editor.utils

import java.io.File
import java.io.FileInputStream
import java.nio.file.Files
import java.util.LinkedList
import java.util.Scanner

object LastBundlesUtil
{
    private fun validateFile(): Boolean
    {
        val file = FileUtils.LAST_USED_FILE
        if (file.exists())
        {
            return true
        }
        file.createNewFile()
        return false
    }

    fun getLastBundles(): MutableList<String>
    {
        if (!validateFile())
        {
            return LinkedList()
        }
        Scanner(FileInputStream(FileUtils.LAST_USED_FILE)).use {
            val output = LinkedList<String>()
            var needsUpdate = false

            while (it.hasNextLine())
            {
                val line = it.nextLine()

                if (File(line).exists())
                {
                    output.add(line)
                }
                else
                {
                    needsUpdate = true
                }
            }

            if (needsUpdate)
            {
                val file = FileUtils.LAST_USED_FILE
                file.delete()
                Files.write(file.toPath(), output)
            }

            return output
        }
    }

    fun addToList(new: String)
    {
        this.validateFile()

        var all = this.getLastBundles()

        if(all.contains(new))
        {
            all.remove(new)
        }

        all.add(0, new)

        while(all.size > 30)
        {
            all = all.subList(0, 30)
        }

        val file = FileUtils.LAST_USED_FILE
        file.delete()
        Files.write(file.toPath(), all)
    }
}