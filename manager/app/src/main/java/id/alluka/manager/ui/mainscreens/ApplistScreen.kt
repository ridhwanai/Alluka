package id.alluka.manager.ui.mainscreens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.alluka.manager.R

data class AllukaAppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val icon: ImageVector,
    val iconColor: Color,
    val isGame: Boolean,
    val isSystem: Boolean,
    var isEnabled: Boolean,
    val isRecommended: Boolean,
    val version: String = "v1.0.0",
    var perfLiteMode: String = "default",
    var bypassCharging: String = "default",
    var gamePreload: String = "default",
    var appPriority: String = "default",
    var dndOnGaming: String = "default",
    var refreshRate: String = "default",
    var renderEngine: String = "default",
    var downscalePercent: Int = 0,
    var targetFps: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplistScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSystemApps by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var appToConfig by remember { mutableStateOf<AllukaAppItem?>(null) }

    // Exact AZenith Apps Data (11 default enabled games from azenithApplist.json + user apps + system apps)
    val appsList = remember {
        mutableStateListOf(
            AllukaAppItem("bluearchive", "Blue Archive", "com.nexon.bluearchive", Icons.Rounded.SportsEsports, Color(0xFF0284C7), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v1.60.260522"),
            AllukaAppItem("ddlc", "Doki Doki Literature Club Plus!", "com.serenityforge.dokidokiliteratureclub", Icons.Rounded.AutoStories, Color(0xFFF43F5E), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v1.0.4"),
            AllukaAppItem("pjsekai", "HATSUNE MIKU: COLORFUL STAGE!", "com.sega.ColorfulStage.en", Icons.Rounded.MusicNote, Color(0xFF14B8A6), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v2.8.1"),
            AllukaAppItem("hsr", "Honkai: Star Rail", "com.HoYoverse.hkrpgoversea", Icons.Rounded.RocketLaunch, Color(0xFF8B5CF6), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v2.6.0"),
            AllukaAppItem("mlbb", "Mobile Legends: Bang Bang", "com.mobile.legends", Icons.Rounded.SportsEsports, Color(0xFFF59E0B), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v1.8.78.9511"),
            AllukaAppItem("onmyoji", "Onmyoji: The World", "com.netease.yysls", Icons.Rounded.LocalFireDepartment, Color(0xFFE11D48), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v1.0.22"),
            AllukaAppItem("reverse1999", "Reverse: 1999", "com.bluepoch.m.en.reverse1999", Icons.Rounded.HourglassBottom, Color(0xFFD97706), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v1.9.0"),
            AllukaAppItem("stellasora", "Stella Sora", "com.YoStarEN.StellaSora", Icons.Rounded.Star, Color(0xFF818CF8), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v1.1.0"),
            AllukaAppItem("imouto", "Teaching Feeling", "com.FFhouse.ImoutoToIchaLoveSeikatsu", Icons.Rounded.Favorite, Color(0xFFF472B6), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v3.0.1"),
            AllukaAppItem("umamusume", "Uma Musume Pretty Derby", "com.cygames.umamusume", Icons.Rounded.Pets, Color(0xFF10B981), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v1.38.0"),
            AllukaAppItem("wuthering", "Wuthering Waves", "com.kurogame.wutheringwaves.global", Icons.Rounded.Air, Color(0xFF06B6D4), isGame = true, isSystem = false, isEnabled = true, isRecommended = true, version = "v1.3.0"),
            
            // Installed User Apps (Disabled by default in AZenith)
            AllukaAppItem("codm", "Call of Duty: Mobile", "com.activision.callofduty.shopper", Icons.Rounded.MilitaryTech, Color(0xFF64748B), isGame = true, isSystem = false, isEnabled = false, isRecommended = true, version = "v1.0.46"),
            AllukaAppItem("genshin", "Genshin Impact", "com.miHoYo.GenshinImpact", Icons.Rounded.LocalFireDepartment, Color(0xFFEC4899), isGame = true, isSystem = false, isEnabled = false, isRecommended = true, version = "v5.1.0"),
            AllukaAppItem("pubgm", "PUBG Mobile", "com.tencent.ig", Icons.Rounded.Shield, Color(0xFFF97316), isGame = true, isSystem = false, isEnabled = false, isRecommended = true, version = "v3.4.0"),
            AllukaAppItem("spotify", "Spotify: Music & Podcasts", "com.spotify.music", Icons.Rounded.Headphones, Color(0xFF10B981), isGame = false, isSystem = false, isEnabled = false, isRecommended = false, version = "v8.9.74"),
            AllukaAppItem("tiktok", "TikTok", "com.zhiliaoapp.musically", Icons.Rounded.VideoLibrary, Color(0xFF06B6D4), isGame = false, isSystem = false, isEnabled = false, isRecommended = false, version = "v36.2.4"),
            AllukaAppItem("wa", "WhatsApp Messenger", "com.whatsapp", Icons.Rounded.Chat, Color(0xFF22C55E), isGame = false, isSystem = false, isEnabled = false, isRecommended = false, version = "v2.24.18.75"),
            AllukaAppItem("yt", "YouTube", "com.google.android.youtube", Icons.Rounded.PlayArrow, Color(0xFFEF4444), isGame = false, isSystem = false, isEnabled = false, isRecommended = false, version = "v19.34.35"),

            // System Apps
            AllukaAppItem("android_sys", "Android System", "android", Icons.Rounded.Android, Color(0xFF3B82F6), isGame = false, isSystem = true, isEnabled = false, isRecommended = false, version = "v13 (Tiramisu)"),
            AllukaAppItem("play_services", "Google Play Services", "com.google.android.gms", Icons.Rounded.Extension, Color(0xFF3B82F6), isGame = false, isSystem = true, isEnabled = false, isRecommended = false, version = "v24.32.33"),
            AllukaAppItem("settings_app", "Settings", "com.android.settings", Icons.Rounded.Settings, Color(0xFF64748B), isGame = false, isSystem = true, isEnabled = false, isRecommended = false, version = "v13.0"),
            AllukaAppItem("sysui", "System UI", "com.android.systemui", Icons.Rounded.Widgets, Color(0xFF64748B), isGame = false, isSystem = true, isEnabled = false, isRecommended = false, version = "v13.0")
        )
    }

    // Exact AZenith Sorting: Enabled first, then Recommended first, then alphabetical label
    val filteredApps = remember(searchQuery, showSystemApps, appsList.toList()) {
        appsList.filter { app ->
            val matchSystem = showSystemApps || !app.isSystem
            val matchQuery = if (searchQuery.isBlank()) true else {
                app.name.contains(searchQuery, ignoreCase = true) ||
                        app.packageName.contains(searchQuery, ignoreCase = true)
            }
            matchSystem && matchQuery
        }.sortedWith(
            compareByDescending<AllukaAppItem> { it.isEnabled }
                .thenByDescending { it.isRecommended }
                .thenBy { it.name }
        )
    }

    val largeCorner = 26.dp
    val smallCorner = 4.dp
    val topShape = RoundedCornerShape(topStart = largeCorner, topEnd = largeCorner, bottomStart = smallCorner, bottomEnd = smallCorner)
    val middleShape = RoundedCornerShape(smallCorner)
    val bottomShape = RoundedCornerShape(topStart = smallCorner, topEnd = smallCorner, bottomStart = largeCorner, bottomEnd = largeCorner)
    val singleShape = RoundedCornerShape(largeCorner)

    if (appToConfig != null) {
        // Full AZenith AppSettingsScreen Replica
        AllukaAppSettingsScreen(
            app = appToConfig!!,
            onBack = { appToConfig = null }
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
                                placeholder = { Text("Search apps.", fontSize = 15.sp) },
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
                                    onClick = { menuExpanded = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("Show System Apps") },
                                    trailingIcon = {
                                        if (showSystemApps) {
                                            Icon(Icons.Rounded.Check, contentDescription = null)
                                        }
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
            containerColor = Color.Transparent,
            modifier = modifier
        ) { innerPadding ->
            if (filteredApps.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No apps match this search",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
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
                                // App Icon (60.dp squircle)
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(app.iconColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = app.icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
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
                                        if (app.isEnabled) {
                                            AzLabelText("Enabled", Color(0xFF4CAF50))
                                        } else {
                                            AzLabelText("Disabled", MaterialTheme.colorScheme.error)
                                        }
                                        if (app.isRecommended) {
                                            AzLabelText("Recommended", MaterialTheme.colorScheme.primary)
                                        }
                                        if (app.isSystem) {
                                            AzLabelText("System", MaterialTheme.colorScheme.secondary)
                                        }
                                    }
                                }

                                Switch(
                                    checked = app.isEnabled,
                                    onCheckedChange = { isChecked ->
                                        app.isEnabled = isChecked
                                    },
                                    modifier = Modifier.padding(start = 8.dp)
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
    onBack: () -> Unit
) {
    var masterOn by remember { mutableStateOf(app.isEnabled) }
    val colorScheme = MaterialTheme.colorScheme

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
                    IconButton(onClick = {}) {
                        Icon(Icons.AutoMirrored.Rounded.Launch, contentDescription = "Launch")
                    }
                    IconButton(onClick = {}) {
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
                            text = "App-specific settings will override global settings; keep default to inherit global configuration",
                            style = MaterialTheme.typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // App Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(app.iconColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(app.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(44.dp))
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
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Rounded.PowerSettingsNew, contentDescription = null, tint = colorScheme.primary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Alluka Service", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                            Text(text = "Enabling this allows the performance profile to be triggered", fontSize = 12.sp, color = colorScheme.outline)
                        }
                        Switch(
                            checked = masterOn,
                            onCheckedChange = {
                                masterOn = it
                                app.isEnabled = it
                            }
                        )
                    }
                }
            }

            // Expanded Settings
            if (masterOn) {
                item {
                    Text(
                        text = "PERFORMANCE",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp)
                    )
                }
                item {
                    SettingOptionCard(icon = Icons.Rounded.Speed, title = "Performance Lite", desc = "Reduce CPU frequency to lower heat", value = "Default")
                }

                item {
                    Text(
                        text = "ADDITIONAL SETTINGS",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp)
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(colorScheme.surfaceColorAtElevation(1.dp))
                    ) {
                        SettingOptionRow(icon = Icons.Rounded.Cable, title = "Bypass Charging", desc = "Configure Alluka bypass charging feature", value = "Default")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        SettingOptionRow(icon = Icons.Rounded.RocketLaunch, title = "Game Preloading", desc = "Preload runtime libraries when a game starts", value = "Default")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        SettingOptionRow(icon = Icons.Rounded.SwapVerticalCircle, title = "App Priority", desc = "Boost I/O scheduling priority", value = "Default")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        SettingOptionRow(icon = Icons.Rounded.DoNotDisturbOn, title = "Do Not Disturb Mode", desc = "Block notifications while gaming", value = "Default")
                    }
                }

                item {
                    Text(
                        text = "DISPLAY & RENDER SETTINGS",
                        style = MaterialTheme.typography.labelLarge,
                        color = colorScheme.primary,
                        modifier = Modifier.fillMaxWidth().padding(start = 12.dp, top = 8.dp)
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(26.dp))
                            .background(colorScheme.surfaceColorAtElevation(1.dp))
                    ) {
                        SettingOptionRow(icon = Icons.Rounded.WebStories, title = "Refresh Rate", desc = "Set preferred screen refresh rate", value = "Default")
                        HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                        SettingOptionRow(icon = Icons.Rounded.Layers, title = "Current Render Engine", desc = "Set preferred render engine", value = "Default")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun SettingOptionCard(icon: ImageVector, title: String, desc: String, value: String) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        SettingOptionRow(icon = icon, title = title, desc = desc, value = value)
    }
}

@Composable
fun SettingOptionRow(icon: ImageVector, title: String, desc: String, value: String) {
    Row(
        modifier = Modifier.padding(14.dp),
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
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}
