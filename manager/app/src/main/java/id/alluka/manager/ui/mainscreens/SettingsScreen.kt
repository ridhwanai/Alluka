package id.alluka.manager.ui.mainscreens

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.alluka.manager.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current

    var stateToast by remember { mutableStateOf(true) }
    var profileNotifications by remember { mutableStateOf(true) }
    var disableAutoMode by remember { mutableStateOf(false) }
    var disableTweak by remember { mutableStateOf(false) }
    var profileTimeout by remember { mutableStateOf(true) }

    var hideLauncher by remember { mutableStateOf(false) }
    var verboseLog by remember { mutableStateOf(false) }

    var showUninstallDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pengaturan",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Info Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Image(
                                painter = painterResource(R.drawable.avatar),
                                contentDescription = "Alluka Avatar",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Alluka Manager", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            Text(text = "v1.0 Stable • Rooted (KernelSU)", fontSize = 11.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                            Text(text = "Konfigurasi daemon, perilaku profil, dan sistem.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Group 1: Fitur Alluka Engine (Features)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "FITUR ALLUKA ENGINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        SettingsSwitchRow(
                            icon = Icons.Rounded.Notifications,
                            title = "Tampilkan Toast Profil",
                            desc = "Munculkan toast saat mode otomatis aktif",
                            checked = stateToast,
                            onCheckedChange = { stateToast = it }
                        )

                        SettingsSwitchRow(
                            icon = Icons.Rounded.NotificationsActive,
                            title = "Notifikasi Status Persisten",
                            desc = "Tampilkan info profil & PID di bilah status",
                            checked = profileNotifications,
                            onCheckedChange = { profileNotifications = it }
                        )

                        SettingsSwitchRow(
                            icon = Icons.Rounded.SmartToy,
                            title = "Nonaktifkan Mode Otomatis",
                            desc = "Kunci profil manual tanpa auto-switch",
                            checked = disableAutoMode,
                            onCheckedChange = { disableAutoMode = it }
                        )

                        SettingsSwitchRow(
                            icon = Icons.Rounded.Tune,
                            title = "Nonaktifkan Tweaking Kernel",
                            desc = "Jeda tuning frekuensi tanpa uninstall",
                            checked = disableTweak,
                            onCheckedChange = { disableTweak = it }
                        )

                        SettingsSwitchRow(
                            icon = Icons.Rounded.Timer,
                            title = "Batas Waktu Profil (Timeout)",
                            desc = "Kembalikan ke Daily setelah 30 detik game ditutup",
                            checked = profileTimeout,
                            onCheckedChange = { profileTimeout = it }
                        )
                    }
                }
            }

            // Group 2: Sistem & Utilitas
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "SISTEM & UTILITAS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        SettingsSwitchRow(
                            icon = Icons.Rounded.VisibilityOff,
                            title = "Sembunyikan Ikon Launcher",
                            desc = "Buka Alluka Manager hanya dari KernelSU",
                            checked = hideLauncher,
                            onCheckedChange = { hideLauncher = it }
                        )

                        SettingsActionRow(
                            icon = Icons.Rounded.RestartAlt,
                            title = "Mulai Ulang Service Alluka",
                            desc = "Jalankan ulang skrip apply.sh & reload",
                            onClick = { /* Shell restart service */ }
                        )

                        SettingsActionRow(
                            icon = Icons.Rounded.Save,
                            title = "Simpan & Ekspor Log",
                            desc = "Ekspor alluka.log untuk diagnostik",
                            onClick = { /* Export log */ }
                        )

                        SettingsSwitchRow(
                            icon = Icons.Rounded.BugReport,
                            title = "Mode Log Verbose / Debug",
                            desc = "Catat eksekusi parameter kernel secara detail",
                            checked = verboseLog,
                            onCheckedChange = { verboseLog = it }
                        )

                        SettingsActionRow(
                            icon = Icons.Rounded.DeleteForever,
                            title = "Copot Pemasangan Modul",
                            desc = "Hapus modul Alluka dan kembalikan ke default",
                            isDanger = true,
                            onClick = { showUninstallDialog = true }
                        )
                    }
                }
            }

            // Group 3: Tentang Modul
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "TENTANG MODUL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        SettingsActionRow(
                            icon = Icons.Rounded.Article,
                            title = "Catatan Rilis (Changelog)",
                            desc = "Versi 1.0 Stable • Hitori Engine & AZenith UI",
                            onClick = { showChangelogDialog = true }
                        )

                        SettingsActionRow(
                            icon = Icons.Rounded.Forum,
                            title = "Komunitas Telegram",
                            desc = "@Alluka_id • Diskusi & Update",
                            onClick = { uriHandler.openUri("https://t.me/Alluka_id") }
                        )

                        SettingsActionRow(
                            icon = Icons.Rounded.Code,
                            title = "Repositori GitHub",
                            desc = "ridhwanai/Alluka • Lisensi Apache 2.0",
                            onClick = { uriHandler.openUri("https://github.com/ridhwanai/Alluka.git") }
                        )
                    }
                }
            }
        }

        // Uninstall Confirm Dialog
        if (showUninstallDialog) {
            AlertDialog(
                onDismissRequest = { showUninstallDialog = false },
                title = { Text("Copot Pemasangan Modul", fontWeight = FontWeight.Bold) },
                text = { Text("Apakah Anda yakin ingin menghapus modul Alluka? Perangkat akan me-reset governor ke default setelah reboot.") },
                confirmButton = {
                    Button(
                        onClick = { showUninstallDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Uninstall")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUninstallDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // Changelog Dialog
        if (showChangelogDialog) {
            AlertDialog(
                onDismissRequest = { showChangelogDialog = false },
                title = { Text("Changelog v1.0", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "• Alluka Manager v1.0 rilis perdana\n" +
                                "• Desain Material 3 Expressive & AZenith Floating Navbar\n" +
                                "• Profil dinamis: Sleep, Daily, dan Nanika Peforma\n" +
                                "• Integrasi Hitori Kernel tuning (Schedutil clusters & GED Boost)\n" +
                                "• Per-app profiling & Bypass Charging protection"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showChangelogDialog = false }) {
                        Text("Tutup")
                    }
                }
            )
        }
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    desc: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    desc: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDanger) Color(0x26EF4444) else Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDanger) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDanger) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
            )
            Text(text = desc, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(16.dp)
        )
    }
}
