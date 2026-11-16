package com.example.ui.page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.Comment
import com.example.model.Post
import com.example.model.ReactionType
import com.example.model.SocialPage
import com.example.model.User
import com.example.ui.components.FidaVerifiedBadge
import com.example.ui.components.ReactionUsersDialog
import com.example.ui.theme.BangladeshGreen
import com.example.viewmodel.FaceFeedViewModel

/**
 * Requirement 3: 10 Reactions with Names
 * Requirement 4: Comments and Replies (Showing Page name and Page photo)
 * Requirement 5: Share & Boost (Sponsored label)
 */
@Composable
fun PagePostItemCard(
    post: Post,
    page: SocialPage,
    viewModel: FaceFeedViewModel,
    onBoostClick: (Post) -> Unit,
    onShareClick: (Post) -> Unit,
    onOpenProfile: () -> Unit,
    onToast: (String) -> Unit
) {
    var showReactionPicker by remember { mutableStateOf(false) }
    var showReactorsDialog by remember { mutableStateOf(false) }
    var showCommentsSection by remember { mutableStateOf(false) }
    var newCommentText by remember { mutableStateOf("") }
    var replyingToCommentId by remember { mutableStateOf<String?>(null) }
    var replyText by remember { mutableStateOf("") }

    val pageAuthor = remember(page) {
        User(
            id = "page_${page.id}",
            username = page.name.lowercase().replace(" ", "_"),
            fullName = page.name,
            avatarUrl = page.avatarUrl,
            isVerified = true
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Post Header: Page Avatar + Name + Sponsored label + Timestamp + Menu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, BangladeshGreen),
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { onOpenProfile() }
                ) {
                    AsyncImage(
                        model = post.author.avatarUrl.ifEmpty { page.avatarUrl },
                        contentDescription = post.author.fullName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.author.fullName.ifEmpty { page.name },
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FidaVerifiedBadge(size = 14.dp)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Sponsored Badge if Boosted
                        if (post.isBoosted) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFE8F5E9)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Sponsored / স্পনসরড", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BangladeshGreen)
                                }
                            }
                            Text("•", fontSize = 11.sp, color = Color.Gray)
                        }

                        Text(
                            text = post.timestamp,
                            color = Color(0xFF65676B),
                            fontSize = 11.sp
                        )
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = "Public",
                            tint = Color(0xFF65676B),
                            modifier = Modifier.size(12.dp)
                        )
                        if (post.location != null) {
                            Text("• 📍 ${post.location}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }

                IconButton(onClick = { onToast("পোস্ট সেটিংস") }) {
                    Icon(Icons.Default.MoreHoriz, contentDescription = "More", tint = Color(0xFF65676B))
                }
            }

            // Post Title (if exists) - Requirement 2: Title writing option on top: "Add Title"
            if (!post.title.isNullOrBlank()) {
                Text(
                    text = post.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            // Post Text
            if (post.text.isNotBlank()) {
                Text(
                    text = post.text,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = Color(0xFF1C1E21),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            // Hashtags
            if (post.hashtags.isNotEmpty()) {
                Text(
                    text = post.hashtags.joinToString(" "),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = BangladeshGreen,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                )
            }

            // Post Images
            if (post.images.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                AsyncImage(
                    model = post.images.first(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    contentScale = ContentScale.Crop
                )
            }

            // Reaction Counts & Comments Stats Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Total reaction count (tap opens reactors list with names - Requirement 3)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { showReactorsDialog = true }
                        .padding(vertical = 2.dp)
                ) {
                    val topEmoji = post.userReaction?.emoji ?: "👍"
                    Text(text = topEmoji, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.totalReactions.coerceAtLeast(14)} জন রিঅ্যাক্ট করেছেন (নাম দেখুন)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF65676B)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "${post.comments.size} কমেন্ট",
                        fontSize = 12.sp,
                        color = Color(0xFF65676B),
                        modifier = Modifier.clickable { showCommentsSection = !showCommentsSection }
                    )
                    Text(
                        text = "${post.shareCount} শেয়ার",
                        fontSize = 12.sp,
                        color = Color(0xFF65676B),
                        modifier = Modifier.clickable { onShareClick(post) }
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFE4E6EB), thickness = 0.8.dp)

            // Interactive Floating 10 Reactions Picker (Requirement 3: Like 👍, Dislike 👎, Nice 👌, Love ❤️, Adorable 🥰, Wow 😮, Happy 😄, Sad 😢, Angry 😡, 100 💯)
            Box(modifier = Modifier.fillMaxWidth()) {
                if (showReactionPicker) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(y = (-95).dp, x = 8.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            // Row 1: Like 👍, Dislike 👎, Nice 👌, Love ❤️, Adorable 🥰
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    ReactionType.LIKE,
                                    ReactionType.DISLIKE,
                                    ReactionType.OK,
                                    ReactionType.LOVE,
                                    ReactionType.CARE
                                ).forEach { rx ->
                                    Surface(
                                        onClick = {
                                            viewModel.reactToPost(post.id, rx)
                                            showReactionPicker = false
                                            onToast("${rx.englishName} ${rx.emoji} রিঅ্যাকশন দেওয়া হয়েছে")
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF7F8FA)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(rx.emoji, fontSize = 20.sp)
                                            Text(rx.englishName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(rx.colorHex))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Row 2: Wow 😮, Happy 😄, Sad 😢, Angry 😡, 100 💯
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(
                                    ReactionType.WOW,
                                    ReactionType.HAHA,
                                    ReactionType.SAD,
                                    ReactionType.ANGRY,
                                    ReactionType.HUNDRED
                                ).forEach { rx ->
                                    Surface(
                                        onClick = {
                                            viewModel.reactToPost(post.id, rx)
                                            showReactionPicker = false
                                            onToast("${rx.englishName} ${rx.emoji} রিঅ্যাকশন দেওয়া হয়েছে")
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF7F8FA)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(rx.emoji, fontSize = 20.sp)
                                            Text(rx.englishName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(rx.colorHex))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // 4 Action Buttons: Reaction, Comment, Share, Boost (Requirement 3, 4, 5)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reaction Button
                    TextButton(
                        onClick = {
                            if (post.userReaction == null) {
                                viewModel.reactToPost(post.id, ReactionType.LIKE)
                            } else {
                                viewModel.reactToPost(post.id, post.userReaction)
                            }
                        }
                    ) {
                        Text(
                            text = post.userReaction?.let { "${it.emoji} ${it.englishName}" } ?: "👍 Like",
                            color = post.userReaction?.let { Color(it.colorHex) } ?: Color(0xFF65676B),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }

                    // 10 Reactions Picker button
                    IconButton(onClick = { showReactionPicker = !showReactionPicker }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.AddReaction, contentDescription = "10 Reactions", tint = BangladeshGreen, modifier = Modifier.size(18.dp))
                    }

                    // Comment Button
                    TextButton(onClick = { showCommentsSection = !showCommentsSection }) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color(0xFF65676B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Comment", color = Color(0xFF65676B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Share Button
                    TextButton(onClick = { onShareClick(post) }) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF65676B), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", color = Color(0xFF65676B), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Boost Button (Only for Page Admin - Requirement 5)
                    Surface(
                        onClick = { onBoostClick(post) },
                        shape = RoundedCornerShape(14.dp),
                        color = if (post.isBoosted) BangladeshGreen.copy(alpha = 0.15f) else Color(0xFFE8F5E9),
                        border = BorderStroke(1.dp, BangladeshGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = BangladeshGreen, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (post.isBoosted) "Boosted 🚀" else "Boost",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BangladeshGreen
                            )
                        }
                    }
                }
            }

            // ========================================================
            // Requirement 4: Comments and Replies (Showing Page Name & Photo)
            // ========================================================
            if (showCommentsSection) {
                HorizontalDivider(color = Color(0xFFF0F2F5))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF9FAFB))
                        .padding(12.dp)
                ) {
                    // Comment input as Page
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = page.avatarUrl,
                            contentDescription = page.name,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = newCommentText,
                            onValueChange = { newCommentText = it },
                            placeholder = { Text("Comment as ${page.name}...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (newCommentText.isNotBlank()) {
                                    viewModel.addComment(post.id, newCommentText, customAuthor = pageAuthor)
                                    newCommentText = ""
                                    onToast("মন্তব্য সফলভাবে পোস্ট হয়েছে ✅")
                                }
                            }
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send", tint = BangladeshGreen)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Existing comments
                    post.comments.forEach { comment ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AsyncImage(
                                            model = comment.author.avatarUrl,
                                            contentDescription = comment.author.fullName,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(comment.author.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(comment.timestamp, fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }

                                    // Delete Menu
                                    Row {
                                        IconButton(
                                            onClick = {
                                                viewModel.deleteComment(post.id, comment.id)
                                                onToast("কমেন্ট মুছে ফেলা হয়েছে 🗑️")
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(comment.text, fontSize = 13.sp, color = Color(0xFF1F2937))

                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    Text(
                                        text = "👍 Like",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BangladeshGreen,
                                        modifier = Modifier.clickable { onToast("কমেন্টে লাইক দেওয়া হয়েছে") }
                                    )
                                    Text(
                                        text = "💬 Reply (উত্তর দিন)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1976D2),
                                        modifier = Modifier.clickable {
                                            replyingToCommentId = if (replyingToCommentId == comment.id) null else comment.id
                                        }
                                    )
                                }

                                // Replies List
                                if (comment.replies.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 24.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        comment.replies.forEach { rep ->
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color(0xFFF3F4F6),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    AsyncImage(
                                                        model = rep.author.avatarUrl,
                                                        contentDescription = rep.author.fullName,
                                                        modifier = Modifier
                                                            .size(20.dp)
                                                            .clip(CircleShape),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Column {
                                                        Text(rep.author.fullName, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                                        Text(rep.text, fontSize = 11.5.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Reply input as Page (Requirement 4: When replying from Page, reply shows Page name & photo)
                                if (replyingToCommentId == comment.id) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        AsyncImage(
                                            model = page.avatarUrl,
                                            contentDescription = page.name,
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        OutlinedTextField(
                                            value = replyText,
                                            onValueChange = { replyText = it },
                                            placeholder = { Text("Reply as ${page.name}...", fontSize = 11.sp) },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(16.dp),
                                            singleLine = true
                                        )
                                        IconButton(
                                            onClick = {
                                                if (replyText.isNotBlank()) {
                                                    viewModel.replyToComment(post.id, comment.id, replyText, customAuthor = pageAuthor)
                                                    replyText = ""
                                                    replyingToCommentId = null
                                                    onToast("পেইজের পক্ষ থেকে রিপ্লাই দেওয়া হয়েছে ✅")
                                                }
                                            }
                                        ) {
                                            Icon(Icons.Default.Send, contentDescription = "Send", tint = BangladeshGreen)
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

    // Reaction Users Dialog (Requirement 3: Who reacted & names)
    if (showReactorsDialog) {
        ReactionUsersDialog(
            post = post,
            onDismiss = { showReactorsDialog = false }
        )
    }
}
