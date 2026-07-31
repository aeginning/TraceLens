@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pl.tracelens.app.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.annotation.StringRes
import androidx.core.os.LocaleListCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.MonochromePhotos
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import pl.tracelens.app.AppViewModel
import pl.tracelens.app.R
import pl.tracelens.app.data.ImageFilters
import pl.tracelens.app.data.AppSettings
import pl.tracelens.app.data.EraserPoint
import pl.tracelens.app.data.LibraryImage
import pl.tracelens.app.data.TraceProject
import pl.tracelens.app.data.LocalTransferServer
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import pl.tracelens.app.ui.camera.TraceWorkspace

private sealed interface Screen {
    data object Library : Screen
    data object Projects : Screen
    data object Help : Screen
    data object Settings : Screen
    data object Patterns : Screen
    data class Editor(val imageId: String) : Screen
    data class Workspace(val projectId: String) : Screen
    data class Gallery(val projectId: String, val fromWorkspace: Boolean) : Screen
}

@Composable
fun TraceLensApp(
    viewModel: AppViewModel,
    incomingIntent: Intent? = null,
    onIncomingIntentConsumed: () -> Unit = {},
) {
    val darkTheme = when (viewModel.appSettings.themeMode) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    MaterialTheme(colorScheme = if (darkTheme) darkColorScheme() else lightColorScheme()) {
        var screen: Screen by remember { mutableStateOf(Screen.Library) }
        var showLibrarySearch by remember { mutableStateOf(false) }
        var showLibraryFilters by remember { mutableStateOf(false) }
        var showHelpSearch by remember { mutableStateOf(false) }
        val snackbar = remember { SnackbarHostState() }
        val message = viewModel.message
        LaunchedEffect(message) {
            if (message != null) {
                snackbar.showSnackbar(message)
                viewModel.consumeMessage()
            }
        }
        LaunchedEffect(incomingIntent) {
            if (incomingIntent != null) {
                @Suppress("DEPRECATION")
                val uri = when (incomingIntent.action) {
                    Intent.ACTION_SEND -> incomingIntent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    Intent.ACTION_VIEW -> incomingIntent.data
                    else -> null
                }
                if (uri != null) {
                    if (incomingIntent.type?.startsWith("image/") == true) {
                        viewModel.importImage(uri) { image -> screen = Screen.Editor(image.id) }
                    } else {
                        viewModel.importProject(uri) { screen = Screen.Projects }
                    }
                }
                onIncomingIntentConsumed()
            }
        }

        Box(Modifier.fillMaxSize()) {
            when (val current = screen) {
                Screen.Library -> MainScaffold(
                    selectedTab = 0,
                    snackbar = snackbar,
                    onLibrary = { screen = Screen.Library },
                    onProjects = { screen = Screen.Projects },
                    onHelp = { screen = Screen.Help },
                    onSettings = { screen = Screen.Settings },
                    onSearch = { showLibrarySearch = !showLibrarySearch },
                    onFilters = { showLibraryFilters = !showLibraryFilters },
                ) { padding ->
                    LibraryScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(padding),
                        showSearch = showLibrarySearch,
                        showFilters = showLibraryFilters,
                        onEdit = { screen = Screen.Editor(it.id) },
                        onOpenPatterns = { screen = Screen.Patterns },
                        onOpenProject = {
                            viewModel.markImageUsed(it.imageId)
                            screen = Screen.Workspace(it.id)
                        },
                    )
                }
                Screen.Projects -> MainScaffold(
                    selectedTab = 1,
                    snackbar = snackbar,
                    onLibrary = { screen = Screen.Library },
                    onProjects = { screen = Screen.Projects },
                    onHelp = { screen = Screen.Help },
                    onSettings = { screen = Screen.Settings },
                ) { padding ->
                    ProjectsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(padding),
                        onOpen = {
                            viewModel.markImageUsed(it.imageId)
                            screen = Screen.Workspace(it.id)
                        },
                        onGallery = { screen = Screen.Gallery(it.id, false) },
                    )
                }
                Screen.Help -> MainScaffold(
                    selectedTab = 2,
                    snackbar = snackbar,
                    onLibrary = { screen = Screen.Library },
                    onProjects = { screen = Screen.Projects },
                    onHelp = { screen = Screen.Help },
                    onSettings = { screen = Screen.Settings },
                    onSearch = { showHelpSearch = !showHelpSearch },
                ) { padding ->
                    HelpScreen(
                        modifier = Modifier.padding(padding),
                        showSearch = showHelpSearch,
                    )
                }
                Screen.Settings -> MainScaffold(
                    selectedTab = 3,
                    snackbar = snackbar,
                    onLibrary = { screen = Screen.Library },
                    onProjects = { screen = Screen.Projects },
                    onHelp = { screen = Screen.Help },
                    onSettings = { screen = Screen.Settings },
                ) { padding ->
                    SettingsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(padding),
                    )
                }
                Screen.Patterns -> StarterPatternsGalleryScreen(
                    viewModel = viewModel,
                    onBack = { screen = Screen.Library },
                    onProjectCreated = {
                        viewModel.markImageUsed(it.imageId)
                        screen = Screen.Workspace(it.id)
                    },
                )
                is Screen.Editor -> {
                    val image = viewModel.images.firstOrNull { it.id == current.imageId }
                    if (image == null) screen = Screen.Library
                    else EditorScreen(viewModel, image, onBack = { screen = Screen.Library })
                }
                is Screen.Workspace -> {
                    val project = viewModel.projects.firstOrNull { it.id == current.projectId }
                    val image = project?.let(viewModel::imageForProject)
                    if (project == null || image == null) screen = Screen.Projects
                    else TraceWorkspace(
                        viewModel = viewModel,
                        initialProject = project,
                        image = image,
                        onBack = { screen = Screen.Projects },
                    )
                }
                is Screen.Gallery -> {
                    val project = viewModel.projects.firstOrNull { it.id == current.projectId }
                    if (project == null) screen = Screen.Projects
                    else ProjectGalleryScreen(
                        viewModel = viewModel,
                        initialProject = project,
                        onBack = {
                            screen = if (current.fromWorkspace) {
                                Screen.Workspace(current.projectId)
                            } else {
                                Screen.Projects
                            }
                        },
                    )
                }
            }
            if (viewModel.busy) {
                Box(
                    Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }
            if (!viewModel.appSettings.tutorialCompleted) {
                TutorialDialog(
                    onComplete = {
                        viewModel.updateSettings(
                            viewModel.appSettings.copy(tutorialCompleted = true)
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun MainScaffold(
    selectedTab: Int,
    snackbar: SnackbarHostState,
    onLibrary: () -> Unit,
    onProjects: () -> Unit,
    onHelp: () -> Unit,
    onSettings: () -> Unit,
    onSearch: (() -> Unit)? = null,
    onFilters: (() -> Unit)? = null,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            0 -> stringResource(R.string.library_title)
                            1 -> stringResource(R.string.projects_title)
                            2 -> stringResource(R.string.help_title)
                            else -> stringResource(R.string.settings_title)
                        }
                    )
                },
                actions = {
                    if (onSearch != null) {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Default.Search, stringResource(R.string.show_search))
                        }
                    }
                    if (onFilters != null) {
                        IconButton(onClick = onFilters) {
                            Icon(Icons.Default.FilterList, stringResource(R.string.show_filters))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = onLibrary,
                    icon = { Icon(Icons.Default.Image, null) },
                    label = { Text(stringResource(R.string.nav_images)) },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = onProjects,
                    icon = { Icon(Icons.Default.Folder, null) },
                    label = { Text(stringResource(R.string.nav_projects)) },
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = onHelp,
                    icon = { Icon(Icons.Default.MenuBook, null) },
                    label = { Text(stringResource(R.string.nav_help)) },
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = onSettings,
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text(stringResource(R.string.nav_settings)) },
                )
            }
        },
        content = content,
    )
}

@Composable
private fun SettingsScreen(
    viewModel: AppViewModel,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val settings = viewModel.appSettings
    var storageRefresh by remember { mutableStateOf(0) }
    val storage = remember(viewModel.images, viewModel.projects, storageRefresh) {
        viewModel.storageSummary()
    }
    val backCamera = stringResource(R.string.back_camera)
    val frontCamera = stringResource(R.string.front_camera)
    val otherCamera = stringResource(R.string.camera_number)
    val cameras = remember(backCamera, frontCamera, otherCamera) {
        val manager = context.getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager
        manager.cameraIdList.mapIndexed { index, id ->
            val facing = manager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.LENS_FACING)
            val label = when (facing) {
                CameraCharacteristics.LENS_FACING_BACK -> backCamera.format(index + 1)
                CameraCharacteristics.LENS_FACING_FRONT -> frontCamera.format(index + 1)
                else -> otherCamera.format(index + 1)
            }
            id to label
        }
    }
    fun save(updated: AppSettings) = viewModel.updateSettings(updated)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(stringResource(R.string.appearance), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.language), style = MaterialTheme.typography.bodySmall)
            val selectedLanguage = AppCompatDelegate.getApplicationLocales()
                .toLanguageTags()
                .substringBefore(',')
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedLanguage.isBlank(),
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.getEmptyLocaleList()
                            )
                        },
                        label = { Text(stringResource(R.string.language_system)) },
                    )
                }
                item {
                    FilterChip(
                        selected = selectedLanguage.startsWith("en"),
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags("en")
                            )
                        },
                        label = { Text(stringResource(R.string.language_english)) },
                    )
                }
                item {
                    FilterChip(
                        selected = selectedLanguage.startsWith("pl"),
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(
                                LocaleListCompat.forLanguageTags("pl")
                            )
                        },
                        label = { Text(stringResource(R.string.language_polish)) },
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.theme), style = MaterialTheme.typography.bodySmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = settings.themeMode == 0,
                        onClick = { save(settings.copy(themeMode = 0)) },
                        label = { Text(stringResource(R.string.theme_system)) },
                    )
                }
                item {
                    FilterChip(
                        selected = settings.themeMode == 1,
                        onClick = { save(settings.copy(themeMode = 1)) },
                        label = { Text(stringResource(R.string.theme_light)) },
                    )
                }
                item {
                    FilterChip(
                        selected = settings.themeMode == 2,
                        onClick = { save(settings.copy(themeMode = 2)) },
                        label = { Text(stringResource(R.string.theme_dark)) },
                    )
                }
            }
        }
        item {
            ToggleRow(stringResource(R.string.show_icon_hints), settings.showIconHints) {
                save(settings.copy(showIconHints = it))
            }
        }
        item {
            FilledTonalButton(
                onClick = { save(settings.copy(tutorialCompleted = false)) },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.show_tutorial_again))
            }
        }
        item {
            Text(stringResource(R.string.default_project_settings), fontWeight = FontWeight.SemiBold)
            Text(
                stringResource(R.string.default_project_settings_info),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        item {
            Text(stringResource(R.string.opacity_value, (settings.defaultOpacity * 100).toInt()))
            Slider(
                value = settings.defaultOpacity,
                onValueChange = { save(settings.copy(defaultOpacity = it)) },
            )
        }
        item {
            ToggleRow(stringResource(R.string.default_grid), settings.defaultGridEnabled) {
                save(settings.copy(defaultGridEnabled = it))
            }
        }
        if (settings.defaultGridEnabled) {
            item {
                Text(stringResource(R.string.grid_cells, settings.defaultGridDivisions))
                Slider(
                    value = settings.defaultGridDivisions.toFloat(),
                    onValueChange = {
                        save(settings.copy(defaultGridDivisions = it.toInt().coerceIn(2, 12)))
                    },
                    valueRange = 2f..12f,
                    steps = 9,
                )
            }
        }
        item {
            Text(stringResource(R.string.grid_color), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(
                    0xFFFFFFFF, 0xFFE0C8FF, 0xFFFF5252, 0xFFFFD740,
                    0xFF69F0AE, 0xFF40C4FF, 0xFF000000,
                ).forEach { value ->
                    Box(
                        Modifier.size(38.dp).background(Color(value), CircleShape)
                            .border(
                                if (settings.defaultGridColor == value) 3.dp else 1.dp,
                                if (value == 0xFF000000) Color.LightGray else Color.DarkGray,
                                CircleShape,
                            )
                            .clickable { save(settings.copy(defaultGridColor = value)) },
                    )
                }
            }
        }
        item {
            ToggleRow(stringResource(R.string.default_blink), settings.defaultBlinkEnabled) {
                save(settings.copy(defaultBlinkEnabled = it))
            }
        }
        item {
            ToggleRow(stringResource(R.string.default_timelapse), settings.defaultTimelapseEnabled) {
                save(settings.copy(defaultTimelapseEnabled = it))
            }
        }
        item {
            ToggleRow(stringResource(R.string.default_torch), settings.defaultTorchEnabled) {
                save(settings.copy(defaultTorchEnabled = it))
            }
        }
        item {
            Text(stringResource(R.string.default_lens), fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = settings.defaultCameraId == null,
                        onClick = { save(settings.copy(defaultCameraId = null)) },
                        label = { Text(stringResource(R.string.automatic)) },
                    )
                }
                items(cameras, key = { it.first }) { camera ->
                    FilterChip(
                        selected = settings.defaultCameraId == camera.first,
                        onClick = { save(settings.copy(defaultCameraId = camera.first)) },
                        label = { Text(camera.second) },
                    )
                }
            }
        }
        item {
            Text(stringResource(R.string.drawing_screen_information), fontWeight = FontWeight.SemiBold)
        }
        item {
            ToggleRow(stringResource(R.string.show_phone_level), settings.showLevelIndicator) {
                save(settings.copy(showLevelIndicator = it))
            }
        }
        item {
            ToggleRow(stringResource(R.string.movement_warning), settings.showMovementWarning) {
                save(settings.copy(showMovementWarning = it))
            }
        }
        item {
            ToggleRow(stringResource(R.string.show_device_monitor), settings.showDeviceMonitor) {
                save(settings.copy(showDeviceMonitor = it))
            }
        }
        item {
            FilledTonalButton(
                onClick = {
                    context.startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            android.net.Uri.parse("https://tracelens.kasztelan.me/privacy.php"),
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.OpenInNew, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.privacy_policy))
            }
        }
        item {
            Text(stringResource(R.string.app_storage), fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text(stringResource(R.string.storage_total, formatStorageBytes(storage.totalBytes)))
            Text(
                stringResource(
                    R.string.storage_details,
                    formatStorageBytes(storage.imagesBytes),
                    formatStorageBytes(storage.projectsBytes),
                    formatStorageBytes(storage.cacheBytes),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.safe_cleanup_info),
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            FilledTonalButton(
                onClick = {
                    viewModel.cleanupStorage()
                    storageRefresh++
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.CleaningServices, null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.safe_cleanup))
            }
        }
    }
}

private fun formatStorageBytes(bytes: Long): String = when {
    bytes >= 1024L * 1024L * 1024L -> "%.2f GB".format(bytes / (1024.0 * 1024.0 * 1024.0))
    bytes >= 1024L * 1024L -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> "%.1f kB".format(bytes / 1024.0)
    else -> "$bytes B"
}

private enum class LibrarySort(@StringRes val labelRes: Int) {
    Favorites(R.string.sort_favorites),
    Name(R.string.sort_name),
    Added(R.string.sort_added),
    LastUsed(R.string.sort_last_used),
}

@Composable
private fun LibraryScreen(
    viewModel: AppViewModel,
    modifier: Modifier,
    showSearch: Boolean,
    showFilters: Boolean,
    onEdit: (LibraryImage) -> Unit,
    onOpenPatterns: () -> Unit,
    onOpenProject: (TraceProject) -> Unit,
) {
    var imageToRename by remember { mutableStateOf<LibraryImage?>(null) }
    var imageForProject by remember { mutableStateOf<LibraryImage?>(null) }
    var imageToDelete by remember { mutableStateOf<LibraryImage?>(null) }
    var imageCollections by remember { mutableStateOf<LibraryImage?>(null) }
    var showCreateCollection by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var selectedCollection by remember { mutableStateOf<String?>(null) }
    var sort by remember { mutableStateOf(LibrarySort.Favorites) }
    val gridState = rememberLazyGridState()
    LaunchedEffect(showSearch, showFilters) {
        if (showSearch || showFilters) gridState.animateScrollToItem(0)
    }
    val collections = viewModel.collectionNames
    val visibleImages = viewModel.images
        .filter { it.name.contains(query.trim(), ignoreCase = true) }
        .filter { selectedCollection == null || selectedCollection in it.collectionNames }
        .let { images ->
            when (sort) {
                LibrarySort.Favorites -> images.sortedWith(
                    compareByDescending<LibraryImage> { it.favorite }.thenByDescending { it.createdAt }
                )
                LibrarySort.Name -> images.sortedBy { it.name.lowercase() }
                LibrarySort.Added -> images.sortedByDescending { it.createdAt }
                LibrarySort.LastUsed -> images.sortedByDescending { it.lastUsedAt }
            }
        }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.importImage(uri, onEdit)
    }
    Box(modifier.fillMaxSize()) {
        if (viewModel.images.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.add_first_image),
                detail = stringResource(R.string.choose_image_detail),
                button = stringResource(R.string.choose_image),
                onClick = { picker.launch(arrayOf("image/png", "image/jpeg")) },
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                state = gridState,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp, 12.dp, 12.dp, 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (showSearch || showFilters) item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (showSearch) {
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text(stringResource(R.string.search_image)) },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                singleLine = true,
                            )
                        }
                        if (showFilters) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                item {
                                    FilterChip(
                                        selected = selectedCollection == null,
                                        onClick = { selectedCollection = null },
                                        label = { Text(stringResource(R.string.all)) },
                                    )
                                }
                                item {
                                    IconButton(onClick = { showCreateCollection = true }) {
                                        Icon(Icons.Default.Add, stringResource(R.string.create_collection))
                                    }
                                }
                                items(collections, key = { it }) { collection ->
                                    FilterChip(
                                        selected = selectedCollection == collection,
                                        onClick = {
                                            selectedCollection =
                                                if (selectedCollection == collection) null else collection
                                        },
                                        label = { Text(collection) },
                                    )
                                }
                            }
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(Icons.Default.Sort, null)
                                LazyRow(
                                    modifier = Modifier.weight(1f),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    items(LibrarySort.entries) { option ->
                                        FilterChip(
                                            selected = sort == option,
                                            onClick = { sort = option },
                                            label = { Text(stringResource(option.labelRes)) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                items(visibleImages, key = { it.id }) { image ->
                    ImageCard(
                        image = image,
                        bitmap = viewModel.bitmap(image),
                        onFavorite = { viewModel.toggleFavorite(image) },
                        onRename = { imageToRename = image },
                        onEdit = { onEdit(image) },
                        onCreateProject = { imageForProject = image },
                        onCollections = { imageCollections = image },
                        onDelete = { imageToDelete = image },
                    )
                }
            }
            FilledTonalButton(
                onClick = onOpenPatterns,
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
            ) {
                Icon(Icons.Default.Star, null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.open_starter_patterns))
            }
            FloatingActionButton(
                onClick = { picker.launch(arrayOf("image/png", "image/jpeg")) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            ) { Icon(Icons.Default.Add, stringResource(R.string.add_image)) }
        }
        if (viewModel.images.isEmpty()) {
            FilledTonalButton(
                onClick = onOpenPatterns,
                modifier = Modifier.align(Alignment.BottomStart).padding(20.dp),
            ) {
                Icon(Icons.Default.Star, null)
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.open_starter_patterns))
            }
        }
    }
    imageCollections?.let { image ->
        ImageCollectionsDialog(
            image = image,
            availableCollections = collections,
            onDismiss = { imageCollections = null },
            onSave = {
                viewModel.updateImageCollections(image, it)
                imageCollections = null
            },
        )
    }
    if (showCreateCollection) {
        CreateCollectionDialog(
            onDismiss = { showCreateCollection = false },
            onCreate = {
                if (viewModel.createCollection(it)) showCreateCollection = false
            },
        )
    }
    imageToRename?.let { image ->
        RenameImageDialog(
            image = image,
            onDismiss = { imageToRename = null },
            onRename = { newName ->
                viewModel.renameImage(image, newName)
                imageToRename = null
            },
        )
    }
    imageForProject?.let { image ->
        CreateProjectDialog(
            images = listOf(image),
            fixedImage = image,
            onDismiss = { imageForProject = null },
            onCreate = { name, selectedImage ->
                imageForProject = null
                onOpenProject(viewModel.createProject(name, selectedImage))
            },
        )
    }
    imageToDelete?.let { image ->
        val linkedProjects = viewModel.projects.count { it.imageId == image.id }
        AlertDialog(
            onDismissRequest = { imageToDelete = null },
            title = { Text(stringResource(R.string.delete_image_question)) },
            text = {
                Text(
                    if (linkedProjects == 0) {
                        stringResource(R.string.delete_image_detail, image.name)
                    } else {
                        stringResource(R.string.delete_linked_image_detail, image.name, linkedProjects)
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteImage(image)
                    imageToDelete = null
                }) { Text(stringResource(R.string.delete_image)) }
            },
            dismissButton = {
                TextButton(onClick = { imageToDelete = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@Composable
private fun StarterPatternsGalleryScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onProjectCreated: (TraceProject) -> Unit,
) {
    val context = LocalContext.current
    val locale = LocalConfiguration.current.locales[0]
    val patterns = remember { pl.tracelens.app.image.StarterPatterns.load(context) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var imageForProject by remember { mutableStateOf<LibraryImage?>(null) }
    val patternSnackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.starter_patterns))
                        Text(
                            pluralStringResource(
                                R.plurals.patterns_count_plural,
                                patterns.size,
                                patterns.size,
                            ),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                },
            )
        },
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(150.dp),
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(patterns, key = { it.id }) { pattern ->
                val index = patterns.indexOf(pattern)
                val name = pl.tracelens.app.image.StarterPatterns.localizedName(pattern, locale)
                val preview = remember(pattern.id) {
                    pl.tracelens.app.image.StarterPatterns.bitmap(context, pattern)
                }
                Card(onClick = { selectedIndex = index }) {
                    Column {
                        Box(
                            Modifier.fillMaxWidth().height(190.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (preview != null) {
                                Image(
                                    preview.asImageBitmap(),
                                    name,
                                    Modifier.fillMaxSize().padding(8.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                        Text(
                            name,
                            Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            stringResource(
                                R.string.pattern_author_license,
                                pattern.author,
                                pattern.license,
                            ),
                            Modifier.padding(horizontal = 10.dp).padding(bottom = 10.dp),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }

    selectedIndex?.let { initialIndex ->
        if (patterns.isNotEmpty()) {
            val pagerState = rememberPagerState(
                initialPage = initialIndex.coerceIn(0, patterns.lastIndex)
            ) { patterns.size }
            Dialog(
                onDismissRequest = { selectedIndex = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                ),
            ) {
                Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) { page ->
                            val pattern = patterns[page]
                            val name = pl.tracelens.app.image.StarterPatterns
                                .localizedName(pattern, locale)
                            val bitmap = remember(pattern.id) {
                                pl.tracelens.app.image.StarterPatterns.bitmap(context, pattern)
                            }
                            if (bitmap != null) {
                                Image(
                                    bitmap.asImageBitmap(),
                                    name,
                                    Modifier.fillMaxSize().padding(24.dp),
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                        IconButton(
                            onClick = { selectedIndex = null },
                            modifier = Modifier.align(Alignment.TopStart)
                                .background(Color.Black.copy(alpha = 0.55f)),
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                stringResource(R.string.close),
                                tint = Color.White,
                            )
                        }
                        SnackbarHost(
                            hostState = patternSnackbar,
                            modifier = Modifier.align(Alignment.TopCenter)
                                .padding(top = 56.dp),
                        )
                        Column(
                            Modifier.align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.72f))
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            val pattern = patterns[pagerState.currentPage]
                            val name = pl.tracelens.app.image.StarterPatterns
                                .localizedName(pattern, locale)
                            val addedMessage = stringResource(R.string.pattern_added, name)
                            val duplicateMessage = stringResource(
                                R.string.pattern_already_in_library,
                                name,
                            )
                            Text(name, color = Color.White, style = MaterialTheme.typography.titleLarge)
                            Text(
                                stringResource(
                                    R.string.pattern_author_license,
                                    pattern.author,
                                    pattern.license,
                                ),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                "${pagerState.currentPage + 1} / ${patterns.size}",
                                color = Color.White,
                            )
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                FilledTonalButton(
                                    onClick = {
                                        viewModel.addStarterPattern(pattern, name) { _, added ->
                                            scope.launch {
                                                patternSnackbar.showSnackbar(
                                                    if (added) addedMessage else duplicateMessage
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.add_to_library))
                                }
                                Button(
                                    onClick = {
                                        viewModel.addStarterPattern(pattern, name) { image, _ ->
                                            imageForProject = image
                                            selectedIndex = null
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(Icons.Default.Add, null)
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.create_project_from_pattern))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    imageForProject?.let { image ->
        CreateProjectDialog(
            images = listOf(image),
            fixedImage = image,
            onDismiss = { imageForProject = null },
            onCreate = { name, selectedImage ->
                imageForProject = null
                onProjectCreated(viewModel.createProject(name, selectedImage))
            },
        )
    }
}

@Composable
private fun ImageCard(
    image: LibraryImage,
    bitmap: Bitmap?,
    onFavorite: () -> Unit,
    onRename: () -> Unit,
    onEdit: () -> Unit,
    onCreateProject: () -> Unit,
    onCollections: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(onClick = onEdit) {
        Column {
            Box(
                Modifier.fillMaxWidth().height(150.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center,
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap.asImageBitmap(),
                        image.name,
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit,
                    )
                } else Icon(Icons.Default.Image, null, Modifier.size(48.dp))
                IconButton(onClick = onFavorite, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(
                        if (image.favorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        stringResource(R.string.favorites),
                        tint = if (image.favorite) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp)) {
                Text(
                    image.name,
                    Modifier.fillMaxWidth().height(52.dp).clickable(onClick = onRename),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 28.dp) {
                        IconButton(onClick = onRename, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.DriveFileRenameOutline, stringResource(R.string.rename_image), Modifier.size(18.dp))
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, stringResource(R.string.edit_image), Modifier.size(18.dp))
                        }
                        IconButton(onClick = onCreateProject, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, stringResource(R.string.create_project_from_image), Modifier.size(18.dp))
                        }
                        IconButton(onClick = onCollections, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Folder, stringResource(R.string.image_collections), Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, stringResource(R.string.remove_image_library), Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ImageCollectionsDialog(
    image: LibraryImage,
    availableCollections: List<String>,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit,
) {
    var selected by remember(image.id) { mutableStateOf(image.collectionNames.toSet()) }
    var newCollection by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.image_collections)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(image.name, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (availableCollections.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableCollections, key = { it }) { collection ->
                            FilterChip(
                                selected = collection in selected,
                                onClick = {
                                    selected = if (collection in selected) {
                                        selected - collection
                                    } else {
                                        selected + collection
                                    }
                                },
                                label = { Text(collection) },
                            )
                        }
                    }
                }
                TextField(
                    value = newCollection,
                    onValueChange = { newCollection = it },
                    label = { Text(stringResource(R.string.new_collection)) },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val normalized = newCollection.trim()
                                if (normalized.isNotEmpty()) {
                                    selected = selected + normalized
                                    newCollection = ""
                                }
                            },
                        ) { Icon(Icons.Default.Add, stringResource(R.string.create_collection)) }
                    },
                )
                if (selected.isEmpty()) {
                    Text(stringResource(R.string.image_no_collection), style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        stringResource(R.string.selected_collections, selected.sorted().joinToString()),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val pending = newCollection.trim()
                onSave((selected + pending.takeIf { it.isNotEmpty() }.orEmpty()).filter { it.isNotEmpty() })
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_collection)) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.collection_name)) },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank(),
            ) { Text(stringResource(R.string.create)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun RenameImageDialog(
    image: LibraryImage,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
) {
    val initialName = remember(image.id) {
        image.name.substringBeforeLast('.', image.name)
    }
    var name by remember(image.id) { mutableStateOf(initialName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_image)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.library_name)) },
                    placeholder = { Text(stringResource(R.string.name_example)) },
                    singleLine = true,
                )
                Text(
                    stringResource(R.string.extension_info),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRename(name) },
                enabled = name.trim().isNotEmpty(),
            ) { Text(stringResource(R.string.save_name)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun EditorScreen(
    viewModel: AppViewModel,
    image: LibraryImage,
    onBack: () -> Unit,
) {
    val original = remember(image.id) { viewModel.bitmap(image, processed = false) }
    var filters by remember(image.id) { mutableStateOf(image.filters) }
    var showCropDialog by remember { mutableStateOf(false) }
    var eraserMode by remember { mutableStateOf(false) }
    var eraserSize by remember { mutableStateOf(0.04f) }
    var showOriginal by remember { mutableStateOf(false) }
    val editorSnackbar = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        snackbarHost = { SnackbarHost(editorSnackbar) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.image_editor)) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, stringResource(R.string.back)) } },
                actions = {
                    TextButton(
                        enabled = original != null,
                        onClick = { viewModel.updateFilters(image, filters) { onBack() } },
                    ) { Text(stringResource(R.string.save)) }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (original != null) {
                ColorPickImage(
                    bitmap = original,
                    filters = filters,
                    showOriginal = showOriginal,
                    eraserMode = eraserMode,
                    eraserSize = eraserSize,
                    onColor = { filters = filters.copy(removeColor = it) },
                    onErase = { points ->
                        filters = filters.copy(eraserPoints = filters.eraserPoints + points)
                    },
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                FilledTonalButton(
                    onClick = { eraserMode = !eraserMode },
                    modifier = Modifier.weight(1f),
                ) {
                    Icon(Icons.Default.CleaningServices, null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(if (eraserMode) R.string.disable_eraser else R.string.eraser))
                }
                Row(
                    modifier = Modifier.size(48.dp)
                        .background(
                            MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(24.dp),
                        )
                        .pointerInput(Unit) {
                            awaitEachGesture {
                                awaitFirstDown(requireUnconsumed = false)
                                showOriginal = true
                                waitForUpOrCancellation()
                                showOriginal = false
                            }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Icon(Icons.Default.Compare, stringResource(R.string.compare_original))
                }
            }
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (eraserMode) {
                    item {
                        Text(stringResource(R.string.eraser_size, (eraserSize * 100).toInt()))
                        Slider(
                            value = eraserSize,
                            onValueChange = { eraserSize = it },
                            valueRange = 0.01f..0.15f,
                        )
                        if (filters.eraserPoints.isNotEmpty()) {
                            TextButton(onClick = { filters = filters.copy(eraserPoints = emptyList()) }) {
                                Text(stringResource(R.string.clear_eraser_marks, filters.eraserPoints.size))
                            }
                        }
                    }
                }
            item {
                FilledTonalButton(
                    onClick = { showCropDialog = true },
                    enabled = original != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(Icons.Default.Crop, null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (filters.isCropped()) stringResource(R.string.change_crop)
                        else stringResource(R.string.crop_image)
                    )
                }
            }
            item {
                Text(stringResource(R.string.rotation_value, filters.rotationDegrees.toInt()))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilledTonalButton(
                        onClick = {
                            filters = filters.copy(
                                rotationDegrees = normalizeRotation(filters.rotationDegrees - 90f)
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.RotateLeft, stringResource(R.string.rotate_left))
                    }
                    FilledTonalButton(
                        onClick = {
                            original?.let {
                                filters = filters.copy(
                                    rotationDegrees = pl.tracelens.app.image.ImageProcessor
                                        .detectStraighteningAngle(it)
                                )
                            }
                        },
                        enabled = original != null,
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.Straighten, stringResource(R.string.straighten_image))
                    }
                    FilledTonalButton(
                        onClick = {
                            filters = filters.copy(
                                rotationDegrees = normalizeRotation(filters.rotationDegrees + 90f)
                            )
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Default.RotateRight, stringResource(R.string.rotate_right))
                    }
                }
                Slider(
                    value = filters.rotationDegrees.coerceIn(-15f, 15f),
                    onValueChange = { filters = filters.copy(rotationDegrees = it) },
                    valueRange = -15f..15f,
                    steps = 29,
                )
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EditorFilterButton(
                        icon = Icons.Default.MonochromePhotos,
                        label = stringResource(R.string.grayscale),
                        selected = filters.grayscale,
                    ) { filters = filters.copy(grayscale = !filters.grayscale) }
                    EditorFilterButton(
                        icon = Icons.Default.AutoFixHigh,
                        label = stringResource(R.string.outline_sketch),
                        selected = filters.contour,
                    ) { filters = filters.copy(contour = !filters.contour) }
                    EditorFilterButton(
                        icon = Icons.Default.InvertColors,
                        label = stringResource(R.string.invert_colors),
                        selected = filters.invert,
                    ) { filters = filters.copy(invert = !filters.invert) }
                    EditorFilterButton(
                        icon = Icons.Default.CleaningServices,
                        label = stringResource(R.string.auto_remove_background),
                        selected = filters.autoRemoveBackground,
                    ) {
                        filters = filters.copy(autoRemoveBackground = !filters.autoRemoveBackground)
                    }
                    EditorFilterButton(
                        icon = Icons.Default.ColorLens,
                        label = stringResource(R.string.simplify_colors),
                        selected = filters.simplifyLevels >= 2,
                    ) {
                        filters = filters.copy(
                            simplifyLevels = if (filters.simplifyLevels >= 2) 0 else 8
                        )
                    }
                }
            }
            item {
                val simplifyEnabled = filters.simplifyLevels >= 2
                if (simplifyEnabled) {
                    Text(stringResource(R.string.levels_per_channel, filters.simplifyLevels))
                    Slider(
                        value = filters.simplifyLevels.toFloat(),
                        onValueChange = {
                            filters = filters.copy(simplifyLevels = it.toInt().coerceIn(2, 16))
                        },
                        valueRange = 2f..16f,
                        steps = 13,
                    )
                }
            }
            item {
                Text(stringResource(R.string.artifact_cleanup_value, filters.artifactCleanup))
                Slider(
                    value = filters.artifactCleanup.toFloat(),
                    onValueChange = {
                        filters = filters.copy(artifactCleanup = it.toInt().coerceIn(0, 100))
                    },
                    valueRange = 0f..100f,
                    steps = 19,
                )
            }
            item {
                Text(stringResource(R.string.contrast_value, (filters.contrast * 100).toInt()))
                Slider(
                    value = filters.contrast,
                    onValueChange = { filters = filters.copy(contrast = it) },
                    valueRange = 0.5f..3f,
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.selected_remove_color))
                        Text(
                            if (filters.removeColor == null) {
                                stringResource(R.string.tap_select_remove_color)
                            } else {
                                stringResource(R.string.tap_change_remove_color)
                            },
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Box(
                        Modifier.size(42.dp)
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                            .border(3.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(10.dp))
                            .padding(4.dp)
                            .background(
                                filters.removeColor?.let { Color(it) } ?: Color.Transparent,
                                RoundedCornerShape(5.dp),
                            )
                    )
                    if (filters.removeColor != null) {
                        TextButton(onClick = { filters = filters.copy(removeColor = null) }) {
                            Text(stringResource(R.string.clear_selection))
                        }
                    }
                }
            }
            item {
                Text(stringResource(R.string.color_tolerance, (filters.colorTolerance * 100).toInt()))
                Slider(
                    value = filters.colorTolerance,
                    onValueChange = { filters = filters.copy(colorTolerance = it) },
                    valueRange = 0f..0.5f,
                )
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    FilledTonalButton(
                        onClick = {
                            viewModel.exportPreparedImage(image, filters) { resultMessage ->
                                coroutineScope.launch {
                                    editorSnackbar.showSnackbar(resultMessage)
                                }
                            }
                        },
                        enabled = original != null,
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) {
                        Icon(Icons.Default.SaveAlt, null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.export))
                    }
                    Button(
                        onClick = { viewModel.updateFilters(image, filters) { onBack() } },
                        enabled = original != null,
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.save))
                    }
                }
            }
            }
        }
    }
    if (showCropDialog && original != null) {
        CropImageDialog(
            bitmap = original,
            filters = filters,
            onDismiss = { showCropDialog = false },
            onApply = {
                filters = it
                showCropDialog = false
            },
        )
    }
}

@Composable
private fun EditorFilterButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    TooltipBox(
        positionProvider = androidx.compose.material3.TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState(),
    ) {
        Surface(
            color = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            shape = RoundedCornerShape(14.dp),
        ) {
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = if (selected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
private fun ColorPickImage(
    bitmap: Bitmap,
    filters: ImageFilters,
    showOriginal: Boolean,
    eraserMode: Boolean,
    eraserSize: Float,
    onColor: (Int) -> Unit,
    onErase: (List<EraserPoint>) -> Unit,
) {
    val processed = remember(bitmap, filters) {
        pl.tracelens.app.image.ImageProcessor.process(bitmap, filters)
    }
    val originalCrop = remember(bitmap, filters.cropLeft, filters.cropTop, filters.cropRight, filters.cropBottom) {
        pl.tracelens.app.image.ImageProcessor.crop(bitmap, filters)
    }
    val preview = if (showOriginal) originalCrop else processed
    Image(
        preview.asImageBitmap(),
        stringResource(R.string.preview),
        Modifier.fillMaxWidth().height(260.dp).padding(horizontal = 16.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .pointerInput(bitmap, filters, eraserMode, eraserSize) {
                fun normalized(position: androidx.compose.ui.geometry.Offset): Pair<Float, Float>? {
                    val bitmapRatio = preview.width.toFloat() / preview.height
                    val viewRatio = size.width.toFloat() / size.height
                    val drawnWidth: Float
                    val drawnHeight: Float
                    val left: Float
                    val top: Float
                    if (bitmapRatio > viewRatio) {
                        drawnWidth = size.width.toFloat()
                        drawnHeight = drawnWidth / bitmapRatio
                        left = 0f
                        top = (size.height - drawnHeight) / 2f
                    } else {
                        drawnHeight = size.height.toFloat()
                        drawnWidth = drawnHeight * bitmapRatio
                        left = (size.width - drawnWidth) / 2f
                        top = 0f
                    }
                    return if (position.x in left..(left + drawnWidth) && position.y in top..(top + drawnHeight)) {
                        ((position.x - left) / drawnWidth).coerceIn(0f, 1f) to
                            ((position.y - top) / drawnHeight).coerceIn(0f, 1f)
                    } else null
                }
                if (eraserMode) {
                    val stroke = mutableListOf<EraserPoint>()
                    detectDragGestures(
                        onDragStart = { position ->
                            stroke.clear()
                            normalized(position)?.let { (x, y) ->
                                stroke += EraserPoint(x, y, eraserSize)
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            normalized(change.position)?.let { (x, y) ->
                                val point = EraserPoint(x, y, eraserSize)
                                val previous = stroke.lastOrNull()
                                if (previous == null ||
                                    kotlin.math.abs(previous.x - x) + kotlin.math.abs(previous.y - y) > eraserSize / 3f
                                ) {
                                    stroke += point
                                }
                            }
                        },
                        onDragEnd = {
                            if (stroke.isNotEmpty()) onErase(stroke.toList())
                            stroke.clear()
                        },
                        onDragCancel = { stroke.clear() },
                    )
                } else {
                    detectTapGestures { position ->
                        normalized(position)?.let { (normalizedX, normalizedY) ->
                        val sourceX = filters.cropLeft +
                            normalizedX * (filters.cropRight - filters.cropLeft)
                        val sourceY = filters.cropTop +
                            normalizedY * (filters.cropBottom - filters.cropTop)
                        val x = (sourceX * bitmap.width).toInt()
                            .coerceIn(0, bitmap.width - 1)
                        val y = (sourceY * bitmap.height).toInt()
                            .coerceIn(0, bitmap.height - 1)
                        onColor(bitmap.getPixel(x, y))
                        }
                    }
                }
            },
        contentScale = ContentScale.Fit,
    )
}

@Composable
private fun CropImageDialog(
    bitmap: Bitmap,
    filters: ImageFilters,
    onDismiss: () -> Unit,
    onApply: (ImageFilters) -> Unit,
) {
    var horizontal by remember {
        mutableStateOf(filters.cropLeft..filters.cropRight)
    }
    var vertical by remember {
        mutableStateOf(filters.cropTop..filters.cropBottom)
    }
    val cropFilters = filters.copy(
        cropLeft = horizontal.start,
        cropRight = horizontal.endInclusive,
        cropTop = vertical.start,
        cropBottom = vertical.endInclusive,
    )
    val preview = remember(bitmap, cropFilters) {
        pl.tracelens.app.image.ImageProcessor.crop(bitmap, cropFilters)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.cropping)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(
                    preview.asImageBitmap(),
                    stringResource(R.string.crop_preview),
                    Modifier.fillMaxWidth().height(260.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
                Text(stringResource(R.string.horizontal_range))
                RangeSlider(
                    value = horizontal,
                    onValueChange = {
                        if (it.endInclusive - it.start >= 0.05f) horizontal = it
                    },
                    valueRange = 0f..1f,
                )
                Text(stringResource(R.string.vertical_range))
                RangeSlider(
                    value = vertical,
                    onValueChange = {
                        if (it.endInclusive - it.start >= 0.05f) vertical = it
                    },
                    valueRange = 0f..1f,
                )
                Text(
                    stringResource(R.string.crop_preview_hint),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onApply(cropFilters) }) { Text(stringResource(R.string.apply_crop)) }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    horizontal = 0f..1f
                    vertical = 0f..1f
                }) { Text(stringResource(R.string.full_image)) }
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
            }
        },
    )
}

private fun ImageFilters.isCropped(): Boolean =
    cropLeft > 0.001f || cropTop > 0.001f || cropRight < 0.999f || cropBottom < 0.999f

private fun normalizeRotation(value: Float): Float {
    var normalized = value % 360f
    if (normalized > 180f) normalized -= 360f
    if (normalized < -180f) normalized += 360f
    return normalized
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onChange(!checked) }.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, Modifier.weight(1f))
        Switch(checked, onCheckedChange = onChange)
    }
}

@Composable
private fun ProjectsScreen(
    viewModel: AppViewModel,
    modifier: Modifier,
    onOpen: (TraceProject) -> Unit,
    onGallery: (TraceProject) -> Unit,
) {
    val context = LocalContext.current
    var createDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<TraceProject?>(null) }
    var projectToRename by remember { mutableStateOf<TraceProject?>(null) }
    var projectToExport by remember { mutableStateOf<TraceProject?>(null) }
    var transferServer by remember { mutableStateOf<LocalTransferServer?>(null) }
    var showArchived by remember { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/vnd.tracelens.project"),
    ) { uri ->
        val project = projectToExport
        projectToExport = null
        if (uri != null && project != null) viewModel.exportProject(project, uri)
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) viewModel.importProject(uri)
    }
    val visibleProjects = viewModel.projects.filter { it.archived == showArchived }
    val lastActiveProject = viewModel.projects.firstOrNull { !it.archived }
    Box(modifier.fillMaxSize()) {
        if (viewModel.projects.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.no_projects),
                detail = if (viewModel.images.isEmpty()) {
                    stringResource(R.string.empty_projects_no_images)
                } else {
                    stringResource(R.string.empty_projects)
                },
                button = stringResource(R.string.new_project),
                enabled = viewModel.images.isNotEmpty(),
                onClick = { createDialog = true },
                secondaryButton = stringResource(R.string.import_project),
                onSecondaryClick = {
                    importLauncher.launch(
                        arrayOf(
                            "application/vnd.tracelens.project",
                            "application/zip",
                            "application/octet-stream",
                        )
                    )
                },
            )
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp, 12.dp, 12.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = !showArchived,
                            onClick = { showArchived = false },
                            label = { Text(stringResource(R.string.active)) },
                        )
                        FilterChip(
                            selected = showArchived,
                            onClick = { showArchived = true },
                            label = { Text(stringResource(R.string.archive)) },
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(
                            onClick = {
                                importLauncher.launch(
                                    arrayOf(
                                        "application/vnd.tracelens.project",
                                        "application/zip",
                                        "application/octet-stream",
                                    )
                                )
                            },
                        ) {
                            Icon(Icons.Default.FileUpload, stringResource(R.string.import_project))
                        }
                    }
                }
                if (!showArchived && lastActiveProject != null) {
                    item {
                        FilledTonalButton(
                            onClick = { onOpen(lastActiveProject) },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                        ) {
                            Icon(Icons.Default.PlayArrow, null)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                stringResource(R.string.resume_project, lastActiveProject.name),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (visibleProjects.isEmpty()) {
                    item {
                        Text(
                            stringResource(
                                if (showArchived) R.string.archive_empty else R.string.no_active_projects
                            ),
                            modifier = Modifier.padding(vertical = 24.dp),
                        )
                    }
                }
                items(visibleProjects, key = { it.id }) { project ->
                    val image = viewModel.imageForProject(project)
                    Card(onClick = { onOpen(project) }, modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            val previewPhoto = project.coverPhoto ?: project.progressPhotos.lastOrNull()
                            val bitmap = if (previewPhoto != null) {
                                remember(project.id, previewPhoto) {
                                    viewModel.progressBitmap(project.id, previewPhoto, 320)
                                }
                            } else image?.let(viewModel::bitmap)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap.asImageBitmap(), null,
                                        Modifier.size(88.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                                        contentScale = ContentScale.Fit,
                                    )
                                } else Icon(Icons.Default.Image, null, Modifier.size(88.dp))
                                Spacer(Modifier.width(14.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        project.name,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Text(
                                        stringResource(R.string.progress_photos_count, project.progressPhotos.size),
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                Icon(Icons.Default.PlayArrow, stringResource(R.string.open))
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                IconButton(
                                    onClick = { onGallery(project) },
                                    enabled = project.progressPhotos.isNotEmpty(),
                                ) { Icon(Icons.Default.PhotoLibrary, stringResource(R.string.progress_photos_count, project.progressPhotos.size)) }
                                IconButton(onClick = { projectToRename = project }) {
                                    Icon(Icons.Default.DriveFileRenameOutline, stringResource(R.string.rename_project))
                                }
                                IconButton(onClick = { viewModel.toggleProjectArchive(project) }) {
                                    Icon(
                                        if (project.archived) Icons.Default.Unarchive else Icons.Default.Archive,
                                        stringResource(if (project.archived) R.string.restore_project else R.string.archive_project),
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        projectToExport = project
                                        exportLauncher.launch("${project.name}.tracelens")
                                    },
                                ) { Icon(Icons.Default.FileDownload, stringResource(R.string.export_project)) }
                                IconButton(
                                    onClick = {
                                        viewModel.prepareLocalTransfer(project) {
                                            transferServer?.close()
                                            transferServer = it
                                        }
                                    },
                                ) { Icon(Icons.Default.QrCode, stringResource(R.string.transfer_qr)) }
                                IconButton(onClick = { projectToDelete = project }) {
                                    Icon(Icons.Default.Delete, stringResource(R.string.delete_project))
                                }
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { createDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            ) { Icon(Icons.Default.Add, stringResource(R.string.new_project)) }
        }
    }
    projectToRename?.let { project ->
        RenameProjectDialog(
            project = project,
            onDismiss = { projectToRename = null },
            onRename = {
                viewModel.renameProject(project, it)
                projectToRename = null
            },
        )
    }
    if (createDialog) {
        ProjectImageSelectorDialog(
            viewModel = viewModel,
            onDismiss = { createDialog = false },
            onCreate = { name, image ->
                createDialog = false
                onOpen(viewModel.createProject(name, image))
            },
        )
    }
    projectToDelete?.let { project ->
        AlertDialog(
            onDismissRequest = { projectToDelete = null },
            title = { Text(stringResource(R.string.delete_project_question)) },
            text = {
                Text(
                    stringResource(R.string.delete_project_detail, project.name)
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteProject(project)
                    projectToDelete = null
                }) { Text(stringResource(R.string.delete_project)) }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
    transferServer?.let { server ->
        LocalTransferDialog(
            server = server,
            onDismiss = {
                server.close()
                transferServer = null
            },
        )
    }
}

@Composable
private fun LocalTransferDialog(server: LocalTransferServer, onDismiss: () -> Unit) {
    val qr = remember(server.url) {
        val matrix = MultiFormatWriter().encode(server.url, BarcodeFormat.QR_CODE, 700, 700)
        Bitmap.createBitmap(700, 700, Bitmap.Config.RGB_565).apply {
            for (y in 0 until 700) for (x in 0 until 700) {
                setPixel(x, y, if (matrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE)
            }
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.transfer_project)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(qr.asImageBitmap(), stringResource(R.string.transfer_qr), Modifier.fillMaxWidth())
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.transfer_scan))
                Text(stringResource(R.string.transfer_keep_open), style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.finish)) } },
    )
}

@Composable
private fun RenameProjectDialog(
    project: TraceProject,
    onDismiss: () -> Unit,
    onRename: (String) -> Unit,
) {
    var name by remember(project.id) { mutableStateOf(project.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_project)) },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.project_name)) },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(onClick = { onRename(name) }, enabled = name.trim().isNotEmpty()) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

private enum class ProjectImageFilter {
    All,
    Favorites,
    Recent,
}

@Composable
private fun ProjectImageSelectorDialog(
    viewModel: AppViewModel,
    onDismiss: () -> Unit,
    onCreate: (String, LibraryImage) -> Unit,
) {
    var projectName by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ProjectImageFilter.All) }
    var selected by remember { mutableStateOf<LibraryImage?>(null) }
    val visibleImages = remember(viewModel.images, query, filter) {
        val matching = viewModel.images.filter { it.name.contains(query.trim(), ignoreCase = true) }
        when (filter) {
            ProjectImageFilter.All -> matching
            ProjectImageFilter.Favorites -> matching.filter { it.favorite }
            ProjectImageFilter.Recent -> matching.sortedByDescending { it.createdAt }.take(30)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                TopAppBar(
                    title = { Text(stringResource(R.string.new_project)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.cancel))
                        }
                    },
                )
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    TextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.project_name)) },
                        singleLine = true,
                    )
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.search_image_by_name)) },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        singleLine = true,
                    )
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilterChip(
                            selected = filter == ProjectImageFilter.All,
                            onClick = { filter = ProjectImageFilter.All },
                            label = { Text(stringResource(R.string.all)) },
                        )
                        FilterChip(
                            selected = filter == ProjectImageFilter.Favorites,
                            onClick = { filter = ProjectImageFilter.Favorites },
                            label = { Text(stringResource(R.string.favorites)) },
                        )
                        FilterChip(
                            selected = filter == ProjectImageFilter.Recent,
                            onClick = { filter = ProjectImageFilter.Recent },
                            label = { Text(stringResource(R.string.recent)) },
                        )
                    }
                }
                if (visibleImages.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_matching_images))
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(120.dp),
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(visibleImages, key = { it.id }) { image ->
                            val isSelected = selected?.id == image.id
                            Card(
                                onClick = { selected = image },
                                modifier = Modifier.border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                ),
                            ) {
                                Box(
                                    Modifier.fillMaxWidth().height(110.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    viewModel.thumbnail(image)?.let { bitmap ->
                                        Image(
                                            bitmap.asImageBitmap(),
                                            image.name,
                                            Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Fit,
                                        )
                                    }
                                    if (image.favorite) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            "Ulubiony",
                                            Modifier.align(Alignment.TopStart).padding(6.dp),
                                            tint = Color(0xFFFFB300),
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            "Wybrany obraz",
                                            Modifier.align(Alignment.TopEnd).padding(6.dp),
                                            tint = MaterialTheme.colorScheme.primary,
                                        )
                                    }
                                }
                                Text(
                                    image.name,
                                    Modifier.fillMaxWidth().height(48.dp).padding(6.dp),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
                Surface(shadowElevation = 8.dp) {
                    Row(
                        Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            selected?.let { stringResource(R.string.selected_image, it.name) }
                                ?: stringResource(R.string.choose_prepared_image),
                            Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
                        Button(
                            onClick = { selected?.let { onCreate(projectName, it) } },
                            enabled = selected != null,
                        ) { Text(stringResource(R.string.create)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProjectGalleryScreen(
    viewModel: AppViewModel,
    initialProject: TraceProject,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val noPhotoAppMessage = stringResource(R.string.no_photo_app)
    var project by remember(initialProject.id) { mutableStateOf(initialProject) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var photoToDelete by remember { mutableStateOf<String?>(null) }
    var showSocialExport by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project.name)
                        Text(
                            stringResource(R.string.gallery_title_count, project.progressPhotos.size),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSocialExport = true },
                        enabled = project.progressPhotos.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.Movie, stringResource(R.string.create_share_material))
                    }
                    IconButton(
                        onClick = {
                            shareProjectPhotos(
                                context,
                                project.progressPhotos.map {
                                    viewModel.progressPhotoUri(project.id, it)
                                },
                            )
                        },
                        enabled = project.progressPhotos.isNotEmpty(),
                    ) {
                        Icon(Icons.Default.Share, stringResource(R.string.share_gallery))
                    }
                },
            )
        },
    ) { padding ->
        if (project.progressPhotos.isEmpty()) {
            EmptyState(
                title = stringResource(R.string.no_progress_photos),
                detail = stringResource(R.string.no_progress_photos_detail),
                button = stringResource(R.string.back),
                onClick = onBack,
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(140.dp),
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(project.progressPhotos, key = { it }) { fileName ->
                    val index = project.progressPhotos.indexOf(fileName)
                    val bitmap = remember(project.id, fileName) {
                        viewModel.progressBitmap(project.id, fileName, 520)
                    }
                    Card(onClick = { selectedIndex = index }) {
                        Column {
                            Box(
                                Modifier.fillMaxWidth().height(180.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center,
                            ) {
                                if (bitmap != null) {
                                    Image(
                                        bitmap.asImageBitmap(),
                                        stringResource(R.string.progress_photo_number, index + 1),
                                        Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }
                            Text(
                                progressPhotoDate(fileName, stringResource(R.string.progress_photo)),
                                Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }

    selectedIndex?.let { initialIndex ->
        val photos = project.progressPhotos
        if (photos.isNotEmpty()) {
            val safeIndex = initialIndex.coerceIn(0, photos.lastIndex)
            val pagerState = rememberPagerState(initialPage = safeIndex) { photos.size }
            val zoomByPhoto = remember { mutableStateMapOf<String, Float>() }
            Dialog(
                onDismissRequest = { selectedIndex = null },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    decorFitsSystemWindows = false,
                ),
            ) {
                Surface(color = Color.Black, modifier = Modifier.fillMaxSize()) {
                    Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                            userScrollEnabled =
                                (zoomByPhoto[photos[pagerState.currentPage]] ?: 1f) <= 1f,
                        ) { page ->
                            val fileName = photos[page]
                            val bitmap = remember(project.id, fileName) {
                                viewModel.progressBitmap(project.id, fileName)
                            }
                            val zoom = zoomByPhoto[fileName] ?: 1f
                            if (bitmap != null) {
                                Image(
                                    bitmap.asImageBitmap(),
                                    stringResource(R.string.photo_position, page + 1, photos.size),
                                    Modifier.fillMaxSize()
                                        .graphicsLayer(
                                            scaleX = zoom,
                                            scaleY = zoom,
                                        )
                                        .pointerInput(fileName) {
                                            detectTapGestures(
                                                onDoubleTap = {
                                                    zoomByPhoto[fileName] =
                                                        if ((zoomByPhoto[fileName] ?: 1f) > 1f) 1f else 2.5f
                                                },
                                            )
                                        },
                                    contentScale = ContentScale.Fit,
                                )
                            }
                        }
                        IconButton(
                            onClick = { selectedIndex = null },
                            modifier = Modifier.align(Alignment.TopStart)
                                .background(Color.Black.copy(alpha = 0.55f)),
                        ) {
                            Icon(Icons.Default.ArrowBack, stringResource(R.string.close), tint = Color.White)
                        }
                        Row(
                            Modifier.align(Alignment.BottomCenter)
                                .background(Color.Black.copy(alpha = 0.65f)).padding(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                "${pagerState.currentPage + 1} / ${photos.size}",
                                color = Color.White,
                            )
                            IconButton(onClick = {
                                val fileName = photos[pagerState.currentPage]
                                zoomByPhoto[fileName] =
                                    if ((zoomByPhoto[fileName] ?: 1f) > 1f) 1f else 2.5f
                            }) {
                                Icon(Icons.Default.ZoomIn, stringResource(R.string.zoom_photo), tint = Color.White)
                            }
                            IconButton(onClick = {
                                val fileName = photos[pagerState.currentPage]
                                val uri = viewModel.progressPhotoUri(project.id, fileName)
                                val intent = Intent(Intent.ACTION_VIEW).apply {
                                    setDataAndType(uri, "image/jpeg")
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                runCatching { context.startActivity(intent) }.onFailure {
                                    android.widget.Toast.makeText(
                                        context,
                                        noPhotoAppMessage,
                                        android.widget.Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }) {
                                Icon(Icons.Default.OpenInNew, stringResource(R.string.open_phone_gallery), tint = Color.White)
                            }
                            IconButton(onClick = {
                                val fileName = photos[pagerState.currentPage]
                                project = viewModel.setProjectCover(project, fileName)
                            }) {
                                Icon(
                                    Icons.Default.Star,
                                    if (project.coverPhoto == photos[pagerState.currentPage]) {
                                        stringResource(R.string.project_cover_current)
                                    } else stringResource(R.string.set_project_cover),
                                    tint = if (project.coverPhoto == photos[pagerState.currentPage]) {
                                        Color(0xFFFFD54F)
                                    } else Color.White,
                                )
                            }
                            IconButton(onClick = {
                                val fileName = photos[pagerState.currentPage]
                                shareProjectPhotos(
                                    context,
                                    listOf(viewModel.progressPhotoUri(project.id, fileName)),
                                )
                            }) {
                                Icon(Icons.Default.Share, stringResource(R.string.share_photo), tint = Color.White)
                            }
                            IconButton(onClick = {
                                photoToDelete = photos[pagerState.currentPage]
                                selectedIndex = null
                            }) {
                                Icon(Icons.Default.Delete, stringResource(R.string.delete_photo), tint = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }

    photoToDelete?.let { fileName ->
        AlertDialog(
            onDismissRequest = { photoToDelete = null },
            title = { Text(stringResource(R.string.delete_photo_question)) },
            text = { Text(stringResource(R.string.delete_photo_detail)) },
            confirmButton = {
                Button(onClick = {
                    project = viewModel.deleteProgressPhoto(project, fileName)
                    photoToDelete = null
                }) { Text(stringResource(R.string.delete_photo)) }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
    if (showSocialExport) {
        SocialExportDialog(
            viewModel = viewModel,
            project = project,
            onDismiss = { showSocialExport = false },
        )
    }
}

private fun shareProjectPhotos(context: android.content.Context, uris: List<android.net.Uri>) {
    if (uris.isEmpty()) return
    val intent = if (uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uris.first())
        }
    } else {
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/jpeg"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(uris))
        }
    }.apply {
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_project_photos)))
}

private fun progressPhotoDate(fileName: String, fallback: String): String {
    val timestamp = fileName.substringAfter("progress_", "").substringBefore('.').toLongOrNull()
        ?: return fallback
    return SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
}

@Composable
private fun CreateProjectDialog(
    images: List<LibraryImage>,
    fixedImage: LibraryImage? = null,
    onDismiss: () -> Unit,
    onCreate: (String, LibraryImage) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(fixedImage ?: images.firstOrNull()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.new_project)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    name,
                    { name = it },
                    label = { Text(stringResource(R.string.project_name)) },
                    singleLine = true,
                )
                if (fixedImage != null) {
                    Text(stringResource(R.string.image_label, fixedImage.name))
                } else {
                    Text(stringResource(R.string.choose_prepared_image))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(images, key = { it.id }) { image ->
                            FilledTonalButton(
                                onClick = { selected = image },
                                enabled = selected?.id != image.id,
                            ) { Text(image.name, maxLines = 1) }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { selected?.let { onCreate(name, it) } }, enabled = selected != null) {
                Text(stringResource(R.string.create))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
    )
}

@Composable
private fun EmptyState(
    title: String,
    detail: String,
    button: String,
    enabled: Boolean = true,
    onClick: () -> Unit,
    secondaryButton: String? = null,
    onSecondaryClick: () -> Unit = {},
) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(Icons.Default.PhotoCamera, null, Modifier.size(64.dp))
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(detail, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(20.dp))
        if (secondaryButton != null) {
            FilledTonalButton(
                onClick = onSecondaryClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.FileUpload, null)
                Spacer(Modifier.width(8.dp))
                Text(secondaryButton)
            }
            Spacer(Modifier.height(10.dp))
        }
        Button(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        ) { Text(button) }
    }
}

@Composable
private fun TutorialDialog(onComplete: () -> Unit) {
    var page by remember { mutableStateOf(0) }
    val titles = listOf(
        R.string.tutorial_welcome_title,
        R.string.tutorial_prepare_title,
        R.string.tutorial_draw_title,
        R.string.tutorial_projects_title,
    )
    val texts = listOf(
        R.string.tutorial_welcome_text,
        R.string.tutorial_prepare_text,
        R.string.tutorial_draw_text,
        R.string.tutorial_projects_text,
    )
    val icons = listOf(
        Icons.Default.Star,
        Icons.Default.AutoFixHigh,
        Icons.Default.PhotoCamera,
        Icons.Default.Folder,
    )
    AlertDialog(
        onDismissRequest = {},
        icon = { Icon(icons[page], null, Modifier.size(48.dp)) },
        title = { Text(stringResource(titles[page])) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(stringResource(texts[page]))
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    repeat(titles.size) { index ->
                        Text(
                            if (index == page) "●" else "○",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (page == titles.lastIndex) onComplete() else page++
                },
            ) {
                Text(
                    stringResource(
                        if (page == titles.lastIndex) R.string.tutorial_start
                        else R.string.tutorial_next
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onComplete) {
                Text(stringResource(R.string.tutorial_skip))
            }
        },
    )
}
