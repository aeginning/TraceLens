@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pl.tracelens.app.ui

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
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
import androidx.compose.material.icons.filled.MonochromePhotos
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.ColorLens
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
import pl.tracelens.app.data.EraserPoint
import pl.tracelens.app.data.LibraryImage
import pl.tracelens.app.data.TraceProject
import pl.tracelens.app.ui.camera.TraceWorkspace

private sealed interface Screen {
    data object Library : Screen
    data object Projects : Screen
    data class Editor(val imageId: String) : Screen
    data class Workspace(val projectId: String) : Screen
    data class Gallery(val projectId: String, val fromWorkspace: Boolean) : Screen
}

@Composable
fun TraceLensApp(viewModel: AppViewModel) {
    MaterialTheme {
        var screen: Screen by remember { mutableStateOf(Screen.Library) }
        val snackbar = remember { SnackbarHostState() }
        val message = viewModel.message
        LaunchedEffect(message) {
            if (message != null) {
                snackbar.showSnackbar(message)
                viewModel.consumeMessage()
            }
        }

        Box(Modifier.fillMaxSize()) {
            when (val current = screen) {
                Screen.Library -> MainScaffold(
                    selectedLibrary = true,
                    snackbar = snackbar,
                    onLibrary = { screen = Screen.Library },
                    onProjects = { screen = Screen.Projects },
                ) { padding ->
                    LibraryScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(padding),
                        onEdit = { screen = Screen.Editor(it.id) },
                        onOpenProject = { screen = Screen.Workspace(it.id) },
                    )
                }
                Screen.Projects -> MainScaffold(
                    selectedLibrary = false,
                    snackbar = snackbar,
                    onLibrary = { screen = Screen.Library },
                    onProjects = { screen = Screen.Projects },
                ) { padding ->
                    ProjectsScreen(
                        viewModel = viewModel,
                        modifier = Modifier.padding(padding),
                        onOpen = { screen = Screen.Workspace(it.id) },
                        onGallery = { screen = Screen.Gallery(it.id, false) },
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
    selectedLibrary: Boolean,
    snackbar: SnackbarHostState,
    onLibrary: () -> Unit,
    onProjects: () -> Unit,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(if (selectedLibrary) "Biblioteka obrazów" else "Projekty") })
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedLibrary,
                    onClick = onLibrary,
                    icon = { Icon(Icons.Default.Image, null) },
                    label = { Text("Obrazy") },
                )
                NavigationBarItem(
                    selected = !selectedLibrary,
                    onClick = onProjects,
                    icon = { Icon(Icons.Default.Folder, null) },
                    label = { Text("Projekty") },
                )
            }
        },
        content = content,
    )
}

@Composable
private fun LibraryScreen(
    viewModel: AppViewModel,
    modifier: Modifier,
    onEdit: (LibraryImage) -> Unit,
    onOpenProject: (TraceProject) -> Unit,
) {
    var imageToRename by remember { mutableStateOf<LibraryImage?>(null) }
    var imageForProject by remember { mutableStateOf<LibraryImage?>(null) }
    var imageToDelete by remember { mutableStateOf<LibraryImage?>(null) }
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
                items(viewModel.images, key = { it.id }) { image ->
                    ImageCard(
                        image = image,
                        bitmap = viewModel.bitmap(image),
                        onFavorite = { viewModel.toggleFavorite(image) },
                        onRename = { imageToRename = image },
                        onEdit = { onEdit(image) },
                        onCreateProject = { imageForProject = image },
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
            Column(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp)) {
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
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 32.dp) {
                        IconButton(onClick = onRename, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.DriveFileRenameOutline, "Zmień nazwę", Modifier.size(20.dp))
                        }
                        IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, "Edytuj obraz", Modifier.size(20.dp))
                        }
                        IconButton(onClick = onCreateProject, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Add, "Utwórz projekt z tego obrazu", Modifier.size(20.dp))
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Usuń obraz z biblioteki", Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
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
    var createDialog by remember { mutableStateOf(false) }
    var projectToDelete by remember { mutableStateOf<TraceProject?>(null) }
    Box(modifier.fillMaxSize()) {
        if (viewModel.projects.isEmpty()) {
            EmptyState(
                title = "Brak projektów",
                detail = if (viewModel.images.isEmpty()) "Najpierw dodaj obraz w bibliotece." else "Utwórz projekt i rozpocznij odrysowywanie.",
                button = "Nowy projekt",
                enabled = viewModel.images.isNotEmpty(),
                onClick = { createDialog = true },
            )
        } else {
            LazyColumn(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp, 12.dp, 12.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(viewModel.projects, key = { it.id }) { project ->
                    val image = viewModel.imageForProject(project)
                    Card(onClick = { onOpen(project) }, modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            val bitmap = image?.let(viewModel::bitmap)
                            if (bitmap != null) {
                                Image(
                                    bitmap.asImageBitmap(), null,
                                    Modifier.size(72.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentScale = ContentScale.Fit,
                                )
                            } else Icon(Icons.Default.Image, null, Modifier.size(72.dp))
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(project.name, fontWeight = FontWeight.SemiBold)
                                Text("${project.progressPhotos.size} zdjęć postępu", style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(
                                onClick = { onGallery(project) },
                                enabled = project.progressPhotos.isNotEmpty(),
                            ) {
                                Icon(Icons.Default.PhotoLibrary, "Otwórz zdjęcia postępu")
                            }
                            IconButton(onClick = { projectToDelete = project }) {
                                Icon(Icons.Default.Delete, "Usuń projekt")
                            }
                            Icon(Icons.Default.PlayArrow, "Otwórz")
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
        Button(onClick = onClick, enabled = enabled) { Text(button) }
    }
}
