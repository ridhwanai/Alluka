package id.alluka.manager.ui.mainscreens

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.alluka.manager.data.AllukaEngine
import id.alluka.manager.ui.component.AppIconImage
import id.alluka.manager.ui.component.SkeletonContent
import id.alluka.manager.ui.component.SkeletonListRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.Collator
import java.util.Locale

data class AllukaRealAppItem(
    val packageName: String,
    val name: String,
    val isGame: Boolean,
    val isSystem: Boolean,
    val appInfo: ApplicationInfo?,
    val isEnabled: Boolean,
    val versionName: String,
    var renderEngine: String = "default"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplistScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    var appsList by remember { mutableStateOf<List<AllukaRealAppItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var showSystemApps by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }

    var selectedAppForConfig by remember { mutableStateOf<AllukaRealAppItem?>(null) }
    var showAppDetailSheet by remember { mutableStateOf(false) }

    suspend fun queryInstalledApps(): List<AllukaRealAppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val enabledSet = AllukaEngine.getEnabledApps()
        val installed = try {
            pm.getInstalledPackages(PackageManager.GET_META_DATA)
        } catch (_: Exception) {
            emptyList()
        }

        val result = ArrayList<AllukaRealAppItem>(installed.size)
        for (pkg in installed) {
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

            result.add(
                AllukaRealAppItem(
                    packageName = pkg.packageName,
                    name = label,
                    isGame = isGame,
                    isSystem = isSystem,
                    appInfo = appInfo,
                    isEnabled = enabledSet.contains(pkg.packageName),
                    versionName = pkg.versionName ?: "v1.0"
                )
            )
        }

        val collator = Collator.getInstance(Locale.getDefault())
        result.sortWith(
            compareByDescending<AllukaRealAppItem> { it.isEnabled }
                .thenByDescending { it.isGame }
                .then(compareBy(collator) { it.name })
        )
        result
    }

    fun refreshApps() {
        scope.launch {
            isRefreshing = true
            val start = System.currentTimeMillis()
            val loaded = queryInstalledApps()
            appsList = loaded
            val elapsed = System.currentTimeMillis() - start
            if (elapsed < 600) delay(600 - elapsed)
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (isSearchActive) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Cari game atau aplikasi...", fontSize = 14.sp) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = CircleShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(imageVector = Icons.Rounded.Close, contentDescription = "Clear", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        )
                    } else {
                        Text(
                            text = "App List",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp
                        )
                    }
                },
                navigationIcon = {
                    if (isSearchActive) {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    if (!isSearchActive) {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(imageVector = Icons.Rounded.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { refreshApps() }) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Refresh")
                            }
                        }
                        Box {
                            IconButton(onClick = { menuExpanded = true }) {
                                Icon(imageVector = Icons.Rounded.MoreVert, contentDescription = "Filter")
                            }
                            DropdownMenu(
                                expanded = menuExpanded,
                                onDismissRequest = { menuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = {
                                        Text(if (showSystemApps) "Sembunyikan Aplikasi Sistem" else "Tampilkan Aplikasi Sistem")
                                    },
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
                        }
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
        Crossfade(
            targetState = isLoading,
            animationSpec = androidx.compose.animation.core.tween(300),
            label = "AppListLoadingFade"
        ) { loading ->
            if (loading) {
                // Modern Android 16 / AZenith Expressive Shimmer Skeleton Loading
                SkeletonContent(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(8) {
                            SkeletonListRow()
                        }
                    }
                }
            } else {
                if (filteredApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Widgets,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = if (searchQuery.isNotEmpty()) "Tidak ada aplikasi cocok dengan '$searchQuery'" else "Tidak ada aplikasi ditemukan",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 100.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Summary Chip
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${filteredApps.size} Aplikasi Terpasang",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    val enabledCount = appsList.count { it.isEnabled }
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                    ) {
                                        Text(
                                            text = "$enabledCount Dioptimalkan",
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        items(filteredApps, key = { it.packageName }) { app ->
                            AppItemRow(
                                app = app,
                                onToggleEnabled = { enabled ->
                                    val updated = appsList.map {
                                        if (it.packageName == app.packageName) it.copy(isEnabled = enabled) else it
                                    }
                                    appsList = updated
                                    scope.launch {
                                        AllukaEngine.setAppEnabled(app.packageName, enabled)
                                        snackbarHostState.showSnackbar(
                                            if (enabled) "${app.name} ditambahkan ke optimasi Alluka" else "${app.name} dihapus dari optimasi"
                                        )
                                    }
                                },
                                onClick = {
                                    selectedAppForConfig = app
                                    showAppDetailSheet = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet for App Details & Quick Options
    if (showAppDetailSheet && selectedAppForConfig != null) {
        val app = selectedAppForConfig!!
        ModalBottomSheet(
            onDismissRequest = { showAppDetailSheet = false },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 36.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AppIconImage(
                        packageName = app.packageName,
                        appInfo = app.appInfo,
                        appName = app.name,
                        size = 52.dp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = app.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
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
                        Text(
                            text = "Versi: ${app.versionName} • ${if (app.isGame) "Game" else if (app.isSystem) "Sistem" else "Aplikasi Pengguna"}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            showAppDetailSheet = false
                            try {
                                val launchIntent = context.packageManager.getLaunchIntentForPackage(app.packageName)
                                if (launchIntent != null) {
                                    context.startActivity(launchIntent)
                                } else {
                                    scope.launch { snackbarHostState.showSnackbar("Tidak dapat membuka ${app.name}") }
                                }
                            } catch (_: Exception) {}
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Buka App")
                    }

                    OutlinedButton(
                        onClick = {
                            showAppDetailSheet = false
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.fromParts("package", app.packageName, null)
                            }
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Info Sistem")
                    }
                }
            }
        }
    }
}

@Composable
fun AppItemRow(
    app: AllukaRealAppItem,
    onToggleEnabled: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (app.isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.05f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppIconImage(
                packageName = app.packageName,
                appInfo = app.appInfo,
                appName = app.name,
                size = 42.dp
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = app.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (app.isGame) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = "GAME",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Text(
                    text = app.packageName,
                    fontSize = 10.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(
                checked = app.isEnabled,
                onCheckedChange = onToggleEnabled
            )
        }
    }
}
