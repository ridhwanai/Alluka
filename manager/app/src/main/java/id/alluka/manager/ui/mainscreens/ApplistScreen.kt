package id.alluka.manager.ui.mainscreens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class AllukaAppItem(
    val id: String,
    val name: String,
    val packageName: String,
    val icon: ImageVector,
    val isGame: Boolean,
    val isSystem: Boolean,
    var targetMode: String = "peforma",
    var fpsTarget: Int = 120,
    var bypassCharge: Boolean = true,
    var isEnabled: Boolean = true,
    val isRecommended: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplistScreen(
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("all") } // all, game, system
    var appToConfig by remember { mutableStateOf<AllukaAppItem?>(null) }

    val defaultApps = remember {
        mutableStateListOf(
            AllukaAppItem("mlbb", "Mobile Legends: Bang Bang", "com.mobile.legends", Icons.Rounded.SportsEsports, isGame = true, isSystem = false, targetMode = "peforma", fpsTarget = 120, bypassCharge = true, isRecommended = true),
            AllukaAppItem("genshin", "Genshin Impact", "com.miHoYo.GenshinImpact", Icons.Rounded.LocalFireDepartment, isGame = true, isSystem = false, targetMode = "peforma", fpsTarget = 60, bypassCharge = true, isRecommended = true),
            AllukaAppItem("pubgm", "PUBG Mobile", "com.tencent.ig", Icons.Rounded.MilitaryTech, isGame = true, isSystem = false, targetMode = "peforma", fpsTarget = 90, bypassCharge = true, isRecommended = true),
            AllukaAppItem("wa", "WhatsApp Messenger", "com.whatsapp", Icons.Rounded.Chat, isGame = false, isSystem = false, targetMode = "daily", fpsTarget = 60, bypassCharge = false, isRecommended = false),
            AllukaAppItem("yt", "YouTube", "com.google.android.youtube", Icons.Rounded.PlayArrow, isGame = false, isSystem = false, targetMode = "daily", fpsTarget = 60, bypassCharge = false, isRecommended = false),
            AllukaAppItem("tiktok", "TikTok", "com.zhiliaoapp.musically", Icons.Rounded.VideoLibrary, isGame = false, isSystem = false, targetMode = "daily", fpsTarget = 120, bypassCharge = false, isRecommended = false),
            AllukaAppItem("spotify", "Spotify", "com.spotify.music", Icons.Rounded.Headphones, isGame = false, isSystem = false, targetMode = "sleep", fpsTarget = 60, bypassCharge = false, isRecommended = false),
            AllukaAppItem("sysui", "System UI", "com.android.systemui", Icons.Rounded.Widgets, isGame = false, isSystem = true, targetMode = "daily", fpsTarget = 120, bypassCharge = false, isRecommended = false),
            AllukaAppItem("gms", "Google Play Services", "com.google.android.gms", Icons.Rounded.Extension, isGame = false, isSystem = true, targetMode = "sleep", fpsTarget = 60, bypassCharge = false, isRecommended = false)
        )
    }

    val filteredApps = remember(searchQuery, selectedFilter, defaultApps.toList()) {
        defaultApps.filter { app ->
            val matchFilter = when (selectedFilter) {
                "game" -> app.isGame
                "system" -> app.isSystem
                else -> true
            }
            val matchQuery = if (searchQuery.isBlank()) true else {
                app.name.contains(searchQuery, ignoreCase = true) ||
                        app.packageName.contains(searchQuery, ignoreCase = true)
            }
            matchFilter && matchQuery
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        TextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari aplikasi / package...", fontSize = 14.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Aplikasi",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp
                        )
                    }
                },
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = { isSearchActive = false; searchQuery = "" }) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Kembali")
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Rounded.Search, contentDescription = "Cari")
                        }
                    } else if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Hapus")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Filter row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedFilter == "all",
                        onClick = { selectedFilter = "all" },
                        label = { Text("Semua") }
                    )
                    FilterChip(
                        selected = selectedFilter == "game",
                        onClick = { selectedFilter = "game" },
                        leadingIcon = { Icon(Icons.Rounded.SportsEsports, null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Game") }
                    )
                    FilterChip(
                        selected = selectedFilter == "system",
                        onClick = { selectedFilter = "system" },
                        leadingIcon = { Icon(Icons.Rounded.Security, null, modifier = Modifier.size(16.dp)) },
                        label = { Text("Sistem") }
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${filteredApps.size} Aplikasi",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // List items
            items(filteredApps, key = { it.id }) { app ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { appToConfig = app },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    when (app.targetMode) {
                                        "peforma" -> Color(0x33C026D3)
                                        "sleep" -> Color(0x33818CF8)
                                        else -> Color(0x33FF6584)
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = app.icon,
                                contentDescription = null,
                                tint = when (app.targetMode) {
                                    "peforma" -> Color(0xFFE879F9)
                                    "sleep" -> Color(0xFFA5B4FC)
                                    else -> Color(0xFFFF8FA3)
                                },
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = app.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = app.packageName,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0x2610B981)
                                ) {
                                    Text(
                                        text = "Aktif",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                                if (app.isRecommended) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0x2EC084FC)
                                    ) {
                                        Text(
                                            text = "Recommended",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFC084FC),
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color.White.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = "${app.targetMode.replaceFirstChar { it.uppercase() }} (${app.fpsTarget} FPS)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFCBD5E1),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Per-App Configuration Dialog
        appToConfig?.let { app ->
            var tempMode by remember { mutableStateOf(app.targetMode) }
            var tempFps by remember { mutableIntStateOf(app.fpsTarget) }
            var tempBypass by remember { mutableStateOf(app.bypassCharge) }

            AlertDialog(
                onDismissRequest = { appToConfig = null },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(app.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column {
                            Text(text = app.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(text = app.packageName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Target Profil Modul:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("daily", "peforma", "sleep").forEach { mode ->
                                FilterChip(
                                    selected = tempMode == mode,
                                    onClick = { tempMode = mode },
                                    label = { Text(mode.replaceFirstChar { it.uppercase() }) }
                                )
                            }
                        }

                        Text("Target FPS (FAS/FpsGo):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(60, 90, 120).forEach { fps ->
                                FilterChip(
                                    selected = tempFps == fps,
                                    onClick = { tempFps = fps },
                                    label = { Text("$fps FPS") }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Bypass Charging", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("Cegah panas saat gaming", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = tempBypass, onCheckedChange = { tempBypass = it })
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        app.targetMode = tempMode
                        app.fpsTarget = tempFps
                        app.bypassCharge = tempBypass
                        appToConfig = null
                    }) {
                        Text("Simpan")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { appToConfig = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}
