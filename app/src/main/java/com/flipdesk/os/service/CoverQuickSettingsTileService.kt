package com.flipdesk.os.service

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.flipdesk.core.platform.CoverDisplayManager
import com.flipdesk.os.CoverScreenActivity
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CoverQuickSettingsTileService : TileService() {

    @Inject lateinit var coverDisplayManager: CoverDisplayManager

    override fun onStartListening() {
        super.onStartListening()
        qsTile?.apply {
            state = Tile.STATE_ACTIVE
            label = "FlipDesk Cover"
            updateTile()
        }
    }

    override fun onClick() {
        super.onClick()
        val intent = Intent(this, CoverScreenActivity::class.java)
        coverDisplayManager.launchOnCoverOrFallback(intent)
    }
}
