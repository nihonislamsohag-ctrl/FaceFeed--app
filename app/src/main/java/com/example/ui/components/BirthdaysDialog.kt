package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Celebration
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

data class BirthdayPerson(
    val id: String,
    val name: String,
    val avatarUrl: String,
    val isToday: Boolean,
    val birthdayLabel: String,
    val ageText: String
)

/**
 * Birthdays Dialog
 * Per user request:
 * "Birthdays এর বিতরে যারা ভাডে উইস করবে এখানে দেখাজাবে"
 */
@Composable
fun BirthdaysDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var wishedIds by remember { mutableStateOf(setOf<String>()) }

    val todayBirthdays = remember {
        listOf(
            BirthdayPerson(
                id = "bday_1",
                name = "আরিফুল ইসলাম (Arif)",
                avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=500",
                isToday = true,
                birthdayLabel = "আজকের জন্মদিন 🎉",
                ageText = "আজ ২৫তম জন্মদিন পূর্ণ হলো"
            ),
            BirthdayPerson(
                id = "bday_2",
                name = "সাদিয়া আফরিন (Sadia)",
                avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=500",
                isToday = true,
                birthdayLabel = "আজকের জন্মদিন 🎂",
                ageText = "আজ জন্মদিন উদযাপন করছেন"
            )
        )
    }

    val upcomingBirthdays = remember {
        listOf(
            BirthdayPerson(
                id = "bday_3",
                name = "তানভীর হাসান (Tanvir)",
                avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=500",
                isToday = false,
                birthdayLabel = "আগামীকাল (৮ নভেম্বর)",
                ageText = "১ দিন পর জন্মদিন"
            ),
            BirthdayPerson(
                id = "bday_4",
                name = "রফিকুল ইসলাম (Rafiq)",
                avatarUrl = "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=500",
                isToday = false,
                birthdayLabel = "১০ নভেম্বর",
                ageText = "৩ দিন পর জন্মদিন"
            ),
            BirthdayPerson(
                id = "bday_5",
                name = "নুসরাত জাহান (Nusrat)",
                avatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=500",
                isToday = false,
                birthdayLabel = "১৫ নভেম্বর",
                ageText = "৮ দিন পর জন্মদিন"
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
                // Top Header
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
                            text = "🎂 বন্ধুদের জন্মদিন (Birthdays)",
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
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Today's Birthdays Banner Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFFECEF),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB3C1)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF4081)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Celebration,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "আজকের শুভ জন্মদিন 🎂🎈",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFFC2185B)
                                    )
                                    Text(
                                        text = "বন্ধুদের স্পেশাল দিনে সুন্দর উইশ ও শুভেচ্ছা বার্তা পাঠান",
                                        fontSize = 12.sp,
                                        color = Color(0xFF880E4F)
                                    )
                                }
                            }
                        }
                    }

                    // Section: Today
                    item {
                        Text(
                            text = "আজকের জন্মদিন (${todayBirthdays.size} জন)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(todayBirthdays) { person ->
                        val isWished = wishedIds.contains(person.id)
                        var customWish by remember { mutableStateOf("") }
                        var showWishInput by remember { mutableStateOf(false) }

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        AsyncImage(
                                            model = person.avatarUrl,
                                            contentDescription = person.name,
                                            modifier = Modifier
                                                .size(52.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = Color(0xFFFF4081),
                                            modifier = Modifier.size(18.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Cake,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.padding(2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = person.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = person.ageText,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (isWished) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = BangladeshGreen.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "উইশ সম্পন্ন ✅",
                                                color = BangladeshGreen,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Quick Wish Chips
                                if (!isWished) {
                                    Text(
                                        text = "দ্রুত শুভেচ্ছা পাঠান:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        SuggestionChip(
                                            onClick = {
                                                wishedIds = wishedIds + person.id
                                                Toast.makeText(context, "${person.name}-কে জন্মদিনের শুভেচ্ছা পাঠানো হয়েছে! 🎂🎉", Toast.LENGTH_SHORT).show()
                                            },
                                            label = { Text("শুভ জন্মদিন প্রিয় ভাই! 🎂", fontSize = 11.sp) }
                                        )
                                        SuggestionChip(
                                            onClick = {
                                                wishedIds = wishedIds + person.id
                                                Toast.makeText(context, "${person.name}-কে জন্মদিনের শুভেচ্ছা পাঠানো হয়েছে! 🎁🎉", Toast.LENGTH_SHORT).show()
                                            },
                                            label = { Text("Happy Birthday! 🎈", fontSize = 11.sp) }
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Button(
                                        onClick = {
                                            wishedIds = wishedIds + person.id
                                            Toast.makeText(context, "${person.name}-কে উইশ পাঠানো হয়েছে! 🎂", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp)
                                    ) {
                                        Icon(Icons.Default.Celebration, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("উইশ পাঠান 🎂🎉", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Section: Upcoming Birthdays
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "আসন্ন জন্মদিনসমূহ (${upcomingBirthdays.size} জন)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    items(upcomingBirthdays) { person ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = person.avatarUrl,
                                    contentDescription = person.name,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = person.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${person.birthdayLabel} • ${person.ageText}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = "আসছে 📅",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
