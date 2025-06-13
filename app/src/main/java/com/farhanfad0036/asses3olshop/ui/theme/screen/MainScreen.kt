package com.farhanfad0036.asses3olshop.ui.theme.screen

import ProjekDialog
import android.content.ContentResolver
import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.farhanfad0036.asses3olshop.BuildConfig
import com.farhanfad0036.asses3olshop.R
import com.farhanfad0036.asses3olshop.model.Projek
import com.farhanfad0036.asses3olshop.model.User
import com.farhanfad0036.asses3olshop.network.ApiStatus
import com.farhanfad0036.asses3olshop.network.ProjekApi
import com.farhanfad0036.asses3olshop.network.UserDataStore
import com.farhanfad0036.asses3olshop.ui.theme.theme.Asses3OlshopTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    val cropOptions = CropImageContractOptions(
        null, CropImageOptions(
            imageSourceIncludeGallery = true,
            imageSourceIncludeCamera = true,
            fixAspectRatio = true
        )
    )

    var showDialog by remember { mutableStateOf(false) }
    var showProjekDialog by remember { mutableStateOf(false) }
    var showHapusDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var projekToDelete by remember { mutableStateOf<Projek?>(null) }
    var projekToEdit by remember { mutableStateOf<Projek?>(null) }

    var isEditing by remember { mutableStateOf(false) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCropperImage(context.contentResolver, it)
        if (bitmap != null) {
            if (isEditing) {
                showEditDialog = true
            } else {
                showProjekDialog = true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                        }
                        else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_account_circle_24),
                            contentDescription = stringResource(R.string.profil),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton =  {
            FloatingActionButton(onClick = {
                if (user.email.isEmpty()) {
                    Toast.makeText(context, "Harus login terlebih dahulu", Toast.LENGTH_SHORT).show()
                } else {
                    launcher.launch(cropOptions)
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.tambah_projek)
                )
            }
        }
    ) { innerPadding ->
        ScreenContent(
            viewModel = viewModel,
            userId = user.email,
            onDeleteClick = { projek ->
                projekToDelete = projek
                showHapusDialog = true
            },
            onEditItem = { projek ->
                projekToEdit = projek
                isEditing = true
                launcher.launch(cropOptions)
            },
            modifier = Modifier.padding(innerPadding)
        )

        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                showDialog = false
            }
        }

        if (showProjekDialog) {
            ProjekDialog(
                bitmap = bitmap,
                onDismissRequest = { showProjekDialog = false }) { semester, mataKuliah ->
                viewModel.saveData(user.email, semester, mataKuliah, bitmap!!)
                showProjekDialog = false
            }
        }

        if (showEditDialog) {
            projekToEdit?.let { projek ->
                ProjekEditDialog(
                    projek = projek,
                    bitmap = bitmap,
                    onDismissRequest = {
                        showEditDialog = false
                        isEditing = false
                    },
                    onConfirmation = { semester, mataKuliah ->
                        viewModel.updateData(
                            projek.id.toString(),
                            user.email,
                            semester,
                            mataKuliah,
                            bitmap!!
                        )
                        showEditDialog = false
                        isEditing = false
                    }
                )
            }
        }

        if (showHapusDialog) {
            DisplayAlertDialog(
                onDismissRequest = {
                    showHapusDialog = false
                    projekToDelete = null
                },
                onConfirmation = {
                    projekToDelete?.let { projek ->
                        viewModel.deletedData(user.email, projek.id)
                    }
                }
            )
        }

        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(
    viewModel: MainViewModel,
    userId: String,
    onDeleteClick: (Projek) -> Unit,
    onEditItem: (Projek) -> Unit,
    modifier: Modifier = Modifier
) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    LaunchedEffect(userId) {
        viewModel.retrieveData(userId)
    }

    when (status) {
        ApiStatus.LOADING -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        ApiStatus.SUCCESS -> {
            val context = LocalContext.current
            LazyVerticalGrid(
                modifier = modifier.fillMaxSize().padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                itemsIndexed(data) { index, projek ->
                    val showDeleteButton = index >= 1
                    ListItem(
                        projek = projek,
                        onItemClick = {
                            if (index == 0) {
                                Toast.makeText(
                                    context,
                                    "Data ini tidak bisa diedit",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onEditItem(projek)
                            }
                        },
                        onDeleteClick = { onDeleteClick(projek) },
                        showDeleteButton = showDeleteButton
                    )
                    Log.d("DEBUG_DELETE", "Index: $index, Semester ${projek.semester}, ShowDeleteButton: $showDeleteButton")
                }
            }
        }

        ApiStatus.FAILED -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = { viewModel.retrieveData(userId) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
}

@Composable
fun ListItem(
    projek: Projek,
    onDeleteClick: () -> Unit,
    onItemClick: () -> Unit,
    showDeleteButton: Boolean
) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onItemClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(ProjekApi.getProjekUrl(projek.gambar))
                .crossfade(true)
                .build(),
            contentDescription = stringResource(R.string.gambar, projek.semester),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.loading_img),
            error = painterResource(id = R.drawable.broken_img),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        )
        if (showDeleteButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(35.dp)
                    .background(
                        Color.Black.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                    .clickable { onDeleteClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)
                )
                .padding(8.dp)
        ) {
            Text(
                text = projek.semester,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = projek.mataKuliah,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()
    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(
    result: GetCredentialResponse,
    dataStore: UserDataStore
) {
    val credential = result.credential
    if (credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.savedData(User(nama, email,photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    }
    else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.savedData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCropperImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("Image", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Composable
fun ProjekEditDialog(
    projek: Projek,
    bitmap: Bitmap?,
    onDismissRequest: () -> Unit,
    onConfirmation: (String, String) -> Unit
) {
    var semester by remember { mutableStateOf(projek.semester) }
    var mataKuliah by remember { mutableStateOf(projek.mataKuliah) }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Edit Projek",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = semester,
                    onValueChange = { semester = it },
                    label = { Text("Semester") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = mataKuliah,
                    onValueChange = { mataKuliah = it },
                    label = { Text("Mata Kuliah") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onDismissRequest() }) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        onConfirmation(semester, mataKuliah)
                    }) {
                        Text("Simpan Perubahan")
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun MainScreenPreview() {
    Asses3OlshopTheme {
        MainScreen()
    }
}