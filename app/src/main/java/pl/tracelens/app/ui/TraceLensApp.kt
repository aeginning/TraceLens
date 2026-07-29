@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package pl.tracelens.app.ui

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.tracelens.app.AppViewModel
import pl.tracelens.app.data.ImageFilters
import pl.tracelens.app.data.LibraryImage
import pl.tracelens.app.data.TraceProject
import pl.tracelens.app.ui.camera.TraceWorkspace

private sealed interface Screen {
    data object Library : Screen
    data object Projects : Screen
    data class Editor(val imageId: String) : Screen
    data class Workspace(val projectId: String) : Screen
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
) {
    var imageToRename by remember { mutableStateOf<LibraryImage?>(null) }
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
}

@Composable
private fun ImageCard(
    image: LibraryImage,
    bitmap: Bitmap?,
    onFavorite: () -> Unit,
    onRename: () -> Unit,
    onEdit: () -> Unit,
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
            Row(
                Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    image.name,
                    Modifier.weight(1f).clickable(onClick = onRename),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                IconButton(onClick = onRename, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.DriveFileRenameOutline, "Zmień nazwę", Modifier.size(20.dp))
                }
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, "Edytuj obraz", Modifier.size(20.dp))
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
    Scaffold(
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
        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                if (original != null) {
                    ColorPickImage(
                        bitmap = original,
                        filters = filters,
                        onColor = { filters = filters.copy(removeColor = it) },
                    )
                }
            }
            item { Text("Dotknij obrazu, aby wybrać kolor do usunięcia.", style = MaterialTheme.typography.bodySmall) }
            item { ToggleRow("Skala szarości", filters.grayscale) { filters = filters.copy(grayscale = it) } }
            item { ToggleRow("Kontur / szkic", filters.contour) { filters = filters.copy(contour = it) } }
            item { ToggleRow("Odwróć kolory", filters.invert) { filters = filters.copy(invert = it) } }
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
                            if (filters.removeColor == null) "Nie wybrano koloru" else "Dotknij obrazu, aby zmienić",
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
                Button(
                    onClick = { viewModel.updateFilters(image, filters) { onBack() } },
                    enabled = original != null,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) { Text("Zapisz i wróć do biblioteki") }
            }
        }
    }
}

@Composable
private fun ColorPickImage(bitmap: Bitmap, filters: ImageFilters, onColor: (Int) -> Unit) {
    val preview = remember(bitmap, filters) { pl.tracelens.app.image.ImageProcessor.process(bitmap, filters) }
    Image(
        preview.asImageBitmap(),
        "Podgląd",
        Modifier.fillMaxWidth().height(320.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
            .pointerInput(bitmap) {
                detectTapGestures { position ->
                    val bitmapRatio = bitmap.width.toFloat() / bitmap.height
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
                    if (position.x in left..(left + drawnWidth) && position.y in top..(top + drawnHeight)) {
                        val x = (((position.x - left) / drawnWidth) * bitmap.width).toInt()
                            .coerceIn(0, bitmap.width - 1)
                        val y = (((position.y - top) / drawnHeight) * bitmap.height).toInt()
                            .coerceIn(0, bitmap.height - 1)
                        onColor(bitmap.getPixel(x, y))
                    }
                }
            },
        contentScale = ContentScale.Fit,
    )
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
) {
    var createDialog by remember { mutableStateOf(false) }
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
        CreateProjectDialog(
            images = viewModel.images,
            onDismiss = { createDialog = false },
            onCreate = { name, image ->
                createDialog = false
                onOpen(viewModel.createProject(name, image))
            },
        )
    }
}

@Composable
private fun CreateProjectDialog(
    images: List<LibraryImage>,
    onDismiss: () -> Unit,
    onCreate: (String, LibraryImage) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var selected by remember { mutableStateOf(images.firstOrNull()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nowy projekt") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(name, { name = it }, label = { Text("Nazwa projektu") }, singleLine = true)
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
