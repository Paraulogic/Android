package com.arnyminerz.paraulogic.ui.dialog

import android.app.Activity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ListItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.utils.getLocale
import com.arnyminerz.paraulogic.utils.updateAppLocale
import java.util.Locale

@Composable
@ExperimentalMaterialApi
fun LanguageDialog(onDismissRequested: () -> Unit) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismissRequested,
        confirmButton = {
            Button(onClick = onDismissRequested) {
                Text(text = stringResource(R.string.action_close))
            }
        },
        text = {
            val systemLocale = stringResource(R.string.system_locale)
            val localeKeys = listOf(/*systemLocale,*/ "en-US", "ca-ES")
            val locales = localeKeys
                .associateWith { key -> Locale.forLanguageTag(key).displayLanguage }
            val currentLocale = (context as? Activity)?.getLocale()?.language ?: systemLocale

            LazyColumn {
                items(locales.toList()) { (tag, name) ->
                    ListItem(
                        icon = {
                            Icon(
                                imageVector = if (currentLocale == tag)
                                    Icons.Rounded.RadioButtonChecked
                                else Icons.Rounded.RadioButtonUnchecked,
                                contentDescription = "",
                            )
                        },
                        modifier = Modifier
                            .clickable {
                                context.updateAppLocale(tag)
                                (context as? Activity)?.recreate()
                                onDismissRequested()
                            }
                    ) {
                        Text(text = name)
                    }
                }
            }
        },
    )
}
