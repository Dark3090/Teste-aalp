package com.darkk.compatcontrol.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.darkk.compatcontrol.data.AppInfo
import com.darkk.compatcontrol.ui.MainViewModel
import com.darkk.compatcontrol.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    viewModel: MainViewModel,
    onAppClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val filteredApps = viewModel.filteredApps()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 48.dp, 16.dp, 8.dp)
        ) {
            Column {
                Text(
                    "CompatControl",
                    color = Accent,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Controle de compatibilidade por app",
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Root status indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (state.isRootAvailable) Green else Red)
                )
                Text(
                    if (state.isRootAvailable) "Root" else "Sem Root",
                    color = if (state.isRootAvailable) Green else Red,
                    fontSize = 11.sp
                )
            }
        }

        // Search bar
        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = viewModel::setSearchQuery,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Buscar app...", color = TextMuted, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextMuted) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Accent,
                unfocusedBorderColor = Border,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Accent,
                focusedContainerColor = Surface,
                unfocusedContainerColor = Surface
            ),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        // System apps toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${filteredApps.size} apps",
                color = TextMuted,
                fontSize = 11.sp
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Apps do sistema", color = TextMuted, fontSize = 11.sp)
                Switch(
                    checked = state.showSystemApps,
                    onCheckedChange = { viewModel.toggleSystemApps() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = BgDark,
                        checkedTrackColor = Accent,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = Border
                    ),
                    modifier = Modifier.height(24.dp)
                )
            }
        }

        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Accent)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filteredApps, key = { it.packageName }) { app ->
                    AppItem(
                        app = app,
                        isConfigured = app.packageName in state.configuredApps,
                        onClick = { onAppClick(app.packageName) }
                    )
                }
            }
        }
    }
}

@Composable
fun AppItem(app: AppInfo, isConfigured: Boolean, onClick: () -> Unit) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(12.dp),
        border = if (isConfigured)
            androidx.compose.foundation.BorderStroke(1.dp, Accent.copy(alpha = 0.4f))
        else
            androidx.compose.foundation.BorderStroke(1.dp, Border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App icon
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(context.packageManager.getApplicationIcon(app.packageName))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    app.label,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    app.packageName,
                    color = TextMuted,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (isConfigured) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Accent.copy(alpha = 0.1f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ATIVO", color = Accent, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
                if (app.isSystemApp) {
                    Text("sistema", color = TextMuted, fontSize = 9.sp)
                }
            }

            Icon(
                Icons.Default.Settings,
                contentDescription = null,
                tint = if (isConfigured) Accent else TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
