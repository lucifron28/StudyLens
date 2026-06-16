package com.example.studylensmobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    bodyStyle: TextStyle = MaterialTheme.typography.bodyLarge
) {
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Heading -> MarkdownInlineText(
                    markdown = block.text,
                    color = color,
                    style = headingStyle(block.level)
                )
                is MarkdownBlock.Paragraph -> MarkdownInlineText(
                    markdown = block.text,
                    color = color,
                    style = bodyStyle
                )
                is MarkdownBlock.Bullet -> MarkdownListRow(
                    marker = "-",
                    text = block.text,
                    color = color,
                    style = bodyStyle
                )
                is MarkdownBlock.Numbered -> MarkdownListRow(
                    marker = "${block.number}.",
                    text = block.text,
                    color = color,
                    style = bodyStyle
                )
                is MarkdownBlock.Quote -> MarkdownInlineText(
                    markdown = block.text,
                    color = color,
                    style = bodyStyle.copy(fontStyle = FontStyle.Italic),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )
                is MarkdownBlock.Code -> Text(
                    text = block.text,
                    color = color,
                    style = bodyStyle.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun MarkdownInlineText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null
) {
    val annotatedText = remember(markdown) { markdown.toMarkdownAnnotatedString() }

    Text(
        text = annotatedText,
        color = color,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
private fun MarkdownListRow(
    marker: String,
    text: String,
    color: Color,
    style: TextStyle
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = marker,
            color = color,
            style = style,
            fontWeight = FontWeight.Bold
        )
        MarkdownInlineText(
            markdown = text,
            color = color,
            style = style,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun headingStyle(level: Int): TextStyle {
    return when (level) {
        1 -> MaterialTheme.typography.titleLarge
        2 -> MaterialTheme.typography.titleMedium
        else -> MaterialTheme.typography.bodyLarge
    }.copy(fontWeight = FontWeight.Bold)
}

private sealed interface MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock
    data class Paragraph(val text: String) : MarkdownBlock
    data class Bullet(val text: String) : MarkdownBlock
    data class Numbered(val number: String, val text: String) : MarkdownBlock
    data class Quote(val text: String) : MarkdownBlock
    data class Code(val text: String) : MarkdownBlock
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val paragraphLines = mutableListOf<String>()
    val lines = markdown.replace("\r\n", "\n").lines()
    var index = 0

    fun flushParagraph() {
        if (paragraphLines.isNotEmpty()) {
            blocks += MarkdownBlock.Paragraph(paragraphLines.joinToString(" "))
            paragraphLines.clear()
        }
    }

    while (index < lines.size) {
        val line = lines[index]
        val trimmed = line.trim()

        when {
            trimmed.isBlank() -> flushParagraph()
            trimmed.startsWith("```") -> {
                flushParagraph()
                val codeLines = mutableListOf<String>()
                index++
                while (index < lines.size && !lines[index].trim().startsWith("```")) {
                    codeLines += lines[index]
                    index++
                }
                blocks += MarkdownBlock.Code(codeLines.joinToString("\n").trimEnd())
            }
            HEADING_REGEX.matches(trimmed) -> {
                flushParagraph()
                val match = HEADING_REGEX.matchEntire(trimmed)
                if (match != null) {
                    blocks += MarkdownBlock.Heading(
                        level = match.groupValues[1].length,
                        text = match.groupValues[2]
                    )
                }
            }
            BULLET_REGEX.matches(trimmed) -> {
                flushParagraph()
                BULLET_REGEX.matchEntire(trimmed)?.let { match ->
                    blocks += MarkdownBlock.Bullet(match.groupValues[1])
                }
            }
            NUMBERED_REGEX.matches(trimmed) -> {
                flushParagraph()
                NUMBERED_REGEX.matchEntire(trimmed)?.let { match ->
                    blocks += MarkdownBlock.Numbered(
                        number = match.groupValues[1],
                        text = match.groupValues[2]
                    )
                }
            }
            trimmed.startsWith(">") -> {
                flushParagraph()
                blocks += MarkdownBlock.Quote(trimmed.removePrefix(">").trim())
            }
            else -> paragraphLines += trimmed
        }

        index++
    }

    flushParagraph()
    return blocks.ifEmpty { listOf(MarkdownBlock.Paragraph(markdown.trim())) }
}

private fun String.toMarkdownAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        appendMarkdownInline(this@toMarkdownAnnotatedString)
    }
}

private fun AnnotatedString.Builder.appendMarkdownInline(source: String) {
    var index = 0

    while (index < source.length) {
        when {
            source.startsWith("**", index) -> {
                val end = source.indexOf("**", startIndex = index + 2)
                if (end > index + 2) {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(source.substring(index + 2, end))
                    pop()
                    index = end + 2
                } else {
                    append(source[index])
                    index++
                }
            }
            source[index] == '`' -> {
                val end = source.indexOf('`', startIndex = index + 1)
                if (end > index + 1) {
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x1A000000)
                        )
                    )
                    append(source.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(source[index])
                    index++
                }
            }
            source[index] == '*' || source[index] == '_' -> {
                val marker = source[index]
                val end = source.indexOf(marker, startIndex = index + 1)
                if (end > index + 1) {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                    append(source.substring(index + 1, end))
                    pop()
                    index = end + 1
                } else {
                    append(source[index])
                    index++
                }
            }
            source[index] == '[' -> {
                val labelEnd = source.indexOf("](", startIndex = index)
                val linkEnd = if (labelEnd != -1) source.indexOf(')', startIndex = labelEnd + 2) else -1
                if (labelEnd > index + 1 && linkEnd > labelEnd) {
                    pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                    append(source.substring(index + 1, labelEnd))
                    pop()
                    index = linkEnd + 1
                } else {
                    append(source[index])
                    index++
                }
            }
            else -> {
                append(source[index])
                index++
            }
        }
    }
}

private val HEADING_REGEX = Regex("^(#{1,6})\\s+(.+)$")
private val BULLET_REGEX = Regex("^[-*+]\\s+(.+)$")
private val NUMBERED_REGEX = Regex("^(\\d+)[.)]\\s+(.+)$")
