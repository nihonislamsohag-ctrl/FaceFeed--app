package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.viewmodel.FaceFeedViewModel

/**
 * Screenshot 2: Create Page Dialog ("পেইজ তৈরি করুন")
 * Matches Screenshot_20261105_151621.jpg
 *
 * - Header: "পেইজ তৈরি করুন" with close button
 * - Gallery photo picker: "গ্যালারি থেকে ছবি যুক্ত করুন"
 * - OutlinedTextField: "পেইজের নাম"
 * - OutlinedTextField: "পেইজের বিবরণ..."
 * - Checkbox: "পাবলিক পেইজ (Public Page)"
 * - Buttons: "বাতিল" (teal/green text) and "কনফার্ম" (dark green button)
 */
@Composable
fun CreatePageImage2Dialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit,
    onPageCreated: (name: String, category: String, imageUri: Uri?) -> Unit
) {
    val context = LocalContext.current
    var pageName by remember { mutableStateOf("") }
    var pageCategory by remember { mutableStateOf("") }
    var isPublicPage by remember { mutableStateOf(true) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Phone Gallery Photo Picker (Android Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            Toast.makeText(context, "গ্যালারি থেকে ছবি সফলভাবে যুক্ত হয়েছে ✅", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            shadowElevation = 8.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 22.dp, vertical = 20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "পেইজ তৈরি করুন",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B)
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color(0xFFF1F3F4), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "ব্যাক / বন্ধ করুন",
                            tint = Color(0xFF424242),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Gallery Photo Picker Section
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF8F9FA),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9))
                                .border(1.5.dp, Color(0xFF0D652D), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (selectedImageUri != null) {
                                AsyncImage(
                                    model = selectedImageUri,
                                    contentDescription = "Page Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Text("🖼️", fontSize = 24.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "গ্যালারি থেকে ছবি যুক্ত করুন",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1B1B1B)
                            )
                            Text(
                                text = "ফোনের গ্যালারি থেকে প্রোফাইল ছবি দিন 🖼️",
                                fontSize = 11.sp,
                                color = Color(0xFF616161)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Text Fields: পেইজের নাম & ক্যাটাগরি/বিবরণ
                OutlinedTextField(
                    value = pageName,
                    onValueChange = { pageName = it },
                    placeholder = { Text("পেইজের নাম", color = Color(0xFF9E9E9E), fontSize = 14.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D652D),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = Color(0xFF1B1B1B),
                        unfocusedTextColor = Color(0xFF1B1B1B)
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = pageCategory,
                    onValueChange = { pageCategory = it },
                    placeholder = { Text("পেইজের বিবরণ...", color = Color(0xFF9E9E9E), fontSize = 14.sp) },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF0D652D),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedTextColor = Color(0xFF1B1B1B),
                        unfocusedTextColor = Color(0xFF1B1B1B)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Checkbox: পাবলিক পেইজ
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { isPublicPage = !isPublicPage }
                ) {
                    Checkbox(
                        checked = isPublicPage,
                        onCheckedChange = { isPublicPage = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0D652D))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "পাবলিক পেইজ (Public Page)",
                        fontSize = 13.sp,
                        color = Color(0xFF424242)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: বাতিল & কনফার্ম
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "বাতিল",
                            color = Color(0xFF00796B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = {
                            if (pageName.isNotBlank()) {
                                viewModel.createPage(pageName, pageCategory.ifEmpty { "Personal blog" })
                                onPageCreated(pageName, pageCategory.ifEmpty { "Personal blog" }, selectedImageUri)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D652D)),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "কনফার্ম",
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
