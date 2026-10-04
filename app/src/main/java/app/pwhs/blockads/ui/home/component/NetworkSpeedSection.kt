package app.pwhs.blockads.ui.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.pwhs.blockads.R
import app.pwhs.blockads.data.network.NetworkSpeed
import app.pwhs.blockads.ui.theme.AccentBlue
import app.pwhs.blockads.ui.theme.SecurityOrange
import app.pwhs.blockads.utils.formatSpeed

@Composable
fun NetworkSpeedSection(
    networkSpeed: NetworkSpeed,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            icon = Icons.Default.Download,
            label = stringResource(R.string.home_download_speed),
            value = formatSpeed(networkSpeed.downloadBps),
            color = AccentBlue,
            chartData = networkSpeed.downloadHistory
        )
        StatCard(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            icon = Icons.Default.Upload,
            label = stringResource(R.string.home_upload_speed),
            value = formatSpeed(networkSpeed.uploadBps),
            color = SecurityOrange,
            chartData = networkSpeed.uploadHistory
        )
    }
}
