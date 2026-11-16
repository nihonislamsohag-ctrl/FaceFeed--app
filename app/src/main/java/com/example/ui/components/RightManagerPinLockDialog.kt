package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.BangladeshRed
import com.example.viewmodel.FaceFeedViewModel

/**
 * Right Manager Fida Master Password Lock Screen (রাইট ম্যানেজার ফিদা তালা 🔒)
 * 1. Exclusively restricted to Niyon Islam Sohag only
 * 2. Master Password: 8-digit PIN (never displayed or hinted below on screen)
 * 3. Change Password functionality for owner Niyon Islam Sohag
 */
@Composable
fun RightManagerPinLockDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    if (!viewModel.isCurrentUserNiyonIslamSohag()) {
        LaunchedEffect(currentUser.id) {
            onDismiss()
        }
        return
    }

    val currentPin by viewModel.rightManagerPin.collectAsState()
    val pinLength = currentPin.length.coerceAtLeast(4)
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showChangePinDialog by remember { mutableStateOf(false) }

    // Pulsing lock animation
    val infiniteTransition = rememberInfiniteTransition(label = "lock_pulse")
    val lockScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Auto verify when full password length is entered
    LaunchedEffect(enteredPin, pinLength) {
        if (enteredPin.length == pinLength) {
            if (viewModel.unlockRightManager(enteredPin)) {
                isError = false
                errorMessage = null
            } else {
                isError = true
                errorMessage = "❌ ভুল পাসওয়ার্ড! আবার চেষ্টা করুন।"
                kotlinx.coroutines.delay(950)
                enteredPin = ""
                isError = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Bar with Back Button & Change Password Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    TextButton(onClick = { showChangePinDialog = true }) {
                        Icon(
                            Icons.Default.VpnKey,
                            contentDescription = null,
                            tint = BangladeshGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "পাসওয়ার্ড পরিবর্তন",
                            fontSize = 12.sp,
                            color = BangladeshGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Middle Content: Lock Icon, Title, Masked Dots Indicator (NO password hint shown)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .scale(lockScale)
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(Color(0xFFFF5252), Color(0xFFD50000))
                                )
                            )
                            .border(3.dp, Color(0xFFFFD700), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock",
                            tint = Color.White,
                            modifier = Modifier.size(46.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "রাইট ম্যানেজার ফিদা (তালা 🔒)",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "শুধুমাত্র Niyon Islam Sohag গোপন পাসওয়ার্ড দিয়ে প্রবেশ করুন",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Masked Dot Indicator (Supports 8-digit password cleanly on one row)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(if (pinLength > 6) 10.dp else 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pinLength) { index ->
                            val isFilled = index < enteredPin.length
                            Box(
                                modifier = Modifier
                                    .size(if (pinLength > 6) 14.dp else 18.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isError) Color.Red
                                        else if (isFilled) BangladeshGreen
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isError) Color.Red
                                        else if (isFilled) BangladeshGreen
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }

                    // Show ONLY error message if wrong password entered — NEVER show password below!
                    Spacer(modifier = Modifier.height(14.dp))
                    Box(
                        modifier = Modifier.heightIn(min = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (errorMessage != null) {
                            Text(
                                text = errorMessage!!,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Red,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Numeric Keypad (1 to 9, Clear, 0, Backspace)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val keypadRows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf("C", "0", "⌫")
                    )

                    keypadRows.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            row.forEach { key ->
                                KeypadButton(
                                    key = key,
                                    onClick = {
                                        errorMessage = null
                                        isError = false
                                        when (key) {
                                            "C" -> enteredPin = ""
                                            "⌫" -> {
                                                if (enteredPin.isNotEmpty()) {
                                                    enteredPin = enteredPin.dropLast(1)
                                                }
                                            }
                                            else -> {
                                                if (enteredPin.length < pinLength) {
                                                    enteredPin += key
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Change Password Dialog (No secret leakage)
    if (showChangePinDialog) {
        ChangePinDialog(
            onDismiss = { showChangePinDialog = false },
            onChangePin = { oldPin, newPin ->
                if (viewModel.changeRightManagerPin(oldPin, newPin)) {
                    showChangePinDialog = false
                    enteredPin = ""
                    true
                } else {
                    false
                }
            }
        )
    }
}

@Composable
private fun KeypadButton(
    key: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (key == "⌫") {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Backspace",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text(
                    text = key,
                    fontSize = if (key == "C") 20.sp else 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (key == "C") BangladeshRed else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ChangePinDialog(
    onDismiss: () -> Unit,
    onChangePin: (String, String) -> Boolean
) {
    var oldPinInput by remember { mutableStateOf("") }
    var newPinInput by remember { mutableStateOf("") }
    var confirmPinInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LockReset, contentDescription = null, tint = BangladeshGreen)
                Spacer(modifier = Modifier.width(8.dp))
                Text("গোপন পাসওয়ার্ড পরিবর্তন", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = oldPinInput,
                    onValueChange = { input ->
                        if (input.length <= 8 && input.all { it.isDigit() }) {
                            oldPinInput = input
                            errorText = null
                        }
                    },
                    label = { Text("পুরোনো গোপন পাসওয়ার্ড") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newPinInput,
                    onValueChange = { input ->
                        if (input.length <= 8 && input.all { it.isDigit() }) {
                            newPinInput = input
                            errorText = null
                        }
                    },
                    label = { Text("নতুন পাসওয়ার্ড (৪-৮ ডিজিট)") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = confirmPinInput,
                    onValueChange = { input ->
                        if (input.length <= 8 && input.all { it.isDigit() }) {
                            confirmPinInput = input
                            errorText = null
                        }
                    },
                    label = { Text("নতুন পাসওয়ার্ড পুনরায় লিখুন") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                errorText?.let {
                    Text(text = it, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (newPinInput.length !in 4..8) {
                        errorText = "❌ নতুন পাসওয়ার্ড ৪ থেকে ৮ ডিজিটের হতে হবে!"
                    } else if (newPinInput != confirmPinInput) {
                        errorText = "❌ নতুন দুটি পাসওয়ার্ড মেলেনি!"
                    } else if (!onChangePin(oldPinInput, newPinInput)) {
                        errorText = "❌ পুরোনো পাসওয়ার্ড সঠিক নয়!"
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
            ) {
                Text("সেভ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
