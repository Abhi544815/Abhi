package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocalFireDepartment
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Prompt
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.DeepSpaceDark
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.HotAmber
import com.example.ui.theme.NeonMagenta
import com.example.ui.theme.SoftSilver
import com.example.ui.theme.SpaceSlate
import com.example.ui.theme.TranslucentGlass

@Composable
fun HomeScreen(viewModel: PromptViewModel) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("discover") }
    var showNotifications by remember { mutableStateOf(false) }

    // Collect View Model States
    val isPremium by viewModel.isPremiumUser.collectAsState()
    val showUpgradeDialog by viewModel.showUpgradeDialog.collectAsState()
    val unreadNotifications by viewModel.unreadNotificationCount.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceDark),
        topBar = {
            TopAppBar(
                isPremium = isPremium,
                unreadNotificationCount = unreadNotifications,
                onNotificationClick = {
                    showNotifications = true
                    viewModel.markAllNotificationsRead()
                },
                onUpgradeClick = { viewModel.showUpgradeDialog.value = true }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                activeTab = activeTab,
                onTabSelect = { activeTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .drawBehind {
                    // Top glowing orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(NeonMagenta.copy(alpha = 0.12f), Color.Transparent),
                            radius = 600f,
                            center = Offset(size.width * 0.8f, 100f)
                        ),
                        radius = 600f,
                        center = Offset(size.width * 0.8f, 100f)
                    )
                    // Bottom glowing orb
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(ElectricCyan.copy(alpha = 0.1f), Color.Transparent),
                            radius = 700f,
                            center = Offset(size.width * 0.2f, size.height * 0.9f)
                        ),
                        radius = 700f,
                        center = Offset(size.width * 0.2f, size.height * 0.9f)
                    )
                }
        ) {
            when (activeTab) {
                "discover" -> DiscoverTab(viewModel, onGenerateClick = { concept ->
                    viewModel.selectedEngine.value = "Midjourney"
                    viewModel.generateRefinedPrompt(context, concept)
                    activeTab = "builder"
                })
                "builder" -> BuilderTab(viewModel)
                "challenges" -> ChallengesTab(viewModel)
                "profile" -> ProfileTab(viewModel)
            }

            // In-App Notifications Overlay Dialog
            if (showNotifications) {
                NotificationsDialog(
                    notificationsFlow = viewModel.notifications,
                    onDismiss = { showNotifications = false }
                )
            }

            // Premium Paywall Bottom Sheet / Popup
            if (showUpgradeDialog) {
                PremiumUpgradeDialog(
                    onDismiss = { viewModel.showUpgradeDialog.value = false },
                    onUpgrade = { viewModel.upgradeToPremium(context) }
                )
            }
        }
    }
}

@Composable
fun TopAppBar(
    isPremium: Boolean,
    unreadNotificationCount: Int,
    onNotificationClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .rotate(12f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(ElectricCyan, NeonMagenta)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TS",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TrendSetter",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif
                    )
                    Text(
                        text = ".",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = ElectricCyan,
                        fontFamily = FontFamily.SansSerif
                    )
                }
                Text(
                    text = "Discover AI's viral creations",
                    fontSize = 11.sp,
                    color = SoftSilver,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isPremium) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(HotAmber, NeonMagenta)
                            )
                        )
                        .clickable { onUpgradeClick() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Upgrade Icon",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "GET PRO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(ElectricCyan, NeonMagenta)),
                            RoundedCornerShape(20.dp)
                        )
                        .background(TranslucentGlass)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "PREMIUM",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Box {
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.testTag("notification_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "In-App Notifications",
                        tint = Color.White
                    )
                }
                if (unreadNotificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 4.dp, end = 4.dp)
                            .size(10.dp)
                            .background(Color.Red, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(activeTab: String, onTabSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .background(DeepSpaceDark)
            .drawBehind {
                drawLine(
                    color = Color.White.copy(0.08f),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = 0.5.dp.toPx()
                )
            }
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navItems = listOf(
            Triple("discover", "Discover", Icons.Default.Explore),
            Triple("builder", "AI Writer", Icons.Default.AutoAwesome),
            Triple("challenges", "Challenges", Icons.Default.EmojiEvents),
            Triple("profile", "Profile", Icons.Default.Person)
        )

        navItems.forEach { (tag, label, icon) ->
            val isSelected = activeTab == tag
            val tintColor = if (isSelected) ElectricCyan else SoftSilver

            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onTabSelect(tag) }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
                    .testTag("nav_item_$tag"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = tintColor,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = tintColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun DiscoverTab(viewModel: PromptViewModel, onGenerateClick: (String) -> Unit) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isPremium by viewModel.isPremiumUser.collectAsState()
    val filteredPrompts by viewModel.filteredPrompts.collectAsState()
    val recommendedPrompts by viewModel.recommendedPrompts.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()

    val searchTags = listOf("Cinematic", "Realistic", "Fashion", "Travel", "Food", "Product Ads", "Influencer", "Sports", "Cars", "Luxury")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search & Tag Filter Box
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Search Input with standard M3 outlining and glass theme
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input"),
                    placeholder = { Text("Search cinematic, realistic, food...", color = SoftSilver) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon", tint = SoftSilver) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear search", tint = SoftSilver)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCyan,
                        unfocusedBorderColor = TranslucentGlass,
                        focusedContainerColor = SpaceSlate.copy(0.4f),
                        unfocusedContainerColor = SpaceSlate.copy(0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Scrollable Quick Search Tags
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchTags) { tag ->
                        val isSelected = searchQuery.equals(tag, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) ElectricCyan else GlassBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .background(if (isSelected) ElectricCyan else TranslucentGlass)
                                .clickable {
                                    if (isSelected) viewModel.searchQuery.value = ""
                                    else viewModel.searchQuery.value = tag
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Daily Sync Hero banner
        item {
            HeroTrendBanner(
                isSyncing = isSyncing,
                onSyncClick = { viewModel.syncTrends(context) }
            )
        }

        // Horizontal Category Row
        item {
            Column {
                Text(
                    text = "AI Platforms & Styles",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
                LazyRow(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        "All", "ChatGPT", "Gemini", "Midjourney", "Flux", "Instagram Viral",
                        "YouTube Content", "Logo Design", "Product Photography", "Cinematic Portraits", "Ghibli Style"
                    )
                    items(categories) { category ->
                        val isSelected = selectedCategory == category
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) ElectricCyan else GlassBorder,
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .background(if (isSelected) ElectricCyan else TranslucentGlass)
                                .clickable { viewModel.selectedCategory.value = category }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = category,
                                color = if (isSelected) Color.Black else Color.White.copy(alpha = 0.8f),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Recommended Carousel (only visible when filtering All)
        if (selectedCategory == "All" && searchQuery.isEmpty()) {
            item {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Recommended for You",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElectricCyan
                        )
                        Text(
                            text = "AI Curated",
                            fontSize = 11.sp,
                            color = SoftSilver
                        )
                    }
                    LazyRow(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(recommendedPrompts) { prompt ->
                            RecommendedPromptCard(
                                prompt = prompt,
                                onCardClick = { onGenerateClick(prompt.promptText) }
                            )
                        }
                    }
                }
            }
        }

        // In-feed Ads mock banner for free version
        if (!isPremium) {
            item {
                AdMobBanner()
            }
        }

        // Feed header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Trending Prompts Feed",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "${filteredPrompts.size} items",
                    fontSize = 12.sp,
                    color = SoftSilver
                )
            }
        }

        // List of scrolling prompt cards
        if (filteredPrompts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Empty list",
                        tint = SoftSilver.copy(0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matching prompts found.",
                        color = SoftSilver,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Try syncing live trends or altering search.",
                        color = SoftSilver.copy(0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        } else {
            items(filteredPrompts, key = { it.id }) { prompt ->
                PromptCard(
                    prompt = prompt,
                    onCopy = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Copied AI Prompt", prompt.promptText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    onBookmark = { viewModel.toggleBookmark(context, prompt) },
                    onShare = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, prompt.title)
                            putExtra(Intent.EXTRA_TEXT, "Check out this viral ${prompt.category} AI prompt in Trend Setter: \"${prompt.promptText}\"")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Prompt"))
                    },
                    onGenerate = { onGenerateClick(prompt.promptText) }
                )
            }
        }
    }
}

@Composable
fun HeroTrendBanner(isSyncing: Boolean, onSyncClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(NeonMagenta.copy(alpha = 0.2f), SpaceSlate)
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(listOf(NeonMagenta.copy(alpha = 0.5f), ElectricCyan.copy(alpha = 0.3f))),
                RoundedCornerShape(20.dp)
            )
            .padding(16.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Trending icon",
                        tint = HotAmber,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "VIRAL PATTERN ENGINE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = HotAmber,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "AUTO-RUNNING",
                    color = AccentGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Live AI Trend Monitor",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Discover high-relevance prompt recipes matching social visual explosions on Instagram, Midjourney, and YouTube hourly.",
                color = SoftSilver,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSyncClick,
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                modifier = Modifier
                    .align(Alignment.End)
                    .testTag("sync_trends_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Sync",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSyncing) "Analyzing live..." else "Sync Live Trends",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun AdMobBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DarkGrey)
            .border(0.5.dp, Color.White.copy(0.1f), RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "SPONSORED AD",
                fontSize = 8.sp,
                color = SoftSilver.copy(0.5f),
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Learn Prompt Engineering 10x Faster! Upgrade to get unlimited Saves and remove Ad banners.",
                color = SoftSilver,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RecommendedPromptCard(prompt: Prompt, onCardClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TranslucentGlass)
            .border(
                0.5.dp,
                Color.White.copy(0.12f),
                RoundedCornerShape(16.dp)
            )
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = SpaceSlate.copy(0.6f))
    ) {
        Column {
            // Draw visual abstract preview using Canvas
            CategoryArtCanvas(
                category = prompt.category,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            )
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = prompt.category.uppercase(),
                    color = ElectricCyan,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = prompt.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Whatshot,
                        contentDescription = "Trend",
                        tint = HotAmber,
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Score ${prompt.trendScore}/100",
                        color = HotAmber,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun PromptCard(
    prompt: Prompt,
    onCopy: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    onGenerate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .border(
                width = 0.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .testTag("prompt_card_${prompt.id}"),
        colors = CardDefaults.cardColors(containerColor = SpaceSlate.copy(0.45f))
    ) {
        Column {
            // Top Section: Category & Score
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
            ) {
                // Background artistic gradient representation
                CategoryArtCanvas(
                    category = prompt.category,
                    modifier = Modifier.fillMaxSize()
                )

                // Translucent Overlay glass panel on image top
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .align(Alignment.TopStart),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(0.6f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = prompt.category,
                            color = ElectricCyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(0.6f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = "Flame",
                                tint = HotAmber,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Trend Score: ${prompt.trendScore}",
                                color = HotAmber,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Premium Badge on top of image
                if (prompt.isPremium) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Brush.horizontalGradient(listOf(HotAmber, NeonMagenta)))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PREMIUM PACK",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Body Section: Title and prompt details
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = prompt.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = prompt.promptText,
                    color = SoftSilver,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Buttons row with standard minimum targets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Copy Button
                        IconButton(
                            onClick = onCopy,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TranslucentGlass)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Prompt to Clipboard",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Bookmark/Save Button
                        IconButton(
                            onClick = onBookmark,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TranslucentGlass)
                                .testTag("bookmark_button_${prompt.id}")
                        ) {
                            Icon(
                                imageVector = if (prompt.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save to collections",
                                tint = if (prompt.isBookmarked) ElectricCyan else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Share Button
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(TranslucentGlass)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Prompt",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Tweak & Write Button
                    Button(
                        onClick = onGenerate,
                        colors = ButtonDefaults.buttonColors(containerColor = TranslucentGlass),
                        modifier = Modifier
                            .height(36.dp)
                            .border(0.5.dp, ElectricCyan.copy(0.4f), RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Generate",
                                tint = ElectricCyan,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tweak in AI",
                                color = ElectricCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuilderTab(viewModel: PromptViewModel) {
    val context = LocalContext.current
    var concept by remember { mutableStateOf("") }
    val selectedEngine by viewModel.selectedEngine.collectAsState()
    val isOptimizing by viewModel.aiOptimizing.collectAsState()
    val optimizedResult by viewModel.aiOptimizedResult.collectAsState()

    val engines = listOf("Midjourney", "Flux", "ChatGPT", "Gemini", "Instagram Viral")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "✨ Real-Time Prompt Refiner",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
        Text(
            text = "Transform raw ideas into optimized elite variables using the server-side Gemini AI core.",
            color = SoftSilver,
            fontSize = 11.sp
        )

        // Select Platform Row
        Column {
            Text(
                text = "Select Target Platform",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SoftSilver
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(engines) { engine ->
                    val isSelected = selectedEngine == engine
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) ElectricCyan else TranslucentGlass)
                            .clickable { viewModel.selectedEngine.value = engine }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = engine,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Input Field
        Column {
            Text(
                text = "Enter raw concept, scene, or idea",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SoftSilver
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = concept,
                onValueChange = { concept = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .testTag("prompt_concept_input"),
                placeholder = { Text("e.g., astronaut playing acoustic guitar in a field of red flowers...", color = SoftSilver.copy(alpha = 0.5f)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = TranslucentGlass,
                    focusedContainerColor = SpaceSlate.copy(0.4f),
                    unfocusedContainerColor = SpaceSlate.copy(0.2f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Button(
            onClick = { viewModel.generateRefinedPrompt(context, concept) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_prompt_button"),
            colors = ButtonDefaults.buttonColors(containerColor = NeonMagenta)
        ) {
            Text(
                text = if (isOptimizing) "AI Synthesizing..." else "Refine with Gemini AI",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        if (isOptimizing) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(modifier = Modifier.size(40.dp)) {
                        drawCircle(
                            color = ElectricCyan,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Refining tokens...", color = ElectricCyan, fontSize = 12.sp)
                }
            }
        } else if (optimizedResult.isNotEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(SpaceSlate.copy(alpha = 0.4f))
                    .border(0.5.dp, Color.White.copy(0.12f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Refinement Complete!",
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        fontSize = 13.sp
                    )
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Copied Refined Prompt", optimizedResult)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Refined prompt copied!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    item {
                        Text(
                            text = optimizedResult,
                            color = Color.White,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ChallengesTab(viewModel: PromptViewModel) {
    val context = LocalContext.current
    val allPrompts by viewModel.allPrompts.collectAsState()

    // Filter prompts for voting under "Challenges" tab (e.g. challenge style tags)
    val challengeSubmissions = allPrompts.take(3)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "🏆 Daily Creative Challenge",
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )

        // Today's challenge box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.horizontalGradient(listOf(SpaceSlate, NeonMagenta.copy(alpha = 0.2f))))
                .border(1.dp, ElectricCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(HotAmber)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "ACTIVE NOW",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp
                        )
                    }
                    Text(text = "Ends in: 14h 22m", color = SoftSilver, fontSize = 11.sp)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Prompt Challenge: Anime Cyber-Bakery",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Craft an enchanting, neon-lit cyberpunk bakery in anime style. Best composition, lighting and aesthetic details win.",
                    fontSize = 11.sp,
                    color = SoftSilver,
                    lineHeight = 15.sp
                )
            }
        }

        // Leaderboard / submissions
        Text(
            text = "Leaderboard & Voting",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(challengeSubmissions) { prompt ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TranslucentGlass)
                        .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prompt.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = prompt.promptText,
                            fontSize = 10.sp,
                            color = SoftSilver,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Votes: ${prompt.voteCount}",
                            color = AccentGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(
                        onClick = { viewModel.voteOnPrompt(context, prompt) },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.15f))
                            .testTag("vote_button_${prompt.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = "Vote Up",
                            tint = ElectricCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(viewModel: PromptViewModel) {
    val context = LocalContext.current
    val isPremium by viewModel.isPremiumUser.collectAsState()
    val streakCount by viewModel.streakCount.collectAsState()
    val claimedToday by viewModel.hasClaimedStreakToday.collectAsState()
    val savedPrompts by viewModel.bookmarkedPrompts.collectAsState()
    val creations by viewModel.myCreations.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Streaks Tracking Panel
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(TranslucentGlass)
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(HotAmber.copy(0.4f), Color.Transparent)),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(HotAmber.copy(0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalFireDepartment,
                                contentDescription = "Fire Streak",
                                tint = HotAmber,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Streak Monitor",
                                fontSize = 11.sp,
                                color = SoftSilver,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$streakCount Days",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Active",
                                    fontSize = 11.sp,
                                    color = AccentGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.claimDailyStreak(context) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (claimedToday) TranslucentGlass else HotAmber
                        ),
                        enabled = !claimedToday,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (claimedToday) "Claimed" else "Claim Daily",
                            color = if (claimedToday) SoftSilver else Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // Saved list
        item {
            Column {
                Text(
                    text = "Bookmarks & Saved Collections (${savedPrompts.size}/5 Limit on Free)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                if (savedPrompts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TranslucentGlass)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Save prompts in home feed to build collections.",
                            color = SoftSilver,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        savedPrompts.forEach { prompt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SpaceSlate.copy(0.4f))
                                    .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prompt.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = prompt.category,
                                        color = ElectricCyan,
                                        fontSize = 10.sp
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.toggleBookmark(context, prompt) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bookmark,
                                        contentDescription = "Unsave",
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // My creations
        item {
            Column {
                Text(
                    text = "My Custom AI Creations History",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                if (creations.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(TranslucentGlass)
                            .padding(20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No custom prompts optimized yet. Try the AI Writer tab!",
                            color = SoftSilver,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        creations.forEach { creation ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SpaceSlate.copy(0.4f))
                                    .border(0.5.dp, Color.White.copy(0.08f), RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = creation.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = creation.category,
                                        color = NeonMagenta,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = creation.promptText,
                                    color = SoftSilver,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryArtCanvas(category: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Determine background and shape colors based on category
        val colors = when (category) {
            "Cinematic Portraits" -> listOf(Color(0xFF4A154B), Color(0xFF1A0A1C))
            "Ghibli Style" -> listOf(Color(0xFF2E7D32), Color(0xFF0F3810))
            "Product Photography" -> listOf(Color(0xFF37474F), Color(0xFF102027))
            "ChatGPT", "Gemini" -> listOf(Color(0xFF0D47A1), Color(0xFF001035))
            "Midjourney", "Flux" -> listOf(Color(0xFF4A00E0), Color(0xFF8E2DE2))
            else -> listOf(SpaceSlate, DeepSpaceDark)
        }

        // Draw radial glowing background
        drawRect(
            brush = Brush.verticalGradient(colors)
        )

        // Draw beautiful floating abstract glowing vectors/circles to resemble elite generative visuals
        when (category) {
            "Cinematic Portraits" -> {
                // Warm portrait glowing light
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFB74D).copy(alpha = 0.3f), Color.Transparent),
                        radius = height * 0.7f,
                        center = Offset(width * 0.5f, height * 0.4f)
                    ),
                    radius = height * 0.7f,
                    center = Offset(width * 0.5f, height * 0.4f)
                )
                // Silhouette profile loop lines
                drawCircle(
                    color = Color.White.copy(0.04f),
                    radius = height * 0.4f,
                    center = Offset(width * 0.5f, height * 0.5f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            "Ghibli Style" -> {
                // Soft green blobs
                drawCircle(
                    color = Color(0xFF81C784).copy(0.15f),
                    radius = height * 0.35f,
                    center = Offset(width * 0.3f, height * 0.5f)
                )
                drawCircle(
                    color = Color(0xFFAED581).copy(0.1f),
                    radius = height * 0.25f,
                    center = Offset(width * 0.7f, height * 0.4f)
                )
                // Bioluminescent particles
                drawCircle(color = AccentGreen.copy(0.7f), radius = 4f, center = Offset(width * 0.2f, height * 0.3f))
                drawCircle(color = AccentGreen.copy(0.5f), radius = 6f, center = Offset(width * 0.8f, height * 0.6f))
                drawCircle(color = Color.White.copy(0.8f), radius = 3f, center = Offset(width * 0.5f, height * 0.2f))
            }
            "Product Photography" -> {
                // Metallic layout curves
                drawLine(
                    color = ElectricCyan.copy(0.15f),
                    start = Offset(0f, height * 0.8f),
                    end = Offset(width, height * 0.2f),
                    strokeWidth = 3.dp.toPx()
                )
                drawCircle(
                    color = Color.White.copy(0.05f),
                    radius = height * 0.3f,
                    center = Offset(width * 0.5f, height * 0.5f)
                )
            }
            else -> {
                // Cyberpunk / AI theme network lines
                drawCircle(
                    color = ElectricCyan.copy(0.05f),
                    radius = height * 0.5f,
                    center = Offset(width * 0.5f, height * 0.5f),
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCircle(
                    color = NeonMagenta.copy(0.05f),
                    radius = height * 0.3f,
                    center = Offset(width * 0.5f, height * 0.5f),
                    style = Stroke(width = 1.dp.toPx())
                )
                drawLine(
                    color = ElectricCyan.copy(0.1f),
                    start = Offset(0f, 0f),
                    end = Offset(width, height),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

@Composable
fun NotificationsDialog(
    notificationsFlow: StateFlow<List<InAppNotification>>,
    onDismiss: () -> Unit
) {
    val items by notificationsFlow.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.7f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(24.dp))
                .background(SpaceSlate)
                .border(1.dp, Color.White.copy(0.12f), RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🔔 In-App Trend Alerts",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color.White
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(items) { notif ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (notif.isRead) TranslucentGlass.copy(0.5f) else TranslucentGlass)
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = notif.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (notif.isRead) SoftSilver else ElectricCyan
                            )
                            Text(text = notif.time, color = SoftSilver, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = notif.message,
                            fontSize = 11.sp,
                            color = SoftSilver,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumUpgradeDialog(onDismiss: () -> Unit, onUpgrade: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(0.8f))
            .clickable { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(SpaceSlate)
                .border(
                    1.dp,
                    Brush.verticalGradient(listOf(ElectricCyan, Color.Transparent)),
                    RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                )
                .clickable(enabled = false) {}
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium",
                        tint = HotAmber,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Trend Setter PREMIUM",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Text(
                text = "Unleash your creative content game. Get instant hourly syncs, unlimited saved collections, and completely ad-free discover experience.",
                fontSize = 12.sp,
                color = SoftSilver,
                lineHeight = 17.sp
            )

            // Premium benefits bullet list
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumBullet("🚀 Unlimited Saved Collections (Currently 5 Max)")
                PremiumBullet("🚫 Complete Ad Removal (No sponsored banners)")
                PremiumBullet("⏱️ Unlimited AI Refinements (Direct server access)")
                PremiumBullet("⚡ Exclusive Pro Packs (Highlight and copy tags)")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onUpgrade,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("premium_upgrade_confirm"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Upgrade Instantly to PRO",
                    color = Color.Black,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun PremiumBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Bullet",
            tint = HotAmber,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, color = Color.White, fontSize = 12.sp)
    }
}
