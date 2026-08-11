package pl.tracelens.app.ui

import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.tracelens.app.AppViewModel
import pl.tracelens.app.R
import pl.tracelens.app.data.TraceProject

private enum class SocialExportMode { Menu, Timelapse, Collage, ReferenceResult }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SocialExportDialog(
    viewModel: AppViewModel,
    project: TraceProject,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val cancelledMessage = stringResource(R.string.social_export_cancelled)
    val failedMessage = stringResource(R.string.social_export_failed)
    val referenceLabel = stringResource(R.string.reference_label)
    val resultLabel = stringResource(R.string.result_label)
    val scope = rememberCoroutineScope()
    var mode by remember { mutableStateOf(SocialExportMode.Menu) }
    var busy by remember { mutableStateOf(false) }
    var progress by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<Pair<Uri, String>?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var exportJob by remember { mutableStateOf<Job?>(null) }
    val photoNames = remember(project.progressPhotos) { project.progressPhotos.sortedBy(::photoTimestamp) }

    fun runExport(block: suspend () -> Pair<Uri, String>) {
        if (busy) return
        busy = true
        progress = 0
        exportJob = scope.launch {
            try {
                result = block()
            } catch (_: CancellationException) {
                Toast.makeText(context, cancelledMessage, Toast.LENGTH_SHORT).show()
            } catch (error: Throwable) {
                errorMessage = error.localizedMessage ?: failedMessage
            } finally {
                busy = false
                exportJob = null
            }
        }
    }

    Dialog(
        onDismissRequest = { if (!busy) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Surface(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                TopAppBar(
                    title = { Text(stringResource(R.string.create_share_material)) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (!busy) {
                                if (mode == SocialExportMode.Menu) onDismiss() else mode = SocialExportMode.Menu
                            }
                        }) { Icon(Icons.Default.ArrowBack, stringResource(R.string.back)) }
                    },
                )
                when (mode) {
                    SocialExportMode.Menu -> SocialExportMenu(
                        photoCount = photoNames.size,
                        onTimelapse = { mode = SocialExportMode.Timelapse },
                        onCollage = { mode = SocialExportMode.Collage },
                        onReferenceResult = { mode = SocialExportMode.ReferenceResult },
                    )
                    SocialExportMode.Timelapse -> TimelapsePanel(
                        photoCount = photoNames.size,
                        busy = busy,
                        progress = progress,
                        onCancel = { exportJob?.cancel() },
                        onGenerate = { durationMs ->
                            runExport {
                                val photos = withContext(Dispatchers.IO) {
                                    photoNames.mapNotNull { viewModel.progressBitmap(project.id, it, 1400) }
                                }
                                SocialExports.createTimelapse(context, project.name, photos, durationMs) {
                                    progress = it
                                } to "video/mp4"
                            }
                        },
                    )
                    SocialExportMode.Collage -> CollagePanel(
                        viewModel = viewModel,
                        project = project,
                        photoNames = photoNames,
                        busy = busy,
                        onGenerate = { selected ->
                            runExport {
                                val photos = withContext(Dispatchers.IO) {
                                    selected.mapNotNull { viewModel.progressBitmap(project.id, it, 1400) }
                                }
                                SocialExports.createCollage(context, project.name, photos) to "image/png"
                            }
                        },
                    )
                    SocialExportMode.ReferenceResult -> ReferenceResultPanel(
                        viewModel = viewModel,
                        project = project,
                        photoNames = photoNames,
                        busy = busy,
                        onGenerate = { selected, story ->
                            runExport {
                                val reference = requireNotNull(viewModel.imageForProject(project)?.let(viewModel::bitmap))
                                val finished = requireNotNull(withContext(Dispatchers.IO) {
                                    viewModel.progressBitmap(project.id, selected, 1600)
                                })
                                SocialExports.createReferenceResult(
                                    context = context,
                                    projectName = project.name,
                                    reference = reference,
                                    result = finished,
                                    story = story,
                                    referenceLabel = referenceLabel,
                                    resultLabel = resultLabel,
                                ) to "image/png"
                            }
                        },
                    )
                }
            }
        }
    }

    result?.let { (uri, mime) ->
        val preview = remember(uri, mime) {
            if (mime.startsWith("image/")) context.contentResolver.openInputStream(uri)?.use(BitmapFactory::decodeStream)
            else null
        }
        AlertDialog(
            onDismissRequest = { result = null },
            title = { Text(stringResource(R.string.social_export_ready)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (preview != null) {
                        Image(preview.asImageBitmap(), null, Modifier.fillMaxWidth().height(360.dp), contentScale = ContentScale.Fit)
                    }
                    Text(stringResource(R.string.social_export_saved_gallery))
                    FilledTonalButton(
                        onClick = { SocialExports.share(context, uri, mime, "com.instagram.android") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(painterResource(R.drawable.ic_instagram), null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Instagram")
                    }
                    FilledTonalButton(
                        onClick = { SocialExports.share(context, uri, mime, "com.zhiliaoapp.musically") },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Icon(painterResource(R.drawable.ic_tiktok), null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("TikTok")
                    }
                    Button(
                        onClick = { SocialExports.share(context, uri, mime) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.share_photo))
                    }
                    TextButton(
                        onClick = { result = null },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.close))
                    }
                }
            },
            confirmButton = {},
        )
    }
    errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { errorMessage = null },
            title = { Text(stringResource(R.string.social_export_failed)) },
            text = { Text(message) },
            confirmButton = { TextButton(onClick = { errorMessage = null }) { Text(stringResource(R.string.close)) } },
        )
    }
}

@Composable
private fun SocialExportMenu(
    photoCount: Int,
    onTimelapse: () -> Unit,
    onCollage: () -> Unit,
    onReferenceResult: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.social_tools_intro, photoCount))
        SocialToolCard(R.string.generate_timelapse, R.string.generate_timelapse_detail, Icons.Default.Movie, photoCount >= 1, onTimelapse)
        SocialToolCard(R.string.create_progress_collage, R.string.create_progress_collage_detail, Icons.Default.GridView, photoCount >= 2, onCollage)
        SocialToolCard(R.string.reference_and_result, R.string.reference_and_result_detail, Icons.Default.Compare, photoCount >= 1, onReferenceResult)
    }
}

@Composable
private fun SocialToolCard(
    title: Int,
    detail: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Card(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(stringResource(title), fontWeight = FontWeight.SemiBold)
                Text(stringResource(detail), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TimelapsePanel(
    photoCount: Int,
    busy: Boolean,
    progress: Int,
    onCancel: () -> Unit,
    onGenerate: (Long) -> Unit,
) {
    var duration by remember { mutableStateOf(0.2f) }
    val totalSeconds = (photoCount * duration).coerceAtLeast(1f)
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(stringResource(R.string.shot_duration_value, duration), fontWeight = FontWeight.SemiBold)
        Slider(value = duration, onValueChange = { duration = it }, valueRange = 0.1f..3f)
        Text(stringResource(R.string.estimated_video_duration, totalSeconds))
        if (totalSeconds > 90f) Text(stringResource(R.string.long_social_video_warning), color = MaterialTheme.colorScheme.error)
        if (busy) {
            CircularProgressIndicator(progress = { progress / 100f }, modifier = Modifier.align(Alignment.CenterHorizontally))
            Text(stringResource(R.string.export_progress, progress), modifier = Modifier.align(Alignment.CenterHorizontally))
            TextButton(onClick = onCancel, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text(stringResource(R.string.cancel))
            }
        }
        Button(onClick = { onGenerate((duration * 1000).toLong()) }, enabled = !busy, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.generate_mp4))
        }
    }
}

@Composable
private fun CollagePanel(
    viewModel: AppViewModel,
    project: TraceProject,
    photoNames: List<String>,
    busy: Boolean,
    onGenerate: (List<String>) -> Unit,
) {
    val options = listOf(2, 4, 6, 8).filter { it <= photoNames.size }
    var count by remember(options) { mutableIntStateOf(options.firstOrNull() ?: 2) }
    var selected by remember(count) { mutableStateOf(emptyList<String>()) }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                FilterChip(selected = count == option, onClick = { count = option; selected = emptyList() }, label = { Text(option.toString()) })
            }
        }
        Text(stringResource(R.string.select_photos_count, selected.size, count))
        PhotoSelectionGrid(viewModel, project, photoNames, selected, count, Modifier.weight(1f)) { selected = it }
        Button(onClick = { onGenerate(selected) }, enabled = !busy && selected.size == count, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.generate_collage))
        }
    }
}

@Composable
private fun ReferenceResultPanel(
    viewModel: AppViewModel,
    project: TraceProject,
    photoNames: List<String>,
    busy: Boolean,
    onGenerate: (String, Boolean) -> Unit,
) {
    var selected by remember(photoNames) { mutableStateOf(photoNames.lastOrNull()) }
    var story by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !story, onClick = { story = false }, label = { Text(stringResource(R.string.feed_format)) })
            FilterChip(selected = story, onClick = { story = true }, label = { Text(stringResource(R.string.story_format)) })
        }
        Text(stringResource(R.string.choose_result_photo))
        PhotoSelectionGrid(viewModel, project, photoNames, selected?.let(::listOf).orEmpty(), 1, Modifier.weight(1f)) { selected = it.lastOrNull() }
        Button(onClick = { selected?.let { onGenerate(it, story) } }, enabled = !busy && selected != null, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.generate_comparison))
        }
    }
}

@Composable
private fun PhotoSelectionGrid(
    viewModel: AppViewModel,
    project: TraceProject,
    photoNames: List<String>,
    selected: List<String>,
    limit: Int,
    modifier: Modifier,
    onSelected: (List<String>) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(120.dp),
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(photoNames, key = { it }) { fileName ->
            val bitmap = produceState<android.graphics.Bitmap?>(
                initialValue = null,
                project.id,
                fileName,
            ) {
                value = withContext(Dispatchers.IO) {
                    viewModel.progressBitmap(project.id, fileName, 320)
                }
            }.value
            val position = selected.indexOf(fileName)
            Box(
                Modifier.height(150.dp).border(
                    if (position >= 0) 3.dp else 1.dp,
                    if (position >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    MaterialTheme.shapes.medium,
                ).clickable {
                    onSelected(
                        if (position >= 0) selected - fileName
                        else if (limit == 1) listOf(fileName)
                        else if (selected.size < limit) selected + fileName
                        else selected
                    )
                },
            ) {
                if (bitmap != null) Image(bitmap.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                if (position >= 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    ) { Text((position + 1).toString(), Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.onPrimary) }
                }
            }
        }
    }
}

private fun photoTimestamp(fileName: String): Long =
    fileName.substringAfter("progress_", "").substringBefore('.').toLongOrNull() ?: Long.MAX_VALUE
