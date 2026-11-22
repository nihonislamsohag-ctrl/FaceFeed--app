package com.example.ui.components

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.util.AppSoundManager
import com.example.viewmodel.FaceFeedViewModel

/**
 * Support Message Dialog (Screenshot 2: Help & Support)
 * User Prompt:
 * "আর দ্বিতীয় ছবিটার বিতরে এড হবে ছাপোড এর বিতরে মেসেজ লেখার প্রোফাইল আইডির লিংক,
 *  যে কোনো ফোন থেকে গেলারি দিয়ে ৫ টা ছবি বা ইস্কিনসট দিতে পারবে আর নিচে ছাপমিট করুন এড ও চালু হবে"
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportMessageDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var messageText by remember { mutableStateOf("") }
    var profileUrl by remember { mutableStateOf("https://facefeed.com/user/${currentUser.username}") }
    var attachedImages by remember { mutableStateOf<List<String>>(emptyList()) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Gallery Picker Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val stringUrls = uris.map { it.toString() }
            val combined = (attachedImages + stringUrls).take(5)
            attachedImages = combined
            Toast.makeText(context, "${combined.size}টি ছবি যুক্ত করা হয়েছে", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = FacebookBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SupportAgent,
                                    contentDescription = "Support",
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ছাপোড টিম সহায়তা",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customer Support & Helpdesk",
                                fontSize = 11.5.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Field 1: User Profile ID Link
                Text(
                    text = "🔗 আপনার প্রোফাইল আইডির লিংক:",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = profileUrl,
                    onValueChange = { profileUrl = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(Icons.Default.Link, contentDescription = null, tint = FacebookBlue)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FacebookBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Field 2: Support Message Box
                Text(
                    text = "📝 সমস্যা বা মেসেজ বিস্তারিত লিখুন:",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp),
                    shape = RoundedCornerShape(12.dp),
                    placeholder = {
                        Text(
                            text = "আপনার যেকোনো সমস্যা, প্রশ্ন বা মতামত বিস্তারিত লিখুন...",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FacebookBlue,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Field 3: Gallery 5 Photos or Screenshots Attachment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📷 গ্যালারি থেকে ৫টি ছবি বা স্ক্রিনশট:",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${attachedImages.size}/5 টি যুক্ত",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (attachedImages.size >= 5) BangladeshGreen else FacebookBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Attached Images Previews Row
                if (attachedImages.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(attachedImages) { index, imgUrl ->
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
                            ) {
                                AsyncImage(
                                    model = imgUrl,
                                    contentDescription = "Screenshot $index",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Delete icon
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(4.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .clickable {
                                            attachedImages = attachedImages.filterIndexed { i, _ -> i != index }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(12.dp))
                                }

                                // Index badge
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = FacebookBlue.copy(alpha = 0.85f),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(3.dp)
                                ) {
                                    Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Add Photos Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (attachedImages.size < 5) {
                                try {
                                    galleryLauncher.launch("image/*")
                                } catch (e: Exception) {
                                    Toast.makeText(context, "ফোনের গ্যালারি খোলা যায়নি", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "সর্বোচ্চ ৫টি ছবি বা স্ক্রিনশট যুক্ত করা যাবে", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FacebookBlue),
                        border = BorderStroke(1.dp, FacebookBlue)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "গ্যালারি থেকে যোগ (${5 - attachedImages.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit Button ("সাবমিট করুন")
                Button(
                    onClick = {
                        if (messageText.isBlank()) {
                            Toast.makeText(context, "অনুগ্রহ করে আপনার সমস্যা বা মেসেজটি লিখুন", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSubmitting = true
                        viewModel.submitSupportTicket(
                            message = messageText.trim(),
                            profileUrl = profileUrl.trim(),
                            screenshotUrls = attachedImages
                        )
                        AppSoundManager.playReactionChime()
                        Toast.makeText(
                            context,
                            "✅ সাপোর্ট টিমে আপনার মেসেজ ও ${attachedImages.size}টি স্ক্রিনশট সফলভাবে পাঠানো হয়েছে! সাপোর্ট টিম দ্রুত সমাধান প্রদান করবে।",
                            Toast.LENGTH_LONG
                        ).show()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "সাবমিট করুন (Submit to Support)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
