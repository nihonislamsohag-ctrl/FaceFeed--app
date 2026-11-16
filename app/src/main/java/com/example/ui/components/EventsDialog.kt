package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

data class LocalEvent(
    val id: String,
    val title: String,
    val dateText: String,
    val location: String,
    val imageUrl: String,
    val attendeesCount: String
)

/**
 * Events Dialog
 * Per user request:
 * "Events এগুলো বন্ধ আছে, চালু করে দেন"
 */
@Composable
fun EventsDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var goingEvents by remember { mutableStateOf(setOf<String>()) }
    var interestedEvents by remember { mutableStateOf(setOf<String>()) }

    val events = remember {
        listOf(
            LocalEvent(
                id = "ev_1",
                title = "অমর একুশে বইমেলা ও সাহিত্য উৎসব ২০২৬",
                dateText = "১ ফেব্রুয়ারি - ২৮ ফেব্রুয়ারি • প্রতিদিন দুপুর ৩:০০",
                location = "বাংলা একাডেমি ও সোহরাওয়ার্দী উদ্যান, ঢাকা",
                imageUrl = "https://images.unsplash.com/photo-1512820790803-83ca734da794?w=500",
                attendeesCount = "৪৫.২K মানুষ আগ্রহী • ১২.৫K যাবেন"
            ),
            LocalEvent(
                id = "ev_2",
                title = "বাংলাদেশ টেক সামিট ও সফটওয়্যার কার্নিভাল",
                dateText = "১৫ নভেম্বর • সকাল ১০:০০ - রাত ৮:০০",
                location = "বঙ্গবন্ধু আন্তর্জাতিক সম্মেলন কেন্দ্র (BICC), ঢাকা",
                imageUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=500",
                attendeesCount = "১৮.৪K মানুষ আগ্রহী • ৬.১K যাবেন"
            ),
            LocalEvent(
                id = "ev_3",
                title = "ঐতিহ্যবাহী লোকসংগীত ও সুফিয়ানা বাউল মেলা",
                dateText = "২০ নভেম্বর - ২২ নভেম্বর",
                location = "ছেঁউড়িয়া, লালন শাহ মাজার, কুষ্টিয়া",
                imageUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=500",
                attendeesCount = "৩২K মানুষ যাবেন • ৮৯K আগ্রহী"
            )
        )
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
            ) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "📅 আসন্ন ইভেন্টসমূহ (Events)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(events, key = { it.id }) { event ->
                        val isGoing = goingEvents.contains(event.id)
                        val isInterested = interestedEvents.contains(event.id)

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                AsyncImage(
                                    model = event.imageUrl,
                                    contentDescription = event.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    contentScale = ContentScale.Crop
                                )

                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = event.dateText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE91E63)
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = event.title,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = event.location,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = event.attendeesCount,
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (isGoing) {
                                                    goingEvents = goingEvents - event.id
                                                } else {
                                                    goingEvents = goingEvents + event.id
                                                    interestedEvents = interestedEvents - event.id
                                                    Toast.makeText(context, "আপনি '${event.title}' ইভেন্টে যাচ্ছেন! 🎟️", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isGoing) BangladeshGreen else FacebookBlue
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = if (isGoing) Icons.Default.Check else Icons.Default.Event,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isGoing) "যাচ্ছেন ✅" else "যাব (Going)",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (isInterested) {
                                                    interestedEvents = interestedEvents - event.id
                                                } else {
                                                    interestedEvents = interestedEvents + event.id
                                                    goingEvents = goingEvents - event.id
                                                    Toast.makeText(context, "ইভেন্টটিতে আগ্রহ প্রকাশ করেছেন ⭐", Toast.LENGTH_SHORT).show()
                                                }
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = if (isInterested) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurface
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = if (isInterested) Color(0xFFFF9800) else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = if (isInterested) "আগ্রহী ⭐" else "আগ্রহী (Interested)",
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
            }
        }
    }
}
