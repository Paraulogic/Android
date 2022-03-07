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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.game.lettersString
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord
import com.arnyminerz.paraulogic.ui.elements.ButtonsBox
import com.arnyminerz.paraulogic.ui.elements.listeners.HexButtonClickListener
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel

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
                    val answer = text.trim().lowercase()
                    if (answer.length < 3)
                        context.toast(R.string.toast_short)
                    else if (!answer.contains(gameInfo.centerLetter))
                        context.toast(R.string.toast_missing_center)
                    else if (!gameInfo.words.contains(answer))
                        context.toast(answer)
                    else
                        context.toast("Correcte!")

                    viewModel.introduceWord(gameInfo, text, gameInfo.words.contains(answer))
                    text = ""
                },
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text(stringResource(R.string.action_enter))
            }
        }

        Text(
            text = if (foundWords.isNotEmpty())
                stringResource(
                    R.string.state_found_n_words,
                    foundWords.size,
                    foundWords.joinToString(", ") { it.word }
                )
            else stringResource(R.string.state_found_no_words)
        )
    }
}
