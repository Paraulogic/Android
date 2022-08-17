package com.arnyminerz.paraulogic.activity

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Start
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.activity.PermissionActivity.Companion.EXTRA_MESSAGE
import com.arnyminerz.paraulogic.activity.PermissionActivity.Companion.EXTRA_PERMISSIONS
import com.arnyminerz.paraulogic.activity.PermissionActivity.Companion.EXTRA_REQUIRED
import com.arnyminerz.paraulogic.ui.theme.AppTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * Used for requesting permissions to the user.
 *
 * Requires:
 * * [EXTRA_MESSAGE] (`String`): The explanation message to show the reason of the permission.
 * * [EXTRA_PERMISSIONS] (`Array<String>`): An array with all the permissions to request.
 *
 * Optional:
 * * [EXTRA_REQUIRED] (`boolean` - `true`): Whether the permission is required, or optional. If
 * `true` the application cannot be used without that permission.
 */
@ExperimentalMaterial3Api
@ExperimentalPermissionsApi
class PermissionActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_PERMISSIONS = "permissions"
        const val EXTRA_REQUIRED = "required"

        const val RESULT_PERMISSION_GRANTED = 0

        /**
         * Thrown when no extras were set, or there are missing ones.
         * @author Arnau Mora
         * @since 20220817
         */
        const val RESULT_NO_EXTRAS = 1

        /**
         * When the user requests the permission to be skipped. Only available if [EXTRA_REQUIRED] is
         * set to false.
         * @author Arnau Mora
         * @since 20220817
         */
        const val RESULT_PERMISSION_SKIPPED = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extras = intent.extras
        if (extras == null ||
            !extras.containsKey(EXTRA_MESSAGE) ||
            !extras.containsKey(EXTRA_PERMISSIONS)
        ) {
            setResult(RESULT_NO_EXTRAS)
            finish()
            return
        }
        val message = extras.getString(EXTRA_MESSAGE)!!
        val permissions = extras.getStringArray(EXTRA_PERMISSIONS)!!.toList()
        val required = extras.getBoolean(EXTRA_REQUIRED, true)

        setContent {
            AppTheme {
                val permission = rememberMultiplePermissionsState(permissions)

                BackHandler {
                    when {
                        !required -> {
                            setResult(RESULT_PERMISSION_SKIPPED)
                            finish()
                        }
                        !permission.allPermissionsGranted -> finishAndRemoveTask()
                        else -> {
                            setResult(RESULT_PERMISSION_GRANTED)
                            finish()
                        }
                    }
                }

                Scaffold(
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = {
                                when (permission.allPermissionsGranted) {
                                    true -> {
                                        setResult(RESULT_PERMISSION_GRANTED)
                                        finish()
                                    }
                                    !required -> {
                                        setResult(RESULT_PERMISSION_SKIPPED)
                                        finish()
                                    }
                                    else -> permission.launchMultiplePermissionRequest()
                                }
                            }
                        ) {
                            Icon(
                                when (permission.allPermissionsGranted) {
                                    true -> Icons.Rounded.ChevronRight
                                    !required -> Icons.Rounded.Start
                                    else -> Icons.Rounded.Lock
                                },
                                contentDescription = stringResource(
                                    when (permission.allPermissionsGranted) {
                                        true -> R.string.image_desc_continue
                                        !required -> R.string.image_desc_skip_permission
                                        else -> R.string.image_desc_grant_permission
                                    }
                                ),
                            )
                            Text(
                                text = stringResource(
                                    when (permission.allPermissionsGranted) {
                                        true -> R.string.image_desc_continue
                                        !required -> R.string.image_desc_skip_permission
                                        else -> R.string.image_desc_grant_permission
                                    }
                                ),
                            )
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "🔒",
                            fontSize = 80.sp,
                            modifier = Modifier
                                .padding(top = 72.dp),
                        )
                        Text(
                            text = stringResource(R.string.permission_title),
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            modifier = Modifier
                                .padding(start = 12.dp, end = 12.dp, top = 32.dp),
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .padding(start = 28.dp, end = 28.dp, top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}
