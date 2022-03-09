package com.arnyminerz.paraulogic.ui.screen

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.game.GameHistoryItem
import com.arnyminerz.paraulogic.game.maxDate
import com.arnyminerz.paraulogic.game.minDate
import com.arnyminerz.paraulogic.ui.elements.DatePicker
import com.arnyminerz.paraulogic.ui.viewmodel.MainViewModel
import timber.log.Timber
import java.util.Calendar

@Composable
@ExperimentalMaterial3Api
fun StatsScreen(viewModel: MainViewModel, gameHistory: SnapshotStateList<GameHistoryItem>) {
    val selectDateText = stringResource(R.string.action_select_date)
    var buttonText by remember { mutableStateOf(selectDateText) }
    var showPicker by remember { mutableStateOf(false) }

    var historyItem by remember { mutableStateOf<GameHistoryItem?>(null) }
    var historyItemWords by remember { mutableStateOf<List<String>>(emptyList()) }
    var historyItemTutis by remember { mutableStateOf<List<String>>(emptyList()) }

    val dayFoundWords = viewModel.dayFoundWords
    val dayFoundTutis = viewModel.dayFoundTutis

    if (showPicker)
        DatePicker(
            minDate = gameHistory.minDate(),
            maxDate = gameHistory.maxDate(),
            onDateSelected = { date ->
                buttonText = DateFormat.format("dd-MM-yyyy", date).toString()

                val selectedDate = Calendar.getInstance()
                selectedDate.time = date

                for (item in gameHistory) {
                    val itemDate = Calendar.getInstance()
                    itemDate.time = item.date
                    if (itemDate.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR) &&
                        itemDate.get(Calendar.MONTH) == selectedDate.get(
                            Calendar.MONTH
                        ) &&
                        itemDate.get(Calendar.DAY_OF_MONTH) == selectedDate.get(
                            Calendar.DAY_OF_MONTH
                        )
                    ) {
                        historyItem = item
                        historyItemWords = item.gameInfo.words.keys.toList()
                        historyItemTutis = item.gameInfo.tutis
                        break
                    }
                }

                if (historyItem != null)
                    viewModel.loadWordsForDay(historyItem!!.gameInfo, date)
                else
                    Timber.e("Could not find history item for $date")
            },
            onDismissRequest = {
                showPicker = false
            },
        )

    Row {
        Button(
            onClick = { showPicker = true },
            colors = ButtonDefaults.outlinedButtonColors(),
        ) {
            Text(text = buttonText)
        }
    }
    if (historyItem != null && dayFoundWords.isNotEmpty())
        Row {
            Card(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = if (historyItemTutis.size == 1)
                        if (dayFoundTutis.size == historyItemTutis.size)
                            stringResource(
                                R.string.stats_day_tuti,
                                buttonText,
                                dayFoundWords.size,
                                historyItemWords.size
                            )
                        else
                            stringResource(
                                R.string.stats_day_no_tuti,
                                buttonText,
                                dayFoundWords.size,
                                historyItemWords.size
                            )
                    else if (dayFoundTutis.size == historyItemTutis.size)
                        stringResource(
                            R.string.stats_day_all_tutis,
                            buttonText,
                            dayFoundWords.size,
                            historyItemWords.size,
                            historyItemTutis.size
                        )
                    else
                        stringResource(
                            R.string.stats_day_n_tutis,
                            buttonText,
                            dayFoundWords.size,
                            historyItemWords.size,
                            dayFoundTutis.size,
                            historyItemTutis.size
                        )
                )
            }
        }
}
