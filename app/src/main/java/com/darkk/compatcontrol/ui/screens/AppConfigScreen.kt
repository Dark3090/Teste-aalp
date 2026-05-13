package com.darkk.compatcontrol.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.darkk.compatcontrol.data.AppConfig
import com.darkk.compatcontrol.data.ScaleValues
import com.darkk.compatcontrol.ui.MainViewModel
import com.darkk.compatcontrol.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppConfigScreen(
    packageName: String,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val pm = context.packageManager

    val appLabel = remember {
        try { pm.getApplicationLabel(pm.getApplicationInfo(packageName, 0)).toString() }
        catch (e: Exception) { packageName }
    }

    var config by remember { mutableStateOf(viewModel.loadConfig(packageName)) }
    var isRunning by remember { mutableStateOf(false) }
    var memInfo by remember { mutableStateOf<String?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(packageName) {
        isRunning = viewModel.isAppRunning(packageName)
        memInfo = viewModel.getMemInfo(packageName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 48.dp, 16.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
            }
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(try { pm.getApplicationIcon(packageName) } catch (e: Exception) { null })
                    .crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.size(36.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(appLabel, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(packageName, color = TextMuted, fontSize = 9.sp, maxLines = 1)
            }
            IconButton(onClick = { showResetDialog = true }) {
                Icon(Icons.Default.Delete, null, tint = Red)
            }
        }

        // Status bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp, 12.dp, 16.dp, 0.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusChip(
                label = if (isRunning) "Rodando" else "Parado",
                color = if (isRunning) Green else TextMuted
            )
            memInfo?.let { StatusChip(label = "RAM: $it", color = Yellow) }
        }

        Spacer(Modifier.height(16.dp))

        // Scale card
        ConfigCard(title = "ESCALA DE RENDERIZAÇÃO") {
            // Big scale display
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.padding(bottom = 4.dp)
            ) {
                Text(
                    "${config.scale}",
                    color = Accent,
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 56.sp
                )
                Text("%", color = TextMuted, fontSize = 22.sp, modifier = Modifier.padding(bottom = 6.dp))
            }
            Text(
                "${ScaleValues.resolution(config.scale)}  ·  ${ScaleValues.label(config.scale)}",
                color = TextMuted, fontSize = 11.sp
            )

            Spacer(Modifier.height(12.dp))

            // Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("30% Mínimo", color = TextMuted, fontSize = 9.sp)
                Text("100% Nativo", color = TextMuted, fontSize = 9.sp)
            }

            Slider(
                value = config.scale.toFloat(),
                onValueChange = { config = config.copy(scale = (it / 10).toInt() * 10) },
                valueRange = 30f..100f,
                steps = 6,
                colors = SliderDefaults.colors(
                    thumbColor = Accent,
                    activeTrackColor = Accent,
                    inactiveTrackColor = Border
                )
            )

            // Preset buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                ScaleValues.options.forEach { v ->
                    val isActive = config.scale == v
                    val isNative = v == 100
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                when {
                                    isActive && isNative -> Green.copy(alpha = 0.1f)
                                    isActive -> Accent.copy(alpha = 0.1f)
                                    else -> BgDark
                                }
                            )
                            .clickable { config = config.copy(scale = v) }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$v%",
                            color = when {
                                isActive && isNative -> Green
                                isActive -> Accent
                                isNative -> Green.copy(alpha = 0.6f)
                                else -> TextMuted
                            },
                            fontSize = 9.sp,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Compat flags
        ConfigCard(title = "COMPATIBILIDADE") {
            ToggleRow(
                name = "Aspect Ratio",
                description = "Corrige proporção incorreta de tela na splash",
                checked = config.flagAspectRatio,
                onToggle = { config = config.copy(flagAspectRatio = it) }
            )
            ToggleRow(
                name = "Display APIs",
                description = "Acesso irrestrito às APIs de display",
                checked = config.flagSandbox,
                onToggle = { config = config.copy(flagSandbox = it) }
            )
        }

        Spacer(Modifier.height(10.dp))

        // Memory flags
        ConfigCard(title = "MEMÓRIA") {
            ToggleRow(
                name = "Non-Resize",
                description = "Evita recálculo de layout — economiza RAM",
                checked = config.flagNonResize,
                onToggle = { config = config.copy(flagNonResize = it) }
            )
            ToggleRow(
                name = "Nav Insets",
                description = "Reduz processamento da barra de navegação",
                checked = config.flagNavInsets,
                onToggle = { config = config.copy(flagNavInsets = it) },
                isLast = true
            )
        }

        Spacer(Modifier.height(16.dp))

        // Apply button
        Button(
            onClick = { viewModel.applyConfig(config) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            shape = RoundedCornerShape(12.dp),
            enabled = viewModel.uiState.collectAsState().value.isRootAvailable
        ) {
            Text(
                "APLICAR E SALVAR",
                color = BgDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(32.dp))
    }

    // Reset dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Resetar configuração?", color = TextPrimary) },
            text = { Text("Remove todas as flags aplicadas para $appLabel.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetConfig(packageName)
                    showResetDialog = false
                    onBack()
                }) { Text("Resetar", color = Red) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            },
            containerColor = Surface
        )
    }
}

@Composable
fun ConfigCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(title, color = TextMuted, fontSize = 9.sp, letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 6.dp))
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Border)
        ) {
            Column(modifier = Modifier.padding(14.dp), content = content)
        }
    }
}

@Composable
fun ToggleRow(
    name: String,
    description: String,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(description, color = TextMuted, fontSize = 10.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = BgDark,
                checkedTrackColor = Accent,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = Border
            )
        )
    }
    if (!isLast) HorizontalDivider(color = Border, thickness = 0.5.dp)
}

@Composable
fun StatusChip(label: String, color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(label, color = color, fontSize = 10.sp)
    }
}
