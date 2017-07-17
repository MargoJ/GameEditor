package pl.margoj.editor.utils

import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.net.URI
import java.nio.file.*
import java.util.Collections

object JarUtils
{
    private val logger = LogManager.getLogger(JarUtils::class.java)

    fun getFilesInJar(rawPath: String): Collection<Path>
    {
        logger.trace("getFilesInJar(rawPath = $rawPath)")
        val path = if (rawPath.endsWith("/")) rawPath else (rawPath + "/")
        logger.debug("rawPath = $rawPath, path = $path")

        val url = JarUtils::class.java.getResource(path) ?: throw IOException("Invalid resource")
        val uri: URI

        uri = url.toURI()

        logger.debug("uri = $uri")
        var folder: Path

        try
        {
            logger.trace("Paths.get($uri)")
            folder = Paths.get(uri)
            logger.debug("Paths.get success")
        }
        catch(e: FileSystemNotFoundException)
        {
            logger.trace("FileSystems.newFileSystem($uri).getPath($path)")
            folder = FileSystems.newFileSystem(uri, Collections.emptyMap<String, String>()).getPath(path)
        }

        logger.debug("folder = $folder")

        val out = ArrayList<Path>()

        val iter = Files.walk(folder, 1).iterator()

        while (iter.hasNext())
        {
            val file = iter.next()
            if (file == folder)
            {
                continue
            }
            out.add(file.fileName)
        }

        return out
    }
}