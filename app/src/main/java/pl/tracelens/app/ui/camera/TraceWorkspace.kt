@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
)

package pl.tracelens.app.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.PowerManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ScreenLockLandscape
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import pl.tracelens.app.AppViewModel
import pl.tracelens.app.data.LibraryImage
import pl.tracelens.app.data.TraceProject
import java.io.File
import kotlin.math.roundToInt

private data class CameraOption(val id: String, val label: String)

@Composable
fun TraceWorkspace(
    viewModel: AppViewModel,
    initialProject: TraceProject,
    image: LibraryImage,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity
    var project by remember(initialProject.id) { mutableStateOf(initialProject) }
    var cameraPermission by remember {
        mutableStateOf(context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        cameraPermission = it
    }
    LaunchedEffect(Unit) {
        if (!cameraPermission) permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    val overlay = remember(image.id, image.processedFileName) { viewModel.bitmap(image) }
    var locked by remember { mutableStateOf(true) }
    var torch by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameras by remember { mutableStateOf<List<CameraOption>>(emptyList()) }
    var thermalStatus by remember { mutableIntStateOf(PowerManager.THERMAL_STATUS_NONE) }
    var showProgress by remember { mutableStateOf(false) }

    val powerManager = remember { context.getSystemService(Context.POWER_SERVICE) as PowerManager }
    DisposableEffect(Unit) {
        val listener = PowerManager.OnThermalStatusChangedListener { thermalStatus = it }
        thermalStatus = powerManager.currentThermalStatus
        powerManager.addThermalStatusListener(context.mainExecutor, listener)
        onDispose { powerManager.removeThermalStatusListener(listener) }
    }
    LaunchedEffect(thermalStatus) {
        if (thermalStatus >= PowerManager.THERMAL_STATUS_SEVERE && torch) {
            camera?.cameraControl?.enableTorch(false)
            torch = false
            Toast.makeText(context, "Latarka wyłączona: telefon jest zbyt gorący.", Toast.LENGTH_LONG).show()
        }
    }
    DisposableEffect(project.orientation) {
        activity.requestedOrientation = when (project.orientation) {
            1 -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            2 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            else -> ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
        onDispose { }
    }
    DisposableEffect(Unit) {
        onDispose {
            camera?.cameraControl?.enableTorch(false)
            activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    fun saveProject(updated: TraceProject) {
        project = updated
        viewModel.updateProject(updated)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.name) },
                navigationIcon = {
                    IconButton(onClick = {
                        saveProject(project)
                        onBack()
                    }) { Icon(Icons.Default.ArrowBack, "Wróć") }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.weight(1f).fillMaxWidth().background(Color.Black)) {
                if (cameraPermission) {
                    CameraPreview(
                        selectedCameraId = project.cameraId,
                        onCameraReady = { boundCamera, capture, options, actualId ->
                            camera = boundCamera
                            imageCapture = capture
                            cameras = options
                            if (project.cameraId != actualId) saveProject(project.copy(cameraId = actualId))
                        },
                    )
                } else {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.align(Alignment.Center),
                    ) { Text("Zezwól na dostęp do kamery") }
                }

                if (overlay != null) {
                    OverlayImage(
                        bitmap = overlay,
                        project = project,
                        locked = locked,
                        onTransform = { panX, panY, zoom, rotation ->
                            project = project.copy(
                                offsetX = project.offsetX + panX,
                                offsetY = project.offsetY + panY,
                                scale = (project.scale * zoom).coerceIn(0.1f, 12f),
                                rotation = project.rotation + rotation,
                            )
                        },
                        onTransformEnd = { saveProject(project) },
                    )
                }

                if (thermalStatus >= PowerManager.THERMAL_STATUS_MODERATE) {
                    Text(
                        "Telefon się nagrzewa",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.TopCenter)
                            .padding(8.dp).background(Color(0xCCB45309)).padding(8.dp),
                    )
                }
            }

            Card(Modifier.fillMaxWidth().navigationBarsPadding()) {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Przezroczystość: ${(project.opacity * 100).roundToInt()}%")
                    Slider(
                        value = project.opacity,
                        onValueChange = { project = project.copy(opacity = it) },
                        onValueChangeFinished = { saveProject(project) },
                    )
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WorkspaceButton(
                            label = if (locked) "Odblokuj" else "Zablokuj",
                            icon = if (locked) Icons.Default.Lock else Icons.Default.LockOpen,
                            onClick = {
                                locked = !locked
                                if (locked) saveProject(project)
                            },
                        )
                        WorkspaceButton("Odbij X", Icons.Default.Flip) {
                            saveProject(project.copy(mirrorX = !project.mirrorX))
                        }
                        WorkspaceButton("Odbij Y", Icons.Default.FlipCameraAndroid) {
                            saveProject(project.copy(mirrorY = !project.mirrorY))
                        }
                        WorkspaceButton(
                            if (torch) "Latarka wył." else "Latarka wł.",
                            if (torch) Icons.Default.FlashOff else Icons.Default.FlashOn,
                        ) {
                            if (thermalStatus < PowerManager.THERMAL_STATUS_SEVERE) {
                                val next = !torch
                                camera?.cameraControl?.enableTorch(next)
                                torch = next
                            }
                        }
                        WorkspaceButton("Zdjęcie", Icons.Default.PhotoCamera) {
                            captureProgress(
                                context = context,
                                capture = imageCapture,
                                output = viewModel.newProgressFile(project.id),
                                onSaved = { file ->
                                    project = viewModel.addProgressPhoto(project, file.name)
                                    Toast.makeText(context, "Zapisano etap pracy.", Toast.LENGTH_SHORT).show()
                                },
                            )
                        }
                        WorkspaceButton("Pion", Icons.Default.ScreenLockPortrait) {
                            saveProject(project.copy(orientation = 1))
                        }
                        WorkspaceButton("Poziom", Icons.Default.ScreenLockLandscape) {
                            saveProject(project.copy(orientation = 2))
                        }
                    }
                    if (cameras.size > 1) {
                        Text("Obiektyw")
                        Row(
                            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            cameras.forEach { option ->
                                Button(
                                    onClick = {
                                        torch = false
                                        saveProject(project.copy(cameraId = option.id))
                                    },
                                    enabled = option.id != project.cameraId,
                                ) {
                                    Icon(Icons.Default.Cameraswitch, null)
                                    Spacer(Modifier.size(6.dp))
                                    Text(option.label)
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = { showProgress = true },
                        enabled = project.progressPhotos.isNotEmpty(),
                    ) {
                        Text("Zdjęcia postępu: ${project.progressPhotos.size}")
                    }
                }
            }
        }
    }
    if (showProgress) {
        AlertDialog(
            onDismissRequest = { showProgress = false },
            title = { Text("Galeria postępu") },
            text = {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(project.progressPhotos) { fileName ->
                        val bitmap = remember(fileName) {
                            BitmapFactory.decodeFile(viewModel.progressFile(project.id, fileName).absolutePath)
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap.asImageBitmap(),
                                "Etap pracy",
                                Modifier.size(220.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProgress = false }) { Text("Zamknij") }
            },
        )
    }
}

@Composable
@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
private fun CameraPreview(
    selectedCameraId: String?,
    onCameraReady: (Camera, ImageCapture, List<CameraOption>, String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())

    LaunchedEffect(selectedCameraId, lifecycleOwner) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            val provider = providerFuture.get()
            val options = provider.availableCameraInfos.mapIndexed { index, info ->
                val id = Camera2CameraInfo.from(info).cameraId
                val facing = info.lensFacing
                val label = when (facing) {
                    CameraSelector.LENS_FACING_FRONT -> "Przednia ${index + 1}"
                    CameraSelector.LENS_FACING_BACK -> "Tylna ${index + 1}"
                    else -> "Kamera ${index + 1}"
                }
                CameraOption(id, label)
            }
            val chosen = options.firstOrNull { it.id == selectedCameraId }
                ?: options.firstOrNull { option ->
                    provider.availableCameraInfos.firstOrNull {
                        Camera2CameraInfo.from(it).cameraId == option.id
                    }?.lensFacing == CameraSelector.LENS_FACING_BACK
                }
                ?: options.firstOrNull()
                ?: return@addListener

            val selector = CameraSelector.Builder().addCameraFilter { infos ->
                infos.filter { Camera2CameraInfo.from(it).cameraId == chosen.id }
            }.build()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }
            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            runCatching {
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)
            }.onSuccess { onCameraReady(it, capture, options, chosen.id) }
        }, ContextCompat.getMainExecutor(context))
    }
}

@Composable
private fun OverlayImage(
    bitmap: Bitmap,
    project: TraceProject,
    locked: Boolean,
    onTransform: (Float, Float, Float, Float) -> Unit,
    onTransformEnd: () -> Unit,
) {
    Image(
        bitmap.asImageBitmap(),
        "Nakładka",
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
            .graphicsLayer {
                translationX = project.offsetX
                translationY = project.offsetY
                scaleX = project.scale * if (project.mirrorX) -1f else 1f
                scaleY = project.scale * if (project.mirrorY) -1f else 1f
                rotationZ = project.rotation
                alpha = project.opacity
            }
            .pointerInput(locked) {
                if (!locked) {
                    detectTransformGestures(
                        onGesture = { _, pan, zoom, rotation ->
                            onTransform(pan.x, pan.y, zoom, rotation)
                        }
                    )
                }
            },
    )
    DisposableEffect(locked) {
        onDispose { if (!locked) onTransformEnd() }
    }
}

@Composable
private fun WorkspaceButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    FilledIconButton(onClick = onClick, modifier = Modifier.size(52.dp)) {
        Icon(icon, label)
    }
}

private fun captureProgress(
    context: Context,
    capture: ImageCapture?,
    output: File,
    onSaved: (File) -> Unit,
) {
    if (capture == null) {
        Toast.makeText(context, "Kamera nie jest jeszcze gotowa.", Toast.LENGTH_SHORT).show()
        return
    }
    val options = ImageCapture.OutputFileOptions.Builder(output).build()
    capture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) = onSaved(output)
            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, "Nie udało się zapisać zdjęcia.", Toast.LENGTH_LONG).show()
            }
        },
    )
}
