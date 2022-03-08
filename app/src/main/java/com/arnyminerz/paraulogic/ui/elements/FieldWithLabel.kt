package com.arnyminerz.paraulogic.ui.elements

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp

@Composable
fun FieldWithLabel(
    value: String,
    onValueChange: (newText: String) -> Unit,
    enabled: Boolean,
    @StringRes labelText: Int,
    @StringRes helpText: Int,
    focusRequester: FocusRequester,
    onNext: KeyboardActionScope.() -> Unit,
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        label = {
            Text(
                stringResource(labelText)
            )
        },
        colors = TextFieldDefaults.outlinedTextFieldColors(),
        modifier = Modifier
            .focusRequester(focusRequester)
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
        singleLine = true,
        maxLines = 1,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = onNext)
    )
    Text(
        stringResource(helpText),
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp)
            .fillMaxWidth(),
    )
}
