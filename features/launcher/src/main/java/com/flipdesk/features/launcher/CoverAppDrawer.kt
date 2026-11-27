package com.flipdesk.features.launcher

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flipdesk.core.database.entity.AppPreferenceEntity
import com.flipdesk.core.design.theme.FluxIconStyle
import com.flipdesk.core.design.theme.FluxThemeTokens
import com.flipdesk.core.platform.AppCoverCompatibility
import com.flipdesk.core.platform.InstalledAppInfo

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CoverAppDrawer(
    apps: List<InstalledAppInfo>,
    preferences: Map<String, AppPreferenceEntity>,
    loadIcon: (String) -> Drawable?,
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onToggleFavorite: (String) -> Unit,
    onToggleHidden: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = FluxThemeTokens.colors
    val iconStyle = FluxThemeTokens.config.iconStyle
    var query by remember { mutableStateOf("") }
    var filterMode by remember { mutableStateOf("All") }
    var selectedForDetails by remember { mutableStateOf<InstalledAppInfo?>(null) }

    val visibleApps = remember(apps, preferences, query, filterMode) {
        apps.filter { app ->
            val pref = preferences[app.packageName]
            val hidden = pref?.isHidden == true
            val matchesQuery = query.isBlank() || app.label.contains(query, ignoreCase = true)
            when (filterMode) {
                "Favorites" -> !hidden && pref?.isFavorite == true && matchesQuery
                "Hidden" -> hidden && matchesQuery
                else -> !hidden && matchesQuery
            }
        }
    }

    val recentApps = remember(apps, preferences) {
        apps.filter { (preferences[it.packageName]?.lastLaunchedAt ?: 0L) > 0L && preferences[it.packageName]?.isHidden != true }
            .sortedByDescending { preferences[it.packageName]?.lastLaunchedAt ?: 0L }
            .take(4)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF0111318),
                        Color(0xF50B0C10)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Frosted Search Pill + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text("Search apps", color = Color.White.copy(alpha = 0.5f), fontSize = 12.sp)
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(color = Color.White, fontSize = 12.sp),
                            cursorBrush = SolidColor(colors.accent),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(Color(0x22FFFFFF))
                        .clickable { onClose() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }

            // Minimal Filter Pills
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("All", "Favorites", "Hidden").forEach { tab ->
                    val selected = filterMode == tab
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) colors.accent.copy(alpha = 0.28f) else Color(0x16FFFFFF))
                            .clickable { filterMode = tab }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            tab,
                            color = if (selected) Color.White else Color.White.copy(alpha = 0.65f),
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            if (recentApps.isNotEmpty() && query.isBlank() && filterMode == "All") {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(recentApps, key = { "recent_${it.packageName}" }) { app ->
                        AppGridTile(
                            app = app,
                            iconStyle = iconStyle,
                            isFavorite = preferences[app.packageName]?.isFavorite == true,
                            loadIcon = loadIcon,
                            onClick = { onLaunchApp(app) },
                            onLongClick = { selectedForDetails = app }
                        )
                    }
                }
            }

            // ColorOS-inspired 3-column squircle app grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(visibleApps, key = { it.packageName }) { app ->
                    AppGridTile(
                        app = app,
                        iconStyle = iconStyle,
                        isFavorite = preferences[app.packageName]?.isFavorite == true,
                        loadIcon = loadIcon,
                        onClick = { onLaunchApp(app) },
                        onLongClick = { selectedForDetails = app }
                    )
                }
            }
        }
    }

    selectedForDetails?.let { app ->
        val pref = preferences[app.packageName]
        AlertDialog(
            onDismissRequest = { selectedForDetails = null },
            title = { Text(app.label, fontSize = 15.sp, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Cover Status: ${app.compatibility.label}", color = colors.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(app.compatibility.explanation, fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = { onToggleFavorite(app.packageName); selectedForDetails = null }) {
                            Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(if (pref?.isFavorite == true) " Unfavorite" else " Favorite", fontSize = 11.sp)
                        }
                        TextButton(onClick = { onToggleHidden(app.packageName); selectedForDetails = null }) {
                            Icon(Icons.Rounded.VisibilityOff, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text(if (pref?.isHidden == true) " Unhide" else " Hide", fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { onLaunchApp(app); selectedForDetails = null }) {
                    Text("Open")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedForDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
private fun AppGridTile(
    app: InstalledAppInfo,
    iconStyle: FluxIconStyle,
    isFavorite: Boolean,
    loadIcon: (String) -> Drawable?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val colors = FluxThemeTokens.colors
    val bitmap = remember(app.packageName) { loadIcon(app.packageName)?.toBitmapOrNull() }
    val shape = when (iconStyle) {
        FluxIconStyle.CIRCLE -> CircleShape
        FluxIconStyle.ROUNDED_SQUIRCLE -> RoundedCornerShape(16.dp)
        FluxIconStyle.MINIMAL_OUTLINE -> RoundedCornerShape(12.dp)
    }

    Column(
        modifier = Modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(shape)
                    .background(Color(0x18FFFFFF)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = app.label,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(54.dp).clip(shape)
                    )
                }
            }
            if (isFavorite) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = null,
                    tint = colors.warning,
                    modifier = Modifier.size(12.dp).align(Alignment.TopEnd)
                )
            }
        }
        Text(
            text = app.label,
            color = Color.White.copy(alpha = 0.92f),
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private fun Drawable.toBitmapOrNull(): Bitmap? = runCatching {
    if (this is BitmapDrawable && bitmap != null) return bitmap
    val w = intrinsicWidth.coerceIn(1, 144)
    val h = intrinsicHeight.coerceIn(1, 144)
    val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    bmp
}.getOrNull()
