package pl.margoj.editor.npc

import org.fxmisc.richtext.model.StyleSpans
import org.fxmisc.richtext.model.StyleSpansBuilder
import java.util.Collections
import java.util.TreeMap
import java.util.regex.Pattern

class NpcEditorHighlighter
{
    private var compiled = false
    private var counter = 0
    private var patterns = TreeMap<String, Pair<String, String>>()
    private lateinit var globalPattern: Pattern

    fun registerPattern(style: String, pattern: String)
    {
        if (compiled)
        {
            throw IllegalStateException("Already compiled")
        }

        this.patterns.put("HIGHTLIGHT${++counter}", Pair(pattern, style))
    }

    fun compile()
    {
        val patternString = StringBuilder()

        var first = true
        for ((highlightGroup, pattern) in this.patterns)
        {
            if (first)
            {
                first = false
            }
            else
            {
                patternString.append("|")
            }

            patternString.append("(?<").append(highlightGroup).append(">").append(pattern.first).append(")")
        }

        this.globalPattern = Pattern.compile(patternString.toString())
        this.compiled = true
    }

    fun computeHighlighting(text: String): StyleSpans<Collection<String>>
    {
        if (!this.compiled)
        {
            throw IllegalStateException("not compiled")
        }

        val spansBuilder = StyleSpansBuilder<Collection<String>>()
        val matcher = this.globalPattern.matcher(text)
        var lastKwEnd = 0

        while (matcher.find())
        {
            var styleClass: String? = null

            for ((group, pattern) in patterns)
            {
                if (matcher.group(group) != null)
                {
                    styleClass = pattern.second
                    break
                }
            }

            spansBuilder.add(Collections.emptyList(), matcher.start() - lastKwEnd)
            spansBuilder.add(Collections.singleton(styleClass!!), matcher.end() - matcher.start())
            lastKwEnd = matcher.end()
        }

        spansBuilder.add(Collections.emptyList(), text.length - lastKwEnd)
        return spansBuilder.create()
    }
}