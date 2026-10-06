package id.alluka.manager.ui.mainscreens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.alluka.manager.R
import id.alluka.manager.data.AllukaEngine
import id.alluka.manager.ui.component.AllukaDynamicBanner
import id.alluka.manager.ui.component.AllukaProfile
import id.alluka.manager.ui.navigation.AllukaFloatingNavBar
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var currentProfile by remember { mutableStateOf(AllukaProfile.DAILY) }
    var isModuleActive by remember { mutableStateOf(true) }
    var hasRoot by remember { mutableStateOf(true) }
    var isApplying by remember { mutableStateOf(false) }

    var selectedNavRoute by remember { mutableStateOf("home") }
    var showProfileDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val rootOk = AllukaEngine.checkRoot()
        hasRoot = rootOk
        val modOk = AllukaEngine.isModuleInstalled()
        isModuleActive = modOk
        val active = AllukaEngine.getActiveProfile()
        currentProfile = when (active) {
            "sleep" -> AllukaProfile.SLEEP
            "peforma" -> AllukaProfile.PEFORMA
            else -> AllukaProfile.DAILY
        }
    }

    fun selectProfile(profile: AllukaProfile) {
        if (isApplying || currentProfile == profile) return
        isApplying = true
        coroutineScope.launch {
            AllukaEngine.applyProfile(profile.id)
            currentProfile = profile
            isApplying = false
            showProfileDialog = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.avatar),
                                    contentDescription = "Avatar",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Text(
                                text = "Alluka",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 21.sp,
                                lineHeight = 21.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { uriHandler.openUri("https://t.me/Alluka_id") }) {
                            Icon(imageVector = Icons.Rounded.Share, contentDescription = "Telegram")
                        }
                        IconButton(onClick = { /* Reboot Menu */ }) {
                            Icon(imageVector = Icons.Rounded.PowerSettingsNew, contentDescription = "Reboot")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Dynamic Hero Banner (Tinggi kompak 165dp, tanpa quote, mode di kiri atas)
                item {
                    AllukaDynamicBanner(
                        currentProfile = currentProfile,
                        pid = "2841",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(165.dp)
                            .clickable { showProfileDialog = true }
                    )
                }

                // 2. Compact 2x1 Info Tiles (Status Modul: Aktif | Akses Root: Diperoleh)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp)),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Widgets,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Status Modul", fontSize = 10.sp, lineHeight = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = if (isModuleActive) "Aktif" else "Tidak Aktif", fontSize = 13.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp)),
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (hasRoot) Color(0x2610B981) else Color(0x26EF4444)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Security,
                                        contentDescription = null,
                                        tint = if (hasRoot) Color(0xFF10B981) else Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = "Akses Root", fontSize = 10.sp, lineHeight = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(text = if (hasRoot) "Diperoleh" else "Ditolak", fontSize = 13.sp, lineHeight = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // 3. Informasi Perangkat (Header Berada di Dalam Kotak Card)
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(22.dp)),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(22.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header di Dalam Kotak Card
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.PhoneAndroid,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Informasi Perangkat",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Terverifikasi",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF10B981)
                                    )
                                }
                            }

                            Divider(color = Color.White.copy(alpha = 0.05f))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                DeviceInfoItem(modifier = Modifier.weight(1f), icon = Icons.Rounded.Smartphone, label = "Nama Perangkat", value = "Redmi Note 9")
                                DeviceInfoItem(modifier = Modifier.weight(1f), icon = Icons.Rounded.Memory, label = "Chipset", value = "Helio G85")
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                DeviceInfoItem(modifier = Modifier.weight(1f), icon = Icons.Rounded.Code, label = "Versi Kernel", value = "Linux 4.14.336")
                                DeviceInfoItem(modifier = Modifier.weight(1f), icon = Icons.Rounded.AutoAwesome, label = "Versi Alluka", value = "v1.0 Stable", isHighlight = true)
                            }
                        }
                    }
                }

                // 4. Refined Elegant Maintainer Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp)),
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.avatar),
                                    contentDescription = "Alluka Maintainer",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "MAINTAINER • ALLUKA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Modul optimasi performa & baterai adaptif.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { uriHandler.openUri("https://t.me/Alluka_id") },
                                        color = Color(0x2238BDF8),
                                        shape = RoundedCornerShape(12.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x4438BDF8))
                                    ) {
                                        Text(
                                            text = "Telegram @Alluka_id",
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // AZenith Floating Navbar Positioned at Bottom Center (Lebih Tinggi)
        AllukaFloatingNavBar(
            selectedRoute = selectedNavRoute,
            onRouteSelected = { selectedNavRoute = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )

        // AZenith Profile Selection Dialog
        if (showProfileDialog) {
            AlertDialog(
                onDismissRequest = { showProfileDialog = false },
                title = { Text(text = "Select Profile", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        AllukaProfile.values().forEach { profile ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { selectProfile(profile) },
                                color = if (currentProfile == profile) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = when (profile) {
                                            AllukaProfile.SLEEP -> "🌙"
                                            AllukaProfile.DAILY -> "🌸"
                                            AllukaProfile.PEFORMA -> "⚡"
                                        },
                                        fontSize = 18.sp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = profile.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text(text = profile.subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (currentProfile == profile) {
                                        Icon(imageVector = Icons.Rounded.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showProfileDialog = false }) {
                        Text(text = "Tutup")
                    }
                }
            )
        }
    }
}

@Composable
private fun DeviceInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    isHighlight: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.04f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
        }
        Column {
            Text(text = label, fontSize = 10.sp, lineHeight = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = value, fontSize = 12.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold, color = if (isHighlight) MaterialTheme.colorScheme.primary else Color.White)
        }
    }
}
