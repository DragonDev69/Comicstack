package com.comicstack.ui

import android.graphics.BitmapFactory
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.comicstack.data.db.CollectedCardEntity
import com.comicstack.data.model.CardStats
import com.comicstack.data.model.Rarity
import com.comicstack.data.repository.CardRepository
import com.comicstack.ui.components.CollectibleCard
import com.comicstack.ui.theme.*
import com.comicstack.util.MihonLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

// ═══════════════════════════════════════════════════════════
// HARDCODED DEMO DATA — Absolute Batman #1, Cover A
// ═══════════════════════════════════════════════════════════
private const val ISSUE_ID = "absolute-batman-1"
private const val ISSUE_TITLE = "Absolute Batman #1"
private const val SERIES = "Absolute Batman"
private const val ISSUE_NUM = 1
private const val WRITER = "Scott Snyder"
private const val ARTIST = "Nick Dragotta"
private const val COVER_ASSET = "covers/ab1_cover_a.jpg"
private const val COVER_TYPE = "Cover A"
private val COVER_RARITY = Rarity.COMMON
private const val RAC_URL = "https://readallcomics.com/absolute-batman-001/"
private const val SYNOPSIS = "Alfred Pennyworth arrives in Gotham to surveil the Party Animals — a " +
    "new gang responsible for a 700% spike in the murder rate. But when they attack City Hall, " +
    "a blue-collar vigilante with an axe steps out of the shadows. Without the mansion, without " +
    "the money, without the butler — the Absolute Dark Knight rises."

// ═══════════════════════════════════════════════════════════
// VIEWMODEL
// ═══════════════════════════════════════════════════════════
@HiltViewModel
class MainViewModel @Inject constructor(
    private val cardRepo: CardRepository
) : ViewModel() {

    val collection = cardRepo.getAllCards()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _hasRead = MutableStateFlow(false)
    val hasRead = _hasRead.asStateFlow()

    private val _mintedCard = MutableStateFlow<CollectedCardEntity?>(null)
    val mintedCard = _mintedCard.asStateFlow()

    init {
        viewModelScope.launch {
            val existing = cardRepo.getCardForIssue(ISSUE_ID)
            _hasRead.value = existing != null
        }
    }

    fun readAndMint() {
        viewModelScope.launch {
            if (_hasRead.value) return@launch
            val card = cardRepo.mintCard(
                issueId = ISSUE_ID,
                title = ISSUE_TITLE,
                series = SERIES,
                issueNum = ISSUE_NUM,
                artist = ARTIST,
                coverType = COVER_TYPE,
                coverAsset = COVER_ASSET,
                rarity = COVER_RARITY
            )
            _hasRead.value = true
            _mintedCard.value = card
        }
    }

    fun clearReveal() {
        _mintedCard.value = null
    }
}

// ═══════════════════════════════════════════════════════════
// APP ROOT
// ═══════════════════════════════════════════════════════════
@Composable
fun ComicstackApp(viewModel: MainViewModel = hiltViewModel()) {
    val context = LocalContext.current
    val hasRead by viewModel.hasRead.collectAsState()
    val mintedCard by viewModel.mintedCard.collectAsState()
    val collection by viewModel.collection.collectAsState()
    var currentTab by remember { mutableIntStateOf(0) }

    // Card reveal overlay
    if (mintedCard != null) {
        CardRevealOverlay(
            card = mintedCard!!,
            onDismiss = {
                viewModel.clearReveal()
                currentTab = 1 // Jump to collection
            }
        )
    }

    Scaffold(
        containerColor = Background,
        bottomBar = {
            NavigationBar(containerColor = Surface, contentColor = Gold) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.MenuBook, "Read") },
                    label = { Text("Read", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold, selectedTextColor = Gold,
                        unselectedIconColor = OnSurfaceDim, unselectedTextColor = OnSurfaceDim,
                        indicatorColor = Gold.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        BadgedBox(badge = {
                            if (collection.isNotEmpty()) {
                                Badge(containerColor = Gold, contentColor = Color.Black) {
                                    Text("${collection.size}")
                                }
                            }
                        }) { Icon(Icons.Default.Style, "Collection") }
                    },
                    label = { Text("Collection", fontSize = 10.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Gold, selectedTextColor = Gold,
                        unselectedIconColor = OnSurfaceDim, unselectedTextColor = OnSurfaceDim,
                        indicatorColor = Gold.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                0 -> IssueScreen(
                    hasRead = hasRead,
                    onRead = {
                        MihonLauncher.openInMihon(context, RAC_URL)
                        viewModel.readAndMint()
                    }
                )
                1 -> CollectionScreen(cards = collection)
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// ISSUE SCREEN
// ═══════════════════════════════════════════════════════════
@Composable
private fun IssueScreen(hasRead: Boolean, onRead: () -> Unit) {
    val context = LocalContext.current
    val bitmap = remember {
        try {
            context.assets.open(COVER_ASSET).use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
        } catch (_: Exception) { null }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero cover
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = ISSUE_TITLE,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // Gradient scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Background),
                            startY = 300f
                        )
                    )
            )
            // Title overlay
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    "ABSOLUTE UNIVERSE",
                    fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                    color = Gold, letterSpacing = 2.sp, fontWeight = FontWeight.Bold
                )
                Text(
                    ISSUE_TITLE,
                    fontSize = 28.sp, fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif, color = OnBackground
                )
                Text(
                    "$WRITER  ·  $ARTIST",
                    fontSize = 12.sp, color = OnSurfaceDim,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Content
        Column(modifier = Modifier.padding(16.dp)) {
            // READ button
            if (hasRead) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Green.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .border(1.dp, Green.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Green),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("✓", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("READ — Card Earned", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Green)
                        Text("Cover A · Common · #0001", fontSize = 11.sp, color = OnSurfaceDim)
                    }
                }
            } else {
                Button(
                    onClick = onRead,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BatmanRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "READ IN MIHON",
                        fontSize = 15.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Synopsis
            Text(
                "SYNOPSIS",
                fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                color = Gold, letterSpacing = 2.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                SYNOPSIS,
                fontSize = 14.sp, color = OnSurface, lineHeight = 22.sp
            )

            Spacer(Modifier.height(20.dp))

            // Issue info
            Text(
                "DETAILS",
                fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                color = Gold, letterSpacing = 2.sp, fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            InfoRow("Series", SERIES)
            InfoRow("Issue", "#$ISSUE_NUM")
            InfoRow("Writer", WRITER)
            InfoRow("Artist", ARTIST)
            InfoRow("Cover", "$COVER_TYPE · $ARTIST")
            InfoRow("Publisher", "DC Comics")
            InfoRow("Year", "2024")

            Spacer(Modifier.height(60.dp))
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 12.sp, color = OnSurfaceDim, fontFamily = FontFamily.Monospace)
        Text(value, fontSize = 12.sp, color = OnSurface, fontWeight = FontWeight.Medium)
    }
}

// ═══════════════════════════════════════════════════════════
// COLLECTION SCREEN
// ═══════════════════════════════════════════════════════════
@Composable
private fun CollectionScreen(cards: List<CollectedCardEntity>) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "COLLECTION",
            fontSize = 10.sp, fontFamily = FontFamily.Monospace,
            color = Gold, letterSpacing = 2.sp, fontWeight = FontWeight.Bold
        )
        Text(
            "${cards.size} Card${if (cards.size != 1) "s" else ""}",
            fontSize = 26.sp, fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Serif, color = OnBackground
        )
        Spacer(Modifier.height(16.dp))

        if (cards.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🃏", fontSize = 56.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No cards yet",
                        fontSize = 16.sp, color = OnSurfaceDim, fontWeight = FontWeight.Medium
                    )
                    Text(
                        "Read an issue to earn your first card",
                        fontSize = 13.sp, color = OnSurfaceDim.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards) { card ->
                    val rarity = Rarity.entries.find { it.name == card.rarity } ?: Rarity.COMMON
                    CollectibleCard(
                        coverAsset = card.coverAsset,
                        title = card.title,
                        artist = card.artist,
                        rarity = rarity,
                        hp = card.hp, atk = card.atk, def = card.def, spd = card.spd
                    )
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════
// CARD REVEAL OVERLAY
// ═══════════════════════════════════════════════════════════
@Composable
private fun CardRevealOverlay(card: CollectedCardEntity, onDismiss: () -> Unit) {
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(500); revealed = true }

    val rotation by animateFloatAsState(
        targetValue = if (revealed) 0f else 90f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "flip"
    )
    val scale by animateFloatAsState(
        targetValue = if (revealed) 1f else 0.7f,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "scale"
    )

    val rarity = Rarity.entries.find { it.name == card.rarity } ?: Rarity.COMMON
    val rarColor = Color(rarity.colorHex)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = false) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            // Spinning card
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        scaleX = scale
                        scaleY = scale
                    }
            ) {
                CollectibleCard(
                    coverAsset = card.coverAsset,
                    title = card.title,
                    artist = card.artist,
                    rarity = rarity,
                    hp = card.hp, atk = card.atk, def = card.def, spd = card.spd
                )
            }

            if (revealed) {
                Spacer(Modifier.height(24.dp))

                // Card info
                Text(
                    "CARD EARNED",
                    fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                    color = Gold, letterSpacing = 3.sp, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    card.title,
                    fontSize = 20.sp, fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Serif, color = OnBackground,
                    textAlign = TextAlign.Center
                )
                Text(
                    "${card.coverType} · ${card.artist}",
                    fontSize = 12.sp, color = OnSurfaceDim
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    rarity.label.uppercase(),
                    fontSize = 11.sp, fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold, color = rarColor,
                    letterSpacing = 1.sp,
                    modifier = Modifier
                        .background(rarColor.copy(alpha = 0.12f), RoundedCornerShape(4.dp))
                        .border(1.dp, rarColor.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Serial #${card.serialNumber.toString().padStart(4, '0')}",
                    fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = OnSurfaceDim
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "ADD TO COLLECTION",
                        fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
