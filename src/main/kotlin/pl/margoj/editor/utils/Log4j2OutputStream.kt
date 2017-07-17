package pl.margoj.editor.utils

import org.apache.logging.log4j.Level
import org.apache.logging.log4j.Logger
import java.io.OutputStream

class Log4j2OutputStream(private val logger: Logger, private val level: Level) : OutputStream()
{
    override fun write(b: Int)
    {
        print(b.toChar())
    }

    override fun write(b: ByteArray?)
    {
        print(String(b!!))
    }

    override fun write(b: ByteArray?, off: Int, len: Int)
    {
        print(String(b!!, off, len))
    }

    private fun print(string: String)
    {
        val chars = string.toCharArray()
        var i = string.length - 1

        while (i >= 0 && chars[i].isWhitespace())
        {
            i--
        }

        val trimmed = string.substring(0, i + 1)

        if (trimmed.isEmpty())
        {
            return
        }

        synchronized(logger)
        {
            logger.log(level, trimmed)
        }
    }
}