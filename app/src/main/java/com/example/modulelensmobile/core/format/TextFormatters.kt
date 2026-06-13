package com.example.modulelensmobile.core.format

private val WhitespaceRegex = Regex("\\s+")

fun String.toDisplayLabel(): String {
    return split("_", "-", " ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { word -> word.replaceFirstChar { it.uppercase() } }
}

fun String.toPreview(maxLength: Int = 140): String {
    return replace("\n", " ")
        .replace(WhitespaceRegex, " ")
        .trim()
        .take(maxLength)
}

fun String.toReadableDate(): String {
    return takeIf { it.length >= 10 }?.substring(0, 10) ?: this
}
