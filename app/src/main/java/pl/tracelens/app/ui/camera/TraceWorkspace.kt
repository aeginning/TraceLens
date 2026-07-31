@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package pl.tracelens.app.ui.camera

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CaptureRequest
import android.os.PowerManager
import android.os.BatteryManager
import android.os.StatFs
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.camera2.interop.Camera2CameraControl
import androidx.camera.camera2.interop.CaptureRequestOptions
import androidx.camera.core.Camera
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FlipCameraAndroid
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.ScreenLockLandscape
import androidx.compose.material.icons.filled.ScreenLockPortrait
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ControlCamera
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import pl.tracelens.app.AppViewModel
import pl.tracelens.app.R
import pl.tracelens.app.data.LibraryImage
import pl.tracelens.app.data.TraceProject
import java.io.File
import kotlin.math.roundToInt
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

private data class CameraOption(val id: String, val label: String)
private data class DeviceTilt(val horizontal: Float = 0f, val vertical: Float = 0f)
private data class BatteryStatus(val percent: Int = 0, val charging: Boolean = false)

@Composable
fun TraceWorkspace(
    viewModel: AppViewModel,
    initialProject: TraceProject,
    image: LibraryImage,
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val activity = context as Activity
    val torchTooHotMessage = stringResource(R.string.torch_too_hot)
    val lowStorageMessage = stringResource(R.string.low_storage)
    val progressSavedGalleryMessage = stringResource(R.string.progress_saved_gallery)
    val progressSavedNoGalleryMessage = stringResource(R.string.progress_saved_no_gallery)
    val tripodSavedMessage = stringResource(R.string.tripod_saved)
    val cameraUnlockedMessage = stringResource(R.string.camera_unlocked)
    val cameraLockedMessage = stringResource(R.string.camera_locked)
    val timelapseStoppedMessage = stringResource(R.string.timelapse_stopped)
    val cameraLabels = (1..20).associateWith { index ->
        Triple(
            stringResource(R.string.front_camera, index),
            stringResource(R.string.back_camera, index),
            stringResource(R.string.camera_number, index),
        )
    }
    val appSettings = viewModel.appSettings
    var project by remember(initialProject.id) { mutableStateOf(initialProject) }
    val timelapseStartedMessage = stringResource(R.string.timelapse_started, project.timelapseIntervalSeconds)
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
    var torch by remember(initialProject.id) { mutableStateOf(appSettings.defaultTorchEnabled) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var cameras by remember { mutableStateOf<List<CameraOption>>(emptyList()) }
    var thermalStatus by remember { mutableIntStateOf(PowerManager.THERMAL_STATUS_NONE) }
    val batteryStatus = rememberBatteryStatus()
    var focusPoint by remember { mutableStateOf<Offset?>(null) }
    var showControls by remember { mutableStateOf(true) }
    var showCameraPicker by remember { mutableStateOf(false) }
    var overlayEnabled by remember { mutableStateOf(true) }
    var compareHeld by remember { mutableStateOf(false) }
    var blinking by remember(initialProject.id) { mutableStateOf(appSettings.defaultBlinkEnabled) }
    var blinkVisible by remember { mutableStateOf(true) }
    var cameraParametersLocked by remember { mutableStateOf(false) }
    var movementBaseline by remember { mutableStateOf<DeviceTilt?>(null) }
    var movementWarning by remember { mutableStateOf(false) }
    var timelapseEnabled by remember(initialProject.id) {
        mutableStateOf(appSettings.defaultTimelapseEnabled)
    }
    var captureInProgress by remember { mutableStateOf(false) }
    var tripodAlignMode by remember { mutableStateOf(false) }
    val deviceTilt = rememberDeviceTilt()
    val tripodReference = remember(project.tripodReferencePhoto) {
        project.tripodReferencePhoto?.let {
            viewModel.progressBitmap(project.id, it, 1800)
        }
    }

    LaunchedEffect(blinking, project.blinkIntervalMs) {
        blinkVisible = true
        while (blinking) {
            delay(project.blinkIntervalMs)
            blinkVisible = !blinkVisible
        }
    }
    LaunchedEffect(deviceTilt, locked) {
        if (deviceTilt == DeviceTilt()) return@LaunchedEffect
        if (!locked) {
            movementBaseline = null
            movementWarning = false
        } else {
            val baseline = movementBaseline
            if (baseline == null) {
                movementBaseline = deviceTilt
            } else {
                movementWarning = abs(deviceTilt.horizontal - baseline.horizontal) > 3.5f ||
                    abs(deviceTilt.vertical - baseline.vertical) > 3.5f
            }
        }
    }

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
            Toast.makeText(context, torchTooHotMessage, Toast.LENGTH_LONG).show()
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

    fun takeProgressPhoto() {
        if (captureInProgress) return
        val availableBytes = StatFs(context.filesDir.absolutePath).availableBytes
        if (availableBytes < 200L * 1024L * 1024L) {
            timelapseEnabled = false
            Toast.makeText(
                context,
                lowStorageMessage,
                Toast.LENGTH_LONG,
            ).show()
            return
        }
        captureInProgress = true
        captureProgress(
            context = context,
            capture = imageCapture,
            output = viewModel.newProgressFile(project.id),
            onSaved = { file ->
                project = viewModel.addProgressPhoto(project, file.name)
                val published = viewModel.publishProgressPhoto(project.name, file)
                Toast.makeText(
                    context,
                    if (published) {
                        progressSavedGalleryMessage
                    } else {
                        progressSavedNoGalleryMessage
                    },
                    Toast.LENGTH_LONG,
                ).show()
            },
            onFinished = { captureInProgress = false },
        )
    }

    fun saveTripodReference() {
        if (captureInProgress) return
        captureInProgress = true
        captureProgress(
            context = context,
            capture = imageCapture,
            output = viewModel.newProgressFile(project.id),
            onSaved = { file ->
                project = viewModel.setTripodReference(project, file.name)
                tripodAlignMode = false
                Toast.makeText(
                    context,
                    tripodSavedMessage,
                    Toast.LENGTH_LONG,
                ).show()
            },
            onFinished = { captureInProgress = false },
        )
    }

    LaunchedEffect(timelapseEnabled, project.timelapseIntervalSeconds) {
        while (timelapseEnabled) {
            delay(project.timelapseIntervalSeconds * 1_000L)
            if (timelapseEnabled) takeProgressPhoto()
        }
    }

    fun toggleCameraLock() {
        val activeCamera = camera ?: return
        if (cameraParametersLocked) {
            activeCamera.cameraControl.cancelFocusAndMetering()
            applyCameraLocks(activeCamera, false)
            cameraParametersLocked = false
            Toast.makeText(context, cameraUnlockedMessage, Toast.LENGTH_SHORT).show()
        } else {
            val point = SurfaceOrientedMeteringPointFactory(1f, 1f).createPoint(.5f, .5f)
            val action = FocusMeteringAction.Builder(
                point,
                FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE or FocusMeteringAction.FLAG_AWB,
            ).disableAutoCancel().build()
            activeCamera.cameraControl.startFocusAndMetering(action)
            applyCameraLocks(activeCamera, true)
            cameraParametersLocked = true
            Toast.makeText(context, cameraLockedMessage, Toast.LENGTH_SHORT).show()
        }
    }

    fun focusCamera(point: Offset, width: Float, height: Float) {
        val activeCamera = camera ?: return
        if (width <= 0f || height <= 0f) return
        if (cameraParametersLocked) {
            activeCamera.cameraControl.cancelFocusAndMetering()
            applyCameraLocks(activeCamera, false)
            cameraParametersLocked = false
        }
        val meteringPoint = SurfaceOrientedMeteringPointFactory(width, height)
            .createPoint(point.x.coerceIn(0f, width), point.y.coerceIn(0f, height))
        val action = FocusMeteringAction.Builder(
            meteringPoint,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE,
        ).build()
        activeCamera.cameraControl.startFocusAndMetering(action)
        focusPoint = point
    }

    LaunchedEffect(focusPoint) {
        if (focusPoint != null) {
            delay(1_200)
            focusPoint = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(project.name) },
                navigationIcon = {
                    IconButton(onClick = {
                        saveProject(project)
                        onBack()
                    }) { Icon(Icons.Default.ArrowBack, stringResource(R.string.back)) }
                },
            )
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                if (cameraPermission) {
                    CameraPreview(
                        selectedCameraId = project.cameraId,
                        cameraLabels = cameraLabels,
                        onCameraReady = { boundCamera, capture, options, actualId ->
                            camera = boundCamera
                            imageCapture = capture
                            cameras = options
                            if (torch && thermalStatus < PowerManager.THERMAL_STATUS_SEVERE) {
                                boundCamera.cameraControl.enableTorch(true)
                            }
                            if (project.cameraId != actualId) saveProject(project.copy(cameraId = actualId))
                        },
                    )
                } else {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.align(Alignment.Center),
                    ) { Text(stringResource(R.string.camera_permission)) }
                }

                val overlayVisible = overlay != null && overlayEnabled && !compareHeld && (!blinking || blinkVisible)
                if (overlayVisible) {
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
                        onFocusTap = { point, width, height -> focusCamera(point, width, height) },
                    )
                } else if (locked) {
                    Box(
                        Modifier.fillMaxSize().pointerInput(camera) {
                            detectTapGestures { focusCamera(it, size.width.toFloat(), size.height.toFloat()) }
                        }
                    )
                }
                if (project.gridEnabled) {
                    DrawingGrid(
                        divisions = project.gridDivisions,
                        color = Color(appSettings.defaultGridColor),
                        opacity = project.gridOpacity,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                if (project.workSectionsEnabled) {
                    WorkSectionMask(
                        divisions = project.workSectionDivisions,
                        activeSection = project.activeWorkSection,
                        opacity = project.inactiveSectionOpacity,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                focusPoint?.let { point ->
                    Canvas(Modifier.fillMaxSize()) {
                        drawCircle(Color(0xFFE0C8FF), 34f, point, style = androidx.compose.ui.graphics.drawscope.Stroke(4f))
                        drawCircle(Color.White, 28f, point, style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
                    }
                }
                if (tripodAlignMode && tripodReference != null) {
                    Image(
                        tripodReference.asImageBitmap(),
                        stringResource(R.string.tripod_reference),
                        modifier = Modifier.fillMaxSize().alpha(0.45f),
                        contentScale = ContentScale.Crop,
                    )
                    Text(
                        stringResource(R.string.tripod_reference_hint),
                        color = Color.White,
                        modifier = Modifier.align(Alignment.TopCenter)
                            .padding(8.dp).background(Color(0xBB000000)).padding(8.dp),
                    )
                }

                if (thermalStatus >= PowerManager.THERMAL_STATUS_MODERATE) {
                    Text(
                        stringResource(R.string.phone_heating),
                        color = Color.White,
                        modifier = Modifier.align(Alignment.TopCenter)
                            .padding(8.dp).background(Color(0xCCB45309)).padding(8.dp),
                        )
                }
                Column(
                    Modifier.align(Alignment.TopStart).padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (appSettings.showLevelIndicator) {
                        Text(
                            stringResource(R.string.phone_level_value, deviceTilt.horizontal.roundToInt(), deviceTilt.vertical.roundToInt()),
                            color = Color.White,
                            modifier = Modifier.background(
                                if (abs(deviceTilt.horizontal) <= 2f && abs(deviceTilt.vertical) <= 2f) {
                                    Color(0xCC2E7D32)
                                } else Color(0xAA000000)
                            ).padding(horizontal = 8.dp, vertical = 5.dp),
                        )
                    }
                    if (appSettings.showMovementWarning && movementWarning) {
                        Text(
                            stringResource(R.string.phone_moved),
                            color = Color.White,
                            modifier = Modifier.background(Color(0xCCD84315))
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                        )
                    }
                }
                Text(
                    stringResource(
                        R.string.device_monitor,
                        batteryStatus.percent,
                        stringResource(if (batteryStatus.charging) R.string.battery_charging else R.string.battery_discharging),
                        thermalLabel(thermalStatus),
                    ),
                    color = Color.White,
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp).background(
                        if (thermalStatus >= PowerManager.THERMAL_STATUS_MODERATE || batteryStatus.percent <= 15) Color(0xCCB45309)
                        else Color(0xAA000000)
                    ).padding(horizontal = 8.dp, vertical = 5.dp),
                )
                if (!showControls) {
                    Row(
                        Modifier.align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(18.dp, Alignment.CenterHorizontally),
                    ) {
                        FilledIconButton(
                            onClick = ::takeProgressPhoto,
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(Icons.Default.PhotoCamera, stringResource(R.string.take_progress_photo))
                        }
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .pointerInput(Unit) {
                                    awaitEachGesture {
                                        awaitFirstDown(requireUnconsumed = false)
                                        compareHeld = true
                                        waitForUpOrCancellation()
                                        compareHeld = false
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                Icons.Default.VisibilityOff,
                                stringResource(R.string.hold_hide_overlay),
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                        FilledIconButton(
                            onClick = { showControls = true },
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(Icons.Default.Tune, stringResource(R.string.show_controls))
                        }
                        FilledIconButton(
                            onClick = {
                                locked = !locked
                                if (locked) saveProject(project)
                            },
                            modifier = Modifier.size(56.dp),
                        ) {
                            Icon(
                                if (locked) Icons.Default.Lock else Icons.Default.LockOpen,
                                stringResource(if (locked) R.string.unlock else R.string.lock),
                            )
                        }
                    }
                }
            }

            if (showControls) Card(
                Modifier.align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Column(
                    Modifier.heightIn(max = 380.dp)
                        .verticalScroll(rememberScrollState())
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.opacity_value, (project.opacity * 100).roundToInt()),
                            modifier = Modifier.weight(1f),
                        )
                        FilledTonalButton(onClick = { showControls = false }) {
                            Icon(Icons.Default.VisibilityOff, null)
                            Spacer(Modifier.size(6.dp))
                            Text(stringResource(R.string.drawing_mode))
                        }
                    }
                    Slider(
                        value = project.opacity,
                        onValueChange = { project = project.copy(opacity = it) },
                        onValueChangeFinished = { saveProject(project) },
                    )
                    if (project.gridEnabled) {
                        Text(stringResource(R.string.grid_cells, project.gridDivisions))
                        Slider(
                            value = project.gridDivisions.toFloat(),
                            onValueChange = {
                                project = project.copy(gridDivisions = it.roundToInt().coerceIn(2, 12))
                            },
                            onValueChangeFinished = { saveProject(project) },
                            valueRange = 2f..12f,
                            steps = 9,
                        )
                        Text(stringResource(R.string.grid_opacity, (project.gridOpacity * 100).roundToInt()))
                        Slider(
                            value = project.gridOpacity,
                            onValueChange = { project = project.copy(gridOpacity = it) },
                            onValueChangeFinished = { saveProject(project) },
                            valueRange = 0.1f..1f,
                        )
                    }
                    WorkspaceButton(
                        stringResource(if (project.workSectionsEnabled) R.string.sections_enabled else R.string.sections_disabled),
                        Icons.Default.ViewModule,
                    ) {
                        saveProject(project.copy(workSectionsEnabled = !project.workSectionsEnabled))
                    }
                    if (project.workSectionsEnabled) {
                        Text(stringResource(R.string.sections_grid, project.workSectionDivisions, project.workSectionDivisions))
                        Slider(
                            value = project.workSectionDivisions.toFloat(),
                            onValueChange = {
                                val value = it.roundToInt().coerceIn(2, 6)
                                project = project.copy(
                                    workSectionDivisions = value,
                                    activeWorkSection = project.activeWorkSection.coerceAtMost(value * value - 1),
                                )
                            },
                            onValueChangeFinished = { saveProject(project) },
                            valueRange = 2f..6f,
                            steps = 3,
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            IconButton(onClick = {
                                saveProject(project.copy(activeWorkSection = (project.activeWorkSection - 1).coerceAtLeast(0)))
                            }) { Icon(Icons.Default.NavigateBefore, stringResource(R.string.previous_section)) }
                            Text(
                                stringResource(
                                    R.string.active_section,
                                    project.activeWorkSection + 1,
                                    project.workSectionDivisions * project.workSectionDivisions,
                                ),
                                modifier = Modifier.align(Alignment.CenterVertically),
                            )
                            IconButton(onClick = {
                                val max = project.workSectionDivisions * project.workSectionDivisions - 1
                                saveProject(project.copy(activeWorkSection = (project.activeWorkSection + 1).coerceAtMost(max)))
                            }) { Icon(Icons.Default.NavigateNext, stringResource(R.string.next_section)) }
                        }
                        Text(stringResource(R.string.inactive_area_opacity, (project.inactiveSectionOpacity * 100).roundToInt()))
                        Slider(
                            value = project.inactiveSectionOpacity,
                            onValueChange = { project = project.copy(inactiveSectionOpacity = it) },
                            onValueChangeFinished = { saveProject(project) },
                            valueRange = 0.15f..0.85f,
                        )
                    }
                    if (blinking) {
                        Text(stringResource(R.string.blink_interval, (project.blinkIntervalMs / 100f).roundToInt() / 10f))
                        Slider(
                            value = project.blinkIntervalMs.toFloat(),
                            onValueChange = { project = project.copy(blinkIntervalMs = it.toLong()) },
                            onValueChangeFinished = { saveProject(project) },
                            valueRange = 250f..2_000f,
                        )
                    }
                    if (timelapseEnabled) {
                        Text(stringResource(R.string.timelapse_interval, project.timelapseIntervalSeconds))
                        Slider(
                            value = project.timelapseIntervalSeconds.toFloat(),
                            onValueChange = {
                                project = project.copy(
                                    timelapseIntervalSeconds = it.roundToInt().coerceIn(5, 300)
                                )
                            },
                            onValueChangeFinished = { saveProject(project) },
                            valueRange = 5f..300f,
                        )
                    }
                    FlowRow(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        WorkspaceButton(
                            label = stringResource(if (locked) R.string.unlock else R.string.lock),
                            icon = if (locked) Icons.Default.Lock else Icons.Default.LockOpen,
                            onClick = {
                                locked = !locked
                                if (locked) saveProject(project)
                            },
                        )
                        WorkspaceButton(stringResource(R.string.flip_x), Icons.Default.Flip) {
                            saveProject(project.copy(mirrorX = !project.mirrorX))
                        }
                        WorkspaceButton(stringResource(R.string.flip_y), Icons.Default.FlipCameraAndroid) {
                            saveProject(project.copy(mirrorY = !project.mirrorY))
                        }
                        WorkspaceButton(
                            stringResource(if (project.gridEnabled) R.string.grid_enabled else R.string.grid_disabled),
                            Icons.Default.GridOn,
                        ) {
                            saveProject(project.copy(gridEnabled = !project.gridEnabled))
                        }
                        WorkspaceButton(
                            stringResource(if (cameraParametersLocked) R.string.unlock_camera else R.string.lock_camera),
                            Icons.Default.CenterFocusStrong,
                            onClick = ::toggleCameraLock,
                        )
                        WorkspaceButton(
                            stringResource(if (overlayEnabled) R.string.view_combined else R.string.view_camera),
                            Icons.Default.Compare,
                        ) {
                            overlayEnabled = !overlayEnabled
                            blinking = false
                        }
                        WorkspaceButton(
                            stringResource(if (blinking) R.string.blinking_enabled else R.string.blinking_disabled),
                            Icons.Default.Timer,
                        ) {
                            blinking = !blinking
                            overlayEnabled = true
                        }
                        WorkspaceButton(
                            stringResource(if (timelapseEnabled) R.string.timelapse_enabled else R.string.timelapse_disabled),
                            Icons.Default.Timer,
                        ) {
                            timelapseEnabled = !timelapseEnabled
                            Toast.makeText(
                                context,
                                if (timelapseEnabled) {
                                    timelapseStartedMessage
                                } else timelapseStoppedMessage,
                                Toast.LENGTH_SHORT,
                            ).show()
                        }
                        WorkspaceButton(
                            if (project.tripodReferencePhoto == null) {
                                stringResource(R.string.save_tripod)
                            } else if (tripodAlignMode) {
                                stringResource(R.string.hide_reference)
                            } else {
                                stringResource(R.string.align_tripod)
                            },
                            Icons.Default.ControlCamera,
                        ) {
                            if (project.tripodReferencePhoto == null) {
                                saveTripodReference()
                            } else {
                                tripodAlignMode = !tripodAlignMode
                            }
                        }
                        WorkspaceButton(
                            stringResource(
                                R.string.blend_mode,
                                stringResource(
                                    when (project.overlayBlendMode) {
                                        1 -> R.string.blend_multiply
                                        2 -> R.string.blend_screen
                                        else -> R.string.blend_normal
                                    }
                                )
                            ),
                            Icons.Default.ScreenRotation,
                        ) {
                            saveProject(project.copy(overlayBlendMode = (project.overlayBlendMode + 1) % 3))
                        }
                        WorkspaceButton(
                            stringResource(if (torch) R.string.torch_enabled else R.string.torch_disabled),
                            if (torch) Icons.Default.FlashOff else Icons.Default.FlashOn,
                        ) {
                            if (thermalStatus < PowerManager.THERMAL_STATUS_SEVERE) {
                                val next = !torch
                                camera?.cameraControl?.enableTorch(next)
                                torch = next
                            }
                        }
                        WorkspaceButton(stringResource(R.string.portrait), Icons.Default.ScreenLockPortrait) {
                            saveProject(project.copy(orientation = 1))
                        }
                        WorkspaceButton(stringResource(R.string.landscape), Icons.Default.ScreenLockLandscape) {
                            saveProject(project.copy(orientation = 2))
                        }
                    }
                    if (cameras.isNotEmpty()) {
                        val selectedCamera = cameras.firstOrNull { it.id == project.cameraId }
                        FilledTonalButton(
                            onClick = { showCameraPicker = !showCameraPicker },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(Icons.Default.Cameraswitch, null)
                            Spacer(Modifier.size(6.dp))
                            Text(
                                stringResource(R.string.lens_label, selectedCamera?.label ?: stringResource(R.string.default_lens_short)),
                                modifier = Modifier.weight(1f),
                            )
                            Icon(
                                if (showCameraPicker) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                stringResource(if (showCameraPicker) R.string.collapse else R.string.expand),
                            )
                        }
                    }
                    if (showCameraPicker && cameras.size > 1) {
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
                }
            }
        }
    }
}

@Composable
@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
private fun CameraPreview(
    selectedCameraId: String?,
    cameraLabels: Map<Int, Triple<String, String, String>>,
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
                    CameraSelector.LENS_FACING_FRONT -> cameraLabels[index + 1]?.first.orEmpty()
                    CameraSelector.LENS_FACING_BACK -> cameraLabels[index + 1]?.second.orEmpty()
                    else -> cameraLabels[index + 1]?.third.orEmpty()
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
    onFocusTap: (Offset, Float, Float) -> Unit,
) {
    Image(
        bitmap.asImageBitmap(),
        stringResource(R.string.overlay),
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
            .graphicsLayer {
                translationX = project.offsetX
                translationY = project.offsetY
                scaleX = project.scale * if (project.mirrorX) -1f else 1f
                scaleY = project.scale * if (project.mirrorY) -1f else 1f
                rotationZ = project.rotation
                alpha = project.opacity
                blendMode = when (project.overlayBlendMode) {
                    1 -> BlendMode.Multiply
                    2 -> BlendMode.Screen
                    else -> BlendMode.SrcOver
                }
            }
            .pointerInput(locked) {
                if (locked) {
                    detectTapGestures { onFocusTap(it, size.width.toFloat(), size.height.toFloat()) }
                } else {
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
    FilledTonalButton(onClick = onClick) {
        Icon(icon, null, Modifier.size(20.dp))
        Spacer(Modifier.size(6.dp))
        Text(label)
    }
}

@Composable
private fun DrawingGrid(
    divisions: Int,
    color: Color,
    opacity: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val count = divisions.coerceIn(2, 12)
        val lineColor = color.copy(alpha = opacity.coerceIn(0.1f, 1f))
        for (index in 1 until count) {
            val x = size.width * index / count
            val y = size.height * index / count
            drawLine(lineColor, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = 1.5f)
            drawLine(lineColor, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = 1.5f)
        }
        drawLine(lineColor, start = Offset(size.width / 2f, 0f), end = Offset(size.width / 2f, size.height), strokeWidth = 3f)
        drawLine(lineColor, start = Offset(0f, size.height / 2f), end = Offset(size.width, size.height / 2f), strokeWidth = 3f)
    }
}

@Composable
private fun WorkSectionMask(
    divisions: Int,
    activeSection: Int,
    opacity: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        val count = divisions.coerceIn(2, 6)
        val safeActive = activeSection.coerceIn(0, count * count - 1)
        val cellWidth = size.width / count
        val cellHeight = size.height / count
        for (index in 0 until count * count) {
            val row = index / count
            val column = index % count
            val topLeft = Offset(column * cellWidth, row * cellHeight)
            if (index != safeActive) {
                drawRect(Color.Black.copy(alpha = opacity), topLeft, androidx.compose.ui.geometry.Size(cellWidth, cellHeight))
            } else {
                drawRect(
                    Color(0xFFE0C8FF), topLeft, androidx.compose.ui.geometry.Size(cellWidth, cellHeight),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(4f),
                )
            }
        }
    }
}

@Composable
private fun rememberBatteryStatus(): BatteryStatus {
    val context = LocalContext.current
    var status by remember { mutableStateOf(BatteryStatus()) }
    DisposableEffect(context) {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val receiver = object : android.content.BroadcastReceiver() {
            override fun onReceive(receiverContext: Context?, intent: Intent?) {
                if (intent == null) return
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100).coerceAtLeast(1)
                val state = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
                status = BatteryStatus(
                    percent = level * 100 / scale,
                    charging = state == BatteryManager.BATTERY_STATUS_CHARGING || state == BatteryManager.BATTERY_STATUS_FULL,
                )
            }
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return status
}

@Composable
private fun thermalLabel(status: Int): String = stringResource(
    when {
        status >= PowerManager.THERMAL_STATUS_SEVERE -> R.string.thermal_severe
        status >= PowerManager.THERMAL_STATUS_MODERATE -> R.string.thermal_warm
        else -> R.string.thermal_normal
    }
)

@Composable
private fun rememberDeviceTilt(): DeviceTilt {
    val context = LocalContext.current
    var tilt by remember { mutableStateOf(DeviceTilt()) }
    DisposableEffect(context) {
        val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val horizontal = Math.toDegrees(atan2(x.toDouble(), sqrt((y * y + z * z).toDouble()))).toFloat()
                val vertical = Math.toDegrees(atan2(y.toDouble(), sqrt((x * x + z * z).toDouble()))).toFloat()
                tilt = DeviceTilt(horizontal, vertical)
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        if (sensor != null) manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        onDispose { manager.unregisterListener(listener) }
    }
    return tilt
}

@androidx.annotation.OptIn(androidx.camera.camera2.interop.ExperimentalCamera2Interop::class)
private fun applyCameraLocks(camera: Camera, enabled: Boolean) {
    val options = CaptureRequestOptions.Builder()
        .setCaptureRequestOption(CaptureRequest.CONTROL_AE_LOCK, enabled)
        .setCaptureRequestOption(CaptureRequest.CONTROL_AWB_LOCK, enabled)
        .build()
    Camera2CameraControl.from(camera.cameraControl).setCaptureRequestOptions(options)
}

private fun captureProgress(
    context: Context,
    capture: ImageCapture?,
    output: File,
    onSaved: (File) -> Unit,
    onFinished: () -> Unit = {},
) {
    if (capture == null) {
        Toast.makeText(context, context.getString(R.string.camera_not_ready), Toast.LENGTH_SHORT).show()
        onFinished()
        return
    }
    val options = ImageCapture.OutputFileOptions.Builder(output).build()
    capture.takePicture(
        options,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                onSaved(output)
                onFinished()
            }
            override fun onError(exception: ImageCaptureException) {
                Toast.makeText(context, context.getString(R.string.photo_save_failed), Toast.LENGTH_LONG).show()
                onFinished()
            }
        },
    )
}
