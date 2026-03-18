package com.example.first_app_0_0_1.uicomponents

import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import java.util.regex.Pattern

@Composable
fun ClickableNoteText(text: String, onLinkClick: (String) -> Unit) {
    val annotatedString = buildAnnotatedString {
        val pattern = Pattern.compile("\\[\\[(.*?)\\]\\]")
        val matcher = pattern.matcher(text)

        var lastEnd = 0
        while (matcher.find()) {
            val start = matcher.start()
            val end = matcher.end()
            val linkText = matcher.group(1) ?: ""

            if (start > lastEnd) {
                append(text.substring(lastEnd, start))
            }

            pushStringAnnotation(tag = "URL", annotation = linkText)
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline)) {
                append(linkText)
            }
            pop()

            lastEnd = end
        }

        if (lastEnd < text.length) {
            append(text.substring(lastEnd))
        }
    }

    @Suppress("DEPRECATION")
    ClickableText(text = annotatedString) { offset ->
        annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
            .firstOrNull()?.let {
                onLinkClick(it.item)
            }
    }
}
