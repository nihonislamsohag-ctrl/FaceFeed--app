package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.Translations
import com.example.ui.components.PhoneMediaGalleryDialog
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun AuthDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)

    var isSignUpTab by remember { mutableStateOf(true) }

    // Sign Up Fields
    var fullName by remember { mutableStateOf("Niyon Islam Sohag") }
    var username by remember { mutableStateOf("Niyon Islam Sohag") }
    var email by remember { mutableStateOf("smd224292@gmail.com") }
    var gender by remember { mutableStateOf("পুরুষ") }
    var country by remember { mutableStateOf("বাংলাদেশ 🇧🇩") }
    var birthDate by remember { mutableStateOf("01/05/2004") }
    var password by remember { mutableStateOf("12345678") }
    var confirmPassword by remember { mutableStateOf("12345678") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedAvatarUrl by remember { mutableStateOf<String?>("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500") }
    var showPhoneGalleryDialog by remember { mutableStateOf(false) }

    // Preset avatars as shown in user screenshot
    val presetAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
        "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500",
        "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=500",
        "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=500"
    )

    // Storage Picker for avatar upload
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedAvatarUrl = uri.toString()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A).copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Close button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = Color.LightGray)
                        }
                    }

                    // Tab Selector: Sign Up vs Log In (matching user screenshot)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Sign Up Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSignUpTab) FacebookBlue else Color.Transparent)
                                .clickable { isSignUpTab = true }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.signUp,
                                color = Color.White,
                                fontWeight = if (isSignUpTab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }

                        // Log In Tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isSignUpTab) FacebookBlue else Color.Transparent)
                                .clickable { isSignUpTab = false }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = strings.login,
                                color = Color.White,
                                fontWeight = if (!isSignUpTab) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isSignUpTab) {
                        // Upload photo from phone button (with camera launcher)
                        Button(
                            onClick = {
                                showPhoneGalleryDialog = true
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ফোন থেকে ছবি আপলোড করুন",
                                    color = FacebookBlue,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Or choose from presets text
                        Text(
                            text = "অথবা নিচের যেকোনো একটি ছবি বেছে নিন:",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Preset Avatars row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            presetAvatars.forEach { url ->
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .border(
                                            width = if (selectedAvatarUrl == url) 2.5.dp else 0.dp,
                                            color = if (selectedAvatarUrl == url) FacebookBlue else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { selectedAvatarUrl = url }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Avatar",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Full Name
                        AuthField(
                            label = "${strings.fullName} *",
                            value = fullName,
                            onValueChange = { fullName = it },
                            leadingIcon = Icons.Default.Person
                        )

                        // Username
                        AuthField(
                            label = strings.username,
                            value = username,
                            onValueChange = { username = it },
                            leadingIcon = Icons.Default.AlternateEmail
                        )

                        // Email
                        AuthField(
                            label = "${strings.email} *",
                            value = email,
                            onValueChange = { email = it },
                            leadingIcon = Icons.Default.Email,
                            keyboardType = KeyboardType.Email
                        )

                        // Gender Selector (পুরুষ ও নারী)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = strings.gender,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                listOf(strings.male, strings.female).forEach { g ->
                                    val isSelected = gender == g
                                    Button(
                                        onClick = { gender = g },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isSelected) FacebookBlue else Color(0xFF0F172A)
                                        )
                                    ) {
                                        Text(text = g, color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Country (কোন দেশ)
                        AuthField(
                            label = strings.country,
                            value = country,
                            onValueChange = { country = it },
                            leadingIcon = Icons.Default.Public
                        )

                        // Date of birth (জন্মতারিখ)
                        AuthField(
                            label = "${strings.birthDate} *",
                            value = birthDate,
                            onValueChange = { birthDate = it },
                            leadingIcon = Icons.Default.CalendarToday
                        )

                        // Password with eye icon
                        AuthField(
                            label = "${strings.password} *",
                            value = password,
                            onValueChange = { password = it },
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible }
                        )

                        // Confirm Password with eye icon
                        AuthField(
                            label = "${strings.confirmPassword} *",
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            leadingIcon = Icons.Default.LockReset,
                            isPassword = true,
                            passwordVisible = confirmPasswordVisible,
                            onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Apple ID Sign-In Button (user explicitly requested: "সাথে আই ফোনে এপেট আইডি এটিও এড করা থাকবে")
                        Button(
                            onClick = { viewModel.loginWithAppleId() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = strings.appleIdLogin,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Terms text
                        Text(
                            text = "সাইন আপ করার মাধ্যমে আপনি FaceFeed এর নীতিমালা মেনে নিচ্ছেন।",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Confirm Account Submit Button
                        Button(
                            onClick = {
                                viewModel.register(
                                    fullName = fullName,
                                    username = username,
                                    email = email,
                                    gender = gender,
                                    country = country,
                                    birthDate = birthDate,
                                    pass = password,
                                    avatarUrl = selectedAvatarUrl
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = strings.confirmAccount,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                            }
                        }

                    } else {
                        // Log In View
                        Column(modifier = Modifier.fillMaxWidth()) {
                            AuthField(
                                label = "${strings.email} / ${strings.username} *",
                                value = email,
                                onValueChange = { email = it },
                                leadingIcon = Icons.Default.Person
                            )

                            AuthField(
                                label = "${strings.password} *",
                                value = password,
                                onValueChange = { password = it },
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onTogglePassword = { passwordVisible = !passwordVisible }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Apple ID Sign-In
                            Button(
                                onClick = { viewModel.loginWithAppleId() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(strings.appleIdLogin, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.login(email, password) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                            ) {
                                Text(
                                    text = strings.login,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Phone Media Gallery & Camera Studio
        if (showPhoneGalleryDialog) {
            PhoneMediaGalleryDialog(
                title = "প্রোফাইল ছবি নির্বাচন",
                allowMultiple = false,
                onMediaSelected = { urls ->
                    urls.firstOrNull()?.let { url ->
                        selectedAvatarUrl = url
                    }
                    showPhoneGalleryDialog = false
                },
                onLaunchSystemFilePicker = {
                    filePickerLauncher.launch("image/*")
                    showPhoneGalleryDialog = false
                },
                onDismiss = { showPhoneGalleryDialog = false }
            )
        }
    }
}

@Composable
fun AuthField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            leadingIcon = {
                Icon(imageVector = leadingIcon, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(18.dp))
            },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle Password",
                            tint = Color.LightGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else null,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = FacebookBlue,
                focusedBorderColor = FacebookBlue,
                unfocusedBorderColor = Color(0xFF334155)
            )
        )
    }
}
