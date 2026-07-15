@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.ui.screens.progress

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gymtracker.R
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import com.gymtracker.data.photos.MAX_PROGRESS_PHOTO_NOTES_LENGTH
import com.gymtracker.domain.parseLocalizedDecimal
import com.gymtracker.ui.components.GtEmptyState
import com.gymtracker.ui.components.GtSectionHeader
import com.gymtracker.ui.theme.GtSize
import com.gymtracker.ui.theme.GtSpacing
import java.text.DateFormat
import java.util.Date

@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedUri = uri
    }
    val messageText = uiState.message?.let { progressMessageText(it) }

    LaunchedEffect(messageText) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            viewModel.messageShown()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(onClick = { launcher.launch("image/*") }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.progress_photos_add))
            }
        }
    ) { padding ->
        ProgressPhotoContent(
            photos = photos,
            uiState = uiState,
            onAddPhoto = { launcher.launch("image/*") },
            onDeletePhoto = viewModel::deletePhoto,
            modifier = Modifier.padding(padding)
        )
    }

    selectedUri?.let { uri ->
        SavePhotoDialog(
            isSaving = uiState.isSaving,
            onDismiss = { if (!uiState.isSaving) selectedUri = null },
            onConfirm = { angle, weight, notes ->
                viewModel.savePhoto(uri, angle, weight, notes) { selectedUri = null }
            }
        )
    }
}

@Composable
fun ProgressPhotoContent(
    photos: List<ProgressPhoto>,
    uiState: ProgressPhotoUiState,
    onAddPhoto: () -> Unit,
    onDeletePhoto: (ProgressPhoto) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedAngleName by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedPhoto by remember { mutableStateOf<ProgressPhoto?>(null) }
    var pendingDelete by remember { mutableStateOf<ProgressPhoto?>(null) }
    val selectedAngle = selectedAngleName?.let { PhotoAngle.valueOf(it) }
    val filtered = selectedAngle?.let { angle -> photos.filter { it.angle == angle } } ?: photos

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (filtered.isEmpty()) Modifier.verticalScroll(rememberScrollState())
                    else Modifier
                )
                .padding(horizontal = GtSpacing.md)
                .testTag("progress-photo-content"),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.md)
        ) {
        GtSectionHeader(
            title = stringResource(R.string.progress_photos_title),
            supportingText = stringResource(R.string.progress_photos_subtitle),
            modifier = Modifier.padding(top = GtSpacing.lg)
        )
        Text(
            text = stringResource(R.string.progress_photos_device_only),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(GtSpacing.xs),
            contentPadding = PaddingValues(end = GtSpacing.xl)
        ) {
            item {
                FilterChip(
                    selected = selectedAngle == null,
                    onClick = { selectedAngleName = null },
                    label = { Text(stringResource(R.string.progress_photos_all)) }
                )
            }
            items(PhotoAngle.values(), key = PhotoAngle::name) { angle ->
                FilterChip(
                    selected = selectedAngle == angle,
                    onClick = { selectedAngleName = if (selectedAngle == angle) null else angle.name },
                    label = { Text(angleLabel(angle)) }
                )
            }
        }

        if (filtered.isEmpty()) {
            GtEmptyState(
                title = stringResource(
                    if (photos.isEmpty()) R.string.progress_photos_empty_title
                    else R.string.progress_photos_filter_empty_title
                ),
                body = stringResource(
                    if (photos.isEmpty()) R.string.progress_photos_empty_body
                    else R.string.progress_photos_filter_empty_body
                ),
                actionLabel = if (photos.isEmpty()) stringResource(R.string.progress_photos_first_action) else null,
                onAction = if (photos.isEmpty()) onAddPhoto else null
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("progress-photo-grid"),
                horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(GtSpacing.sm),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(filtered, key = ProgressPhoto::id) { photo ->
                    PhotoGridItem(
                        photo = photo,
                        onOpen = { selectedPhoto = photo },
                        onDelete = { pendingDelete = photo }
                    )
                }
            }
        }
        }

        if (uiState.isSaving) {
            Surface(
                modifier = Modifier
                    .align(Alignment.Center)
                    .testTag("progress-photo-saving"),
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(GtSpacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(GtSpacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(GtSize.iconMedium))
                    Text(stringResource(R.string.progress_photos_saving))
                }
            }
        }
    }

    selectedPhoto?.let { photo ->
        PhotoViewerDialog(photo = photo, onDismiss = { selectedPhoto = null })
    }

    pendingDelete?.let { photo ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(stringResource(R.string.progress_photos_delete_title)) },
            text = { Text(stringResource(R.string.progress_photos_delete_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeletePhoto(photo)
                        if (selectedPhoto?.id == photo.id) selectedPhoto = null
                        pendingDelete = null
                    }
                ) { Text(stringResource(R.string.action_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun PhotoGridItem(
    photo: ProgressPhoto,
    onOpen: () -> Unit,
    onDelete: () -> Unit
) {
    var loadFailed by remember(photo.photoUri) { mutableStateOf(false) }
    val date = remember(photo.date) { DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(photo.date)) }
    val description = stringResource(R.string.progress_photos_item_description, date, angleLabel(photo.angle))
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (photo.storageVersion == 0) Modifier.testTag("progress-photo-legacy") else Modifier)
            .clickable(onClick = onOpen, role = Role.Button)
            .semantics(mergeDescendants = true) {
                contentDescription = description
                role = Role.Button
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            AsyncImage(
                model = photo.thumbnailUri ?: photo.photoUri,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                onError = { loadFailed = true },
                onSuccess = { loadFailed = false }
            )
            if (loadFailed) {
                Icon(
                    Icons.Default.BrokenImage,
                    contentDescription = stringResource(R.string.progress_photos_unavailable),
                    modifier = Modifier
                        .size(48.dp)
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            IconButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.progress_photos_delete_description, date),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            if (photo.storageVersion == 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(GtSpacing.xs),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        stringResource(R.string.progress_photos_legacy_badge),
                        modifier = Modifier.padding(horizontal = GtSpacing.xs, vertical = GtSpacing.xxs),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        Column(
            modifier = Modifier.padding(GtSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(GtSpacing.xxs)
        ) {
            Text(date, style = MaterialTheme.typography.titleMedium)
            Text(angleLabel(photo.angle), color = MaterialTheme.colorScheme.primary)
            photo.weightKg?.let {
                Text(stringResource(R.string.progress_photos_weight_value, it))
            }
            if (photo.notes.isNotBlank()) {
                Text(
                    photo.notes,
                    maxLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PhotoViewerDialog(photo: ProgressPhoto, onDismiss: () -> Unit) {
    val date = remember(photo.date) { DateFormat.getDateInstance(DateFormat.LONG).format(Date(photo.date)) }
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
                Box {
                    AsyncImage(
                        model = photo.photoUri,
                        contentDescription = stringResource(
                            R.string.progress_photos_item_description,
                            date,
                            angleLabel(photo.angle)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 520.dp)
                            .clip(MaterialTheme.shapes.large),
                        contentScale = ContentScale.Fit
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = stringResource(R.string.action_close))
                    }
                }
                Column(
                    modifier = Modifier.padding(start = GtSpacing.md, end = GtSpacing.md, bottom = GtSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(GtSpacing.xs)
                ) {
                    Text(date, style = MaterialTheme.typography.titleLarge)
                    Text(angleLabel(photo.angle), color = MaterialTheme.colorScheme.primary)
                    photo.weightKg?.let { Text(stringResource(R.string.progress_photos_weight_value, it)) }
                    if (photo.notes.isNotBlank()) Text(photo.notes)
                    if (photo.storageVersion == 0) {
                        Text(
                            stringResource(R.string.progress_photos_legacy_support),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavePhotoDialog(
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (PhotoAngle, Float?, String) -> Unit
) {
    var selectedAngleName by rememberSaveable { mutableStateOf(PhotoAngle.FRONT.name) }
    var weight by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val selectedAngle = PhotoAngle.valueOf(selectedAngleName)
    val parsedWeight = weight.takeIf(String::isNotBlank)?.let(::parseLocalizedDecimal)
    val weightValid = weight.isBlank() || parsedWeight?.let { it in 20f..400f } == true
    val notesValid = notes.length <= MAX_PROGRESS_PHOTO_NOTES_LENGTH

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.progress_photos_save_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(GtSpacing.sm)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = angleLabel(selectedAngle),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.progress_photos_angle)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        PhotoAngle.values().forEach { angle ->
                            DropdownMenuItem(
                                text = { Text(angleLabel(angle)) },
                                onClick = {
                                    selectedAngleName = angle.name
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text(stringResource(R.string.progress_photos_weight_optional)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = !weightValid,
                    supportingText = if (!weightValid) {
                        { Text(stringResource(R.string.progress_photos_weight_error)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(R.string.progress_photos_notes_optional)) },
                    isError = !notesValid,
                    supportingText = {
                        Text(stringResource(R.string.progress_photos_notes_count, notes.length, MAX_PROGRESS_PHOTO_NOTES_LENGTH))
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                enabled = weightValid && notesValid && !isSaving,
                onClick = { onConfirm(selectedAngle, parsedWeight, notes) }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(enabled = !isSaving, onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun angleLabel(angle: PhotoAngle): String = stringResource(
    when (angle) {
        PhotoAngle.FRONT -> R.string.progress_photos_angle_front
        PhotoAngle.BACK -> R.string.progress_photos_angle_back
        PhotoAngle.SIDE_LEFT -> R.string.progress_photos_angle_left
        PhotoAngle.SIDE_RIGHT -> R.string.progress_photos_angle_right
    }
)

@Composable
private fun progressMessageText(message: ProgressPhotoMessage): String = stringResource(
    when (message) {
        ProgressPhotoMessage.SAVED -> R.string.progress_photos_saved
        ProgressPhotoMessage.DELETED -> R.string.progress_photos_deleted
        ProgressPhotoMessage.SAVE_FAILED -> R.string.progress_photos_save_failed
        ProgressPhotoMessage.DELETE_FAILED -> R.string.progress_photos_delete_failed
        ProgressPhotoMessage.MAINTENANCE_FAILED -> R.string.progress_photos_maintenance_failed
        ProgressPhotoMessage.WEIGHT_OUT_OF_RANGE -> R.string.progress_photos_weight_error
        ProgressPhotoMessage.NOTES_TOO_LONG -> R.string.progress_photos_notes_error
    }
)
