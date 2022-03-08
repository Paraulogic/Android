package com.arnyminerz.paraulogic.ui

import androidx.annotation.UiThread
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_ALREADY_FOUND
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_CENTER_MISSING
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_CORRECT
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_INCORRECT
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_SHORT
import com.arnyminerz.paraulogic.game.getTutis
import com.arnyminerz.paraulogic.game.lettersString
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.ui.elements.ButtonsBox
import com.arnyminerz.paraulogic.ui.elements.listeners.HexButtonClickListener
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import timber.log.Timber

@UiThread
@Composable
fun Game(
    paddingValues: PaddingValues,
    gameInfo: GameInfo,
    foundWords: List<IntroducedWord>,
    viewModel: MainViewModel
) {
    Column(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val context = LocalContext.current
        var text by remember { mutableStateOf("") }

        TextField(
            value = text,
            onValueChange = { text = it },
            readOnly = true,
            singleLine = true,
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = MaterialTheme.colorScheme.background,
                disabledIndicatorColor = MaterialTheme.colorScheme.primary,
                textColor = MaterialTheme.colorScheme.onBackground,
            ),
            textStyle = LocalTextStyle.current.copy(
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
            ),
            modifier = Modifier.padding(top = 50.dp)
        )

        val letters by remember { gameInfo.letters }

        ButtonsBox(
            letters.lettersString(gameInfo.centerLetter),
            object : HexButtonClickListener {
                override fun onClick(index: Int, letter: Char) {
                    text += letter
                }
            },
            modifier = Modifier.padding(top = 16.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = { if (text.isNotEmpty()) text = text.substring(0, text.length - 1) },
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(stringResource(R.string.action_delete))
            }
            IconButton(onClick = { gameInfo.shuffle() }) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.action_shuffle)
                )
            }
            Button(
                onClick = {
                    val wordCheck = gameInfo.checkWord(text, foundWords)
                    when (wordCheck) {
                        CHECK_WORD_ALREADY_FOUND -> context.toast(R.string.toast_already_found)
                        CHECK_WORD_CENTER_MISSING -> context.toast(R.string.toast_missing_center)
                        CHECK_WORD_INCORRECT -> context.toast(text)
                        CHECK_WORD_SHORT -> context.toast(R.string.toast_short)
                        CHECK_WORD_CORRECT -> context.toast(R.string.toast_correct)
                    }

                    viewModel.introduceWord(gameInfo, text, wordCheck == CHECK_WORD_CORRECT)
                    text = ""
                },
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(stringResource(R.string.action_enter))
            }
        }

        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 8.dp, end = 8.dp),
            text = buildAnnotatedString {
                if (foundWords.isNotEmpty()) {
                    val string = stringResource(R.string.state_found_n_words)
                    val countPosArg = string.indexOf("%1\$d")
                    val totalPosArg = string.indexOf("%2\$d")
                    val listPosArg = string.indexOf("%3\$s")

                    append(string.substring(0, countPosArg))

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(foundWords.size.toString())
                    }
                    append(string.substring(countPosArg + 4, totalPosArg))

                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(gameInfo.words.size.toString())
                    }
                    append(string.substring(totalPosArg + 4, listPosArg))
                    for (word in foundWords) {
                        withStyle(
                            SpanStyle(
                                color = if (gameInfo.isTuti(word.word)) Color.Red else Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(gameInfo.words[word.word.lowercase()] ?: "<error>")
                        }
                        if (foundWords.last() != word)
                            append(", ")
                    }
                } else
                    append(stringResource(R.string.state_found_no_words))
            }
        )

        val tutis = foundWords.getTutis(gameInfo)
        val todayTutis = gameInfo.tutisCount
        Timber.i("Found tutis: $tutis. Today count: $todayTutis")
        Text(
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp),
            text = if (tutis.isEmpty())
                if (todayTutis > 1)
                    stringResource(R.string.state_found_no_tutis/*, todayTutis*/)
                else
                    stringResource(R.string.state_found_no_tuti)
            else if (todayTutis == 1)
                stringResource(R.string.state_found_tuti)
            else if (tutis.size == todayTutis)
                stringResource(R.string.state_found_tutis_all)
            else
                stringResource(R.string.state_found_tutis/*, tutis.size, todayTutis*/)
        )
    }
}
