package com.arnyminerz.paraulogic.ui.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.utils.launchUrl

@Composable
fun PointsText(foundWords: List<IntroducedWord>, gameInfo: GameInfo) {
    val context = LocalContext.current

    val words = foundWords.sortedBy { it.word }

    val annotatedText = buildAnnotatedString {
        if (words.isNotEmpty()) {
            val string = stringResource(R.string.state_found_n_words)
            val countPosArg = string.indexOf("%1\$d")
            val totalPosArg = string.indexOf("%2\$d")
            val listPosArg = string.indexOf("%3\$s")

            append(string.substring(0, countPosArg))

            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(words.size.toString())
            }
            append(string.substring(countPosArg + 4, totalPosArg))

            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                append(gameInfo.words.size.toString())
            }
            append(string.substring(totalPosArg + 4, listPosArg))

            for (word in words) {
                pushStringAnnotation("definition", word.word.lowercase())
                withStyle(
                    SpanStyle(
                        color = if (gameInfo.isTuti(word.word))
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Bold
                    )
                ) {
                    append(gameInfo.words[word.word.lowercase()] ?: "<error>")
                }
                pop()
                if (words.last() != word)
                    append(", ")
            }
        } else
            append(stringResource(R.string.state_found_no_words))
    }

    ClickableText(
        style = MaterialTheme.typography.bodyMedium
            .copy(
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            ),
        modifier = Modifier
            .padding(start = 8.dp, end = 8.dp),
        text = annotatedText,
        onClick = { offset ->
            annotatedText
                .getStringAnnotations(tag = "definition", start = offset, end = offset)
                .firstOrNull()
                ?.let { annotation ->
                    context.launchUrl("https://dlc.iec.cat/Results?DecEntradaText=${annotation.item}")
                }
        }
    )
}
