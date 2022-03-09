package com.arnyminerz.paraulogic.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.arnyminerz.paraulogic.R

/**
 * The dialog that shows the rules of the game.
 * @author Arnau Mora
 * @since 20220309
 * @param onDismissRequest Gets called when the dialog should be hid up.
 */
@Composable
fun HelpDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.info_rules_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(text = stringResource(R.string.info_rules))
                Text(
                    text = stringResource(R.string.info_points_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(text = stringResource(R.string.info_points))
            }
        },
        confirmButton = {
            Button(onClick = onDismissRequest) {
                Text(text = stringResource(R.string.action_close))
            }
        },
    )
}
