package com.arnyminerz.paraulogic.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.ui.elements.FieldWithLabel
import com.arnyminerz.paraulogic.ui.toast
import com.arnyminerz.paraulogic.utils.activity
import io.sentry.Sentry
import io.sentry.UserFeedback

@Composable
@ExperimentalMaterial3Api
fun FeedbackScreen() {
    val context = LocalContext.current

    var fieldsEnabled by remember { mutableStateOf(true) }
    var nameField by remember { mutableStateOf("") }
    var emailField by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }

    val nameFocusRequester = FocusRequester()
    val emailFocusRequester = FocusRequester()
    val msgFocusRequester = FocusRequester()

    var isMessageAnError by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (message.isBlank())
                        isMessageAnError = true
                    else {
                        fieldsEnabled = false
                        val sentryId = Sentry.captureMessage(message)
                        val userFeedback = UserFeedback(sentryId)
                            .apply {
                                if (emailField.isNotEmpty())
                                    email = emailField
                                if (nameField.isNotEmpty())
                                    name = nameField
                            }
                        Sentry.captureUserFeedback(userFeedback)

                        context.toast(R.string.toast_message_sent)
                        fieldsEnabled = true

                        context.activity?.onBackPressed()
                    }
                }
            ) {
                Icon(
                    Icons.Rounded.Send,
                    contentDescription = stringResource(R.string.action_send)
                )
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.feedback_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.Center,
            )
            FieldWithLabel(
                nameField,
                { nameField = it },
                fieldsEnabled,
                R.string.feedback_field_name,
                R.string.feedback_field_name_hint,
                nameFocusRequester,
            ) { emailFocusRequester.requestFocus() }
            FieldWithLabel(
                emailField,
                { emailField = it },
                fieldsEnabled,
                R.string.feedback_field_email,
                R.string.feedback_field_email_hint,
                emailFocusRequester,
            ) { msgFocusRequester.requestFocus() }
            TextField(
                value = message,
                onValueChange = { message = it; isMessageAnError = false },
                modifier = Modifier
                    .focusRequester(msgFocusRequester)
                    .padding(start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                label = {
                    Text(text = stringResource(R.string.feedback_field_message))
                },
                isError = isMessageAnError,
                colors = TextFieldDefaults.outlinedTextFieldColors(),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            )
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun FeedbackScreenPreview() {
    FeedbackScreen()
}