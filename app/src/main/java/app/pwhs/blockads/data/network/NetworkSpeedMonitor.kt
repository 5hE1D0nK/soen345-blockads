package app.pwhs.blockads.data.network

import android.net.TrafficStats
import android.os.SystemClock
import app.pwhs.blockads.ui.home.component.MiniBarChartDefaults
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

data class NetworkSpeed(
    val downloadBps: Long = 0L,
    val uploadBps: Long = 0L,
    val downloadHistory: List<Float> = MiniBarChartDefaults.TotalQueriesSample,
    val uploadHistory: List<Float> = MiniBarChartDefaults.BlockedQueriesSample
)

object NetworkSpeedMonitor {
    private const val HISTORY_SIZE = 10

    fun observeNetworkSpeed(intervalMs: Long = 1000L): Flow<NetworkSpeed> = flow {
        var lastRx = TrafficStats.getTotalRxBytes()
        var lastTx = TrafficStats.getTotalTxBytes()
        var lastTime = SystemClock.elapsedRealtime()

        val rxHistory = ArrayDeque<Long>(HISTORY_SIZE)
        val txHistory = ArrayDeque<Long>(HISTORY_SIZE)

        repeat(HISTORY_SIZE) {
            rxHistory.add(0L)
            txHistory.add(0L)
        }

        while (true) {
            delay(intervalMs)
            val now = SystemClock.elapsedRealtime()
            val currentRx = TrafficStats.getTotalRxBytes()
            val currentTx = TrafficStats.getTotalTxBytes()

            val dt = ((now - lastTime).coerceAtLeast(1)) / 1000f

            val rxDiff = if (lastRx != TrafficStats.UNSUPPORTED.toLong() && currentRx >= lastRx) {
                currentRx - lastRx
            } else 0L

            val txDiff = if (lastTx != TrafficStats.UNSUPPORTED.toLong() && currentTx >= lastTx) {
                currentTx - lastTx
            } else 0L

            val rxSpeed = (rxDiff / dt).toLong()
            val txSpeed = (txDiff / dt).toLong()

            lastRx = currentRx
            lastTx = currentTx
            lastTime = now

            if (rxHistory.size >= HISTORY_SIZE) rxHistory.removeFirst()
            rxHistory.add(rxSpeed)

            if (txHistory.size >= HISTORY_SIZE) txHistory.removeFirst()
            txHistory.add(txSpeed)

            val maxRx = rxHistory.maxOrNull()?.coerceAtLeast(1L) ?: 1L
            val maxTx = txHistory.maxOrNull()?.coerceAtLeast(1L) ?: 1L

            val normRx = if (maxRx <= 1024L) {
                MiniBarChartDefaults.TotalQueriesSample
            } else {
                rxHistory.map {
                    if (it == 0L) 0.15f
                    else (it.toFloat() / maxRx).coerceIn(0.18f, 1f)
                }
            }

            val normTx = if (maxTx <= 1024L) {
                MiniBarChartDefaults.BlockedQueriesSample
            } else {
                txHistory.map {
                    if (it == 0L) 0.15f
                    else (it.toFloat() / maxTx).coerceIn(0.18f, 1f)
                }
            }

            emit(
                NetworkSpeed(
                    downloadBps = rxSpeed,
                    uploadBps = txSpeed,
                    downloadHistory = normRx,
                    uploadHistory = normTx
                )
            )
        }
    }
}
