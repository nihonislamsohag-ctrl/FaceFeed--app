package com.example.ui.components

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Storefront
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
import com.example.model.User
import com.example.ui.theme.BangladeshGreen
import com.example.ui.theme.FacebookBlue
import com.example.viewmodel.AppTab
import com.example.viewmodel.FaceFeedViewModel

data class MarketProduct(
    val id: String,
    val title: String,
    val priceBdt: String,
    val location: String,
    val imageUrl: String,
    val category: String,
    val sellerName: String,
    val sellerAvatarUrl: String = "",
    val sellerId: String = "seller_default",
    val isVerified: Boolean = true
)

/**
 * FotoFilm Marketplace Dialog
 * User specifications:
 * 1. Top right '+' button opens phone gallery to select photos for selling products.
 * 2. On each product card, displays the seller's profile avatar icon & name above the product.
 * 3. Message button initiates a direct chat with the seller/buyer (e.g. user posted watch, Rahim messaged to buy),
 *    supporting continuous replies back and forth.
 */
@Composable
fun MarketplaceDialog(
    viewModel: FaceFeedViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    var selectedCategory by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }

    // Dialog & Sheet States
    var showGalleryPickerSheet by remember { mutableStateOf(false) }
    var showCreateProductDialog by remember { mutableStateOf(false) }
    var selectedProductForMessage by remember { mutableStateOf<MarketProduct?>(null) }
    var selectedNewProductImage by remember { mutableStateOf<String?>(null) }

    // System Android Photo Picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedNewProductImage = uri.toString()
            showGalleryPickerSheet = false
            showCreateProductDialog = true
        }
    }

    // User-added products (no demo/mock listings)
    var productList by remember { mutableStateOf(emptyList<MarketProduct>()) }

    val filteredProducts = remember(productList, selectedCategory, searchQuery) {
        productList.filter { prod ->
            val matchCat = selectedCategory == "all" || prod.category == selectedCategory
            val matchQuery = searchQuery.isBlank() || prod.title.contains(searchQuery, ignoreCase = true) || prod.location.contains(searchQuery, ignoreCase = true)
            matchCat && matchQuery
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
            ) {
                // Top Header (Screenshot 2: FotoFilm মার্কেটপ্লেস + বিক্রি করুন button)
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

                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛍️ FotoFilm মার্কেটপ্লেস",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            FidaVerifiedBadge(size = 17.dp)
                        }

                        // Requirement 2: '+' Button opens Gallery to sell products
                        Button(
                            onClick = { showGalleryPickerSheet = true },
                            colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(36.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Product", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("বিক্রি করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("মার্কেটপ্লেসে যেকোনো জিনিস খুঁজুন...", fontSize = 13.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = FacebookBlue) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                )

                // Category Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == "all",
                        onClick = { selectedCategory = "all" },
                        label = { Text("সব পণ্য") }
                    )
                    FilterChip(
                        selected = selectedCategory == "fashion",
                        onClick = { selectedCategory = "fashion" },
                        label = { Text("ঘড়ি ও পোশাক") }
                    )
                    FilterChip(
                        selected = selectedCategory == "mobile",
                        onClick = { selectedCategory = "mobile" },
                        label = { Text("মোবাইল") }
                    )
                    FilterChip(
                        selected = selectedCategory == "electronics",
                        onClick = { selectedCategory = "electronics" },
                        label = { Text("ইলেকট্রনিক্স") }
                    )
                }

                if (filteredProducts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Storefront,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "মার্কেটপ্লেসে এখনো কোনো পণ্য নেই",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "উপরের (+) বাটনে চাপ দিয়ে ফোনের গ্যালারি থেকে পণ্যের ছবি আপলোড করুন।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Product Grid (2 columns)
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentPadding = PaddingValues(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredProducts, key = { it.id }) { product ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Requirement 3: Seller Profile Icon & Name above product
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        FotoFilmAvatar(
                                            imageUrl = product.sellerAvatarUrl,
                                            name = product.sellerName,
                                            contentDescription = product.sellerName,
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                        )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = product.sellerName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (product.isVerified) {
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "Verified",
                                                    tint = FacebookBlue,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "বিক্রেতা • সক্রিয়",
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                // Product Image
                                AsyncImage(
                                    model = product.imageUrl,
                                    contentDescription = product.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(130.dp),
                                    contentScale = ContentScale.Crop
                                )

                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = product.priceBdt,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FacebookBlue
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = product.title,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = product.location,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Requirement 4: Message button with interactive replies
                                    Button(
                                        onClick = {
                                            selectedProductForMessage = product
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(32.dp),
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(Icons.Default.ChatBubble, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("মেসেজ দিন", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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

    // =========================================================================
    // 1. Phone Gallery Picker Modal Sheet (Opened on '+' button press)
    // =========================================================================
    if (showGalleryPickerSheet) {
        val galleryPhotos = remember { emptyList<String>() }

        Dialog(
            onDismissRequest = { showGalleryPickerSheet = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = BangladeshGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ফোনের গ্যালারি থেকে ছবি বাছুন",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { showGalleryPickerSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        Text(
                            text = "আপনি যে পণ্য বা ঘড়ি বিক্রি করতে চান তার ছবি সিলেক্ট করুন:",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        if (galleryPhotos.isNotEmpty()) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(galleryPhotos) { photoUrl ->
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = "Gallery item",
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                selectedNewProductImage = photoUrl
                                                showGalleryPickerSheet = false
                                                showCreateProductDialog = true
                                            },
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // System File/Gallery Picker Button
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("গ্যালারি ফাইল ম্যানেজার থেকে আরও ছবি বাছুন 📁")
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // 2. Create / Post Product for Sale Dialog
    // =========================================================================
    if (showCreateProductDialog && selectedNewProductImage != null) {
        var productTitleInput by remember { mutableStateOf("স্মার্টওয়াচ / রোলেক্স গোল্ডেন হাতঘড়ি") }
        var productPriceInput by remember { mutableStateOf("৩,৫০০") }
        var productLocationInput by remember { mutableStateOf("ঢাকা, বাংলাদেশ") }
        var productCategoryInput by remember { mutableStateOf("fashion") }

        Dialog(
            onDismissRequest = { showCreateProductDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "নতুন পণ্য বিক্রি করুন 🏷️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { showCreateProductDialog = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Selected Photo Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = selectedNewProductImage,
                            contentDescription = "Selected product photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Seller display
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FotoFilmAvatar(
                            imageUrl = currentUser.avatarUrl,
                            name = currentUser.fullName,
                            contentDescription = "Seller",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "বিক্রেতা: ${currentUser.fullName}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "আপনার নিজস্ব প্রোফাইল থেকে পোস্ট হবে",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = productTitleInput,
                        onValueChange = { productTitleInput = it },
                        label = { Text("পণ্যের শিরোনাম / বিবরণ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = productPriceInput,
                        onValueChange = { productPriceInput = it },
                        label = { Text("দাম (টাকা)") },
                        prefix = { Text("৳ ") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = productLocationInput,
                        onValueChange = { productLocationInput = it },
                        label = { Text("লোকেশন / শহর") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val prodId = "prod_user_${System.currentTimeMillis()}"
                            val rawImg = selectedNewProductImage!!
                            val newProduct = MarketProduct(
                                id = prodId,
                                title = productTitleInput.ifBlank { "স্মার্টওয়াচ / রোলেক্স গোল্ডেন হাতঘড়ি" },
                                priceBdt = "৳ " + productPriceInput.replace("৳", "").trim(),
                                location = productLocationInput.ifBlank { "ঢাকা, বাংলাদেশ" },
                                imageUrl = rawImg,
                                category = productCategoryInput,
                                sellerName = currentUser.fullName,
                                sellerAvatarUrl = currentUser.avatarUrl,
                                sellerId = currentUser.id,
                                isVerified = true
                            )
                            productList = listOf(newProduct) + productList
                            showCreateProductDialog = false
                            if ((rawImg.startsWith("content://") || rawImg.startsWith("file://")) &&
                                com.example.data.CloudinaryManager.isEnabled() &&
                                com.example.data.CloudinaryManager.isConfigured()
                            ) {
                                com.example.data.CloudinaryManager.uploadMedia(
                                    uri = Uri.parse(rawImg),
                                    isVideoOrAudio = false,
                                    folder = "fotofilm/marketplace",
                                    contextOverride = context,
                                    onSuccess = { cloudUrl ->
                                        productList = productList.map { p ->
                                            if (p.id == prodId) p.copy(imageUrl = cloudUrl) else p
                                        }
                                        Toast.makeText(context, "পণ্য ও ছবি Cloudinary ক্লাউডে সেভ হয়েছে ✓", Toast.LENGTH_SHORT).show()
                                    },
                                    onFailure = { }
                                )
                            } else {
                                Toast.makeText(context, "আপনার পণ্যটি মার্কেটপ্লেসে সফলভাবে পোস্ট হয়েছে! 🎉", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text("মার্কেটপ্লেসে পোস্ট করুন 🚀", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }

    // =========================================================================
    // 3. Message Seller / Buyer Interactive Dialog
    // =========================================================================
    selectedProductForMessage?.let { product ->
        val isUserSeller = product.sellerName.equals(currentUser.fullName, ignoreCase = true)
        var messageText by remember {
            mutableStateOf(
                if (isUserSeller) {
                    "আসসালামু আলাইকুম সোহাগ ভাই! আমি আপনার এই ${product.title} ঘড়িটি কিনতে চাই। এটা কি এখনও আছে?"
                } else {
                    "আসসালামু আলাইকুম, আমি আপনার এই পণ্যটি কিনতে আগ্রহী: ${product.title} (${product.priceBdt})। এটি কি এখনও পাওয়া যাবে?"
                }
            )
        }

        Dialog(
            onDismissRequest = { selectedProductForMessage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isUserSeller) "রহিম হোসেন-এর মেসেজ 💬" else "${product.sellerName}-কে মেসেজ পাঠান 💬",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { selectedProductForMessage = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Product Summary Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = product.imageUrl,
                                contentDescription = product.title,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = product.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = product.priceBdt,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = FacebookBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Preset Quick Message Chips
                    Text(
                        text = "দ্রুত মেসেজ পাঠান:",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(
                            listOf(
                                "দাম কি কিছুটা কম হবে?",
                                "ক্যাশ অন ডেলিভারিতে পাওয়া যাবে?",
                                "ঘড়িটির সরাসরি ছবি বা ভিডিও দেখতে চাই"
                            )
                        ) { chipText ->
                            SuggestionChip(
                                onClick = { messageText = chipText },
                                label = { Text(chipText, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Send Button
                    Button(
                        onClick = {
                            val targetUser = User(
                                id = product.sellerId.ifBlank { "seller_${product.id}" },
                                username = "seller",
                                fullName = product.sellerName,
                                avatarUrl = product.sellerAvatarUrl
                            )

                            viewModel.sendMarketplaceInquiry(
                                productTitle = product.title,
                                productPrice = product.priceBdt,
                                targetUser = targetUser,
                                customMessage = messageText,
                                isIncomingFromBuyer = isUserSeller
                            )

                            selectedProductForMessage = null
                            onDismiss()
                            Toast.makeText(context, "মেসেঞ্জারে চ্যাট ওপেন হয়েছে! মেসেজের রিপ্লাই দেওয়া যাবে 💬", Toast.LENGTH_LONG).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BangladeshGreen),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(42.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("মেসেঞ্জারে পাঠান 💬", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
