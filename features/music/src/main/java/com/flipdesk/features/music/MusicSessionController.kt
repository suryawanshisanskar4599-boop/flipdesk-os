package com.flipdesk.features.music

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveMediaState(
    val packageName: String = "",
    val title: String = "No Media Playing",
    val artist: String = "Start Spotify or YT Music",
    val albumArt: Bitmap? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val sessionCount: Int = 0
)

@Singleton
class MusicSessionController @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _state = MutableStateFlow(ActiveMediaState())
    val state: StateFlow<ActiveMediaState> = _state.asStateFlow()

    private var activeControllers: List<MediaController> = emptyList()
    private var selectedIndex: Int = 0

    fun refreshSessions(listenerComponent: ComponentName) {
        val msm = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
        val controllers = runCatching { msm.getActiveSessions(listenerComponent) }.getOrDefault(emptyList())
        activeControllers = controllers
        if (controllers.isEmpty()) {
            _state.value = ActiveMediaState()
            return
        }
        if (selectedIndex >= controllers.size) selectedIndex = 0
        publishController(controllers[selectedIndex], controllers.size)
    }

    fun cycleSession() {
        if (activeControllers.size <= 1) return
        selectedIndex = (selectedIndex + 1) % activeControllers.size
        publishController(activeControllers[selectedIndex], activeControllers.size)
    }

    fun togglePlayPause() {
        val controller = activeControllers.getOrNull(selectedIndex) ?: return
        val isPlaying = controller.playbackState?.state == PlaybackState.STATE_PLAYING
        if (isPlaying) controller.transportControls.pause() else controller.transportControls.play()
    }

    fun skipNext() {
        activeControllers.getOrNull(selectedIndex)?.transportControls?.skipToNext()
    }

    fun skipPrevious() {
        activeControllers.getOrNull(selectedIndex)?.transportControls?.skipToPrevious()
    }

    fun seekTo(positionMs: Long) {
        activeControllers.getOrNull(selectedIndex)?.transportControls?.seekTo(positionMs)
    }

    private fun publishController(controller: MediaController, totalCount: Int) {
        val meta = controller.metadata
        val playback = controller.playbackState
        _state.value = ActiveMediaState(
            packageName = controller.packageName,
            title = meta?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: "Unknown Track",
            artist = meta?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: controller.packageName,
            albumArt = meta?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)
                ?: meta?.getBitmap(MediaMetadata.METADATA_KEY_ART),
            isPlaying = playback?.state == PlaybackState.STATE_PLAYING,
            positionMs = playback?.position ?: 0L,
            durationMs = meta?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L,
            sessionCount = totalCount
        )
    }
}
