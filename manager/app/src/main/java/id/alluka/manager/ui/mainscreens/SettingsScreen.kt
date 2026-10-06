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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.alluka.manager.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    // Feature toggles
    var stateToast by remember { mutableStateOf(true) }
    var profileNotifications by remember { mutableStateOf(true) }
    var disableAutoMode by remember { mutableStateOf(false) }
    var disableTweak by remember { mutableStateOf(false) }

    // System toggles
    var showLauncherIcon by remember { mutableStateOf(true) }
    var verboseLog by remember { mutableStateOf(false) }

    // Language state: 2 languages only (Bahasa Indonesia & English)
    var selectedLanguage by remember { mutableStateOf("id") } // "id" or "en"
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Dialog states
    var showUninstallDialog by remember { mutableStateOf(false) }
    var showChangelogDialog by remember { mutableStateOf(false) }
    var showSaveLogDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    // Digital clock state for phone mockup
    var clockHour by remember { mutableStateOf(SimpleDateFormat("HH", Locale.getDefault()).format(Date())) }
    var clockMinute by remember { mutableStateOf(SimpleDateFormat("mm", Locale.getDefault()).format(Date())) }
    var uptimeSeconds by remember { mutableStateOf(14 * 3600 + 25 * 60 + 38L) }

    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            clockHour = SimpleDateFormat("HH", Locale.getDefault()).format(now)
            clockMinute = SimpleDateFormat("mm", Locale.getDefault()).format(now)
            uptimeSeconds++
            delay(1000)
        }
    }

    val uptimeText = remember(uptimeSeconds) {
        val h = uptimeSeconds / 3600
        val m = (uptimeSeconds % 3600) / 60
        val s = uptimeSeconds % 60
        "${h}h ${m.toString().padStart(2, '0')}m ${s.toString().padStart(2, '0')}s"
    }

    val isEn = selectedLanguage == "en"

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
                            contentDescription = "Alluka Avatar",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                title = {
                    Text(
                        text = if (isEn) "Settings" else "Pengaturan",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showChangelogDialog = true }) {
                        Icon(
                            imageVector = Icons.Rounded.Description,
                            contentDescription = "Changelog"
                        )
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
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Card 1: AppInfoHeaderContent + Personalisasi (ExpressiveList Container)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Column {
                        // Phone Mockup Box on Left + 6 Metadata Rows on Right
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Phone Mockup with centered big digital clock (AZenith Signature)
                            Box(
                                modifier = Modifier
                                    .width(62.dp)
                                    .height(122.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = clockHour,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        lineHeight = 26.sp
                                    )
                                    Text(
                                        text = clockMinute,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        lineHeight = 26.sp
                                    )
                                }
                            }

                            // 6 Metadata rows (tight spacing)
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                InfoRow(label = if (isEn) "App name" else "Nama aplikasi", value = "Alluka")
                                InfoRow(label = if (isEn) "Author" else "Penulis", value = "Alluka (@Alluka_id)")
                                InfoRow(label = if (isEn) "Build Date" else "Tanggal build", value = "2026-10-06 16:15")
                                InfoRow(label = if (isEn) "Version Code" else "Kode versi", value = "100")
                                InfoRow(label = if (isEn) "Package Name" else "Nama paket", value = context.packageName)
                                InfoRow(
                                    label = if (isEn) "Device Uptime" else "Waktu aktif perangkat",
                                    value = uptimeText,
                                    isAccent = true
                                )
                            }
                        }
                    }
                }
            }

            // Section 1: FITUR (Features)
            item {
                SectionTitle(if (isEn) "FEATURES" else "FITUR")
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SettingsSwitchRow(
                            icon = Icons.Rounded.Notifications,
                            title = if (isEn) "Show Toast Notifications" else "Tampilkan notifikasi Toast",
                            desc = if (isEn) "Display toast when profile changes" else "Munculkan toast saat mode otomatis aktif",
                            checked = stateToast,
                            onCheckedChange = { stateToast = it }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))

                        SettingsSwitchRow(
                            icon = Icons.Rounded.NotificationsActive,
                            title = if (isEn) "Show Notifications" else "Tampilkan Notifikasi",
                            desc = if (isEn) "Show active profile & PID in notification drawer" else "Tampilkan info profil & PID di bilah status",
                            checked = profileNotifications,
                            onCheckedChange = { profileNotifications = it }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))

                        SettingsSwitchRow(
                            icon = Icons.Rounded.AutoAwesome,
                            title = if (isEn) "Disable Auto Mode" else "Nonaktifkan Mode Otomatis",
                            desc = if (isEn) "Stop automatic profile switching when launching apps" else "Hentikan pengalihan profil otomatis saat game dibuka",
                            checked = disableAutoMode,
                            onCheckedChange = { disableAutoMode = it }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))

                        SettingsSwitchRow(
                            icon = Icons.Rounded.Tune,
                            title = if (isEn) "Disable Tweak" else "Nonaktifkan Tweak",
                            desc = if (isEn) "Restore kernel to default without uninstalling" else "Kembalikan kernel ke default tanpa mencopot pemasangan",
                            checked = disableTweak,
                            onCheckedChange = { disableTweak = it }
                        )
                    }
                }
            }

            // Section 2: LAINNYA (Others)
            item {
                SectionTitle(if (isEn) "OTHER" else "LAINNYA")
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        SettingsSwitchRow(
                            icon = Icons.Rounded.PhoneAndroid,
                            title = if (isEn) "Show App Icon on Home Screen" else "Tampilkan ikon di layar utama",
                            desc = if (isEn) "Display launcher icon in application drawer" else "Tampilkan ikon Alluka Manager di app drawer",
                            checked = showLauncherIcon,
                            onCheckedChange = { showLauncherIcon = it }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))

                        // Pilihan Bahasa (2 Bahasa Saja: Bahasa Indonesia & English)
                        SettingsActionRow(
                            icon = Icons.Rounded.Language,
                            title = if (isEn) "Language" else "Bahasa",
                            desc = if (isEn) "English (US)" else "Bahasa Indonesia",
                            onClick = { showLanguageDialog = true }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))

                        SettingsActionRow(
                            icon = Icons.Rounded.RestartAlt,
                            title = if (isEn) "Restart Service" else "Mulai ulang layanan",
                            desc = if (isEn) "Reinitialize daemon & reload module configuration" else "Muat ulang konfigurasi modul & daemon layanan",
                            onClick = { /* Shell restart */ }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))

                        SettingsActionRow(
                            icon = Icons.Rounded.Save,
                            title = if (isEn) "Save Log" else "Simpan log",
                            desc = if (isEn) "Export compressed diagnostic logs" else "Simpan atau bagikan log diagnostik terkompresi",
                            onClick = { showSaveLogDialog = true }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))

                        SettingsSwitchRow(
                            icon = Icons.Rounded.BugReport,
                            title = if (isEn) "Allow Verbose Log" else "Izinkan log terperinci",
                            desc = if (isEn) "Record detailed debug logs in background" else "Catat debug log tambahan di latar belakang",
                            checked = verboseLog,
                            onCheckedChange = { verboseLog = it }
                        )

                        HorizontalDivider(color = Color.White.copy(alpha = 0.04f))

                        SettingsActionRow(
                            icon = Icons.Rounded.DeleteForever,
                            title = if (isEn) "Uninstall" else "Copot pemasangan",
                            desc = if (isEn) "Remove Alluka module from device" else "Hapus modul Alluka dari perangkat",
                            isDanger = true,
                            onClick = { showUninstallDialog = true }
                        )
                    }
                }
            }

            // Section 3: TENTANG (About)
            item {
                SectionTitle(if (isEn) "ABOUT" else "TENTANG")
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        SettingsActionRow(
                            icon = Icons.Rounded.Info,
                            title = if (isEn) "About Alluka" else "Tentang Alluka",
                            desc = if (isEn) "Version 1.0 (Build 100)" else "Versi 1.0 (Build 100)",
                            onClick = { showAboutDialog = true }
                        )
                    }
                }
            }
        }

        // Language Picker Dialog (2 Bahasa Saja: Bahasa Indonesia & English)
        if (showLanguageDialog) {
            AlertDialog(
                onDismissRequest = { showLanguageDialog = false },
                title = { Text(if (isEn) "Select Language" else "Pilih Bahasa", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedLanguage = "id"
                                    showLanguageDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == "id",
                                onClick = {
                                    selectedLanguage = "id"
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Bahasa Indonesia", fontWeight = FontWeight.Bold)
                                Text("Bahasa Indonesia (Bawaan)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    selectedLanguage = "en"
                                    showLanguageDialog = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedLanguage == "en",
                                onClick = {
                                    selectedLanguage = "en"
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("English", fontWeight = FontWeight.Bold)
                                Text("English (US)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showLanguageDialog = false }) {
                        Text(if (isEn) "Cancel" else "Batal")
                    }
                }
            )
        }

        // Uninstall Confirm Dialog
        if (showUninstallDialog) {
            AlertDialog(
                onDismissRequest = { showUninstallDialog = false },
                title = { Text(if (isEn) "Uninstall Module?" else "Copot Pemasangan Modul?", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        if (isEn)
                            "Are you sure you want to uninstall Alluka? Kernel parameters will be restored to default on next reboot."
                        else
                            "Apakah Anda yakin ingin mencopot modul Alluka? Parameter kernel akan dikembalikan ke bawaan setelah reboot."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showUninstallDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(if (isEn) "Uninstall" else "Copot Modul")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUninstallDialog = false }) {
                        Text(if (isEn) "Cancel" else "Batal")
                    }
                }
            )
        }

        // Changelog Dialog
        if (showChangelogDialog) {
            AlertDialog(
                onDismissRequest = { showChangelogDialog = false },
                title = { Text(if (isEn) "Changelog v1.0" else "Catatan Rilis v1.0", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "• Integrasi UI AZenith Expressive & Floating Navbar\n" +
                                "• Karakter dinamis Sleep, Daily, dan Nanika Peforma\n" +
                                "• Alluka Kernel Engine: Cluster Schedutil & MediaTek GED Boost\n" +
                                "• Dukungan 2 Bahasa: Bahasa Indonesia & English\n" +
                                "• Format gambar teroptimasi JPEG & JPG"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showChangelogDialog = false }) {
                        Text(if (isEn) "Close" else "Tutup")
                    }
                }
            )
        }

        // Save Log Dialog
        if (showSaveLogDialog) {
            AlertDialog(
                onDismissRequest = { showSaveLogDialog = false },
                title = { Text(if (isEn) "Logs & Diagnostics" else "Log & Diagnostik", fontWeight = FontWeight.Bold) },
                text = {
                    Text(if (isEn) "Save or share compressed diagnostic logs (/sdcard/Alluka_Logs.tar.gz)." else "Simpan atau bagikan berkas log diagnostik terkompresi.")
                },
                confirmButton = {
                    TextButton(onClick = { showSaveLogDialog = false }) {
                        Text(if (isEn) "Export Log" else "Simpan ke File")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSaveLogDialog = false }) {
                        Text(if (isEn) "Cancel" else "Batal")
                    }
                }
            )
        }

        // About Dialog
        if (showAboutDialog) {
            AlertDialog(
                onDismissRequest = { showAboutDialog = false },
                title = { Text(if (isEn) "About Alluka" else "Tentang Alluka", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Alluka Manager • v1.0 (Build 100)")
                        Text("Author: Alluka (@Alluka_id)")
                        Text("License: Apache 2.0")
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Telegram: https://t.me/Alluka_id",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { uriHandler.openUri("https://t.me/Alluka_id") }
                        )
                        Text(
                            text = "GitHub: https://github.com/ridhwanai/Alluka.git",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { uriHandler.openUri("https://github.com/ridhwanai/Alluka.git") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showAboutDialog = false }) {
                        Text(if (isEn) "Close" else "Tutup")
                    }
                }
            )
        }


    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 6.dp, top = 6.dp, bottom = 2.dp)
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    isAccent: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            text = label,
            fontSize = 9.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 11.sp
        )
        Text(
            text = value,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = if (isAccent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            lineHeight = 13.sp
        )
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(19.dp))
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
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isDanger) Color(0x26EF4444) else Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (isDanger) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp)
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
            tint = if (isDanger) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(18.dp)
        )
    }
}
