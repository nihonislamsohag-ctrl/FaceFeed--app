package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.Post
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel
import com.example.viewmodel.UniversalSearchCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalSearchDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val query by viewModel.universalSearchQuery.collectAsState()
    val activeCategory by viewModel.universalSearchCategory.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isVerified by viewModel.isFidaVerified.collectAsState()

    // Filter matching results
    val cleanQuery = query.trim().lowercase()

    val filteredPosts = remember(cleanQuery, posts) {
        if (cleanQuery.isEmpty()) emptyList()
        else posts.filter {
            it.text.lowercase().contains(cleanQuery) ||
            it.author.fullName.lowercase().contains(cleanQuery) ||
            it.hashtags.any { tag -> tag.lowercase().contains(cleanQuery) }
        }
    }

    val samplePeople = remember {
        listOf(
            User(id = "p1", fullName = "Niyon Islam Sohag", username = "niyonislamsohag", avatarUrl = "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=200", bio = "FaceFeed Creator 🇧🇩", isVerified = true),
            User(id = "p2", fullName = "Tahmid Hasan", username = "tahmid_dev", avatarUrl = "https://images.unsplash.com/photo-1570295999919-56ceb5ecca61?w=200", bio = "Android & Kotlin Dev"),
            User(id = "p3", fullName = "Fatima Rahman", username = "fatima_art", avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200", bio = "Digital Artist & Blogger"),
            User(id = "p4", fullName = "Sabbir Ahmed", username = "sabbir_films", avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200", bio = "Reels & Video Creator")
        )
    }

    val filteredPeople = remember(cleanQuery, samplePeople) {
        if (cleanQuery.isEmpty()) emptyList()
        else samplePeople.filter {
            it.fullName.lowercase().contains(cleanQuery) ||
            it.username.lowercase().contains(cleanQuery) ||
            it.bio.lowercase().contains(cleanQuery)
        }
    }

    val sampleGroups = remember {
        listOf("Bangladeshi Developers Hub", "FaceFeed Creators Bangladesh", "Photography Lovers BD", "Dhaka Foodies Club")
    }

    val filteredGroups = remember(cleanQuery, sampleGroups) {
        if (cleanQuery.isEmpty()) emptyList()
        else sampleGroups.filter { it.lowercase().contains(cleanQuery) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                // Top Search Header
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        TextField(
                            value = query,
                            onValueChange = { viewModel.setUniversalSearchQuery(it) },
                            placeholder = { Text("যে কোনো বিষয় লিখে সার্চ করুন...", fontSize = 15.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(24.dp),
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = FacebookBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { viewModel.setUniversalSearchQuery("") }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }

                // Filter Category Chips (সব, মানুষ, পোস্ট, ভিডিও, গ্রুপ, পেজ)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UniversalSearchCategory.values().forEach { category ->
                        val isSelected = activeCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setUniversalSearchCategory(category) },
                            label = {
                                Text(
                                    text = category.labelBn,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FacebookBlue,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Search Results or Search Suggestions
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Ask Fida AI Banner if query is entered
                    if (query.isNotBlank()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.openFidaAI()
                                        viewModel.sendFidaAIMessage(query)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF8A2BE2), Color(0xFF00B2FE), Color(0xFF00E676))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Fida AI",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Fida AI-কে জিজ্ঞাসা করুন ✦",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "\"$query\" বিষয়ে তাৎক্ষণিক সুন্দর উত্তর পান",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.ChevronRight,
                                        contentDescription = "Ask",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    if (query.isBlank()) {
                        // Recent / Suggested Searches
                        item {
                            Text(
                                text = "জনপ্রিয় ও সাম্প্রতিক সার্চ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        val recentTopics = listOf(
                            "Niyon Islam Sohag",
                            "FaceFeed নতুন ফিচারসমূহ",
                            "বাংলাদেশের সেরা দর্শনীয় স্থান",
                            "প্রযুক্তি ও অনলাইন ইনকাম",
                            "Fida AI স্মার্ট অ্যাসিস্ট্যান্ট",
                            "সবুজ ভেরিফাইড টিক কীভাবে পাবেন"
                        )

                        items(recentTopics) { topic ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setUniversalSearchQuery(topic) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = topic,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.NorthWest,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    } else {
                        // People results
                        if (activeCategory == UniversalSearchCategory.ALL || activeCategory == UniversalSearchCategory.PEOPLE) {
                            if (filteredPeople.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "মানুষ ও প্রোফাইল",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = FacebookBlue,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }

                                items(filteredPeople) { person ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setTab(AppTab.PROFILE)
                                                onDismiss()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AsyncImage(
                                                model = person.avatarUrl,
                                                contentDescription = person.fullName,
                                                modifier = Modifier
                                                    .size(46.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = person.fullName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 15.sp
                                                    )
                                                    if (person.isVerified || (person.fullName == currentUser.fullName && isVerified)) {
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        // Green Verified Badge!
                                                        Icon(
                                                            imageVector = Icons.Default.CheckCircle,
                                                            contentDescription = "Verified",
                                                            tint = BangladeshGreen,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                                Text(
                                                    text = "@${person.username} • ${person.bio}",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 1
                                                )
                                            }

                                            Button(
                                                onClick = {
                                                    viewModel.setTab(AppTab.PROFILE)
                                                    onDismiss()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text("প্রোফাইল", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Post Results
                        if (activeCategory == UniversalSearchCategory.ALL || activeCategory == UniversalSearchCategory.POSTS) {
                            if (filteredPosts.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "পোস্ট ও স্ট্যাটাস",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = FacebookBlue,
                                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                    )
                                }

                                items(filteredPosts) { post ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setTab(AppTab.HOME)
                                                onDismiss()
                                            }
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                AsyncImage(
                                                    model = post.author.avatarUrl,
                                                    contentDescription = post.author.fullName,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = post.author.fullName,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 14.sp
                                                    )
                                                    Text(
                                                        text = post.timestamp,
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            Text(
                                                text = post.text,
                                                fontSize = 14.sp,
                                                maxLines = 3
                                            )

                                            if (post.hashtags.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    text = post.hashtags.joinToString(" ") { "#$it" },
                                                    fontSize = 12.sp,
                                                    color = FacebookBlue,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Groups results
                        if (activeCategory == UniversalSearchCategory.ALL || activeCategory == UniversalSearchCategory.GROUPS) {
                            if (filteredGroups.isNotEmpty()) {
                                item {
                                    Text(
                                        text = "গ্রুপ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = FacebookBlue,
                                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                                    )
                                }

                                items(filteredGroups) { groupName ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                viewModel.setTab(AppTab.GROUPS)
                                                onDismiss()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(FacebookBlue.copy(alpha = 0.15f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Groups,
                                                    contentDescription = null,
                                                    tint = FacebookBlue
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = groupName,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = "পাবলিক গ্রুপ • ১২.৫K সদস্য",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    viewModel.setTab(AppTab.GROUPS)
                                                    onDismiss()
                                                },
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                            ) {
                                                Text("যোগ দিন", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredPeople.isEmpty() && filteredPosts.isEmpty() && filteredGroups.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Outlined.SearchOff,
                                            contentDescription = null,
                                            modifier = Modifier.size(56.dp),
                                            tint = MaterialTheme.colorScheme.outline
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "\"$query\" সম্পর্কিত কিছু খুঁজে পাওয়া যায়নি",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Fida AI-কে প্রশ্ন করতে ওপরের অপশনে ট্যাপ করুন",
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
