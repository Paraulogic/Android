package com.arnyminerz.paraulogic.ui.elements

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.storage.entity.IntroducedWord

@Composable
fun TutisText(tutisCount: Int, tutis: List<IntroducedWord>) {
    Text(
        style = MaterialTheme.typography.bodyMedium
            .copy(
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            ),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp),
        text = if (tutis.isEmpty())
            if (tutisCount > 1)
                stringResource(R.string.state_found_no_tutis, tutisCount)
            else
                stringResource(R.string.state_found_no_tuti)
        else if (tutisCount == 1)
            stringResource(R.string.state_found_tuti)
        else if (tutis.size == tutisCount)
            stringResource(R.string.state_found_tutis_all)
        else
            stringResource(R.string.state_found_tutis, tutis.size, tutisCount)
    )
}
