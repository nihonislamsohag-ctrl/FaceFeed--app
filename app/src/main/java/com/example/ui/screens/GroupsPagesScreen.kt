package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.SocialGroup
import com.example.model.SocialPage
import com.example.model.Translations
import com.example.ui.components.FidaVerifiedBadge
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun GroupsPagesScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val groups by viewModel.groups.collectAsState()
    val pages by viewModel.pages.collectAsState()

    var selectedSubTab by remember { mutableStateOf(0) } // 0 = Groups, 1 = Pages
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreatePageDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Tab Header: Groups vs Pages
        TabRow(selectedTabIndex = selectedSubTab) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text(strings.groups, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Groups, contentDescription = null) }
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text(strings.pages, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Flag, contentDescription = null) }
            )
        }

        if (selectedSubTab == 0) {
            // Groups view
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // "Create Group" Button
                    Button(
                        onClick = { showCreateGroupDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.createGroup, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(groups) { group ->
                    GroupCard(group = group, strings = strings, onToggleJoin = { viewModel.toggleJoinGroup(group.id) })
                }
            }
        } else {
            // Pages view
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    // "Create Page" Button
                    Button(
                        onClick = { showCreatePageDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(strings.createPage, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                items(pages) { page ->
                    PageCard(page = page, strings = strings, onToggleLike = { viewModel.toggleLikePage(page.id) })
                }
            }
        }
    }

    // Create Group Dialog
    if (showCreateGroupDialog) {
        var groupName by remember { mutableStateOf("") }
        var groupDesc by remember { mutableStateOf("") }
        var isPrivate by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCreateGroupDialog = false },
            title = { Text(strings.createGroup, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = { Text(strings.groupName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        placeholder = { Text("গ্রুপের বিবরণ...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = isPrivate, onCheckedChange = { isPrivate = it })
                        Text("প্রাইভেট গ্রুপ (Private Group)", fontSize = 13.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupName.isNotBlank()) {
                            viewModel.createGroup(groupName, groupDesc, isPrivate)
                            showCreateGroupDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateGroupDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }

    // Create Page Dialog
    if (showCreatePageDialog) {
        var pageName by remember { mutableStateOf("") }
        var pageCategory by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showCreatePageDialog = false },
            title = { Text(strings.createPage, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pageName,
                        onValueChange = { pageName = it },
                        placeholder = { Text(strings.pageName) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pageCategory,
                        onValueChange = { pageCategory = it },
                        placeholder = { Text("ক্যাটাগরি (Community, Tech, News...)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pageName.isNotBlank()) {
                            viewModel.createPage(pageName, pageCategory)
                            showCreatePageDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen)
                ) {
                    Text(strings.confirm)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePageDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
fun GroupCard(
    group: SocialGroup,
    strings: com.example.model.AppTranslations,
    onToggleJoin: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            AsyncImage(
                model = group.coverUrl,
                contentDescription = group.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                contentScale = ContentScale.Crop
            )

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = group.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${group.memberCount} members • ${if (group.isPrivate) "Private" else "Public"}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = group.description,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onToggleJoin,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (group.isJoined) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                    )
                ) {
                    Text(
                        text = if (group.isJoined) "Joined ✓" else strings.join,
                        color = if (group.isJoined) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PageCard(
    page: SocialPage,
    strings: com.example.model.AppTranslations,
    onToggleLike: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = page.avatarUrl,
                contentDescription = page.name,
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = page.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (page.isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        FidaVerifiedBadge(size = 14.dp)
                    }
                }
                Text(
                    text = "${page.category} • ${page.followerCount} followers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onToggleLike,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (page.isLiked) MaterialTheme.colorScheme.surfaceVariant else BangladeshGreen
                    )
                ) {
                    Text(
                        text = if (page.isLiked) "Following ✓" else "Follow Page",
                        color = if (page.isLiked) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
