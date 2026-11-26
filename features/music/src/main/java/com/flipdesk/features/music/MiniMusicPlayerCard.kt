package com.flipdesk.features.music

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipdesk.core.design.components.FluxGlassCard
import com.flipdesk.core.design.theme.FluxThemeTokens

@Composable
fun MiniMusicPlayerCard(
    media: ActiveMediaState,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onSwitchSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FluxThemeTokens.colors

    FluxGlassCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    if (media.albumArt != null) {
                        Image(
                            bitmap = media.albumArt.asImageBitmap(),
                            contentDescription = media.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(54.dp)
                        )
                    } else {
                        Icon(Icons.Rounded.MusicNote, contentDescription = null, tint = colors.accent, modifier = Modifier.size(26.dp))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(media.title, color = colors.textPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(media.artist, color = colors.textSecondary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (media.sessionCount > 1) {
                    IconButton(onClick = onSwitchSession, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Rounded.SwapHoriz, contentDescription = "Switch player", tint = colors.accent, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (media.durationMs > 0L) {
                Slider(
                    value = (media.positionMs.toFloat() / media.durationMs.toFloat()).coerceIn(0f, 1f),
                    onValueChange = { frac -> onSeek((frac * media.durationMs).toLong()) }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Rounded.SkipPrevious, contentDescription = "Previous", tint = colors.textPrimary)
                }
                IconButton(
                    onClick = onPlayPause,
                    modifier = Modifier.size(44.dp).clip(CircleShape).background(colors.primary.copy(alpha = 0.25f))
                ) {
                    Icon(
                        if (media.isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = colors.textPrimary
                    )
                }
                IconButton(onClick = onNext) {
                    Icon(Icons.Rounded.SkipNext, contentDescription = "Next", tint = colors.textPrimary)
                }
            }
        }
    }
}
