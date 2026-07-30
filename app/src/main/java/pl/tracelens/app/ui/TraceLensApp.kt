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
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
    data object Settings : Screen
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
    MaterialTheme {
        var screen: Screen by remember { mutableStateOf(Screen.Library) }
        var showLibrarySearch by remember { mutableStateOf(false) }
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
                    onSettings = { screen = Screen.Settings },
                    onSearch = { showLibrarySearch = !showLibrarySearch },
                ) { padding ->
                    LibraryScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(padding),
                        showSearch = showLibrarySearch,
                        onEdit = { screen = Screen.Editor(it.id) },
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
                Screen.Settings -> MainScaffold(
                    selectedTab = 2,
                    snackbar = snackbar,
                    onLibrary = { screen = Screen.Library },
                    onProjects = { screen = Screen.Projects },
                    onSettings = { screen = Screen.Settings },
                ) { padding ->
                    SettingsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(padding),
                    )
                }
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
        }
    }
}

@Composable
private fun MainScaffold(
    selectedTab: Int,
    snackbar: SnackbarHostState,
    onLibrary: () -> Unit,
    onProjects: () -> Unit,
    onSettings: () -> Unit,
    onSearch: (() -> Unit)? = null,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (selectedTab) {
                            0 -> "Biblioteka obrazów"
                            1 -> "Projekty"
                            else -> "Ustawienia"
                        }
                    )
                },
                actions = {
                    if (onSearch != null) {
                        IconButton(onClick = onSearch) {
                            Icon(Icons.Default.Search, "Pokaż lub ukryj wyszukiwanie")
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
                    label = { Text("Obrazy") },
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = onProjects,
                    icon = { Icon(Icons.Default.Folder, null) },
                    label = { Text("Projekty") },
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = onSettings,
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Ustawienia") },
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
    val cameras = remember {
        val manager = context.getSystemService(android.content.Context.CAMERA_SERVICE) as CameraManager
        manager.cameraIdList.mapIndexed { index, id ->
            val facing = manager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.LENS_FACING)
            val label = when (facing) {
                CameraCharacteristics.LENS_FACING_BACK -> "Tylna ${index + 1}"
                CameraCharacteristics.LENS_FACING_FRONT -> "Przednia ${index + 1}"
                else -> "Kamera ${index + 1}"
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
            Text("Domyślne ustawienia nowych projektów", fontWeight = FontWeight.SemiBold)
            Text(
                "Wartości będą stosowane przy tworzeniu projektu. Ustawienia widoczności działają od razu.",
                style = MaterialTheme.typography.bodySmall,
            )
        }
        item {
            Text("Przezroczystość: ${(settings.defaultOpacity * 100).toInt()}%")
            Slider(
                value = settings.defaultOpacity,
                onValueChange = { save(settings.copy(defaultOpacity = it)) },
            )
        }
        item {
            ToggleRow("Siatka domyślnie włączona", settings.defaultGridEnabled) {
                save(settings.copy(defaultGridEnabled = it))
            }
        }
        if (settings.defaultGridEnabled) {
            item {
                Text("Pola siatki: ${settings.defaultGridDivisions} × ${settings.defaultGridDivisions}")
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
            ToggleRow("Miganie domyślnie włączone", settings.defaultBlinkEnabled) {
                save(settings.copy(defaultBlinkEnabled = it))
            }
        }
        item {
            ToggleRow("Timelapse domyślnie włączony", settings.defaultTimelapseEnabled) {
                save(settings.copy(defaultTimelapseEnabled = it))
            }
        }
        item {
            ToggleRow("Latarka domyślnie włączona", settings.defaultTorchEnabled) {
                save(settings.copy(defaultTorchEnabled = it))
            }
        }
        item {
            Text("Domyślny obiektyw", fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = settings.defaultCameraId == null,
                        onClick = { save(settings.copy(defaultCameraId = null)) },
                        label = { Text("Automatyczny") },
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
            Text("Informacje na ekranie rysowania", fontWeight = FontWeight.SemiBold)
        }
        item {
            ToggleRow("Pokazuj poziom telefonu", settings.showLevelIndicator) {
                save(settings.copy(showLevelIndicator = it))
            }
        }
        item {
            ToggleRow("Ostrzegaj o poruszeniu telefonu lub statywu", settings.showMovementWarning) {
                save(settings.copy(showMovementWarning = it))
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
                Text("Polityka prywatności")
            }
        }
        item {
            Text("Pamięć aplikacji", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(6.dp))
            Text("Łącznie: ${formatStorageBytes(storage.totalBytes)}")
            Text(
                "Obrazy: ${formatStorageBytes(storage.imagesBytes)} · " +
                    "projekty: ${formatStorageBytes(storage.projectsBytes)} · " +
                    "pamięć podręczna: ${formatStorageBytes(storage.cacheBytes)}",
                style = MaterialTheme.typography.bodySmall,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Bezpieczne czyszczenie usuwa wyłącznie pliki, które nie należą już do biblioteki ani projektów, oraz pamięć podręczną.",
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
                Text("Wyczyść bezpiecznie")
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

private enum class LibrarySort(val label: String) {
    Favorites("Ulubione"),
    Name("Nazwa"),
    Added("Data dodania"),
    LastUsed("Ostatnio używane"),
}

@Composable
private fun LibraryScreen(
    viewModel: AppViewModel,
    modifier: Modifier,
    showSearch: Boolean,
    onEdit: (LibraryImage) -> Unit,
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
                title = "Dodaj pierwszy obraz",
                detail = "Wybierz PNG lub JPG z pamięci telefonu.",
                button = "Wybierz obraz",
                onClick = { picker.launch(arrayOf("image/png", "image/jpeg")) },
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp, 12.dp, 12.dp, 96.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (showSearch) {
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("Szukaj obrazu") },
                                leadingIcon = { Icon(Icons.Default.Search, null) },
                                singleLine = true,
                            )
                        }
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedCollection == null,
                                    onClick = { selectedCollection = null },
                                    label = { Text("Wszystkie") },
                                )
                            }
                            item {
                                IconButton(onClick = { showCreateCollection = true }) {
                                    Icon(Icons.Default.Add, "Utwórz kolekcję")
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
                                        label = { Text(option.label) },
                                    )
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
            FloatingActionButton(
                onClick = { picker.launch(arrayOf("image/png", "image/jpeg")) },
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            ) { Icon(Icons.Default.Add, "Dodaj obraz") }
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
            title = { Text("Usunąć obraz?") },
            text = {
                Text(
                    if (linkedProjects == 0) {
                        "Obraz „${image.name}” zostanie trwale usunięty z biblioteki."
                    } else {
                        "Obraz „${image.name}” jest używany w projektach. Liczba powiązanych projektów: $linkedProjects. Usunięcie obrazu usunie również te projekty i ich zdjęcia robocze. Kopie zapisane w galerii telefonu pozostaną bez zmian."
                    }
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteImage(image)
                    imageToDelete = null
                }) { Text("Usuń obraz") }
            },
            dismissButton = {
                TextButton(onClick = { imageToDelete = null }) { Text("Anuluj") }
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
                        "Ulubione",
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
                            Icon(Icons.Default.DriveFileRenameOutline, "Zmień nazwę", Modifier.size(18.dp))
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, "Edytuj obraz", Modifier.size(18.dp))
                        }
                        IconButton(onClick = onCreateProject, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, "Utwórz projekt z tego obrazu", Modifier.size(18.dp))
                        }
                        IconButton(onClick = onCollections, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Folder, "Kolekcje obrazu", Modifier.size(18.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, "Usuń obraz z biblioteki", Modifier.size(18.dp))
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
        title = { Text("Kolekcje obrazu") },
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
                    label = { Text("Nowa kolekcja") },
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
                        ) { Icon(Icons.Default.Add, "Dodaj kolekcję") }
                    },
                )
                if (selected.isEmpty()) {
                    Text("Obraz nie należy do żadnej kolekcji.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        "Wybrane: ${selected.sorted().joinToString()}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val pending = newCollection.trim()
                onSave((selected + pending.takeIf { it.isNotEmpty() }.orEmpty()).filter { it.isNotEmpty() })
            }) { Text("Zapisz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
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
        title = { Text("Nowa kolekcja") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nazwa kolekcji") },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(
                onClick = { onCreate(name) },
                enabled = name.isNotBlank(),
            ) { Text("Utwórz") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
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
        title = { Text("Zmień nazwę obrazu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nazwa w bibliotece") },
                    placeholder = { Text("np. Słodki misio") },
                    singleLine = true,
                )
                Text(
                    "Nie musisz wpisywać rozszerzenia pliku.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onRename(name) },
                enabled = name.trim().isNotEmpty(),
            ) { Text("Zapisz nazwę") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
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
                title = { Text("Edytor obrazu") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Wróć") } },
                actions = {
                    TextButton(
                        enabled = original != null,
                        onClick = { viewModel.updateFilters(image, filters) { onBack() } },
                    ) { Text("Zapisz") }
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
                    Text(if (eraserMode) "Wyłącz gumkę" else "Gumka")
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
                    Icon(Icons.Default.Compare, "Przytrzymaj, aby zobaczyć oryginał")
                }
            }
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (eraserMode) {
                    item {
                        Text("Rozmiar gumki: ${"%.0f".format(eraserSize * 100)}%")
                        Slider(
                            value = eraserSize,
                            onValueChange = { eraserSize = it },
                            valueRange = 0.01f..0.15f,
                        )
                        if (filters.eraserPoints.isNotEmpty()) {
                            TextButton(onClick = { filters = filters.copy(eraserPoints = emptyList()) }) {
                                Text("Wyczyść ślady gumki (${filters.eraserPoints.size})")
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
                        if (filters.isCropped()) "Zmień kadr obrazu"
                        else "Wykadruj fragment obrazu"
                    )
                }
            }
            item {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    EditorFilterButton(
                        icon = Icons.Default.MonochromePhotos,
                        label = "Skala szarości",
                        selected = filters.grayscale,
                    ) { filters = filters.copy(grayscale = !filters.grayscale) }
                    EditorFilterButton(
                        icon = Icons.Default.AutoFixHigh,
                        label = "Kontur / szkic",
                        selected = filters.contour,
                    ) { filters = filters.copy(contour = !filters.contour) }
                    EditorFilterButton(
                        icon = Icons.Default.InvertColors,
                        label = "Odwróć kolory",
                        selected = filters.invert,
                    ) { filters = filters.copy(invert = !filters.invert) }
                    EditorFilterButton(
                        icon = Icons.Default.CleaningServices,
                        label = "Automatycznie usuń tło",
                        selected = filters.autoRemoveBackground,
                    ) {
                        filters = filters.copy(autoRemoveBackground = !filters.autoRemoveBackground)
                    }
                    EditorFilterButton(
                        icon = Icons.Default.ColorLens,
                        label = "Uprość liczbę kolorów",
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
                    Text("Poziomy na kanał: ${filters.simplifyLevels}")
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
                Text("Kontrast: ${"%.0f".format(filters.contrast * 100)}%")
                Slider(
                    value = filters.contrast,
                    onValueChange = { filters = filters.copy(contrast = it) },
                    valueRange = 0.5f..3f,
                )
            }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Wybrany kolor do usunięcia")
                        Text(
                            if (filters.removeColor == null) {
                                "Dotknij obrazu, aby wybrać kolor do usunięcia"
                            } else {
                                "Wybrano kolor — dotknij obrazu, aby go zmienić"
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
                            Text("Usuń wybór")
                        }
                    }
                }
            }
            item {
                Text("Tolerancja koloru: ${"%.0f".format(filters.colorTolerance * 100)}%")
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
                        Text("Eksportuj")
                    }
                    Button(
                        onClick = { viewModel.updateFilters(image, filters) { onBack() } },
                        enabled = original != null,
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) {
                        Icon(Icons.Default.CheckCircle, null)
                        Spacer(Modifier.width(6.dp))
                        Text("Zapisz")
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
        "Podgląd",
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
        title = { Text("Kadrowanie obrazu") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Image(
                    preview.asImageBitmap(),
                    "Podgląd kadru",
                    Modifier.fillMaxWidth().height(260.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit,
                )
                Text("Zakres poziomy")
                RangeSlider(
                    value = horizontal,
                    onValueChange = {
                        if (it.endInclusive - it.start >= 0.05f) horizontal = it
                    },
                    valueRange = 0f..1f,
                )
                Text("Zakres pionowy")
                RangeSlider(
                    value = vertical,
                    onValueChange = {
                        if (it.endInclusive - it.start >= 0.05f) vertical = it
                    },
                    valueRange = 0f..1f,
                )
                Text(
                    "Przesuwaj oba końce suwaków. Podgląd automatycznie powiększa wybrany fragment.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Button(onClick = { onApply(cropFilters) }) { Text("Zastosuj kadr") }
        },
        dismissButton = {
            Row {
                TextButton(onClick = {
                    horizontal = 0f..1f
                    vertical = 0f..1f
                }) { Text("Pełny obraz") }
                TextButton(onClick = onDismiss) { Text("Anuluj") }
            }
        },
    )
}

private fun ImageFilters.isCropped(): Boolean =
    cropLeft > 0.001f || cropTop > 0.001f || cropRight < 0.999f || cropBottom < 0.999f

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
                title = "Brak projektów",
                detail = if (viewModel.images.isEmpty()) {
                    "Zaimportuj wcześniejszy projekt albo dodaj obraz w bibliotece."
                } else {
                    "Zaimportuj wcześniejszy projekt albo utwórz nowy."
                },
                button = "Nowy projekt",
                enabled = viewModel.images.isNotEmpty(),
                onClick = { createDialog = true },
                secondaryButton = "Importuj projekt",
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
                            label = { Text("Aktywne") },
                        )
                        FilterChip(
                            selected = showArchived,
                            onClick = { showArchived = true },
                            label = { Text("Archiwum") },
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
                            Icon(Icons.Default.FileUpload, "Importuj projekt")
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
                                "Wznów ostatni projekt: ${lastActiveProject.name}",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (visibleProjects.isEmpty()) {
                    item {
                        Text(
                            if (showArchived) "Archiwum jest puste." else "Brak aktywnych projektów.",
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
                                        "${project.progressPhotos.size} zdjęć postępu",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                                Icon(Icons.Default.PlayArrow, "Otwórz")
                            }
                            Spacer(Modifier.height(6.dp))
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                            ) {
                                IconButton(
                                    onClick = { onGallery(project) },
                                    enabled = project.progressPhotos.isNotEmpty(),
                                ) { Icon(Icons.Default.PhotoLibrary, "Otwórz zdjęcia postępu") }
                                IconButton(onClick = { projectToRename = project }) {
                                    Icon(Icons.Default.DriveFileRenameOutline, "Zmień nazwę projektu")
                                }
                                IconButton(onClick = { viewModel.toggleProjectArchive(project) }) {
                                    Icon(
                                        if (project.archived) Icons.Default.Unarchive else Icons.Default.Archive,
                                        if (project.archived) "Przywróć projekt" else "Archiwizuj projekt",
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        projectToExport = project
                                        exportLauncher.launch("${project.name}.tracelens")
                                    },
                                ) { Icon(Icons.Default.FileDownload, "Eksportuj projekt") }
                                IconButton(
                                    onClick = {
                                        viewModel.prepareLocalTransfer(project) {
                                            transferServer?.close()
                                            transferServer = it
                                        }
                                    },
                                ) { Icon(Icons.Default.QrCode, "Przenieś przez kod QR") }
                                IconButton(onClick = { projectToDelete = project }) {
                                    Icon(Icons.Default.Delete, "Usuń projekt")
                                }
                            }
                        }
                    }
                }
            }
            FloatingActionButton(
                onClick = { createDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp),
            ) { Icon(Icons.Default.Add, "Nowy projekt") }
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
            title = { Text("Usunąć projekt?") },
            text = {
                Text(
                    "Projekt „${project.name}” oraz wszystkie jego zdjęcia postępu zostaną trwale usunięte. Obraz w bibliotece pozostanie bez zmian."
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteProject(project)
                    projectToDelete = null
                }) { Text("Usuń projekt") }
            },
            dismissButton = {
                TextButton(onClick = { projectToDelete = null }) { Text("Anuluj") }
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
        title = { Text("Przenieś projekt") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(qr.asImageBitmap(), "Kod QR transferu", Modifier.fillMaxWidth())
                Text("Zeskanuj kod drugim telefonem połączonym z tą samą siecią Wi‑Fi.")
                Text("Nie zamykaj tego okna do zakończenia pobierania.", style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { Button(onClick = onDismiss) { Text("Zakończ") } },
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
        title = { Text("Zmień nazwę projektu") },
        text = {
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nazwa projektu") },
                singleLine = true,
            )
        },
        confirmButton = {
            Button(onClick = { onRename(name) }, enabled = name.trim().isNotEmpty()) {
                Text("Zapisz")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
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
                    title = { Text("Nowy projekt") },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.ArrowBack, "Anuluj")
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
                        label = { Text("Nazwa projektu") },
                        singleLine = true,
                    )
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Szukaj obrazu po nazwie") },
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
                            label = { Text("Wszystkie") },
                        )
                        FilterChip(
                            selected = filter == ProjectImageFilter.Favorites,
                            onClick = { filter = ProjectImageFilter.Favorites },
                            label = { Text("Ulubione") },
                        )
                        FilterChip(
                            selected = filter == ProjectImageFilter.Recent,
                            onClick = { filter = ProjectImageFilter.Recent },
                            label = { Text("Ostatnie") },
                        )
                    }
                }
                if (visibleImages.isEmpty()) {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Brak obrazów pasujących do wyszukiwania.")
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
                            selected?.let { "Wybrano: ${it.name}" } ?: "Wybierz obraz",
                            Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        TextButton(onClick = onDismiss) { Text("Anuluj") }
                        Button(
                            onClick = { selected?.let { onCreate(projectName, it) } },
                            enabled = selected != null,
                        ) { Text("Utwórz") }
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
    var project by remember(initialProject.id) { mutableStateOf(initialProject) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var photoToDelete by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project.name)
                        Text(
                            "${project.progressPhotos.size} zdjęć postępu",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Wróć")
                    }
                },
                actions = {
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
                        Icon(Icons.Default.Share, "Udostępnij całą galerię")
                    }
                },
            )
        },
    ) { padding ->
        if (project.progressPhotos.isEmpty()) {
            EmptyState(
                title = "Brak zdjęć postępu",
                detail = "Wróć do projektu i wykonaj pierwsze zdjęcie.",
                button = "Wróć",
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
                                        "Zdjęcie postępu ${index + 1}",
                                        Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                    )
                                }
                            }
                            Text(
                                progressPhotoDate(fileName),
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
                                    "Zdjęcie ${page + 1} z ${photos.size}",
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
                            Icon(Icons.Default.ArrowBack, "Zamknij", tint = Color.White)
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
                                Icon(Icons.Default.ZoomIn, "Powiększ lub pomniejsz", tint = Color.White)
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
                                        "Nie znaleziono aplikacji do otwarcia zdjęcia.",
                                        android.widget.Toast.LENGTH_SHORT,
                                    ).show()
                                }
                            }) {
                                Icon(Icons.Default.OpenInNew, "Otwórz w galerii telefonu", tint = Color.White)
                            }
                            IconButton(onClick = {
                                val fileName = photos[pagerState.currentPage]
                                project = viewModel.setProjectCover(project, fileName)
                            }) {
                                Icon(
                                    Icons.Default.Star,
                                    if (project.coverPhoto == photos[pagerState.currentPage]) {
                                        "To jest okładka projektu"
                                    } else "Ustaw jako okładkę projektu",
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
                                Icon(Icons.Default.Share, "Udostępnij zdjęcie", tint = Color.White)
                            }
                            IconButton(onClick = {
                                photoToDelete = photos[pagerState.currentPage]
                                selectedIndex = null
                            }) {
                                Icon(Icons.Default.Delete, "Usuń zdjęcie", tint = Color.White)
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
            title = { Text("Usunąć zdjęcie?") },
            text = { Text("To zdjęcie zostanie trwale usunięte z projektu.") },
            confirmButton = {
                Button(onClick = {
                    project = viewModel.deleteProgressPhoto(project, fileName)
                    photoToDelete = null
                }) { Text("Usuń zdjęcie") }
            },
            dismissButton = {
                TextButton(onClick = { photoToDelete = null }) { Text("Anuluj") }
            },
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
    context.startActivity(Intent.createChooser(intent, "Udostępnij zdjęcia projektu"))
}

private fun progressPhotoDate(fileName: String): String {
    val timestamp = fileName.substringAfter("progress_", "").substringBefore('.').toLongOrNull()
        ?: return "Zdjęcie postępu"
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
        title = { Text("Nowy projekt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(name, { name = it }, label = { Text("Nazwa projektu") }, singleLine = true)
                if (fixedImage != null) {
                    Text("Obraz: ${fixedImage.name}")
                } else {
                    Text("Wybierz przygotowany obraz")
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
                Text("Utwórz")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Anuluj") } },
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
