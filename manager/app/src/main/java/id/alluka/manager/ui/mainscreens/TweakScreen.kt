package id.alluka.manager.ui.mainscreens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.alluka.manager.R
import id.alluka.manager.data.AllukaEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TweakScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State Tweak Switches
    var liteMode by remember { mutableStateOf(false) }
    var clusterSched by remember { mutableStateOf(true) }
    var dndGaming by remember { mutableStateOf(true) }
    var thermalCore by remember { mutableStateOf(true) }
    var zramVm by remember { mutableStateOf(true) }
    var readAhead by remember { mutableStateOf(true) }
    var schedMigration by remember { mutableStateOf(true) }

    // Tiles State
    var refreshRate by remember { mutableStateOf("60 Hz") }
    var renderEngine by remember { mutableStateOf("VULKAN") }
    var isRefreshRateLoading by remember { mutableStateOf(false) }
    var isRenderEngineLoading by remember { mutableStateOf(false) }

    // Dialog & Sheet States
    var showModeSettingsDialog by remember { mutableStateOf(false) }
    var activeMode by remember { mutableStateOf("daily") }
    var govSleep by remember { mutableStateOf("powersave") }
    var govDaily by remember { mutableStateOf("schedutil") }
    var govPeforma by remember { mutableStateOf("performance") }
    var govAuto by remember { mutableStateOf("schedutil") }
    var ioSleep by remember { mutableStateOf("kyber") }
    var ioDaily by remember { mutableStateOf("bfq") }
    var ioPeforma by remember { mutableStateOf("none") }
    var ioAuto by remember { mutableStateOf("bfq") }

    var showBackupRestoreSheet by remember { mutableStateOf(false) }
    var showRefreshRateDialog by remember { mutableStateOf(false) }
    var showRendererDialog by remember { mutableStateOf(false) }
    var showAllukaLogDialog by remember { mutableStateOf(false) }
    var allukaLogContent by remember { mutableStateOf("Memuat log diagnostik Alluka...") }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 12.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.avatar),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                title = {
                    Text(
                        text = "Tweaks",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showBackupRestoreSheet = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Cloud,
                            contentDescription = "Backup & Restore"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 90.dp)) },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // AZenith Info Notice Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Pengaturan ini berlaku secara global untuk seluruh profil dan aplikasi.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Section 1: KINERJA (PERFORMANCE)
            item {
                TweakSectionHeader("KINERJA (PERFORMANCE)")
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    TweakActionRow(
                        icon = Icons.Rounded.Tune,
                        title = "Pengaturan Mode",
                        subtitle = "Ubah 4 mode (Sleep, Daily, Performance, Auto), CPU governor & I/O scheduler",
                        trailingText = when(activeMode) {
                            "sleep" -> "Sleep"
                            "daily" -> "Daily"
                            "peforma" -> "Performance"
                            "auto" -> "Auto"
                            else -> "Daily"
                        },
                        onClick = { showModeSettingsDialog = true },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
                    )
                    TweakSwitchRow(
                        icon = Icons.Rounded.Speed,
                        title = "Lite Mode",
                        subtitle = "Konfigurasi governor & thread lebih ringan untuk menjaga kestabilan suhu",
                        checked = liteMode,
                        onCheckedChange = {
                            liteMode = it
                            scope.launch {
                                AllukaEngine.setTweakProperty("lite_mode", if (it) "1" else "0")
                                snackbarHostState.showSnackbar(if (it) "Lite Mode diaktifkan" else "Lite Mode dinonaktifkan")
                            }
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    TweakActionRow(
                        icon = Icons.Filled.Speed,
                        title = "MediaTek GED Boost & FPSGO",
                        subtitle = "Akselerasi respons sentuhan GPU & frame tracking Helio G85",
                        trailingText = "Active",
                        onClick = {
                            scope.launch {
                                snackbarHostState.showSnackbar("MediaTek GED Boost & FPSGO Aktif")
                            }
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    TweakSwitchRow(
                        icon = Icons.Rounded.Bolt,
                        title = "Cluster-Aware Schedutil",
                        subtitle = "6 Efficiency (Up 1000us/Down 12000us) + 2 Performance (Up 500us/Down 24000us)",
                        checked = clusterSched,
                        onCheckedChange = { clusterSched = it },
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                }
            }

            // Section 2: PENGATURAN TAMBAHAN (ADDITIONAL SETTINGS)
            item {
                TweakSectionHeader("PENGATURAN TAMBAHAN (ADDITIONAL SETTINGS)")
            }
            item {
                TweakSwitchRow(
                    icon = Icons.Rounded.DoNotDisturbOn,
                    title = "DND Mode Gaming",
                    subtitle = "Blokir notifikasi dan panggilan pop-up saat game berjalan",
                    checked = dndGaming,
                    onCheckedChange = { dndGaming = it },
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // DUO EXPRESSIVE TILES (AZenith Signature)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ExpressiveTweakTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Smartphone,
                        label = "Refresh Rates",
                        value = refreshRate,
                        isLoading = isRefreshRateLoading,
                        onClick = { showRefreshRateDialog = true }
                    )
                    ExpressiveTweakTile(
                        modifier = Modifier.weight(1f),
                        icon = Icons.Rounded.Layers,
                        label = "Render Engine",
                        value = renderEngine,
                        isLoading = isRenderEngineLoading,
                        onClick = { showRendererDialog = true }
                    )
                }
            }

            // Section 3: DAYA & TERMAL (POWER & THERMAL)
            item {
                TweakSectionHeader("DAYA & TERMAL (POWER & THERMAL)")
            }
            item {
                TweakSwitchRow(
                    icon = Icons.Rounded.ThermostatAuto,
                    title = "ThermalCore Service",
                    subtitle = "Pengawasan termal cerdas untuk mencegah thermal throttling ekstrem",
                    checked = thermalCore,
                    onCheckedChange = { thermalCore = it },
                    shape = RoundedCornerShape(24.dp)
                )
            }

            // Section 4: TUNING KERNEL ALLUKA (ALLUKA ENGINE)
            item {
                TweakSectionHeader("TUNING KERNEL ALLUKA (ALLUKA ENGINE)")
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    TweakSwitchRow(
                        icon = Icons.Rounded.Memory,
                        title = "Conservative zRAM & VM",
                        subtitle = "Swappiness adaptif (80/100/120) & page-cluster 0 untuk RAM 4GB",
                        checked = zramVm,
                        onCheckedChange = { zramVm = it },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
                    )
                    TweakSwitchRow(
                        icon = Icons.Rounded.Storage,
                        title = "I/O Storage Read-Ahead",
                        subtitle = "Buffer 128KB pada antrean mmcblk & dm untuk kelancaran loading",
                        checked = readAhead,
                        onCheckedChange = { readAhead = it },
                        shape = RoundedCornerShape(6.dp)
                    )
                    TweakSwitchRow(
                        icon = Icons.Rounded.Hub,
                        title = "Sched Boost & Migration",
                        subtitle = "Migrasi task 500us & timer migration untuk efisiensi CPU multi-core",
                        checked = schedMigration,
                        onCheckedChange = { schedMigration = it },
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                }
            }

            // Section 5: DIAGNOSTIK & LOG (ALLUKA LOG)
            item {
                TweakSectionHeader("DIAGNOSTIK & LOG (ALLUKA LOG)")
            }
            item {
                TweakActionRow(
                    icon = Icons.Rounded.Terminal,
                    title = "Alluka Diagnostic Log",
                    subtitle = "Lihat log eksekusi node kernel (/data/adb/.config/alluka/alluka.log)",
                    trailingText = "Ready",
                    onClick = {
                        scope.launch {
                            allukaLogContent = AllukaEngine.getDiagnosticsLog()
                            showAllukaLogDialog = true
                        }
                    },
                    shape = RoundedCornerShape(24.dp)
                )
            }
        }
    }

    // Alluka Mode Settings Dialog (4 Mode, CPU Governor, I/O Scheduler)
    if (showModeSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showModeSettingsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Pengaturan Mode", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Text(
                            text = "PILIH MODE AKTIF (4 MODE)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(
                                Triple("sleep", "Sleep", "Santai & Hemat Baterai (Alluka)"),
                                Triple("daily", "Daily", "Seimbang & Responsif (Rekomendasi)"),
                                Triple("peforma", "Performance", "Nanika Awakened Gaming Ekstrem"),
                                Triple("auto", "Auto", "Dinamis: Daily (Biasa) • Peforma (Game) • Sleep (Layar Mati)")
                            ).forEach { (key, title, desc) ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            activeMode = key
                                            scope.launch {
                                                AllukaEngine.applyProfile(key)
                                                snackbarHostState.showSnackbar("Mode $title diaktifkan")
                                            }
                                        },
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (activeMode == key) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = if (activeMode == key) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    else null
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        RadioButton(selected = activeMode == key, onClick = null)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "CPU GOVERNOR PER PROFIL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple("Mode Sleep", govSleep) { it: String ->
                                    govSleep = it
                                    scope.launch {
                                        AllukaEngine.setModeGovernor("sleep", it)
                                        snackbarHostState.showSnackbar("Governor Sleep: $it (Tersimpan)")
                                    }
                                },
                                Triple("Mode Daily", govDaily) { it: String ->
                                    govDaily = it
                                    scope.launch {
                                        AllukaEngine.setModeGovernor("daily", it)
                                        snackbarHostState.showSnackbar("Governor Daily: $it (Tersimpan)")
                                    }
                                },
                                Triple("Mode Performance", govPeforma) { it: String ->
                                    govPeforma = it
                                    scope.launch {
                                        AllukaEngine.setModeGovernor("peforma", it)
                                        snackbarHostState.showSnackbar("Governor Performance: $it (Tersimpan)")
                                    }
                                }
                            ).forEach { (label, curVal, onPick) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = curVal,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text(
                            text = "I/O SCHEDULER STORAGE PER PROFIL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(
                                Triple("I/O Sleep", ioSleep) { it: String ->
                                    ioSleep = it
                                    scope.launch {
                                        AllukaEngine.setModeIo("sleep", it)
                                        snackbarHostState.showSnackbar("I/O Sleep: $it (Tersimpan)")
                                    }
                                },
                                Triple("I/O Daily", ioDaily) { it: String ->
                                    ioDaily = it
                                    scope.launch {
                                        AllukaEngine.setModeIo("daily", it)
                                        snackbarHostState.showSnackbar("I/O Daily: $it (Tersimpan)")
                                    }
                                },
                                Triple("I/O Performance", ioPeforma) { it: String ->
                                    ioPeforma = it
                                    scope.launch {
                                        AllukaEngine.setModeIo("peforma", it)
                                        snackbarHostState.showSnackbar("I/O Performance: $it (Tersimpan)")
                                    }
                                }
                            ).forEach { (label, curVal, onPick) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f))
                                    ) {
                                        Text(
                                            text = curVal,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showModeSettingsDialog = false }) {
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Refresh Rate Dialog
    if (showRefreshRateDialog) {
        AlertDialog(
            onDismissRequest = { showRefreshRateDialog = false },
            title = { Text("Pilih Refresh Rate") },
            text = {
                Column {
                    listOf("60 Hz", "90 Hz", "120 Hz").forEach { rate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    refreshRate = rate
                                    showRefreshRateDialog = false
                                    isRefreshRateLoading = true
                                    scope.launch {
                                        AllukaEngine.setRefreshRate(rate)
                                        kotlinx.coroutines.delay(300)
                                        isRefreshRateLoading = false
                                        snackbarHostState.showSnackbar("Refresh rate disetel ke $rate")
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = refreshRate == rate, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(rate, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Renderer Dialog
    if (showRendererDialog) {
        AlertDialog(
            onDismissRequest = { showRendererDialog = false },
            title = { Text("Pilih Render Engine") },
            text = {
                Column {
                    listOf("DEFAULT", "VULKAN", "OPENGL", "SKIAVK", "SKIAVKTHREADED").forEach { eng ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    renderEngine = eng
                                    showRendererDialog = false
                                    isRenderEngineLoading = true
                                    scope.launch {
                                        AllukaEngine.setRenderEngine(eng)
                                        kotlinx.coroutines.delay(350)
                                        isRenderEngineLoading = false
                                        snackbarHostState.showSnackbar("Render engine disetel ke $eng")
                                    }
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = renderEngine == eng, onClick = null)
                            Spacer(Modifier.width(12.dp))
                            Text(eng, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }


    // Alluka Diagnostic Log Dialog
    if (showAllukaLogDialog) {
        AlertDialog(
            onDismissRequest = { showAllukaLogDialog = false },
            title = { Text("Alluka Diagnostic Log") },
            text = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF09060C)
                ) {
                    Text(
                        text = allukaLogContent,
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxSize(),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFCBA8B3),
                        lineHeight = 15.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showAllukaLogDialog = false }) {
                    Text("Tutup")
                }
            }
        )
    }

    // Backup & Restore Sheet
    if (showBackupRestoreSheet) {
        AlertDialog(
            onDismissRequest = { showBackupRestoreSheet = false },
            title = { Text("Backup & Restore") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showBackupRestoreSheet = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Konfigurasi Alluka berhasil dicadangkan")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Backup Configuration (.alk)")
                    }
                    OutlinedButton(
                        onClick = {
                            showBackupRestoreSheet = false
                            scope.launch {
                                snackbarHostState.showSnackbar("Konfigurasi berhasil dipulihkan")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restore Configuration")
                    }
                }
            },
            confirmButton = {}
        )
    }
}

@Composable
fun TweakSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 6.dp, top = 10.dp, bottom = 4.dp),
        fontSize = 11.sp,
        letterSpacing = 0.8.sp
    )
}

@Composable
fun TweakSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    shape: androidx.compose.ui.graphics.Shape
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                Text(text = subtitle, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 13.sp)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun TweakActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailingText: String? = null,
    onClick: () -> Unit,
    shape: androidx.compose.ui.graphics.Shape
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                Text(text = subtitle, fontSize = 10.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 13.sp)
            }
            if (trailingText != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.07f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Text(
                        text = trailingText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun ExpressiveTweakTile(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(26.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(26.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(13.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(82.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(9.dp)
                        .size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                Text(
                    text = label,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(13.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}
