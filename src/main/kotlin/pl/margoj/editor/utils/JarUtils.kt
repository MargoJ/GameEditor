package pl.margoj.editor.utils

import java.io.IOException
import java.net.URI
import java.nio.file.*
import java.util.Collections

object JarUtils
{
    fun getFilesInJar(rawPath: String): Collection<Path>
    {
        val path = if (rawPath.endsWith("/")) rawPath else (rawPath + "/")

        val url = JarUtils::class.java.getResource(path) ?: throw IOException("Invalid resource")
        val uri: URI

        uri = url.toURI()

        var folder: Path

        try
        {
            folder = Paths.get(uri)
        }
        catch(e: FileSystemNotFoundException)
        {
            folder = FileSystems.newFileSystem(uri, Collections.emptyMap<String, String>()).getPath(path)
        }

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