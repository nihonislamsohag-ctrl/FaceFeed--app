package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.credentials.CredentialManager
import coil.compose.AsyncImage
import com.example.data.SavedEmailAccount
import com.example.data.onGoogleSignInClicked
import com.example.data.showPhoneGmailAccountsPicker
import com.example.model.Translations
import com.example.ui.components.FidaVerifiedBadge
import com.example.ui.components.PhoneMediaGalleryDialog
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.util.PrivacyPolicyHelper
import com.example.viewmodel.FaceFeedViewModel

@OptIn(ExperimentalLayoutApi::class)

@Composable
fun AuthDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val savedEmailAccounts by viewModel.savedEmailAccounts.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val authErrorMessage by viewModel.authErrorMessage.collectAsState()
    val canDismiss = isLoggedIn
    val strings = Translations.get(currentLang)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    LaunchedEffect(Unit) {
        viewModel.loadSavedEmailAccounts(context)
        viewModel.clearAuthError()
    }

    var isSignUpTab by remember { mutableStateOf(true) }

    // Sign Up & Log In Fields (clean empty defaults for every new user)
    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("পুরুষ") }
    var country by remember { mutableStateOf("বাংলাদেশ 🇧🇩") }
    var birthDate by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedAvatarUrl by remember { mutableStateOf<String?>(null) }
    var showPhoneGalleryDialog by remember { mutableStateOf(false) }
    var localErrorMessage by remember { mutableStateOf<String?>(null) }

    // Interactive Email/Gmail Account Picker State
    var showSignUpGmailChooser by remember { mutableStateOf(false) }
    var showLoginGmailChooser by remember { mutableStateOf(false) }
    var isFetchingPhoneGmails by remember { mutableStateOf(false) }
    var phoneGmailStatusMessage by remember { mutableStateOf<String?>(null) }
    var showAddGoogleFallback by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selectedAvatarUrl = uri.toString()
        }
    }

    val handlePickPhoneGmails: () -> Unit = {
        isFetchingPhoneGmails = true
        phoneGmailStatusMessage = null
        showAddGoogleFallback = false
        showPhoneGmailAccountsPicker(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { fbUser ->
                isFetchingPhoneGmails = false
                if (fbUser != null) {
                    fbUser.email?.takeIf { it.isNotBlank() }?.let { email = it }
                    fbUser.displayName?.takeIf { it.isNotBlank() }?.let {
                        fullName = it
                        username = it
                    }
                    fbUser.photoUrl?.toString()?.takeIf { it.isNotBlank() }?.let {
                        selectedAvatarUrl = it
                    }
                    viewModel.startCloudSyncForUser(fbUser, context)
                    showSignUpGmailChooser = false
                    showLoginGmailChooser = false
                    onDismiss()
                }
            },
            onNoAccountsOnDevice = {
                isFetchingPhoneGmails = false
                showAddGoogleFallback = true
                phoneGmailStatusMessage =
                    "এই ডিভাইসে এখনো কোনো Gmail লগ-ইন করা নেই। ওপরের তালিকা থেকে বেছে নিন বা নিচে নতুন Gmail যুক্ত করুন।"
            },
            onAuthError = { msg ->
                isFetchingPhoneGmails = false
                phoneGmailStatusMessage = msg
            },
            scope = scope,
            onAuthCancelled = {
                isFetchingPhoneGmails = false
            }
        )
    }

    val handleAddNewGoogleAccount: () -> Unit = {
        isFetchingPhoneGmails = true
        phoneGmailStatusMessage = null
        onGoogleSignInClicked(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = { fbUser ->
                isFetchingPhoneGmails = false
                if (fbUser != null) {
                    fbUser.email?.takeIf { it.isNotBlank() }?.let { email = it }
                    fbUser.displayName?.takeIf { it.isNotBlank() }?.let {
                        fullName = it
                        username = it
                    }
                    fbUser.photoUrl?.toString()?.takeIf { it.isNotBlank() }?.let {
                        selectedAvatarUrl = it
                    }
                    viewModel.startCloudSyncForUser(fbUser, context)
                    showSignUpGmailChooser = false
                    showLoginGmailChooser = false
                    onDismiss()
                }
            },
            onAuthError = { msg ->
                isFetchingPhoneGmails = false
                phoneGmailStatusMessage = msg
            },
            scope = scope,
            onAuthCancelled = {
                isFetchingPhoneGmails = false
            }
        )
    }

    Dialog(
        onDismissRequest = { if (canDismiss) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = canDismiss,
            dismissOnClickOutside = canDismiss
        )
    ) {
        BackHandler(enabled = !canDismiss) {
            // Mandatory Sign Up / Log In gate: users cannot bypass without authenticating
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A).copy(alpha = 0.98f))
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
                    // Close button shown only when already logged in (e.g. switching accounts)
                    if (canDismiss) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = Color.LightGray)
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Tab Selector: Sign Up (left) vs Log In (right)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF0F172A))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSignUpTab) FacebookBlue else Color.Transparent)
                                .clickable {
                                    isSignUpTab = true
                                    showLoginGmailChooser = false
                                    localErrorMessage = null
                                    viewModel.clearAuthError()
                                }
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

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (!isSignUpTab) FacebookBlue else Color.Transparent)
                                .clickable {
                                    isSignUpTab = false
                                    showSignUpGmailChooser = false
                                    localErrorMessage = null
                                    viewModel.clearAuthError()
                                }
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
                        Button(
                            onClick = { showPhoneGalleryDialog = true },
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

                        if (!selectedAvatarUrl.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            AsyncImage(
                                model = selectedAvatarUrl,
                                contentDescription = "নির্বাচিত প্রোফাইল ছবি",
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, FacebookBlue, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        AuthField(
                            label = "${strings.fullName} *",
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                localErrorMessage = null
                                viewModel.clearAuthError()
                            },
                            leadingIcon = Icons.Default.Person,
                            placeholder = "আপনার পুরো নাম লিখুন"
                        )

                        AuthField(
                            label = strings.username,
                            value = username,
                            onValueChange = {
                                username = it
                                localErrorMessage = null
                                viewModel.clearAuthError()
                            },
                            leadingIcon = Icons.Default.AlternateEmail,
                            placeholder = "ইউজারনেম লিখুন (ঐচ্ছিক)"
                        )

                        // Interactive Email / Gmail Field with Device & Saved Accounts Chooser
                        EmailFieldWithAccountChooser(
                            label = "${strings.email} *",
                            value = email,
                            onValueChange = {
                                email = it
                                localErrorMessage = null
                                viewModel.clearAuthError()
                            },
                            leadingIcon = Icons.Default.Email,
                            placeholder = "example@gmail.com",
                            isExpanded = showSignUpGmailChooser,
                            onToggleExpanded = { showSignUpGmailChooser = !showSignUpGmailChooser },
                            onOpenChooser = { showSignUpGmailChooser = true },
                            savedAccounts = savedEmailAccounts,
                            isFetchingPhoneGmails = isFetchingPhoneGmails,
                            statusMessage = phoneGmailStatusMessage,
                            showAddGoogleFallback = showAddGoogleFallback,
                            onSelectSavedAccount = { acc ->
                                email = acc.email
                                fullName = acc.fullName
                                username = acc.username
                                if (acc.avatarUrl.isNotBlank()) {
                                    selectedAvatarUrl = acc.avatarUrl
                                }
                                showSignUpGmailChooser = false
                                localErrorMessage = null
                                viewModel.clearAuthError()
                            },
                            onPickPhoneGmails = handlePickPhoneGmails,
                            onAddNewGoogleAccount = handleAddNewGoogleAccount
                        )

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

                        AuthField(
                            label = strings.country,
                            value = country,
                            onValueChange = { country = it },
                            leadingIcon = Icons.Default.Public,
                            placeholder = "বাংলাদেশ 🇧🇩"
                        )

                        AuthField(
                            label = "${strings.birthDate} *",
                            value = birthDate,
                            onValueChange = {
                                birthDate = it
                                localErrorMessage = null
                                viewModel.clearAuthError()
                            },
                            leadingIcon = Icons.Default.CalendarToday,
                            placeholder = "DD/MM/YYYY (যেমন: 01/05/2004)"
                        )

                        AuthField(
                            label = "${strings.password} *",
                            value = password,
                            onValueChange = {
                                password = it
                                localErrorMessage = null
                                viewModel.clearAuthError()
                            },
                            leadingIcon = Icons.Default.Lock,
                            isPassword = true,
                            passwordVisible = passwordVisible,
                            onTogglePassword = { passwordVisible = !passwordVisible },
                            placeholder = "কমপক্ষে ৬ অক্ষরের পাসওয়ার্ড"
                        )

                        AuthField(
                            label = "${strings.confirmPassword} *",
                            value = confirmPassword,
                            onValueChange = {
                                confirmPassword = it
                                localErrorMessage = null
                                viewModel.clearAuthError()
                            },
                            leadingIcon = Icons.Default.LockReset,
                            isPassword = true,
                            passwordVisible = confirmPasswordVisible,
                            onTogglePassword = { confirmPasswordVisible = !confirmPasswordVisible },
                            placeholder = "পাসওয়ার্ডটি আবার লিখুন"
                        )

                        val displayedError = localErrorMessage ?: authErrorMessage
                        if (!displayedError.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF7F1D1D),
                                border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = displayedError,
                                    color = Color(0xFFFECACA),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Apple ID Sign-In Button
                        Button(
                            onClick = { viewModel.loginWithAppleId(context) },
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

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "সাইন আপ করার মাধ্যমে আপনি FotoFilm এর নীতিমালা মেনে নিচ্ছেন।\n🛡️ ১ ব্যক্তি সর্বোচ্চ ৩টি আইডি • অন্যের ছবি বা বিপরীত লিঙ্গের নামে ফেইক আইডি অটো-ব্লক হবে।",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            lineHeight = 14.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Confirm Account Submit Button
                        Button(
                            onClick = {
                                val trimmedEmail = email.trim()
                                when {
                                    fullName.isBlank() -> {
                                        localErrorMessage = "অনুগ্রহ করে আপনার পুরো নাম লিখুন।"
                                    }
                                    !trimmedEmail.contains("@") || !trimmedEmail.contains(".") -> {
                                        localErrorMessage = "অনুগ্রহ করে সঠিক ইমেইল অ্যাড্রেস লিখুন।"
                                    }
                                    birthDate.isBlank() -> {
                                        localErrorMessage = "অনুগ্রহ করে আপনার জন্মতারিখ লিখুন।"
                                    }
                                    password.length < 6 -> {
                                        localErrorMessage = "পাসওয়ার্ড কমপক্ষে ৬ অক্ষরের হতে হবে।"
                                    }
                                    password != confirmPassword -> {
                                        localErrorMessage = "দুইবারের পাসওয়ার্ড মেলেনি! একই পাসওয়ার্ড দিন।"
                                    }
                                    else -> {
                                        localErrorMessage = null
                                        viewModel.register(
                                            fullName = fullName,
                                            username = username,
                                            email = email,
                                            gender = gender,
                                            country = country,
                                            birthDate = birthDate,
                                            pass = password,
                                            avatarUrl = selectedAvatarUrl,
                                            context = context
                                        )
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("confirm_account_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
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
                            EmailFieldWithAccountChooser(
                                label = "${strings.email} / ${strings.username} *",
                                value = email,
                                onValueChange = {
                                    email = it
                                    localErrorMessage = null
                                    viewModel.clearAuthError()
                                },
                                leadingIcon = Icons.Default.Person,
                                placeholder = "example@gmail.com",
                                isExpanded = showLoginGmailChooser,
                                onToggleExpanded = { showLoginGmailChooser = !showLoginGmailChooser },
                                onOpenChooser = { showLoginGmailChooser = true },
                                savedAccounts = savedEmailAccounts,
                                isFetchingPhoneGmails = isFetchingPhoneGmails,
                                statusMessage = phoneGmailStatusMessage,
                                showAddGoogleFallback = showAddGoogleFallback,
                                onSelectSavedAccount = { acc ->
                                    email = acc.email
                                    showLoginGmailChooser = false
                                    localErrorMessage = null
                                    viewModel.clearAuthError()
                                },
                                onPickPhoneGmails = handlePickPhoneGmails,
                                onAddNewGoogleAccount = handleAddNewGoogleAccount
                            )

                            AuthField(
                                label = "${strings.password} *",
                                value = password,
                                onValueChange = {
                                    password = it
                                    localErrorMessage = null
                                    viewModel.clearAuthError()
                                },
                                leadingIcon = Icons.Default.Lock,
                                isPassword = true,
                                passwordVisible = passwordVisible,
                                onTogglePassword = { passwordVisible = !passwordVisible },
                                placeholder = "আপনার পাসওয়ার্ড লিখুন"
                            )

                            val displayedLoginError = localErrorMessage ?: authErrorMessage
                            if (!displayedLoginError.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFF7F1D1D),
                                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = displayedLoginError,
                                        color = Color(0xFFFECACA),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { viewModel.loginWithAppleId(context) },
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
                                onClick = {
                                    when {
                                        email.isBlank() -> {
                                            localErrorMessage = "অনুগ্রহ করে ইমেইল অ্যাড্রেস অথবা ইউজারনেম লিখুন।"
                                        }
                                        password.length < 6 -> {
                                            localErrorMessage = "অনুগ্রহ করে কমপক্ষে ৬ অক্ষরের পাসওয়ার্ড লিখুন।"
                                        }
                                        else -> {
                                            localErrorMessage = null
                                            viewModel.login(email, password, context)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("login_button"),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Privacy Policy Consent Notice on Login & Sign Up Page
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "By continuing, you agree to our ",
                            color = Color.LightGray,
                            fontSize = 11.5.sp
                        )
                        Text(
                            text = "Privacy Policy",
                            color = Color(0xFF38BDF8),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .testTag("auth_privacy_policy_link")
                                .clickable { PrivacyPolicyHelper.openInBrowser(context) }
                                .padding(horizontal = 2.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

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
                    filePickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    showPhoneGalleryDialog = false
                },
                onDismiss = { showPhoneGalleryDialog = false }
            )
        }
    }
}

@Composable
private fun EmailFieldWithAccountChooser(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    placeholder: String = "",
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onOpenChooser: () -> Unit,
    savedAccounts: List<SavedEmailAccount>,
    isFetchingPhoneGmails: Boolean,
    statusMessage: String?,
    showAddGoogleFallback: Boolean,
    onSelectSavedAccount: (SavedEmailAccount) -> Unit,
    onPickPhoneGmails: () -> Unit,
    onAddNewGoogleAccount: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release) {
                onOpenChooser()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (isExpanded) "বন্ধ করুন ▲" else "সব Gmail দেখুন (${savedAccounts.size}) ▾",
                color = Color(0xFF4ADE80),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { onToggleExpanded() }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auth_email_field"),
            shape = RoundedCornerShape(10.dp),
            singleLine = true,
            placeholder = if (placeholder.isNotBlank()) {
                { Text(text = placeholder, color = Color(0xFF64748B), fontSize = 13.sp) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = onToggleExpanded,
                    modifier = Modifier.testTag("auth_email_dropdown_button")
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "খোলা Gmail একাউন্ট দেখুন",
                        tint = Color(0xFF4ADE80)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF0F172A),
                unfocusedContainerColor = Color(0xFF0F172A),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = FacebookBlue,
                focusedBorderColor = if (isExpanded) Color(0xFF4ADE80) else FacebookBlue,
                unfocusedBorderColor = Color(0xFF334155)
            )
        )

        AnimatedVisibility(visible = isExpanded) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF0B1220),
                border = BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "খোলা Gmail ও ফায়ারবেস অ্যাকাউন্ট (${savedAccounts.size})",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "🔥 Firebase সিঙ্ক",
                            color = Color(0xFF4ADE80),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    savedAccounts.forEach { acc ->
                        val isSelected = value.trim().equals(acc.email.trim(), ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) BangladeshGreen.copy(alpha = 0.22f) else Color(0xFF1E293B),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFF4ADE80) else Color(0xFF334155)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectSavedAccount(acc) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = acc.avatarUrl,
                                    contentDescription = acc.fullName,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = acc.fullName,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (acc.isOwner) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            FidaVerifiedBadge(size = 13.dp)
                                        }
                                    }
                                    Text(
                                        text = acc.email,
                                        color = Color(0xFFCBD5E1),
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (acc.isFirebaseLinked) Color(0xFF14532D) else Color(0xFF334155)
                                ) {
                                    Text(
                                        text = if (acc.isFirebaseLinked) "Firebase ✓" else "সেভড",
                                        color = if (acc.isFirebaseLinked) Color(0xFF4ADE80) else Color.LightGray,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Show all Gmail accounts logged in on this person's Android phone via Credential Manager
                    OutlinedButton(
                        onClick = onPickPhoneGmails,
                        enabled = !isFetchingPhoneGmails,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                            .testTag("pick_phone_gmails_button"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF4ADE80).copy(alpha = 0.65f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF0F172A),
                            contentColor = Color.White
                        )
                    ) {
                        if (isFetchingPhoneGmails) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF4ADE80)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ফোনের Gmail খোঁজা হচ্ছে...", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        } else {
                            Text(
                                text = "G  ফোনের সব খোলা Gmail একাউন্ট দেখুন (Firebase সিঙ্ক)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4ADE80),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (!statusMessage.isNullOrBlank()) {
                        Text(
                            text = statusMessage,
                            color = Color(0xFFFDE68A),
                            fontSize = 10.5.sp,
                            lineHeight = 14.sp
                        )
                    }

                    if (showAddGoogleFallback) {
                        TextButton(
                            onClick = onAddNewGoogleAccount,
                            modifier = Modifier.align(Alignment.End),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "+ এই ডিভাইসে নতুন Google / Gmail লগ-ইন করুন",
                                color = FacebookBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
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
    keyboardType: KeyboardType = KeyboardType.Text,
    placeholder: String = ""
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
            placeholder = if (placeholder.isNotBlank()) {
                { Text(text = placeholder, color = Color(0xFF64748B), fontSize = 13.sp) }
            } else null,
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
