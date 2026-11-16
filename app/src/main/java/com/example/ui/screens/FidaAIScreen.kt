package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ai.FidaAIService
import com.example.ui.components.FidaAILogo
import com.example.ui.components.FidaVerifiedBadge
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.OnlineGreen
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FidaAIMessage(
    val id: String = System.currentTimeMillis().toString(),
    val isUser: Boolean,
    val text: String,
    val imageUri: Uri? = null,
    val timestamp: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
    val isLiked: Boolean? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FidaAIScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isThinking by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    // Persistent conversation history (never deleted, loaded from storage)
    val messages = remember {
        mutableStateListOf<FidaAIMessage>().apply {
            addAll(com.example.data.FidaAIPersistenceManager.loadMessages(context))
        }
    }

    LaunchedEffect(Unit) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    // Photo & Screenshot Picker (Zero-permission Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(context, "ছবি বা স্ক্রিনশট সংযুক্ত হয়েছে 📷", Toast.LENGTH_SHORT).show()
        }
    }

    // System File picker fallback for documents / screenshot files
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(context, "ছবি বা স্ক্রিনশট সংযুক্ত হয়েছে 📷", Toast.LENGTH_SHORT).show()
        }
    }

    BackHandler {
        onBack()
    }

    fun sendMessage(query: String = inputText) {
        val trimmedQuery = query.trim()
        val imageUri = selectedImageUri

        if (trimmedQuery.isBlank() && imageUri == null) return

        val userMsg = FidaAIMessage(
            isUser = true,
            text = trimmedQuery,
            imageUri = imageUri
        )
        messages.add(userMsg)
        com.example.data.FidaAIPersistenceManager.saveAllMessages(context, messages)

        // Clear input state immediately
        inputText = ""
        selectedImageUri = null
        isThinking = true

        coroutineScope.launch {
            // Scroll to bottom
            listState.animateScrollToItem(messages.size - 1)

            // Convert image to Base64 if present
            var base64Data: String? = null
            var mimeType = "image/jpeg"
            if (imageUri != null) {
                val conversion = FidaAIService.uriToBase64(context, imageUri)
                if (conversion != null) {
                    base64Data = conversion.first
                    mimeType = conversion.second
                }
            }

            // Call Fida AI with Gemini
            val aiResponseText = FidaAIService.askFidaAI(
                prompt = trimmedQuery,
                imageBase64 = base64Data,
                mimeType = mimeType
            )

            isThinking = false
            val aiMsg = FidaAIMessage(
                isUser = false,
                text = aiResponseText
            )
            messages.add(aiMsg)
            com.example.data.FidaAIPersistenceManager.saveAllMessages(context, messages)

            // Scroll to newly received answer
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FidaAILogo(size = 36.dp, animated = isThinking)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ফিদা এআই",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                FidaVerifiedBadge(size = 16.dp)
                                Spacer(modifier = Modifier.width(5.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF6A1B9A)
                                ) {
                                    Text(
                                        text = "AI",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(OnlineGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isThinking) "উত্তর লিখছে..." else "অনলাইন • সর্বদা প্রস্তুত",
                                    fontSize = 11.sp,
                                    color = if (isThinking) Color(0xFF9C27B0) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showHelpDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Help",
                            tint = Color(0xFF7E57C2)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Suggestion quick chips when chat is fresh
            if (messages.size <= 2) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val promptChips = listOf(
                        "📸 ছবি/স্ক্রিনশট দেখে প্রশ্নের উত্তর দিন" to "এই স্ক্রিনশট বা ছবির প্রশ্নের সঠিক সমাধান কী?",
                        "💡 যেকোনো প্রশ্নের সমাধান" to "কীভাবে পড়াশোনায় মনোযোগ বাড়ানো যায়?",
                        "✍️ সুন্দর স্ট্যাটাস ও ক্যাপশন" to "সোশ্যাল মিডিয়ার জন্য কিছু সুন্দর স্ট্যাটাস ক্যাপশন দিন",
                        "🌐 ইংরেজি থেকে বাংলা অনুবাদ" to "Translate into Bengali: FotoFilm connects people with creativity and friendship.",
                        "🌸 ফিদা এআই কী করতে পারে?" to "ফিদা এআই তুমি আমাকে কী কী কাজে সাহায্য করতে পারো?"
                    )

                    items(promptChips) { (chipLabel, promptText) ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF3E5F5),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCE93D8)),
                            modifier = Modifier.clickable { sendMessage(promptText) }
                        ) {
                            Text(
                                text = chipLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF6A1B9A),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Messages List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(messages) { message ->
                    FidaAIMessageBubble(
                        message = message,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Fida AI", message.text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "টেক্সট কপি করা হয়েছে 📋", Toast.LENGTH_SHORT).show()
                        },
                        onFeedback = { isLike ->
                            val index = messages.indexOf(message)
                            if (index != -1) {
                                messages[index] = message.copy(isLiked = isLike)
                                val msg = if (isLike) "ধন্যবাদ! ফিডব্যাক গ্রহণ করা হয়েছে 👍" else "ফিডব্যাক গ্রহণ করা হয়েছে 👎"
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }

                // Thinking Indicator Animation
                if (isThinking) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 6.dp, top = 4.dp, bottom = 4.dp)
                        ) {
                            FidaAILogo(size = 28.dp, animated = true)
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ফিদা এআই চিন্তা করছে ও উত্তর তৈরি করছে",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF6A1B9A)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    CircularProgressIndicator(
                                        strokeWidth = 2.dp,
                                        color = Color(0xFF8E24AA),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Attached Image Preview Pill before sending
            AnimatedVisibility(visible = selectedImageUri != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = BangladeshGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "ছবি বা স্ক্রিনশট সংযুক্ত হয়েছে",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Text(
                                text = "এই ছবি সম্পর্কে যেকোনো প্রশ্ন লিখে সেন্ড করুন",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { selectedImageUri = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray)
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Photo / Screenshot Picker Button (Primary Multimodal Trigger)
                    IconButton(
                        onClick = {
                            try {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } catch (e: Exception) {
                                filePickerLauncher.launch("image/*")
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3E5F5))
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Attach Photo / Screenshot",
                            tint = Color(0xFF7E57C2),
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Text Input Field
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = if (selectedImageUri != null) "ছবি নিয়ে প্রশ্ন লিখুন..." else "যেকোনো কিছু লিখুন বা প্রশ্ন করুন...",
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(24.dp),
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { sendMessage() })
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Send Button
                    val canSend = inputText.isNotBlank() || selectedImageUri != null
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                brush = if (canSend) {
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8E24AA), Color(0xFF673AB7))
                                    )
                                } else {
                                    Brush.linearGradient(
                                        colors = listOf(Color.LightGray, Color.Gray)
                                    )
                                }
                            )
                            .clickable(enabled = canSend) { sendMessage() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    // Help & Information Dialog
    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FidaAILogo(size = 28.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ফিদা এআই (Fida AI) সম্পর্কে",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color(0xFF006A4E)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "ফিদা এআই হলো FotoFilm মেসেঞ্জারের অফিশিয়াল ইন্টেলিজেন্ট এআই সহকারী। এটিকে Niyon Islam Sohag ভাই এটি ডিজিটাল প্রযুক্তিতে পরিচালিত।",
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "প্রধান সুবিধাসমূহ:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF006A4E)
                    )

                    val features = listOf(
                        "যেকোনো বিষয়ে বাংলা বা ইংরেজিতে প্রশ্ন করলে উত্তর পাওয়া যায়।",
                        "ক্যামেরা বা গ্যালারি থেকে যেকোনো ছবি বা স্ক্রিনশট দিলে তা বিশ্লেষণ করে উত্তর দেয়।",
                        "পরীক্ষার পড়া, অংক, ইংরেজি অনুবাদ এবং ক্যাপশন তৈরিতে পারদর্শী।",
                        "কোনো কঠিন অংক বা সমীকরণ সমাধান করা।",
                        "যেকোনো বাংলা লেখার ইংরেজি অনুবাদ করা।",
                        "যেকোনো ইংরেজি লেখার বাংলায় অনুবাদ করা।",
                        "সোশ্যাল মিডিয়ার জন্য সুন্দর ক্যাপশন তৈরি করা।"
                    )

                    features.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF006A4E)
                            )
                            Text(
                                text = item,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    Text(
                        text = "Niyon Islam Sohag ভাইকে এতো সুন্দর বাংলাদেশে একটি প্ল্যাটফর্ম তৈরি করার জন্য ধন্যবাদ।",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFD91B2F)
                    )
                    Text(
                        text = "তিনি আরো ৩ টা অ্যাপ তৈরি করছেন: FotoFilm, GramFilm, ProLevelCut।",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val appList = listOf(
                        "FotoFilm হলো সামাজিক যোগাযোগ মাধ্যম।" to Color(0xFF006A4E),
                        "ভিআইপি এদের জন্য GramFilm অ্যাপ।" to Color(0xFFE65100),
                        "ভিডিও এডিট করার জন্য ProLevelCut অ্যাপ।" to Color(0xFF1E88E5)
                    )

                    appList.forEach { (appDesc, dotColor) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "• ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = dotColor
                            )
                            Text(
                                text = appDesc,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showHelpDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF006A4E))
                ) {
                    Text("বুঝেছি", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun FidaAIMessageBubble(
    message: FidaAIMessage,
    onCopy: () -> Unit,
    onFeedback: (Boolean) -> Unit
) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(modifier = Modifier.padding(top = 2.dp, end = 6.dp)) {
                FidaAILogo(size = 30.dp)
            }
        }

        Column(
            modifier = Modifier.widthIn(max = 300.dp),
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) Color(0xFF6A1B9A) else MaterialTheme.colorScheme.surfaceVariant,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Attached Image / Screenshot thumbnail
                    if (message.imageUri != null) {
                        AsyncImage(
                            model = message.imageUri,
                            contentDescription = "Attached image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Message text
                    if (message.text.isNotBlank()) {
                        Text(
                            text = message.text,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Message footer: Time + Copy / Feedback for AI
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Text(
                    text = message.timestamp,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy text",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(13.dp)
                            .clickable { onCopy() }
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (message.isLiked == true) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                        contentDescription = "Good answer",
                        tint = if (message.isLiked == true) FacebookBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(13.dp)
                            .clickable { onFeedback(true) }
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (message.isLiked == false) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                        contentDescription = "Bad answer",
                        tint = if (message.isLiked == false) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(13.dp)
                            .clickable { onFeedback(false) }
                    )
                }
            }
        }
    }
}
