package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.model.SocialGroup
import com.example.model.SocialPage
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import com.example.model.Translations
import com.example.ui.components.CreateGroupImage2Dialog
import com.example.ui.components.CreatePageImage2Dialog
import com.example.ui.components.FidaVerifiedBadge
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel

@Composable
fun GroupsPagesScreen(
    viewModel: FaceFeedViewModel,
    modifier: Modifier = Modifier
) {
    val currentLang by viewModel.currentLanguage.collectAsState()
    val strings = Translations.get(currentLang)
    val currentUser by viewModel.currentUser.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val pages by viewModel.pages.collectAsState()

    var activeGroupDetail by remember { mutableStateOf<SocialGroup?>(null) }
    var activePageDetail by remember { mutableStateOf<SocialPage?>(null) }
    var activePageHomeFeed by remember { mutableStateOf<SocialPage?>(null) }
    val initialSubTab by viewModel.groupsPagesSelectedTab.collectAsState()
    var selectedSubTab by remember(initialSubTab) { mutableStateOf(initialSubTab) } // 0 = Groups, 1 = Pages
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    var showCreatePageDialog by remember { mutableStateOf(false) }
    var selectedGroupForRoles by remember { mutableStateOf<SocialGroup?>(null) }

    // BackHandler to smoothly return without exiting app
    BackHandler {
        if (activePageHomeFeed != null) {
            activePageHomeFeed = null
        } else if (activePageDetail != null) {
            activePageDetail = null
        } else if (activeGroupDetail != null) {
            activeGroupDetail = null
        } else {
            viewModel.setTab(AppTab.MENU)
        }
    }

    // If Page Home Feed is opened (Screenshot 1: পেইজে হোম Feed)
    if (activePageHomeFeed != null) {
        PageHomeFeedScreen(
            page = activePageHomeFeed!!,
            viewModel = viewModel,
            onBack = { activePageHomeFeed = null },
            onOpenProfile = {
                activePageDetail = activePageHomeFeed
                activePageHomeFeed = null
            }
        )
        return
    }

    // If a page is opened, show Screenshot 4 & 5 (PageDetailScreen / পেইজ আইডি)
    if (activePageDetail != null) {
        PageDetailScreen(
            page = activePageDetail!!,
            viewModel = viewModel,
            onBack = { activePageDetail = null },
            onOpenHomeFeed = {
                activePageHomeFeed = activePageDetail
                activePageDetail = null
            }
        )
        return
    }

    // If a group is opened, show Screenshot 4 (GroupDetailScreen / গ্রুপ আইডি)
    if (activeGroupDetail != null) {
        GroupDetailScreen(
            group = activeGroupDetail!!,
            viewModel = viewModel,
            onBack = { activeGroupDetail = null }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Bar with back button to easily return to Menu screen
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.setTab(AppTab.MENU) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Menu",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = if (selectedSubTab == 0) "গ্রুপসমূহ (Groups)" else "পেইজসমূহ (Pages)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // Quick Switch between Groups & Pages
                TextButton(onClick = { selectedSubTab = if (selectedSubTab == 0) 1 else 0 }) {
                    Text(
                        text = if (selectedSubTab == 0) "পেইজে যান ➡️" else "গ্রুপে যান ➡️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = FacebookBlue
                    )
                }
            }
        }

        val isFreeMode by viewModel.isFreeMode.collectAsState()
        val currentSim by viewModel.currentSimOperator.collectAsState()

        if (isFreeMode) {
            Surface(
                color = Color(currentSim.colorHex),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "📶 ${currentSim.banglaName} ফ্রি মোড: গ্রুপ ও পেইজে ০ এমবিতে পোস্ট, ছবি ও চ্যাট ফ্রী ⚡",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

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
            // Screenshot 3: Facebook Groups Screen ("Most visited" groups list)
            var selectedGroupFilterTab by remember { mutableStateOf(0) } // 0 = Your groups, 1 = Posts, 2 = Discover
            var searchQuery by remember { mutableStateOf("") }
            var isSearching by remember { mutableStateOf(false) }

            // Preset Most Visited Groups from Screenshot 3
            val screenshotGroups = remember {
                listOf(
                    SocialGroup(
                        id = "grp_tasmim",
                        name = "Tasmim Tinni",
                        description = "Tasmim Tinni Personal Community & Updates",
                        coverUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400",
                        memberCount = 4200,
                        isPrivate = false,
                        creatorName = "Tasmim Tinni",
                        createdAt = "Updated 17 hours ago."
                    ),
                    SocialGroup(
                        id = "grp_nidra_dc",
                        name = "নিদ্রা ডিসি পয়েন্ট (তালতলী উপজেলা বরগুনা)",
                        description = "নিদ্রা ডিসি পয়েন্ট তালতলী উপজেলা বরগুনা পর্যটন ও ক্যাম্পিং কেন্দ্রিক সাধারণ গ্রুপ।",
                        coverUrl = "", // Uses R.drawable.img_nidra_dc_point_cover
                        memberCount = 1000,
                        isPrivate = false,
                        creatorName = "Niyon Islam Sohag",
                        createdAt = "Updated on Sunday."
                    ),
                    SocialGroup(
                        id = "grp_love_color",
                        name = "❤️ভালোবাসার রং কী কেউ জানে❤️",
                        description = "ভালোবাসার কবিতা, গল্প ও অনুভূতি প্রকাশ করার সুন্দর গ্রুপ।",
                        coverUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?w=400",
                        memberCount = 15400,
                        isPrivate = false,
                        creatorName = "Community Admin",
                        createdAt = "25+ new posts"
                    ),
                    SocialGroup(
                        id = "grp_nidra_beach",
                        name = "নিদ্রার চর সমুদ্র সৈকত ফটো গ্রাফার ইউনিট",
                        description = "বরগুনা তালতলী নিদ্রার চরের প্রাকৃতিক সৌন্দর্য ও আলোকচিত্র প্রদর্শনী।",
                        coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=400",
                        memberCount = 6800,
                        isPrivate = false,
                        creatorName = "Photo Admin",
                        createdAt = "Updated about 3 weeks ago."
                    ),
                    SocialGroup(
                        id = "grp_sheikh_hasina",
                        name = "মাননীয় প্রধানমন্ত্রী শেখ হাসিনা",
                        description = "উন্নয়ন ও অগ্রযাত্রার বাংলাদেশ ভিত্তিক গ্রুপ।",
                        coverUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=400",
                        memberCount = 52000,
                        isPrivate = false,
                        creatorName = "Admin",
                        createdAt = "25+ new posts"
                    ),
                    SocialGroup(
                        id = "grp_al",
                        name = "বাংলাদেশ আওয়ামীলীগ",
                        description = "বাংলাদেশ আওয়ামী লীগ দলীয় সমর্থক ও কর্মী ফোরাম।",
                        coverUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?w=400",
                        memberCount = 78000,
                        isPrivate = false,
                        creatorName = "AL Central",
                        createdAt = "25+ new posts"
                    ),
                    SocialGroup(
                        id = "grp_64_district",
                        name = "৬৪ জেলা আওয়ামীলীগ পরিবার",
                        description = "৬৪ জেলার সকল সদস্য ও কর্মীদের ঐক্য ফোরাম।",
                        coverUrl = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=400",
                        memberCount = 36000,
                        isPrivate = false,
                        creatorName = "District Admin",
                        createdAt = "25+ new posts"
                    ),
                    SocialGroup(
                        id = "grp_school",
                        name = "প্রিয় কায়রীরহাট ৩৩ নং ডিগ্রিচর উচ্চ বিদ্যালয়",
                        description = "প্রাক্তন ও বর্তমান শিক্ষার্থীদের মিলনমেলা ও স্মৃতিচারণ।",
                        coverUrl = "https://images.unsplash.com/photo-1580582932707-520aed937b7b?w=400",
                        memberCount = 2800,
                        isPrivate = false,
                        creatorName = "School Alumni",
                        createdAt = "25+ new posts"
                    ),
                    SocialGroup(
                        id = "grp_followers_boost",
                        name = "ফলোয়ার বাড়ানোর গ্রুপ",
                        description = "সবার প্রোফাইল ও পেইজের ফলোয়ার বাড়ানোর প্ল্যাটফর্ম।",
                        coverUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=400",
                        memberCount = 92000,
                        isPrivate = false,
                        creatorName = "Growth Admin",
                        createdAt = "25+ new posts"
                    ),
                    SocialGroup(
                        id = "grp_f2f",
                        name = "Follow To Follow",
                        description = "সরাসরি একে অপরকে ফলো ব্যাক করুন ও রিচ বৃদ্ধি করুন।",
                        coverUrl = "https://images.unsplash.com/photo-1557683316-973673baf926?w=400",
                        memberCount = 45000,
                        isPrivate = false,
                        creatorName = "F2F Team",
                        createdAt = "25+ new posts"
                    ),
                    SocialGroup(
                        id = "grp_volunteer",
                        name = "বাংলাদেশ আওয়ামী স্বেচ্ছাসেবক লীগ...",
                        description = "স্বেচ্ছাসেবক লীগ কর্মী ও সমাজসেবা কার্যক্রম।",
                        coverUrl = "https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=400",
                        memberCount = 21000,
                        isPrivate = false,
                        creatorName = "Volunteer League",
                        createdAt = "25+ new posts"
                    )
                )
            }

            // Combine predefined groups with dynamic user-created groups from ViewModel
            val combinedGroups = remember(groups, screenshotGroups) {
                val userCreated = groups.filter { g -> screenshotGroups.none { it.id == g.id } }
                userCreated + screenshotGroups
            }

            val filteredList = if (searchQuery.isBlank()) {
                combinedGroups
            } else {
                combinedGroups.filter {
                    it.name.contains(searchQuery, ignoreCase = true) || it.description.contains(searchQuery, ignoreCase = true)
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                // Header Bar matching Screenshot 3: (< Groups, +, 🔍)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Groups",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        // + Icon button to open Screenshot 2 (CreateGroupImage2Dialog)
                        IconButton(
                            onClick = { showCreateGroupDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE4E6EB), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Group",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Search Icon button
                        IconButton(
                            onClick = { isSearching = !isSearching },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0xFFE4E6EB), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = Color.Black,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    if (isSearching) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search groups...") },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                // Top Category Chips matching Screenshot 3: (Your groups, Posts, Discover)
                item {
                    val groupFilters = listOf(
                        Triple(0, "Your groups", Icons.Default.Groups),
                        Triple(1, "Posts", Icons.Default.ChatBubbleOutline),
                        Triple(2, "Discover", Icons.Default.Explore)
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        items(groupFilters) { (idx, label, icon) ->
                            val isSelected = selectedGroupFilterTab == idx
                            Surface(
                                onClick = { selectedGroupFilterTab = idx },
                                shape = RoundedCornerShape(20.dp),
                                color = if (isSelected) Color(0xFFE7F3FF) else Color(0xFFF0F2F5),
                                border = if (isSelected) BorderStroke(1.dp, FacebookBlue.copy(alpha = 0.5f)) else null
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) FacebookBlue else Color.Black,
                                        modifier = Modifier.size(17.dp)
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) FacebookBlue else Color.Black
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE4E6EB), thickness = 0.8.dp)
                }

                // Sub-header: "Most visited" & "Sort"
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Most visited",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Text(
                            text = "Sort",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FacebookBlue,
                            modifier = Modifier.clickable {}
                        )
                    }
                }

                // Prominent Create Group Banner for ANY User
                item {
                    Surface(
                        onClick = { showCreateGroupDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE7F3FF),
                        border = BorderStroke(1.2.dp, FacebookBlue.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(FacebookBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Group",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "+ নতুন গ্রুপ তৈরি করুন (Create Group)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = FacebookBlue
                                )
                                Text(
                                    text = "যেকোনো ব্যবহারকারী নিজের নতুন গ্রুপ তৈরি করতে পারবেন",
                                    fontSize = 12.sp,
                                    color = Color(0xFF1565C0)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = FacebookBlue
                            )
                        }
                    }
                }

                // First Item: "+ Create a group" row matching Screenshot 3
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showCreateGroupDialog = true }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFE4E6EB),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Group",
                                    tint = Color.Black,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = "Create a group",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }

                // List of Most Visited Groups (Matching Screenshot 3)
                items(filteredList) { groupItem ->
                    val isNidra = groupItem.name.contains("নিদ্রা ডিসি পয়েন্ট") || groupItem.id == "grp_nidra_dc"
                    val isDot = groupItem.createdAt.contains("25+")

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // Opens Screenshot 4: GroupDetailScreen (গ্রুপ আইডি)
                                activeGroupDetail = groupItem
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Group Avatar / Cover thumbnail
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(0.5.dp, Color(0xFFE4E6EB), RoundedCornerShape(10.dp))
                        ) {
                            if (isNidra) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(id = R.drawable.img_nidra_dc_point_cover),
                                    contentDescription = groupItem.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                AsyncImage(
                                    model = groupItem.coverUrl.ifEmpty { "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=400" },
                                    contentDescription = groupItem.name,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = groupItem.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (isDot) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(FacebookBlue)
                                    )
                                }
                                Text(
                                    text = groupItem.createdAt,
                                    fontSize = 12.sp,
                                    color = if (isDot) Color(0xFF65676B) else Color(0xFF65676B),
                                    fontWeight = if (isDot) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        } else {
            // =========================================================================
            // Screenshot 3: Facebook Pages Hub (Facebook পেইজ হাব)
            // Matches Screenshot_20261105_203421.jpg:
            // 1. Top bar: "< Pages 🔍"
            // 2. Action pills: "Create", "Meta Business Suite", "Followed Pages"
            // 3. "Recently visited Pages" (See all)
            //    - HM Mahedi Bhayha (Digital creator · 4,835 people follow this)
            //    - Gazi Mehede Hasan (Artist · 4,835 people follow this)
            //    - Rini Islam (Digital creator · 4,188 people follow this)
            //    - Nihon Islam Sohag (Personal blog · 0 followers · Switch button -> opens Screenshot 4 & 5)
            // 4. "Suggested Pages"
            //    - Arisha Noor (1,426 people follow this)
            //    - সুমাইয়া ফ্যাশন (3K others follow this)
            //    - Any user created pages from viewModel.pages
            // =========================================================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Header: Back icon, "Pages", Search icon
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.setTab(AppTab.MENU) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = "Pages",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = { /* Search */ }) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Pages",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Action Pills (Create, Meta Business Suite, Followed Pages)
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Create Pill
                        Surface(
                            onClick = { showCreatePageDialog = true },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Create",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Meta Business Suite
                        Surface(
                            onClick = { /* Info */ },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BusinessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Meta Business Suite",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Followed Pages
                        Surface(
                            onClick = { /* Followed */ },
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Followed Pages",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(top = 8.dp),
                        thickness = 0.6.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    )
                }

                // Prominent Create Page Banner for ANY User
                item {
                    Surface(
                        onClick = { showCreatePageDialog = true },
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.2.dp, BangladeshGreen),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(BangladeshGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Page",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "+ নতুন পেইজ তৈরি করুন (Create Page)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = BangladeshGreen
                                )
                                Text(
                                    text = "যেকোনো ব্যবহারকারী নিজের পেইজ তৈরি ও পরিচালনা করতে পারবেন",
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = BangladeshGreen
                            )
                        }
                    }
                }

                // User Created Pages placed prominently at the top
                if (pages.isNotEmpty()) {
                    item {
                        Text(
                            text = "আপনার পেইজসমূহ (${pages.size}টি)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp)
                        )
                    }
                    items(pages) { userPage ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = userPage.avatarUrl,
                                    contentDescription = userPage.name,
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = userPage.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        FidaVerifiedBadge(size = 14.dp)
                                    }
                                    Text(
                                        text = "${userPage.category} • ${userPage.followerCount} ফলোয়ার",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                // "লগইন করুন" বাটন
                                Button(
                                    onClick = { activePageHomeFeed = userPage },
                                    shape = RoundedCornerShape(18.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Login,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "লগইন করুন",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Section: Recently visited Pages
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recently visited Pages",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "See all",
                            color = FacebookBlue,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            modifier = Modifier.clickable { }
                        )
                    }
                }

                // =========================================================================
                // করিমের ৩টি পেইজ আইডি (অন্য কারো আইডি আসবে না) + সামনে "লগইন করুন" বাটন
                // =========================================================================

                // ১. Nihon Islam Sohag (Personal blog • 15.4K followers • Purple 'N' logo)
                item {
                    val nihonPage = remember {
                        SocialPage(
                            id = "page_nihon_islam_sohag",
                            name = "Nihon Islam Sohag",
                            category = "Personal blog",
                            avatarUrl = "", // triggers purple circle with letter N
                            coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200",
                            followerCount = 15400,
                            creatorName = "Nihon Islam Sohag"
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activePageHomeFeed = nihonPage }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Purple circle with white letter "N"
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8E24AA)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "N",
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Nihon Islam Sohag",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FidaVerifiedBadge(size = 14.dp)
                            }
                            Text(
                                text = "Personal blog · 15.4K followers",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // "লগইন করুন" বাটন (চাপ দিলে পেইজে লগইন হবে ও হোম ফিড চালু হবে)
                        Button(
                            onClick = { activePageHomeFeed = nihonPage },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "লগইন করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // ২. করিম অফিসিয়াল পেইজ (Karim Official)
                item {
                    val karimPage = remember {
                        SocialPage(
                            id = "page_karim_official",
                            name = "করিম অফিসিয়াল পেইজ",
                            category = "Digital creator",
                            avatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400",
                            coverUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=1200",
                            followerCount = 24800,
                            creatorName = "Karim Official"
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activePageHomeFeed = karimPage }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = karimPage.avatarUrl,
                            contentDescription = karimPage.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = karimPage.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FidaVerifiedBadge(size = 14.dp)
                            }
                            Text(
                                text = "Digital creator · 24.8K followers",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // "লগইন করুন" বাটন
                        Button(
                            onClick = { activePageHomeFeed = karimPage },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "লগইন করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // ৩. সোনার বাংলা এন্টারটেইনমেন্ট (Sonar Bangla Media)
                item {
                    val sonarBanglaPage = remember {
                        SocialPage(
                            id = "page_sonar_bangla_media",
                            name = "সোনার বাংলা এন্টারটেইনমেন্ট",
                            category = "Media & News Company",
                            avatarUrl = "https://images.unsplash.com/photo-1516280440614-37939bbacd81?w=400",
                            coverUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1200",
                            followerCount = 52000,
                            creatorName = "Sonar Bangla Media"
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { activePageHomeFeed = sonarBanglaPage }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = sonarBanglaPage.avatarUrl,
                            contentDescription = sonarBanglaPage.name,
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = sonarBanglaPage.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                FidaVerifiedBadge(size = 14.dp)
                            }
                            Text(
                                text = "Media & News · 52K followers",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // "লগইন করুন" বাটন
                        Button(
                            onClick = { activePageHomeFeed = sonarBanglaPage },
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Login,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "লগইন করুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }

                // Section: Suggested Pages
                item {
                    Text(
                        text = "Suggested Pages",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }

                // Suggested Page 1: Arisha Noor
                item {
                    val arishaPage = remember {
                        SocialPage(
                            id = "page_arisha_noor",
                            name = "Arisha Noor",
                            category = "Public figure",
                            avatarUrl = "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?w=300",
                            coverUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=1200",
                            followerCount = 1426,
                            creatorName = "Arisha Noor"
                        )
                    }
                    SuggestedPageCard(
                        page = arishaPage,
                        followerText = "1,426 people follow this",
                        onClick = { activePageDetail = arishaPage }
                    )
                }

                // Suggested Page 2: সুমাইয়া ফ্যাশন
                item {
                    val sumaiyaPage = remember {
                        SocialPage(
                            id = "page_sumaiya_fashion",
                            name = "সুমাইয়া ফ্যাশন",
                            category = "Clothing & Fashion",
                            avatarUrl = "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=300",
                            coverUrl = "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1200",
                            followerCount = 3000,
                            creatorName = "সুমাইয়া ফ্যাশন"
                        )
                    }
                    SuggestedPageCard(
                        page = sumaiyaPage,
                        followerText = "3K others follow this",
                        onClick = { activePageDetail = sumaiyaPage }
                    )
                }

                // User Created Pages
                if (pages.isNotEmpty()) {
                    item {
                        Text(
                            text = "আপনার পেইজসমূহ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp)
                        )
                    }
                    items(pages) { page ->
                        PageCard(
                            page = page,
                            strings = strings,
                            onClick = { activePageDetail = page },
                            onToggleLike = { viewModel.toggleLikePage(page.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }
    }

    // Screenshot 2: Create Group Dialog (CreateGroupImage2Dialog with gallery photo picker, name, description, private checkbox, confirm/cancel)
    if (showCreateGroupDialog) {
        CreateGroupImage2Dialog(
            viewModel = viewModel,
            onDismiss = { showCreateGroupDialog = false },
            onGroupCreated = { newGroupName ->
                showCreateGroupDialog = false
                val newGroup = viewModel.createGroup(
                    name = newGroupName,
                    description = "নির্ধারিত নতুন উন্মুক্ত FotoFilm গ্রুপ",
                    isPrivate = false
                )
                // Immediately open the newly created group in Screenshot 4 (GroupDetailScreen)
                activeGroupDetail = newGroup
            }
        )
    }

    // Screenshot 2: Create Page Dialog (CreatePageImage2Dialog with gallery photo picker, name, category, public checkbox, cancel & confirm)
    if (showCreatePageDialog) {
        CreatePageImage2Dialog(
            viewModel = viewModel,
            onDismiss = { showCreatePageDialog = false },
            onPageCreated = { pageName, pageCategory, imageUri ->
                showCreatePageDialog = false
                val newPage = viewModel.createPage(
                    name = pageName,
                    category = pageCategory.ifBlank { "Personal blog" },
                    avatarUrl = imageUri?.toString() ?: "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=500&auto=format&fit=crop&q=80"
                )
                // Immediately open the newly created page in Screenshot 4 & 5 (PageDetailScreen)
                activePageDetail = newPage
            }
        )
    }

    // গ্রুপ এডমিন ও মডারেটর টিম পরিচালনা ডায়ালগ (৫ এডমিন ও ১৫ মডারেটর)
    selectedGroupForRoles?.let { grp ->
        GroupRolesManagementDialog(
            group = grp,
            viewModel = viewModel,
            onDismiss = { selectedGroupForRoles = null }
        )
    }
}

@Composable
fun GroupCard(
    group: SocialGroup,
    strings: com.example.model.AppTranslations,
    onManageRoles: () -> Unit = {},
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
                // Admins & Moderators Badges (৫ এডমিন ও ১৫ মডারেটর)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF4CAF50))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("👑", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "এডমিন: ${group.admins.size}/৫ জন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE3F2FD),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2196F3))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("🛡️", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "মডারেটর: ${group.moderators.size}/১৫ জন",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onManageRoles,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("টিম (৫ এডমিন ও ১৫ মডারেটর)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onToggleJoin,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (group.isJoined) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                        )
                    ) {
                        Text(
                            text = if (group.isJoined) "Joined ✓" else strings.join,
                            color = if (group.isJoined) MaterialTheme.colorScheme.onSurfaceVariant else Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PageCard(
    page: SocialPage,
    strings: com.example.model.AppTranslations,
    onClick: () -> Unit = {},
    onToggleLike: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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

@Composable
private fun RecentlyVisitedPageRow(
    name: String,
    subtitle: String,
    avatarUrl: String,
    actionText: String = "Follow",
    onClick: () -> Unit
) {
    var isFollowing by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = avatarUrl,
            contentDescription = name,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Surface(
            onClick = { isFollowing = !isFollowing },
            shape = RoundedCornerShape(8.dp),
            color = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
        ) {
            Text(
                text = if (isFollowing) "Following ✓" else actionText,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                color = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
            )
        }
    }
}

@Composable
private fun SuggestedPageCard(
    page: SocialPage,
    followerText: String,
    onClick: () -> Unit
) {
    var isFollowing by remember { mutableStateOf(false) }
    var isDismissed by remember { mutableStateOf(false) }

    if (isDismissed) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                Text(
                    text = page.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = followerText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { isFollowing = !isFollowing },
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else FacebookBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Following ✓" else "+ Follow",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = { isDismissed = true },
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Remove",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * গ্রুপ পরিচালনা টিম: ৫ এডমিন ও ১৫ মডারেটর ডায়ালগ
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupRolesManagementDialog(
    group: SocialGroup,
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val groups by viewModel.groups.collectAsState()
    val allUsers by viewModel.allAppUsers.collectAsState()
    val currentGroup = groups.find { it.id == group.id } ?: group

    var selectedTab by remember { mutableIntStateOf(0) } // 0 = 5 Admins, 1 = 15 Moderators
    var showUserPickerForRole by remember { mutableStateOf<String?>(null) } // "ADMIN" or "MODERATOR"

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentGroup.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "গ্রুপ পরিচালনা টিম (৫ এডমিন ও ১৫ মডারেটর)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher: 5 Admins vs 15 Moderators
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                "👑 ৫ এডমিন (${currentGroup.admins.size}/৫)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                "🛡️ ১৫ মডারেটর (${currentGroup.moderators.size}/১৫)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    // 5 Admins Tab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সর্বোচ্চ ৫ জন এডমিন স্লট:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentGroup.admins.size < 5) {
                            FilledTonalButton(
                                onClick = { showUserPickerForRole = "ADMIN" },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE8F5E9)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ এডমিন যোগ করুন", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentGroup.admins) { admin ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = admin.avatarUrl,
                                        contentDescription = admin.fullName,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(admin.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("👑 গ্রুপ এডমিন (Admin Slot)", fontSize = 11.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeAdminFromGroup(currentGroup.id, admin.id) }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Empty slots display up to 5
                        val emptyAdminSlots = 5 - currentGroup.admins.size
                        repeat(emptyAdminSlots) { index ->
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showUserPickerForRole = "ADMIN" }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("👑", fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("খালি এডমিন স্লট #${currentGroup.admins.size + index + 1}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("ট্যাপ করে বন্ধুকে এডমিন হিসেবে নিযুক্ত করুন", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = FacebookBlue)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // 15 Moderators Tab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "সর্বোচ্চ ১৫ জন মডারেটর স্লট:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentGroup.moderators.size < 15) {
                            FilledTonalButton(
                                onClick = { showUserPickerForRole = "MODERATOR" },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFE3F2FD)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("+ মডারেটর যোগ করুন", fontSize = 11.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(currentGroup.moderators) { mod ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = mod.avatarUrl,
                                        contentDescription = mod.fullName,
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(mod.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("🛡️ গ্রুপ মডারেটর (Moderator Slot)", fontSize = 11.sp, color = Color(0xFF1565C0), fontWeight = FontWeight.SemiBold)
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeModeratorFromGroup(currentGroup.id, mod.id) }
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }

                        // Empty slots display up to 15
                        val emptyModSlots = 15 - currentGroup.moderators.size
                        repeat(emptyModSlots) { index ->
                            item {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                    color = Color.Transparent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showUserPickerForRole = "MODERATOR" }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🛡️", fontSize = 16.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("খালি মডারেটর স্লট #${currentGroup.moderators.size + index + 1}", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("ট্যাপ করে সদস্যকে মডারেটর বানান", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = BangladeshGreen)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FacebookBlue),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("সম্পন্ন করুন (Save & Done)", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }

    // Friend Picker dialog to assign Admin or Moderator
    showUserPickerForRole?.let { role ->
        val existingRoleUserIds = currentGroup.admins.map { it.id }.toSet() + currentGroup.moderators.map { it.id }.toSet()
        val availableUsers = allUsers.filter { !existingRoleUserIds.contains(it.id) }

        AlertDialog(
            onDismissRequest = { showUserPickerForRole = null },
            title = {
                Text(
                    text = if (role == "ADMIN") "👑 নতুন এডমিন যোগ করুন (সর্বোচ্চ ৫ জন)" else "🛡️ নতুন মডারেটর যোগ করুন (সর্বোচ্চ ১৫ জন)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableUsers) { user ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (role == "ADMIN") {
                                        viewModel.addAdminToGroup(currentGroup.id, user)
                                    } else {
                                        viewModel.addModeratorToGroup(currentGroup.id, user)
                                    }
                                    showUserPickerForRole = null
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = user.avatarUrl,
                                    contentDescription = user.fullName,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("@${user.username}", fontSize = 11.sp, color = Color.Gray)
                                }
                                Icon(Icons.Default.Add, contentDescription = "Add", tint = FacebookBlue)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showUserPickerForRole = null }) {
                    Text("বন্ধ করুন")
                }
            }
        )
    }
}
