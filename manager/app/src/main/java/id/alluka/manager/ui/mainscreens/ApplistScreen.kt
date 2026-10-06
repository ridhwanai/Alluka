package id.alluka.manager.ui.mainscreens

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Launch
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.alluka.manager.data.AllukaEngine
import id.alluka.manager.ui.component.AppIconImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

class AllukaAppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val appInfo: ApplicationInfo?,
    val isGame: Boolean,
    val isSystem: Boolean,
    isEnabled: Boolean,
    val isRecommended: Boolean,
    val version: String = "v1.0.0",
    perfLiteMode: String = "default",
    dndOnGaming: String = "default",
    renderEngine: String = "default"
) {
    var isEnabled by mutableStateOf(isEnabled)
    var perfLiteMode by mutableStateOf(perfLiteMode)
    var dndOnGaming by mutableStateOf(dndOnGaming)
    var renderEngine by mutableStateOf(renderEngine)
}

data class OptionSheetItem(
    val value: String,
    val label: String,
    val description: String
)

data class ActiveSheetConfig(
    val title: String,
    val subtitle: String,
    val options: List<OptionSheetItem>,
    val currentValue: String,
    val onSelect: (String) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplistScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val appPrefs = remember { context.getSharedPreferences("alluka_app_configs", Context.MODE_PRIVATE) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSystemApps by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var appToConfig by remember { mutableStateOf<AllukaAppItem?>(null) }

    // Real dynamic apps list from device
    var appsList by remember { mutableStateOf<List<AllukaAppItem>>(emptyList()) }

    suspend fun loadDeviceApps(): List<AllukaAppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val enabledSet = AllukaEngine.getEnabledApps()
        val installedPackages = try {
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        } catch (_: Exception) {
            emptyList()
        }

        val result = ArrayList<AllukaAppItem>(installedPackages.size)
        for (pkg in installedPackages) {
            val appInfo = pkg.applicationInfo ?: continue
            val label = try {
                appInfo.loadLabel(pm).toString()
            } catch (_: Exception) {
                pkg.packageName
            }
            val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            @Suppress("DEPRECATION")
            val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                appInfo.category == ApplicationInfo.CATEGORY_GAME
            } else {
                (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
            }

            val savedPerf = appPrefs.getString("${pkg.packageName}_perf", "default") ?: "default"
            val savedDnd = appPrefs.getString("${pkg.packageName}_dnd", "default") ?: "default"
            val savedRender = appPrefs.getString("${pkg.packageName}_render", "default") ?: "default"

            result.add(
                AllukaAppItem(
                    id = pkg.packageName,
                    name = label,
                    packageName = pkg.packageName,
                    appInfo = appInfo,
                    isGame = isGame,
                    isSystem = isSystem,
                    isEnabled = enabledSet.contains(pkg.packageName),
                    isRecommended = isGame,
                    version = pkg.versionName ?: "v1.0.0",
                    perfLiteMode = savedPerf,
                    dndOnGaming = savedDnd,
                    renderEngine = savedRender
                )
            )
        }

        val collator = Collator.getInstance(Locale.getDefault())
        result.sortWith(
            compareByDescending<AllukaAppItem> { it.isEnabled }
                .thenByDescending { it.isGame }
                .then(compareBy(collator) { it.name })
        )
        result
    }

    fun refreshApps() {
        coroutineScope.launch {
            isRefreshing = true
            val start = System.currentTimeMillis()
            val loaded = loadDeviceApps()
            appsList = loaded
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < 500) delay(500 - elapsed)
            isRefreshing = false
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        refreshApps()
    }

    val filteredApps = remember(appsList, searchQuery, showSystemApps) {
        val query = searchQuery.trim().lowercase(Locale.getDefault())
        appsList.filter { app ->
            val matchQuery = query.isEmpty() ||
                    app.name.lowercase(Locale.getDefault()).contains(query) ||
                    app.packageName.lowercase(Locale.getDefault()).contains(query)
            val matchSystem = showSystemApps || !app.isSystem
            matchQuery && matchSystem
        }
    }

    val topShape = RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp, bottomStart = 6.dp, bottomEnd = 6.dp)
    val middleShape = RoundedCornerShape(6.dp)
    val bottomShape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp, bottomStart = 26.dp, bottomEnd = 26.dp)
    val singleShape = RoundedCornerShape(26.dp)

    AnimatedContent(
        targetState = appToConfig,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            } else {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> width } + fadeOut()
            }
        },
        label = "AppScreenTransition"
    ) { targetApp ->
        if (targetApp != null) {
            AllukaAppSettingsScreen(
                app = targetApp,
                onBack = { appToConfig = null },
                onSaveSetting = { key, value ->
                    appPrefs.edit().putString("${targetApp.packageName}_$key", value).apply()
                    if (key == "enabled") {
                        val enabled = value == "true"
                        targetApp.isEnabled = enabled
                        coroutineScope.launch {
                            AllukaEngine.setAppEnabled(targetApp.packageName, enabled)
                        }
                    }
                }
            )
        } else {
            Scaffold(
                topBar = {
                    if (isSearchActive) {
                        TopAppBar(
                            title = {
                                TextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search installed apps...") },
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            navigationIcon = {
                                IconButton(onClick = { isSearchActive = false; searchQuery = "" }) {
                                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Rounded.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    } else {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "App List",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )
                            },
                            actions = {
                                IconButton(onClick = { isSearchActive = true }) {
                                    Icon(Icons.Rounded.Search, contentDescription = "Search")
                                }
                                IconButton(onClick = { menuExpanded = true }) {
                                    Icon(Icons.Rounded.MoreVert, contentDescription = "Menu")
                                }
                                DropdownMenu(
                                    expanded = menuExpanded,
                                    onDismissRequest = { menuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Refresh") },
                                        leadingIcon = { Icon(Icons.Rounded.Refresh, contentDescription = null) },
                                        onClick = {
                                            menuExpanded = false
                                            refreshApps()
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text(if (showSystemApps) "Hide System Apps" else "Show System Apps") },
                                        leadingIcon = {
                                            Icon(
                                                imageVector = if (showSystemApps) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                                contentDescription = null
                                            )
                                        },
                                        onClick = {
                                            showSystemApps = !showSystemApps
                                            menuExpanded = false
                                        }
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                        )
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState, modifier = Modifier.padding(bottom = 90.dp)) },
                containerColor = Color.Transparent,
                modifier = modifier
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isLoading || isRefreshing) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 110.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            items(8) { index ->
                                val shape = when (index) {
                                    0 -> topShape
                                    7 -> bottomShape
                                    else -> middleShape
                                }
                                SkeletonAppItem(shape = shape)
                            }
                        }
                    } else if (filteredApps.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) "No apps match '$searchQuery'" else "No apps installed",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 110.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            itemsIndexed(filteredApps, key = { _, item -> item.packageName }) { index, app ->
                                val shape = when {
                                    filteredApps.size == 1 -> singleShape
                                    index == 0 -> topShape
                                    index == filteredApps.lastIndex -> bottomShape
                                    else -> middleShape
                                }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(shape)
                                        .clickable { appToConfig = app },
                                    color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                                    shape = shape
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        // App Icon (52.dp squircle with real application icon)
                                        Box(
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            AppIconImage(
                                                packageName = app.packageName,
                                                appInfo = app.appInfo,
                                                appName = app.name,
                                                size = 52.dp
                                            )
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = app.name,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = app.packageName,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(
                                                modifier = Modifier.padding(top = 4.dp),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (app.isGame) {
                                                    AzLabelText("GAME", Color(0xFFF59E0B))
                                                }
                                                if (app.isEnabled) {
                                                    AzLabelText("ACTIVE", Color(0xFF10B981))
                                                }
                                                if (app.isSystem) {
                                                    AzLabelText("SYSTEM", Color(0xFF3B82F6))
                                                }
                                            }
                                        }

                                        Switch(
                                            checked = app.isEnabled,
                                            onCheckedChange = { checked ->
                                                app.isEnabled = checked
                                                coroutineScope.launch {
                                                    AllukaEngine.setAppEnabled(app.packageName, checked)
                                                    snackbarHostState.showSnackbar(
                                                        if (checked) "${app.name} ditambahkan ke optimasi Alluka" else "${app.name} dinonaktifkan dari optimasi"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SkeletonAppItem(shape: RoundedCornerShape) {
    val transition = rememberInfiniteTransition(label = "SkeletonTransition")
    val alpha by transition.animateFloat(
        initialValue = 0.28f,
        targetValue = 0.58f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SkeletonAlpha"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = alpha * 0.15f))
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = alpha * 0.15f))
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.38f)
                        .height(11.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = alpha * 0.15f))
                )
                Box(
                    modifier = Modifier
                        .width(52.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = alpha * 0.15f))
                )
            }
        }
    }
}

@Composable
fun AzLabelText(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllukaAppSettingsScreen(
    app: AllukaAppItem,
    onBack: () -> Unit,
    onSaveSetting: (String, String) -> Unit = { _, _ -> }
) {
    var masterOn by remember { mutableStateOf(app.isEnabled) }
    var activeSheet by remember { mutableStateOf<ActiveSheetConfig?>(null) }
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "App Settings", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        try {
                            val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                            if (launchIntent != null) {
                                context.startActivity(launchIntent)
                            }
                        } catch (_: Exception) {}
                    }) {
                        Icon(Icons.AutoMirrored.Rounded.Launch, contentDescription = "Launch")
                    }
                    IconButton(onClick = {
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                            }
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }) {
                        Icon(Icons.Rounded.Info, contentDescription = "Info")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Info Card
            item {
                Surface(
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(26.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Rounded.Info, contentDescription = null, tint = colorScheme.primary)
                        Text(
                            text = "Pengaturan khusus aplikasi ini akan menimpa pengaturan global Alluka. Biarkan Default untuk mengikuti profil global.",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // App Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        AppIconImage(
                            packageName = app.packageName,
                            appInfo = app.appInfo,
                            appName = app.name,
                            size = 88.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = app.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(text = app.packageName, style = MaterialTheme.typography.bodyMedium, color = colorScheme.primary)
                    Surface(
                        shape = CircleShape,
                        color = colorScheme.secondaryContainer,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text(
                            text = app.version,
                            style = MaterialTheme.typography.labelMedium,
                            color = colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Master Switch
            item {
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = colorScheme.surfaceColorAtElevation(1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Optimasi Khusus Alluka",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Terapkan profil otomatis saat aplikasi dibuka",
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.outline
                            )
                        }
                        Switch(
                            checked = masterOn,
                            onCheckedChange = {
                                masterOn = it
                                app.isEnabled = it
                                onSaveSetting("enabled", it.toString())
                            }
                        )
                    }
                }
            }

            // Detailed Options
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(26.dp)),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Option 1: Perf / Lite Mode
                    SettingOptionCard(
                        icon = Icons.Rounded.Speed,
                        title = "Performa / Lite Mode",
                        desc = "Governor tuning saat aplikasi berada di foreground",
                        value = when (app.perfLiteMode) {
                            "schedutil_lean" -> "Schedutil Lean"
                            "power_throttled" -> "Power Throttled"
                            "extreme" -> "Extreme Perf"
                            else -> "Default"
                        },
                        onClick = {
                            activeSheet = ActiveSheetConfig(
                                title = "Performa / Lite Mode",
                                subtitle = "Pilih profil daya khusus aplikasi ini",
                                options = listOf(
                                    OptionSheetItem("default", "Default", "Mengikuti profil global Alluka Engine"),
                                    OptionSheetItem("schedutil_lean", "Schedutil Lean", "Frekuensi dinamis hemat daya moderat"),
                                    OptionSheetItem("power_throttled", "Power Throttled", "Membatasi clock untuk baterai maksimal"),
                                    OptionSheetItem("extreme", "Extreme Perf", "Performa maksimal tanpa kompromi")
                                ),
                                currentValue = app.perfLiteMode,
                                onSelect = {
                                    app.perfLiteMode = it
                                    onSaveSetting("perf", it)
                                }
                            )
                        }
                    )

                    // Option 2: DND on Gaming
                    SettingOptionCard(
                        icon = Icons.Rounded.DoNotDisturbOn,
                        title = "DND Mode Gaming",
                        desc = "Blokir gangguan notifikasi saat aplikasi dibuka",
                        value = when (app.dndOnGaming) {
                            "block_heads_up" -> "Block Heads-up"
                            "full_silence" -> "Full Silence"
                            else -> "Default"
                        },
                        onClick = {
                            activeSheet = ActiveSheetConfig(
                                title = "DND Mode Gaming",
                                subtitle = "Konfigurasi gangguan notifikasi",
                                options = listOf(
                                    OptionSheetItem("default", "Default", "Mengikuti konfigurasi DND global"),
                                    OptionSheetItem("block_heads_up", "Block Heads-up", "Sembunyikan pop-up mengambang saja"),
                                    OptionSheetItem("full_silence", "Full Silence", "Heningkan seluruh nada dering dan getaran")
                                ),
                                currentValue = app.dndOnGaming,
                                onSelect = {
                                    app.dndOnGaming = it
                                    onSaveSetting("dnd", it)
                                }
                            )
                        }
                    )

                    // Option 3: Render Engine
                    SettingOptionCard(
                        icon = Icons.Rounded.Layers,
                        title = "Current Render Engine",
                        desc = "Driver backend grafis HWUI",
                        value = when (app.renderEngine) {
                            "skiavk" -> "SkiaVK"
                            "skiavkthreaded" -> "SkiaVK (Threaded)"
                            "skiagl" -> "SkiaGL"
                            "opengl" -> "OpenGL ES"
                            "vulkan" -> "Vulkan"
                            else -> "Default"
                        },
                        onClick = {
                            activeSheet = ActiveSheetConfig(
                                title = "Current Render Engine",
                                subtitle = "Pilih backend rendering grafis khusus aplikasi ini",
                                options = listOf(
                                    OptionSheetItem("default", "Default", "Pipeline grafis bawaan sistem Android"),
                                    OptionSheetItem("skiavk", "SkiaVK", "Backend render Skia Vulkan berperforma tinggi"),
                                    OptionSheetItem("skiavkthreaded", "SkiaVK (Threaded)", "Pipeline Skia Vulkan multithreaded"),
                                    OptionSheetItem("skiagl", "SkiaGL", "Backend render Skia OpenGL ES"),
                                    OptionSheetItem("opengl", "OpenGL ES", "Pipeline grafis OpenGL ES standar"),
                                    OptionSheetItem("vulkan", "Vulkan", "Driver Vulkan langsung tingkat rendah")
                                ),
                                currentValue = app.renderEngine,
                                onSelect = {
                                    app.renderEngine = it
                                    onSaveSetting("render", it)
                                }
                            )
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Modal Bottom Sheet Option Picker (AZenith Style)
    if (activeSheet != null) {
        val sheet = activeSheet!!
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = colorScheme.surfaceColorAtElevation(3.dp),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = sheet.title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface
                )
                if (sheet.subtitle.isNotBlank()) {
                    Text(
                        text = sheet.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                sheet.options.forEach { opt ->
                    val isSelected = opt.value == sheet.currentValue
                    Surface(
                        onClick = {
                            sheet.onSelect(opt.value)
                            activeSheet = null
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) colorScheme.primaryContainer.copy(alpha = 0.35f)
                        else Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = {
                                    sheet.onSelect(opt.value)
                                    activeSheet = null
                                },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = colorScheme.primary
                                )
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = opt.label,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) colorScheme.primary else colorScheme.onSurface
                                )
                                Text(
                                    text = opt.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingOptionCard(
    icon: ImageVector,
    title: String,
    desc: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingOptionRow(icon = icon, title = title, desc = desc, value = value, onClick = onClick)
    }
}

@Composable
fun SettingOptionRow(
    icon: ImageVector,
    title: String,
    desc: String,
    value: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.08f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = value,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
