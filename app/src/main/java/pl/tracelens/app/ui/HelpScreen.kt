package pl.tracelens.app.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pl.tracelens.app.R

private data class HelpItem(
    val icon: ImageVector,
    val title: String,
    val detail: String,
)

private data class HelpChapter(
    val title: String,
    val intro: String,
    val screenshots: List<Int>,
    val items: List<HelpItem>,
)

@Composable
internal fun HelpScreen(modifier: Modifier = Modifier) {
    val chapters = helpChapters()
    var selectedChapter by remember { mutableIntStateOf(0) }
    var query by remember { mutableStateOf("") }
    var enlargedImage by remember { mutableStateOf<Int?>(null) }
    val normalizedQuery = query.trim().lowercase()
    val matches = if (normalizedQuery.isBlank()) emptyList() else chapters.mapIndexedNotNull { index, chapter ->
        val matchingItems = chapter.items.filter {
            it.title.lowercase().contains(normalizedQuery) || it.detail.lowercase().contains(normalizedQuery)
        }
        if (chapter.title.lowercase().contains(normalizedQuery) || chapter.intro.lowercase().contains(normalizedQuery) || matchingItems.isNotEmpty()) {
            index to chapter.copy(items = if (matchingItems.isEmpty()) chapter.items else matchingItems)
        } else null
    }

    Column(modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            leadingIcon = { Icon(Icons.Default.Search, null) },
            placeholder = { Text(stringResource(R.string.help_search_hint)) },
            singleLine = true,
        )
        if (normalizedQuery.isBlank()) {
            LazyRow(
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(chapters.indices.toList()) { index ->
                    FilterChip(
                        selected = selectedChapter == index,
                        onClick = { selectedChapter = index },
                        label = { Text(chapters[index].title) },
                    )
                }
            }
            key(selectedChapter) {
                ChapterContent(
                    chapter = chapters[selectedChapter],
                    modifier = Modifier.weight(1f),
                    onImageClick = { enlargedImage = it },
                )
            }
        } else if (matches.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.help_no_results))
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp, 8.dp, 12.dp, 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                matches.forEach { (index, chapter) ->
                    item {
                        Card(
                            onClick = { selectedChapter = index; query = "" },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(chapter.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(6.dp))
                                chapter.items.forEach { HelpItemRow(it) }
                            }
                        }
                    }
                }
            }
        }
    }

    enlargedImage?.let { drawable ->
        Dialog(
            onDismissRequest = { enlargedImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().clickable { enlargedImage = null },
                color = MaterialTheme.colorScheme.scrim,
            ) {
                Image(
                    painterResource(drawable),
                    contentDescription = stringResource(R.string.help_enlarge_close),
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    contentScale = ContentScale.Fit,
                )
            }
        }
    }
}

@Composable
private fun ChapterContent(chapter: HelpChapter, modifier: Modifier, onImageClick: (Int) -> Unit) {
    LazyColumn(
        modifier = modifier,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp, 8.dp, 12.dp, 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(chapter.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(chapter.intro, style = MaterialTheme.typography.bodyLarge)
        }
        if (chapter.screenshots.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(chapter.screenshots) { drawable ->
                        Card(onClick = { onImageClick(drawable) }) {
                            Image(
                                painterResource(drawable),
                                contentDescription = stringResource(R.string.help_screenshot),
                                modifier = Modifier.width(220.dp).height(360.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                }
            }
            item { Text(stringResource(R.string.help_tap_screenshot), style = MaterialTheme.typography.bodySmall) }
        }
        items(chapter.items) { HelpItemRow(it) }
    }
}

@Composable
private fun HelpItemRow(item: HelpItem) {
    Row(Modifier.fillMaxWidth().padding(vertical = 7.dp), verticalAlignment = Alignment.Top) {
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.secondaryContainer) {
            Icon(item.icon, null, Modifier.padding(10.dp).size(24.dp), tint = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(item.title, fontWeight = FontWeight.SemiBold)
            Text(item.detail, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun hi(icon: ImageVector, @StringRes title: Int, @StringRes detail: Int) =
    HelpItem(icon, stringResource(title), stringResource(detail))

@Composable
private fun helpChapters(): List<HelpChapter> = listOf(
    HelpChapter(
        stringResource(R.string.help_chapter_start), stringResource(R.string.help_start_intro), emptyList(),
        listOf(
            hi(Icons.Default.Image, R.string.help_start_1_title, R.string.help_start_1_detail),
            hi(Icons.Default.Tune, R.string.help_start_2_title, R.string.help_start_2_detail),
            hi(Icons.Default.PhotoCamera, R.string.help_start_3_title, R.string.help_start_3_detail),
            hi(Icons.Default.PhotoLibrary, R.string.help_start_4_title, R.string.help_start_4_detail),
        ),
    ),
    HelpChapter(
        stringResource(R.string.help_chapter_library), stringResource(R.string.help_library_intro),
        listOf(R.drawable.tutorial_01_biblioteka, R.drawable.tutorial_02_filtry),
        listOf(
            hi(Icons.Default.Search, R.string.help_search_title, R.string.help_search_detail),
            hi(Icons.Default.FilterList, R.string.help_filters_title, R.string.help_filters_detail),
            hi(Icons.Default.Star, R.string.help_favorite_title, R.string.help_favorite_detail),
            hi(Icons.Default.DriveFileRenameOutline, R.string.help_rename_image_title, R.string.help_rename_image_detail),
            hi(Icons.Default.Edit, R.string.help_edit_image_title, R.string.help_edit_image_detail),
            hi(Icons.Default.Add, R.string.help_project_image_title, R.string.help_project_image_detail),
            hi(Icons.Default.Folder, R.string.help_collection_title, R.string.help_collection_detail),
            hi(Icons.Default.Delete, R.string.help_delete_image_title, R.string.help_delete_image_detail),
            hi(Icons.Default.Image, R.string.help_add_image_title, R.string.help_add_image_detail),
        ),
    ),
    HelpChapter(
        stringResource(R.string.help_chapter_editor_patterns), stringResource(R.string.help_editor_intro),
        listOf(R.drawable.tutorial_03_edytor, R.drawable.tutorial_04_wzory, R.drawable.tutorial_05_wzor_podglad),
        listOf(
            hi(Icons.Default.Crop, R.string.help_crop_title, R.string.help_crop_detail),
            hi(Icons.Default.CleaningServices, R.string.help_remove_background_title, R.string.help_remove_background_detail),
            hi(Icons.Default.AutoFixHigh, R.string.help_filters_editor_title, R.string.help_filters_editor_detail),
            hi(Icons.Default.Compare, R.string.help_before_after_title, R.string.help_before_after_detail),
            hi(Icons.Default.SaveAlt, R.string.help_export_editor_title, R.string.help_export_editor_detail),
            hi(Icons.Default.Edit, R.string.help_save_editor_title, R.string.help_save_editor_detail),
            hi(Icons.Default.Image, R.string.help_patterns_title, R.string.help_patterns_detail),
            hi(Icons.Default.Add, R.string.help_pattern_actions_title, R.string.help_pattern_actions_detail),
        ),
    ),
    HelpChapter(
        stringResource(R.string.help_chapter_projects), stringResource(R.string.help_projects_intro),
        listOf(R.drawable.tutorial_06_projekty, R.drawable.tutorial_07_archiwum),
        listOf(
            hi(Icons.Default.PlayArrow, R.string.help_open_project_title, R.string.help_open_project_detail),
            hi(Icons.Default.PhotoLibrary, R.string.help_project_gallery_title, R.string.help_project_gallery_detail),
            hi(Icons.Default.DriveFileRenameOutline, R.string.help_rename_project_title, R.string.help_rename_project_detail),
            hi(Icons.Default.Archive, R.string.help_archive_title, R.string.help_archive_detail),
            hi(Icons.Default.Unarchive, R.string.help_unarchive_title, R.string.help_unarchive_detail),
            hi(Icons.Default.FileDownload, R.string.help_export_project_title, R.string.help_export_project_detail),
            hi(Icons.Default.FileUpload, R.string.help_import_project_title, R.string.help_import_project_detail),
            hi(Icons.Default.QrCode, R.string.help_transfer_title, R.string.help_transfer_detail),
            hi(Icons.Default.Delete, R.string.help_delete_project_title, R.string.help_delete_project_detail),
        ),
    ),
    HelpChapter(
        stringResource(R.string.help_chapter_drawing), stringResource(R.string.help_drawing_intro),
        listOf(R.drawable.tutorial_09_rysowanie),
        listOf(
            hi(Icons.Default.PhotoCamera, R.string.help_photo_title, R.string.help_photo_detail),
            hi(Icons.Default.VisibilityOff, R.string.help_hide_overlay_title, R.string.help_hide_overlay_detail),
            hi(Icons.Default.Tune, R.string.help_controls_title, R.string.help_controls_detail),
            hi(Icons.Default.Lock, R.string.help_gesture_lock_title, R.string.help_gesture_lock_detail),
            hi(Icons.Default.GridOn, R.string.help_grid_title, R.string.help_grid_detail),
            hi(Icons.Default.ViewModule, R.string.help_sections_title, R.string.help_sections_detail),
            hi(Icons.Default.Flip, R.string.help_flip_title, R.string.help_flip_detail),
            hi(Icons.Default.CenterFocusStrong, R.string.help_camera_lock_title, R.string.help_camera_lock_detail),
            hi(Icons.Default.CenterFocusStrong, R.string.help_manual_focus_title, R.string.help_manual_focus_detail),
            hi(Icons.Default.FlashOn, R.string.help_torch_title, R.string.help_torch_detail),
            hi(Icons.Default.Timer, R.string.help_blink_title, R.string.help_blink_detail),
            hi(Icons.Default.Timer, R.string.help_timelapse_title, R.string.help_timelapse_detail),
            hi(Icons.Default.ControlCamera, R.string.help_tripod_title, R.string.help_tripod_detail),
            hi(Icons.Default.Compare, R.string.help_blending_title, R.string.help_blending_detail),
        ),
    ),
    HelpChapter(
        stringResource(R.string.help_chapter_progress), stringResource(R.string.help_progress_intro),
        listOf(R.drawable.tutorial_10_postep, R.drawable.tutorial_11_materialy, R.drawable.tutorial_12_udostepnianie),
        listOf(
            hi(Icons.Default.OpenInNew, R.string.help_open_photo_title, R.string.help_open_photo_detail),
            hi(Icons.Default.Share, R.string.help_share_gallery_title, R.string.help_share_gallery_detail),
            hi(Icons.Default.Movie, R.string.help_movie_title, R.string.help_movie_detail),
            hi(Icons.Default.GridView, R.string.help_collage_title, R.string.help_collage_detail),
            hi(Icons.Default.Compare, R.string.help_reference_title, R.string.help_reference_detail),
            hi(Icons.Default.Share, R.string.help_social_title, R.string.help_social_detail),
            hi(Icons.Default.Delete, R.string.help_delete_photo_title, R.string.help_delete_photo_detail),
        ),
    ),
    HelpChapter(
        stringResource(R.string.help_chapter_settings), stringResource(R.string.help_settings_intro),
        listOf(R.drawable.tutorial_08_konfiguracja),
        listOf(
            hi(Icons.Default.Settings, R.string.help_defaults_title, R.string.help_defaults_detail),
            hi(Icons.Default.Palette, R.string.help_grid_color_title, R.string.help_grid_color_detail),
            hi(Icons.Default.Language, R.string.help_language_title, R.string.help_language_detail),
            hi(Icons.Default.Tune, R.string.help_indicators_title, R.string.help_indicators_detail),
            hi(Icons.Default.CleaningServices, R.string.help_storage_title, R.string.help_storage_detail),
        ),
    ),
)
