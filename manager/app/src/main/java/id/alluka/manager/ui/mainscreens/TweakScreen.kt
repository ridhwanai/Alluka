package id.alluka.manager.ui.mainscreens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.AltRoute
import androidx.compose.material.icons.automirrored.rounded.Article
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
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val prefs = remember { context.getSharedPreferences("alluka_tweaks", Context.MODE_PRIVATE) }

    // Persistent State Tweak Switches
    var liteMode by remember { mutableStateOf(prefs.getBoolean("lite_mode", false)) }
    var clusterSched by remember { mutableStateOf(prefs.getBoolean("cluster_sched", true)) }
    var dndGaming by remember { mutableStateOf(prefs.getBoolean("dnd_gaming", true)) }
    var thermalCore by remember { mutableStateOf(prefs.getBoolean("thermal_core", true)) }
    var zramVm by remember { mutableStateOf(prefs.getBoolean("zram_vm", true)) }
    var readAhead by remember { mutableStateOf(prefs.getBoolean("read_ahead", true)) }
    var schedMigration by remember { mutableStateOf(prefs.getBoolean("sched_migration", true)) }

    // Tiles State
    var refreshRate by remember { mutableStateOf(prefs.getString("refresh_rate", "60 Hz") ?: "60 Hz") }
    var renderEngine by remember { mutableStateOf(prefs.getString("render_engine", "VULKAN") ?: "VULKAN") }
    var isRefreshRateLoading by remember { mutableStateOf(false) }
    var isRenderEngineLoading by remember { mutableStateOf(false) }

    // Mode Settings States
    var isModeSettingsOpen by remember { mutableStateOf(false) }
    var activeMode by remember { mutableStateOf(prefs.getString("active_mode", "daily") ?: "daily") }
    var govSleep by remember { mutableStateOf(prefs.getString("gov_sleep", "powersave") ?: "powersave") }
    var govDaily by remember { mutableStateOf(prefs.getString("gov_daily", "schedutil") ?: "schedutil") }
    var govPeforma by remember { mutableStateOf(prefs.getString("gov_peforma", "performance") ?: "performance") }
    var ioSleep by remember { mutableStateOf(prefs.getString("io_sleep", "kyber") ?: "kyber") }
    var ioDaily by remember { mutableStateOf(prefs.getString("io_daily", "bfq") ?: "bfq") }
    var ioPeforma by remember { mutableStateOf(prefs.getString("io_peforma", "none") ?: "none") }

    // Dialog & Sheet States
    var showBackupRestoreSheet by remember { mutableStateOf(false) }
    var showRefreshRateDialog by remember { mutableStateOf(false) }
    var showRendererDialog by remember { mutableStateOf(false) }
    var showAllukaLogDialog by remember { mutableStateOf(false) }
    var allukaLogContent by remember { mutableStateOf("Memuat log diagnostik Alluka...") }

    // Synchronize initial values from Engine in background
    LaunchedEffect(Unit) {
        val engMode = AllukaEngine.getActiveProfile()
        if (engMode.isNotBlank()) activeMode = engMode
        govSleep = AllukaEngine.getModeGovernor("sleep", govSleep)
        govDaily = AllukaEngine.getModeGovernor("daily", govDaily)
        govPeforma = AllukaEngine.getModeGovernor("peforma", govPeforma)
        ioSleep = AllukaEngine.getModeIo("sleep", ioSleep)
        ioDaily = AllukaEngine.getModeIo("daily", ioDaily)
        ioPeforma = AllukaEngine.getModeIo("peforma", ioPeforma)
    }

    AnimatedContent(
        targetState = isModeSettingsOpen,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "ModeSettingsTransition"
    ) { openModeSettings ->
        if (openModeSettings) {
            ModeSettingsScreen(
                activeMode = activeMode,
                onModeSelected = { newMode ->
                    activeMode = newMode
                    prefs.edit().putString("active_mode", newMode).apply()
                    scope.launch {
                        AllukaEngine.applyProfile(newMode)
                        val name = when (newMode) {
                            "sleep" -> "Sleep"
                            "daily" -> "Daily Balance"
                            "peforma" -> "Performance"
                            "auto" -> "Auto (Smart Gaming)"
                            else -> newMode
                        }
                        snackbarHostState.showSnackbar("Mode $name berhasil diaktifkan")
                    }
                },
                govSleep = govSleep,
                onGovSleepChange = {
                    govSleep = it
                    prefs.edit().putString("gov_sleep", it).apply()
                    scope.launch {
                        AllukaEngine.setModeGovernor("sleep", it)
                        snackbarHostState.showSnackbar("Governor Sleep disetel ke $it")
                    }
                },
                govDaily = govDaily,
                onGovDailyChange = {
                    govDaily = it
                    prefs.edit().putString("gov_daily", it).apply()
                    scope.launch {
                        AllukaEngine.setModeGovernor("daily", it)
                        snackbarHostState.showSnackbar("Governor Daily disetel ke $it")
                    }
                },
                govPeforma = govPeforma,
                onGovPeformaChange = {
                    govPeforma = it
                    prefs.edit().putString("gov_peforma", it).apply()
                    scope.launch {
                        AllukaEngine.setModeGovernor("peforma", it)
                        snackbarHostState.showSnackbar("Governor Performance disetel ke $it")
                    }
                },
                ioSleep = ioSleep,
                onIoSleepChange = {
                    ioSleep = it
                    prefs.edit().putString("io_sleep", it).apply()
                    scope.launch {
                        AllukaEngine.setModeIo("sleep", it)
                        snackbarHostState.showSnackbar("I/O Sleep disetel ke $it")
                    }
                },
                ioDaily = ioDaily,
                onIoDailyChange = {
                    ioDaily = it
                    prefs.edit().putString("io_daily", it).apply()
                    scope.launch {
                        AllukaEngine.setModeIo("daily", it)
                        snackbarHostState.showSnackbar("I/O Daily disetel ke $it")
                    }
                },
                ioPeforma = ioPeforma,
                onIoPeformaChange = {
                    ioPeforma = it
                    prefs.edit().putString("io_peforma", it).apply()
                    scope.launch {
                        AllukaEngine.setModeIo("peforma", it)
                        snackbarHostState.showSnackbar("I/O Performance disetel ke $it")
                    }
                },
                onBack = { isModeSettingsOpen = false },
                snackbarHostState = snackbarHostState
            )
        } else {
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
                                trailingText = when (activeMode) {
                                    "sleep" -> "Sleep"
                                    "daily" -> "Daily"
                                    "peforma" -> "Performance"
                                    "auto" -> "Auto"
                                    else -> "Daily"
                                },
                                onClick = { isModeSettingsOpen = true },
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
                            )
                            TweakSwitchRow(
                                icon = Icons.Rounded.Speed,
                                title = "Lite Mode",
                                subtitle = "Konfigurasi governor & thread lebih ringan untuk menjaga kestabilan suhu",
                                checked = liteMode,
                                onCheckedChange = {
                                    liteMode = it
                                    prefs.edit().putBoolean("lite_mode", it).apply()
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
                                onCheckedChange = {
                                    clusterSched = it
                                    prefs.edit().putBoolean("cluster_sched", it).apply()
                                    scope.launch {
                                        AllukaEngine.setTweakProperty("cluster_sched", if (it) "1" else "0")
                                    }
                                },
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
                            onCheckedChange = {
                                dndGaming = it
                                prefs.edit().putBoolean("dnd_gaming", it).apply()
                                scope.launch {
                                    AllukaEngine.setTweakProperty("dnd_gaming", if (it) "1" else "0")
                                }
                            },
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
                            onCheckedChange = {
                                thermalCore = it
                                prefs.edit().putBoolean("thermal_core", it).apply()
                                scope.launch {
                                    AllukaEngine.setTweakProperty("thermal_core", if (it) "1" else "0")
                                }
                            },
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
                                onCheckedChange = {
                                    zramVm = it
                                    prefs.edit().putBoolean("zram_vm", it).apply()
                                    scope.launch {
                                        AllukaEngine.setTweakProperty("zram_vm", if (it) "1" else "0")
                                    }
                                },
                                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
                            )
                            TweakSwitchRow(
                                icon = Icons.Rounded.Storage,
                                title = "Storage Read-Ahead (1024KB)",
                                subtitle = "Mempercepat loading game & tekstur aset dari penyimpanan",
                                checked = readAhead,
                                onCheckedChange = {
                                    readAhead = it
                                    prefs.edit().putBoolean("read_ahead", it).apply()
                                    scope.launch {
                                        AllukaEngine.setTweakProperty("read_ahead", if (it) "1" else "0")
                                    }
                                },
                                shape = RoundedCornerShape(6.dp)
                            )
                            TweakSwitchRow(
                                icon = Icons.AutoMirrored.Rounded.AltRoute,
                                title = "Sched Migration Cost Tuning",
                                subtitle = "500000ns untuk meminimalkan stutter task pinning antar core",
                                checked = schedMigration,
                                onCheckedChange = {
                                    schedMigration = it
                                    prefs.edit().putBoolean("sched_migration", it).apply()
                                    scope.launch {
                                        AllukaEngine.setTweakProperty("sched_migration", if (it) "1" else "0")
                                    }
                                },
                                shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                            )
                        }
                    }

                    // Section 5: DIAGNOSTIK & LOG
                    item {
                        TweakSectionHeader("DIAGNOSTIK & LOG")
                    }
                    item {
                        TweakActionRow(
                            icon = Icons.AutoMirrored.Rounded.Article,
                            title = "Lihat Log Diagnostik Alluka",
                            subtitle = "Periksa log eksekusi, governor transition, dan daemon status",
                            trailingText = "Log",
                            onClick = {
                                showAllukaLogDialog = true
                                scope.launch {
                                    allukaLogContent = AllukaEngine.getDiagnosticsLog()
                                }
                            },
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }
            }
        }
    }

    // Refresh Rate Dialog
    if (showRefreshRateDialog) {
        AlertDialog(
            onDismissRequest = { showRefreshRateDialog = false },
            title = { Text("Pilih Refresh Rate", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("60 Hz", "90 Hz", "120 Hz").forEach { rate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    refreshRate = rate
                                    prefs.edit().putString("refresh_rate", rate).apply()
                                    showRefreshRateDialog = false
                                    isRefreshRateLoading = true
                                    scope.launch {
                                        AllukaEngine.setRefreshRate(rate)
                                        kotlinx.coroutines.delay(250)
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
            title = { Text("Pilih Render Engine", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("DEFAULT", "VULKAN", "OPENGL", "SKIAVK", "SKIAVKTHREADED").forEach { eng ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    renderEngine = eng
                                    prefs.edit().putString("render_engine", eng).apply()
                                    showRendererDialog = false
                                    isRenderEngineLoading = true
                                    scope.launch {
                                        AllukaEngine.setRenderEngine(eng)
                                        kotlinx.coroutines.delay(250)
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
            title = { Text("Alluka Diagnostic Log", fontWeight = FontWeight.Bold) },
            text = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
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
                    Text("Tutup", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Backup & Restore Sheet
    if (showBackupRestoreSheet) {
        AlertDialog(
            onDismissRequest = { showBackupRestoreSheet = false },
            title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
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

/**
 * Dedicated Sub-Screen for Pengaturan Mode with Back button,
 * 4 Switchable Modes (Sleep, Daily, Performance, Auto),
 * and Governor / I/O Scheduler bottom sheet pickers for Sleep, Daily, and Performance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModeSettingsScreen(
    activeMode: String,
    onModeSelected: (String) -> Unit,
    govSleep: String,
    onGovSleepChange: (String) -> Unit,
    govDaily: String,
    onGovDailyChange: (String) -> Unit,
    govPeforma: String,
    onGovPeformaChange: (String) -> Unit,
    ioSleep: String,
    onIoSleepChange: (String) -> Unit,
    ioDaily: String,
    onIoDailyChange: (String) -> Unit,
    ioPeforma: String,
    onIoPeformaChange: (String) -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var pickerTitle by remember { mutableStateOf("") }
    var pickerOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentPickedValue by remember { mutableStateOf("") }
    var onPickedCallback by remember { mutableStateOf<(String) -> Unit>({}) }
    var showPickerSheet by remember { mutableStateOf(false) }

    val availableGovernors = remember {
        listOf("powersave", "schedutil", "conservative", "ondemand", "interactive", "performance")
    }

    val availableIoSchedulers = remember {
        listOf("none", "mq-deadline", "kyber", "bfq")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                title = {
                    Text(
                        text = "Pengaturan Mode",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 90.dp)) },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Notice Card
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
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            text = "Pilih mode aktif profil Alluka. Atur CPU Governor dan I/O Scheduler per mode. Semua pengaturan tersimpan secara otomatis.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Section 1: PILIH MODE AKTIF (4 MODE)
            item {
                TweakSectionHeader("PILIH PROFIL MODE AKTIF")
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val modes = listOf(
                        ModeItem("sleep", "Sleep Mode", "Hemat daya optimal saat idle / layar mati", Icons.Rounded.Bedtime, Color(0xFF38BDF8)),
                        ModeItem("daily", "Daily Balance", "Keseimbangan efisiensi dan performa responsif", Icons.Rounded.Balance, Color(0xFF10B981)),
                        ModeItem("peforma", "Performance", "Performa tinggi CPU & GPU untuk gaming intensif", Icons.Rounded.RocketLaunch, Color(0xFFF43F5E)),
                        ModeItem("auto", "Auto (Smart Gaming)", "Otomatis: Daily biasa, Performance saat game, Sleep saat layar mati", Icons.Rounded.AutoAwesome, Color(0xFFF59E0B))
                    )

                    modes.forEach { m ->
                        val isSelected = activeMode == m.id
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onModeSelected(m.id) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f) else MaterialTheme.colorScheme.surfaceContainer,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.05f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(m.color.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = m.icon, contentDescription = null, tint = m.color, modifier = Modifier.size(22.dp))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = m.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = m.desc,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 14.sp
                                    )
                                }
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(imageVector = Icons.Rounded.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: PENGATURAN CPU GOVERNOR (SLEEP, DAILY, PERFORMANCE - AUTO EXCLUDED)
            item {
                TweakSectionHeader("CPU GOVERNOR (PROFIL MANUAL)")
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ModeConfigRow(
                        icon = Icons.Rounded.Bedtime,
                        title = "Sleep Mode Governor",
                        subtitle = "Governor hemat daya saat perangkat idle",
                        currentValue = govSleep,
                        onClick = {
                            pickerTitle = "Pilih CPU Governor (Sleep)"
                            pickerOptions = availableGovernors
                            currentPickedValue = govSleep
                            onPickedCallback = onGovSleepChange
                            showPickerSheet = true
                        },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
                    )
                    ModeConfigRow(
                        icon = Icons.Rounded.Balance,
                        title = "Daily Balance Governor",
                        subtitle = "Governor untuk penggunaan harian seimbang",
                        currentValue = govDaily,
                        onClick = {
                            pickerTitle = "Pilih CPU Governor (Daily)"
                            pickerOptions = availableGovernors
                            currentPickedValue = govDaily
                            onPickedCallback = onGovDailyChange
                            showPickerSheet = true
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    ModeConfigRow(
                        icon = Icons.Rounded.RocketLaunch,
                        title = "Performance Governor",
                        subtitle = "Governor untuk performa maksimal dan respons instan",
                        currentValue = govPeforma,
                        onClick = {
                            pickerTitle = "Pilih CPU Governor (Performance)"
                            pickerOptions = availableGovernors
                            currentPickedValue = govPeforma
                            onPickedCallback = onGovPeformaChange
                            showPickerSheet = true
                        },
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                }
            }

            // Section 3: PENGATURAN I/O SCHEDULER (SLEEP, DAILY, PERFORMANCE - AUTO EXCLUDED)
            item {
                TweakSectionHeader("I/O SCHEDULER STORAGE (PROFIL MANUAL)")
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    ModeConfigRow(
                        icon = Icons.Rounded.Storage,
                        title = "Sleep Mode I/O",
                        subtitle = "Penjadwalan antrean storage saat sleep",
                        currentValue = ioSleep,
                        onClick = {
                            pickerTitle = "Pilih I/O Scheduler (Sleep)"
                            pickerOptions = availableIoSchedulers
                            currentPickedValue = ioSleep
                            onPickedCallback = onIoSleepChange
                            showPickerSheet = true
                        },
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
                    )
                    ModeConfigRow(
                        icon = Icons.Rounded.SdCard,
                        title = "Daily Balance I/O",
                        subtitle = "Penjadwalan antrean storage harian",
                        currentValue = ioDaily,
                        onClick = {
                            pickerTitle = "Pilih I/O Scheduler (Daily)"
                            pickerOptions = availableIoSchedulers
                            currentPickedValue = ioDaily
                            onPickedCallback = onIoDailyChange
                            showPickerSheet = true
                        },
                        shape = RoundedCornerShape(6.dp)
                    )
                    ModeConfigRow(
                        icon = Icons.Rounded.Speed,
                        title = "Performance I/O",
                        subtitle = "Penjadwalan antrean storage bebas latensi",
                        currentValue = ioPeforma,
                        onClick = {
                            pickerTitle = "Pilih I/O Scheduler (Performance)"
                            pickerOptions = availableIoSchedulers
                            currentPickedValue = ioPeforma
                            onPickedCallback = onIoPeformaChange
                            showPickerSheet = true
                        },
                        shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 24.dp, bottomEnd = 24.dp)
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet Picker
    if (showPickerSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPickerSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = pickerTitle,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                pickerOptions.forEach { opt ->
                    val isPicked = opt == currentPickedValue
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onPickedCallback(opt)
                                showPickerSheet = false
                            },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isPicked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = opt,
                                fontWeight = if (isPicked) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp,
                                color = if (isPicked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isPicked) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class ModeItem(
    val id: String,
    val title: String,
    val desc: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
fun ModeConfigRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    currentValue: String,
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
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
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
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
            ) {
                Text(
                    text = currentValue,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
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
            trailingText?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
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
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .height(98.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.04f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                }
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Column {
                Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
