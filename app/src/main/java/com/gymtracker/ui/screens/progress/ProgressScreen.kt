@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.gymtracker.ui.screens.progress

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.gymtracker.data.entities.PhotoAngle
import com.gymtracker.data.entities.ProgressPhoto
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ProgressScreen(viewModel: ProgressViewModel = hiltViewModel()) {
    val photos by viewModel.photos.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedAngle by remember { mutableStateOf<PhotoAngle?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { selectedUri = it; showAddDialog = true }
    }

    val filtered = if (selectedAngle != null) photos.filter { it.angle == selectedAngle } else photos

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.messageShown()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("İlerleme Fotoğrafları", style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            FloatingActionButton(
                onClick = { launcher.launch("image/*") },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) { Icon(Icons.Default.Add, null) }
        }

        Spacer(Modifier.height(12.dp))

        // Angle filter
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedAngle == null,
                onClick = { selectedAngle = null },
                label = { Text("Tümü") }
            )
            PhotoAngle.values().forEach { angle ->
                FilterChip(
                    selected = selectedAngle == angle,
                    onClick = { selectedAngle = if (selectedAngle == angle) null else angle },
                    label = { Text(angle.displayName) }
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        if (filtered.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PhotoLibrary, null,
                        modifier = Modifier.size(72.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("Henüz fotoğraf yok", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { launcher.launch("image/*") }) { Text("Fotoğraf Ekle") }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered) { photo ->
                    PhotoGridItem(photo, onDelete = { viewModel.deletePhoto(photo) })
                }
            }
        }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        if (uiState.isSaving) {
            Surface(
                modifier = Modifier.align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text("Fotoğraf güvenli alana kopyalanıyor…")
                }
            }
        }
    }

    if (showAddDialog && selectedUri != null) {
        SavePhotoDialog(
            onDismiss = { showAddDialog = false; selectedUri = null },
            onConfirm = { angle, weight, notes ->
                viewModel.savePhoto(selectedUri!!, angle, weight, notes)
                showAddDialog = false; selectedUri = null
            }
        )
    }
}

@Composable
private fun PhotoGridItem(photo: ProgressPhoto, onDelete: () -> Unit) {
    val fmt = SimpleDateFormat("dd MMM yy", Locale("tr"))
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .aspectRatio(0.75f)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        AsyncImage(
            model = photo.photoUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // Overlay
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                .padding(8.dp)
        ) {
            Column {
                Text(fmt.format(Date(photo.date)), fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface)
                Text(photo.angle.displayName, fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.primary)
                photo.weightKg?.let {
                    Text("$it kg", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        IconButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.TopEnd).size(32.dp)
        ) {
            Icon(Icons.Default.Delete, null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun SavePhotoDialog(
    onDismiss: () -> Unit,
    onConfirm: (PhotoAngle, Float?, String) -> Unit
) {
    var selectedAngle by remember { mutableStateOf(PhotoAngle.FRONT) }
    var weight by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Fotoğraf Kaydet") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedAngle.displayName, onValueChange = {}, readOnly = true,
                        label = { Text("Açı") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        PhotoAngle.values().forEach { angle ->
                            DropdownMenuItem(
                                text = { Text(angle.displayName) },
                                onClick = { selectedAngle = angle; expanded = false }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = weight, onValueChange = { weight = it },
                    label = { Text("Kilo (kg, opsiyonel)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes, onValueChange = { notes = it },
                    label = { Text("Not (opsiyonel)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(selectedAngle, weight.toFloatOrNull(), notes) }) { Text("Kaydet") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("İptal") } }
    )
}
