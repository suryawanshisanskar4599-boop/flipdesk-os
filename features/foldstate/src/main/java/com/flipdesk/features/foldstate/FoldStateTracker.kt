package com.flipdesk.features.foldstate

import android.app.Activity
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import com.flipdesk.core.platform.CoverDisplayManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class DevicePosture {
    UNFOLDED_FLAT,
    HALF_OPENED_FLEX,
    FOLDED_COVER_ACTIVE,
    UNKNOWN
}

data class FoldStateSnapshot(
    val posture: DevicePosture = DevicePosture.UNKNOWN,
    val coverDisplayId: Int? = null,
    val hingeOrientation: String? = null
)

@Singleton
class FoldStateTracker @Inject constructor(
    private val coverDisplayManager: CoverDisplayManager
) {
    fun observePosture(activity: Activity): Flow<FoldStateSnapshot> {
        val windowFlow = WindowInfoTracker.getOrCreate(activity)
            .windowLayoutInfo(activity)
            .map { layoutInfo ->
                layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>().firstOrNull()
            }

        return combine(windowFlow, coverDisplayManager.observeDisplayChanges()) { foldingFeature, coverInfo ->
            val posture = when {
                coverInfo != null && foldingFeature == null -> DevicePosture.FOLDED_COVER_ACTIVE
                foldingFeature?.state == FoldingFeature.State.HALF_OPENED -> DevicePosture.HALF_OPENED_FLEX
                foldingFeature?.state == FoldingFeature.State.FLAT -> DevicePosture.UNFOLDED_FLAT
                else -> DevicePosture.UNKNOWN
            }
            FoldStateSnapshot(
                posture = posture,
                coverDisplayId = coverInfo?.displayId,
                hingeOrientation = foldingFeature?.orientation?.toString()
            )
        }
    }
}
