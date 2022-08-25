package com.arnyminerz.paraulogic.ui

import android.media.MediaPlayer
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_ALREADY_FOUND
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_CENTER_MISSING
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_CORRECT
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_INCORRECT
import com.arnyminerz.paraulogic.game.annotation.CHECK_WORD_SHORT
import com.arnyminerz.paraulogic.game.lettersString
import com.arnyminerz.paraulogic.ui.elements.ActionsButton
import com.arnyminerz.paraulogic.ui.elements.ButtonsBox
import com.arnyminerz.paraulogic.ui.elements.PointsText
import com.arnyminerz.paraulogic.ui.elements.TutisText
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import timber.log.Timber

@UiThread
@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun AppCompatActivity.Game(
    gameInfo: GameInfo,
    viewModel: MainViewModel,
) {
    val primaryContainerColor = MaterialTheme.colorScheme.primaryContainer
    val onPrimaryContainerColor = MaterialTheme.colorScheme.onPrimaryContainer
    val errorContainerColor = MaterialTheme.colorScheme.errorContainer
    val onErrorContainerColor = MaterialTheme.colorScheme.onErrorContainer

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val context = LocalContext.current
        var text by remember { mutableStateOf("") }

        var oldLevel by remember { mutableStateOf(0) }
        val level = viewModel.level
        val foundWords = viewModel.correctWords
        val tutis = viewModel.introducedTutis

        TextField(
            value = text,
            onValueChange = { /*text = it*/ },
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
            { _, letter -> text += letter },
            modifier = Modifier.padding(top = 16.dp),
            enabled = !viewModel.isLoading,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
            ) {
                ActionsButton(
                    onClick = { if (text.isNotEmpty()) text = text.substring(0, text.length - 1) },
                    onLongClick = { text = "" },
                    colors = ButtonDefaults.outlinedButtonColors(),
                    enabled = !viewModel.isLoading,
                ) { Text(stringResource(R.string.action_delete)) }
            }
            IconButton(
                onClick = { gameInfo.shuffle() },
                enabled = !viewModel.isLoading,
            ) {
                Icon(
                    Icons.Rounded.Refresh,
                    contentDescription = stringResource(R.string.action_shuffle)
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.Start,
            ) {
                Button(
                    onClick = {
                        val wordCheck = gameInfo.checkWord(text, foundWords)
                        when (wordCheck) {
                            CHECK_WORD_ALREADY_FOUND -> customToast(
                                context,
                                R.string.toast_already_found,
                                errorContainerColor,
                                onErrorContainerColor,
                            )
                            CHECK_WORD_CENTER_MISSING -> customToast(
                                context,
                                R.string.toast_missing_center,
                                errorContainerColor,
                                onErrorContainerColor,
                            )
                            CHECK_WORD_INCORRECT -> customToast(
                                context, text.lowercase(),
                                errorContainerColor,
                                onErrorContainerColor,
                            )
                            CHECK_WORD_SHORT -> customToast(
                                context, R.string.toast_short,
                                errorContainerColor,
                                onErrorContainerColor,
                            )
                            CHECK_WORD_CORRECT -> customToast(
                                context, R.string.toast_correct,
                                primaryContainerColor,
                                onPrimaryContainerColor,
                            )
                        }

                        if (wordCheck == CHECK_WORD_CORRECT) {
                            if (oldLevel != level) {
                                Timber.i("Playing sound (level=$level,oldLevel=$oldLevel)...")
                                val mp = MediaPlayer.create(
                                    context,
                                    when (level) {
                                        0 -> R.raw.pollet
                                        1 -> R.raw.colom
                                        2 -> R.raw.anec
                                        3 -> R.raw.cigne
                                        4 -> R.raw.oliba
                                        5 -> R.raw.aguila
                                        else -> R.raw.pao
                                    }
                                )
                                mp.start()
                                oldLevel = level
                            } else
                                Timber.d("Won't play sound. Old level: $oldLevel, level: $level")
                        }

                        viewModel.introduceWord(
                            this@Game,
                            gameInfo,
                            text,
                            wordCheck == CHECK_WORD_CORRECT,
                        )
                        text = ""
                    },
                    colors = ButtonDefaults.outlinedButtonColors(),
                    enabled = !viewModel.isLoading,
                ) { Text(stringResource(R.string.action_enter)) }
            }
        }

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .weight(1f),
        ) {
            PointsText(foundWords, gameInfo)

            val todayTutis = gameInfo.tutisCount
            TutisText(todayTutis, tutis)
        }
    }
}
