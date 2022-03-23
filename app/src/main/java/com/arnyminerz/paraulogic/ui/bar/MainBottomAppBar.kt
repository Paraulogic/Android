package com.arnyminerz.paraulogic.ui.bar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arnyminerz.paraulogic.R
import com.arnyminerz.paraulogic.activity.SettingsActivity
import com.arnyminerz.paraulogic.game.GameInfo
import com.arnyminerz.paraulogic.ui.dialog.HelpDialog
import com.arnyminerz.paraulogic.utils.launch
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch

@Composable
@ExperimentalPagerApi
fun MainBottomAppBar(
    gameInfo: GameInfo?,
    points: Int,
    level: Int,
    pagerState: PagerState,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Shows the instructions dialog when requested
    var showHelpDialog by remember { mutableStateOf(false) }
    if (showHelpDialog)
        HelpDialog { showHelpDialog = false }

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ) {
        AnimatedVisibility(visible = points > 0) {
            Text(
                text = when (level) {
                    0 -> "\uD83D\uDC24" // 🐤
                    1 -> "\uD83D\uDD4A️" // 🕊️
                    2 -> "\uD83E\uDD86" // 🦆
                    3 -> "\uD83E\uDDA2" // 🦢
                    4 -> "\uD83E\uDD89" // 🦉
                    5 -> "\uD83E\uDD85" // 🦅
                    else -> "\uD83E\uDD9A" // 🦚
                },
                fontSize = 22.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Text(
            text = if (gameInfo == null)
                stringResource(R.string.status_loading)
            else
                stringResource(
                    R.string.points,
                    points,
                    gameInfo.maxPoints
                )
        )
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(
                onClick = {
                    scope.launch {
                        pagerState.animateScrollToPage(if (pagerState.currentPage == 0) 1 else 0)
                    }
                }
            ) {
                Icon(
                    if (pagerState.currentPage == 0)
                        Icons.Outlined.Analytics
                    else Icons.Outlined.Gamepad,
                    stringResource(
                        if (pagerState.currentPage == 0)
                            R.string.image_desc_paraulogic
                        else
                            R.string.image_desc_analytics
                    )
                )
            }
            IconButton(
                onClick = { showHelpDialog = true }
            ) {
                Icon(
                    Icons.Outlined.Info,
                    stringResource(R.string.image_desc_help)
                )
            }
            IconButton(
                onClick = { context.launch(SettingsActivity::class.java) }
            ) {
                Icon(
                    Icons.Outlined.Settings,
                    stringResource(R.string.image_desc_settings)
                )
            }
        }
    }
}

@Preview
@Composable
@OptIn(ExperimentalPagerApi::class)
fun MainBottomAppBarPreview() {
    val gameInfo = GameInfo.random()
    val points = 10
    val level = 2

    MainBottomAppBar(gameInfo, points, level, rememberPagerState())
}
