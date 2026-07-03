package com.lr.meow.feature.player.model

data class LyricLine(
    val startTimeMs: Long,
    val text: String
)

fun parseLrc(lrcContent: String?): List<LyricLine> {
    if (lrcContent.isNullOrBlank()) return emptyList()
    
    val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")
    val lines = mutableListOf<LyricLine>()
    
    lrcContent.lines().forEach { line ->
        val matchResult = regex.find(line)
        if (matchResult != null) {
            val minutes = matchResult.groupValues[1].toLong()
            val seconds = matchResult.groupValues[2].toLong()
            val millisStr = matchResult.groupValues[3]
            // LRC format usually has 2 or 3 digits for milliseconds
            val millis = if (millisStr.length == 2) millisStr.toLong() * 10 else millisStr.toLong()
            
            val text = matchResult.groupValues[4].trim()
            
            val totalMillis = minutes * 60 * 1000 + seconds * 1000 + millis
            lines.add(LyricLine(totalMillis, text))
        }
    }
    
    // Some lines might not be in order, just in case
    return lines.sortedBy { it.startTimeMs }
}
