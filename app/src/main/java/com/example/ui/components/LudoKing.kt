package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.FirebaseManager
import com.example.model.User
import com.example.viewmodel.FaceFeedViewModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.firestore.ListenerRegistration
import android.widget.Toast
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.zIndex
import com.example.data.SampleData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Validates whether a Ludo token can legally move by [dice] steps without overshooting Home (57)
 */
fun canTokenMove(token: Token, dice: Int): Boolean {
    if (token.isFinished) return false
    if (token.position == -1) return dice == 6
    if (token.position < 52) {
        val currentSteps = (token.position - token.color.startIndex + 52) % 52
        return if (currentSteps + dice > 50) {
            val remaining = (currentSteps + dice) - 51
            (52 + remaining) <= 57
        } else {
            true
        }
    }
    return (token.position + dice) <= 57
}

/**
 * 4 Ludo Colors with standard board starting indices (0, 13, 26, 39)
 */
enum class LudoColor(val color: Color, val startIndex: Int, val displayName: String, val bengaliName: String) {
    RED(Color(0xFFE53935), 0, "Red", "লাল"),
    GREEN(Color(0xFF43A047), 13, "Green", "সবুজ"),
    YELLOW(Color(0xFFFDD835), 26, "Yellow", "হলুদ"),
    BLUE(Color(0xFF1E88E5), 39, "Blue", "নীল")
}

/**
 * Immutable Token to guarantee 100% reliable Jetpack Compose state updates and recomposition
 */
data class Token(
    val id: Int,
    val color: LudoColor,
    val position: Int = -1, // -1 means in Home Base, 0..51 on outer track, 52..57 in home stretch (57 = finished)
    val isFinished: Boolean = false
)

/**
 * 5 Game Modes requested by user + Firebase Realtime Multiplayer
 */
enum class GameMode(val title: String, val subtitle: String, val icon: String, val tag: String) {
    VS_COMPUTER("Vs Computer (1 Player vs 3 Computers)", "১ প্লেয়ার বনাম ৩ কম্পিউটার (AI)", "🤖", "AI BOT"),
    TWO_BIJOR("2 Players - Bijor (Red vs Blue, 1v1)", "২ জন বিজোড় ১v১ (পাস অ্যান্ড প্লে)", "⚔️", "1v1"),
    THREE_BIJOR("3 Players - Bijor (Red vs Green vs Blue)", "৩ জন বিজোড় (পাস অ্যান্ড প্লে)", "🔺", "3 PLAYERS"),
    FOUR_BIJOR("4 Players - Bijor (All vs All)", "৪ জন বিজোড় (অল বনাম অল, ৪ রং)", "🏆", "4 PLAYERS"),
    TWO_JOR_TEAM("2 Players Jor - Team Mode (Team 2vs2)", "Team Red+Yellow vs Team Green+Blue", "🤝", "TEAM 2v2"),
    ONLINE_MULTIPLAYER("Online Multiplayer (Free Time Play)", "অনলাইন রিয়েলটাইম রুম ও বন্ধুদের সাথে খেলা", "🌐", "ONLINE")
}

/**
 * Full Ludo King Game component with:
 * 1. Big 6/Dice moved much lower down with tactile pound sound & vibration ("৬ কক্কার উপরে চাপ দিলে পাউন্ড অইবে")
 * 2. Token cut whoosh strike sound ("গুটি কাটলে ফস করে সদ্বো হবে")
 * 3. Token home victory fanfare sound ("গুটি পাকলে সদ্বো হবে")
 * 4. Player profile photos & Live Audio Voice Chat indicator with mic toggle for each player
 * 5. 5 Distinct interactive emoji reaction buttons (😂, 🔥, 😡, 🎉, 👏) with unique sound synthesis
 * 6. GUTI GURBE 360° rotation on 6 and 720° spin launch
 * 7. AdMob Banner & Interstitial Ad integration
 */
@Composable
fun LudoKingFullGame(
    viewModel: FaceFeedViewModel? = null,
    initialMode: GameMode? = null,
    onDismiss: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // First Screen is Game Mode Selection if null
    var selectedMode by remember(initialMode) { mutableStateOf<GameMode?>(initialMode) }
    var tokens by remember {
        mutableStateOf(
            LudoColor.entries.flatMap { c -> (0..3).map { Token(it, c) } }
        )
    }
    var currentPlayerIndex by remember { mutableIntStateOf(0) }
    var diceValue by remember { mutableIntStateOf(1) }
    var isDiceRolled by remember { mutableStateOf(false) }
    var isDiceRollingAnimation by remember { mutableStateOf(false) }
    var displayedTumbleValue by remember { mutableIntStateOf(1) }
    var statusText by remember { mutableStateOf("🎲 আপনার চাল (Blue)! নিচে বড় ছক্কায় চাপুন") }
    var winnerAnnouncement by remember { mutableStateOf<String?>(null) }

    // Launch Spin Animation for newly released token (720° spin)
    var launchedTokenKey by remember { mutableStateOf<String?>(null) }
    val launchSpinAnim = remember { Animatable(0f) }

    // Audio Voice Chat State for each of the 4 players (Mic on/off)
    var voiceStates by remember {
        mutableStateOf(
            mapOf(
                LudoColor.BLUE to true,
                LudoColor.RED to true,
                LudoColor.GREEN to true,
                LudoColor.YELLOW to true
            )
        )
    }

    // Sound ON/OFF, Quick Chat, & Online Room Invite States
    var isSoundEnabled by remember { mutableStateOf(true) }
    var showQuickChatBar by remember { mutableStateOf(false) }
    var showOnlineInviteDialog by remember { mutableStateOf(false) }
    var invitedFriendIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    val clipboardManager = LocalClipboardManager.current

    // Active floating emoji or quick-chat bubble per player with auto-dismiss
    var activePlayerEmojis by remember { mutableStateOf<Map<LudoColor, String>>(emptyMap()) }

    // Online Multiplayer States
    var roomId by remember { mutableStateOf("LUDO-${Random.nextInt(1000, 9999)}") }
    var isRoomJoined by remember { mutableStateOf(false) }
    var roomPlayersCount by remember { mutableIntStateOf(1) }

    // Agora Live Ludo Voice Call Integration
    val agoraState by com.example.data.AgoraCallManager.callState.collectAsState()
    var isAgoraLudoVoiceActive by remember { mutableStateOf(false) }
    var pendingLudoVoiceJoin by remember { mutableStateOf(false) }
    val ludoAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted && pendingLudoVoiceJoin) {
            val ludoChannel = com.example.data.AgoraCallManager.sanitizeChannelName("ff_ludo_$roomId")
            val muted = !(voiceStates[LudoColor.BLUE] ?: true)
            com.example.data.AgoraCallManager.joinCall(
                context = context,
                channelName = ludoChannel,
                isVideoCall = false,
                startMuted = muted
            )
            isAgoraLudoVoiceActive = true
        } else if (!granted) {
            Toast.makeText(context, "লুডু লাইভ ভয়েসের জন্য মাইক্রোফোন অনুমতি দিন", Toast.LENGTH_SHORT).show()
        }
        pendingLudoVoiceJoin = false
    }
    fun toggleLudoAgoraVoice() {
        if (isAgoraLudoVoiceActive) {
            com.example.data.AgoraCallManager.leaveCall()
            isAgoraLudoVoiceActive = false
            Toast.makeText(context, "লুডু Agora ভয়েস বন্ধ করা হয়েছে", Toast.LENGTH_SHORT).show()
        } else {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
            if (granted) {
                val ludoChannel = com.example.data.AgoraCallManager.sanitizeChannelName("ff_ludo_$roomId")
                val muted = !(voiceStates[LudoColor.BLUE] ?: true)
                com.example.data.AgoraCallManager.joinCall(
                    context = context,
                    channelName = ludoChannel,
                    isVideoCall = false,
                    startMuted = muted
                )
                isAgoraLudoVoiceActive = true
                Toast.makeText(context, "লুডু Agora HD ভয়েস চালু হয়েছে! 🎙️", Toast.LENGTH_SHORT).show()
            } else {
                pendingLudoVoiceJoin = true
                ludoAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            if (isAgoraLudoVoiceActive) {
                com.example.data.AgoraCallManager.leaveCall()
            }
        }
    }

    // Players active in match
    var players by remember {
        mutableStateOf(listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW))
    }

    val outerPath = remember { generateOuterPath() }
    val safeCells = remember { setOf(0, 8, 13, 21, 26, 34, 39, 47) }

    // Player Avatars (User profile photo for Blue, authentic portraits for others)
    val currentUserProfile = viewModel?.currentUser?.collectAsState()?.value
    val playerAvatars = remember(currentUserProfile) {
        mapOf(
            LudoColor.BLUE to (currentUserProfile?.avatarUrl?.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150" } ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"),
            LudoColor.RED to "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150",
            LudoColor.GREEN to "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=150",
            LudoColor.YELLOW to "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=150"
        )
    }

    fun triggerPlayerEmoji(color: LudoColor, emoji: String) {
        if (isSoundEnabled) {
            LudoSoundManager.playEmojiSound(emoji, context)
        }
        activePlayerEmojis = activePlayerEmojis + (color to emoji)
        coroutineScope.launch {
            delay(2400)
            activePlayerEmojis = activePlayerEmojis - color
        }
    }

    fun resetGame(mode: GameMode) {
        selectedMode = mode
        tokens = LudoColor.entries.flatMap { c -> (0..3).map { Token(it, c) } }
        currentPlayerIndex = 0
        diceValue = 1
        displayedTumbleValue = 1
        isDiceRolled = false
        isDiceRollingAnimation = false
        winnerAnnouncement = null
        launchedTokenKey = null
        activePlayerEmojis = emptyMap()

        players = when (mode) {
            GameMode.VS_COMPUTER -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW)
            GameMode.TWO_BIJOR -> listOf(LudoColor.BLUE, LudoColor.RED)
            GameMode.THREE_BIJOR -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN)
            GameMode.FOUR_BIJOR -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW)
            GameMode.TWO_JOR_TEAM -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW)
            GameMode.ONLINE_MULTIPLAYER -> listOf(LudoColor.BLUE, LudoColor.RED, LudoColor.GREEN, LudoColor.YELLOW)
        }
        statusText = "🎲 ${if (mode == GameMode.VS_COMPUTER) "আপনার চাল (You)" else "${players[0].displayName} চাল দিন"}! বড় ছক্কায় চাপুন"

        if (mode == GameMode.ONLINE_MULTIPLAYER) {
            isRoomJoined = true
        }
    }

    // Firebase Multiplayer Real-time Listener
    DisposableEffect(selectedMode, isRoomJoined, roomId) {
        var registration: ListenerRegistration? = null
        if (selectedMode == GameMode.ONLINE_MULTIPLAYER && isRoomJoined) {
            val currentUser = viewModel?.currentUser?.value ?: User(
                id = "user_host",
                fullName = "You (Blue)",
                username = "blue_player"
            )
            registration = FirebaseManager.createOrJoinLudoRoom(
                roomId = roomId,
                user = currentUser,
                color = "BLUE",
                onRoomUpdate = { data ->
                    val newDice = (data["diceValue"] as? Long)?.toInt()
                    val turnCol = data["turnColor"] as? String
                    val isRolled = data["isDiceRolled"] as? Boolean
                    val playersList = data["players"] as? List<Map<String, Any>>
                    if (playersList != null) {
                        roomPlayersCount = playersList.size
                    }
                    if (newDice != null && turnCol != null && isRolled != null) {
                        val remoteColor = LudoColor.entries.firstOrNull { it.name == turnCol } ?: LudoColor.BLUE
                        val pIndex = players.indexOf(remoteColor)
                        if (pIndex != -1 && pIndex != currentPlayerIndex) {
                            currentPlayerIndex = pIndex
                        }
                        diceValue = newDice
                        displayedTumbleValue = newDice
                        isDiceRolled = isRolled
                    }
                }
            )
        }
        onDispose {
            registration?.remove()
        }
    }

    /**
     * Roll Dice with Tactile Pounding Sound & Rattle ("৬ কক্কার উপরে চাপ দিলে পাউন্ড অইবে")
     */
    fun rollDice() {
        if (isDiceRolled || isDiceRollingAnimation) return

        coroutineScope.launch {
            isDiceRollingAnimation = true

            // Trigger Pounding Sound + Tactile Haptic Vibration
            if (isSoundEnabled) {
                LudoSoundManager.playDiceRollSound(context)
            }

            // Fast tumble animation through random faces
            repeat(6) {
                displayedTumbleValue = Random.nextInt(1, 7)
                delay(60)
            }

            val rolled = Random.nextInt(1, 7)
            diceValue = rolled
            displayedTumbleValue = rolled
            isDiceRollingAnimation = false
            isDiceRolled = true

            val activeColor = players.getOrNull(currentPlayerIndex) ?: players[0]
            val moveable = tokens.filter {
                it.color == activeColor && canTokenMove(it, rolled)
            }

            if (moveable.isEmpty()) {
                statusText = "${activeColor.bengaliName} কোনো চাল নেই (পরের প্লেয়ারের চাল)"
                delay(500)
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size
                isDiceRolled = false
            } else {
                if (rolled == 6) {
                    statusText = "👉 Token-এ ক্লিক করুন বের হওয়ার জন্য (ছক্কা পড়েছে! গুটি ঘুরছে)"
                } else {
                    statusText = "👉 Token-এ ক্লিক করুন চাল দেওয়ার জন্য (ডাইস: $rolled)"
                }
            }

            if (selectedMode == GameMode.ONLINE_MULTIPLAYER && isRoomJoined) {
                FirebaseManager.syncLudoTurnAndDice(roomId, diceValue, activeColor.name, isDiceRolled)
            }
        }
    }

    fun onTokenClick(token: Token) {
        val activeColor = players.getOrNull(currentPlayerIndex) ?: players[0]
        if (token.color != activeColor) return
        if (!isDiceRolled || isDiceRollingAnimation) return

        var cutOpponent = false
        var reachedHome = false

        if (token.position == -1) {
            // Home token can ONLY move if diceValue == 6!
            if (diceValue == 6) {
                // Play 720° spin launch animation
                val key = "${token.color.name}_${token.id}"
                launchedTokenKey = key
                coroutineScope.launch {
                    launchSpinAnim.snapTo(0f)
                    launchSpinAnim.animateTo(
                        targetValue = 720f,
                        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing)
                    )
                    launchedTokenKey = null
                }

                val startPos = token.color.startIndex
                tokens = tokens.map {
                    if (it.id == token.id && it.color == token.color) {
                        it.copy(position = startPos)
                    } else it
                }
                statusText = "🎉 ${token.color.bengaliName} গুটি ${token.id + 1} বেস থেকে বের হয়েছে! (ছক্কার জন্য অতিরিক্ত চাল)"
                // Extra roll on 6
                isDiceRolled = false
            }
        } else if (!token.isFinished) {
            val newPos: Int
            if (token.position < 52) {
                val currentSteps = (token.position - token.color.startIndex + 52) % 52
                if (currentSteps + diceValue > 50) {
                    val remaining = (currentSteps + diceValue) - 51
                    newPos = 52 + remaining
                } else {
                    newPos = (token.position + diceValue) % 52
                }
            } else {
                newPos = token.position + diceValue
            }

            if (newPos <= 57) {
                reachedHome = (newPos == 57)
                tokens = tokens.map { t ->
                    if (t.id == token.id && t.color == token.color) {
                        t.copy(position = newPos, isFinished = reachedHome)
                    } else if (newPos in 0..51 && newPos !in safeCells && t.color != token.color && t.position == newPos) {
                        val isTeammate = selectedMode == GameMode.TWO_JOR_TEAM &&
                            ((token.color == LudoColor.RED && t.color == LudoColor.YELLOW) ||
                             (token.color == LudoColor.YELLOW && t.color == LudoColor.RED) ||
                             (token.color == LudoColor.BLUE && t.color == LudoColor.GREEN) ||
                             (token.color == LudoColor.GREEN && t.color == LudoColor.BLUE))

                        if (!isTeammate && !isJorSafe(tokens, t)) {
                            cutOpponent = true
                            t.copy(position = -1) // Sent back to Home Base
                        } else {
                            t
                        }
                    } else {
                        t
                    }
                }

                // Sound Effect 2: "গুটি কাটলে ফস করে সদ্বো হবে"
                if (cutOpponent) {
                    if (isSoundEnabled) LudoSoundManager.playTokenCutSound(context)
                    statusText = "💥 ফস! ${token.color.bengaliName} গুটি কেটেছে! অতিরিক্ত চাল পেল!"
                    triggerPlayerEmoji(token.color, "🔥")
                } else if (reachedHome) {
                    // Sound Effect 3: "গুটি পাকলে সদ্বো হবে" (Victory fanfare chime)
                    if (isSoundEnabled) LudoSoundManager.playTokenHomeSound(context)
                    statusText = "👑 সাবাশ! ${token.color.bengaliName} গুটি পাকা করল!"
                    triggerPlayerEmoji(token.color, "🎉")
                }

                // Check Win Condition
                if (selectedMode == GameMode.TWO_JOR_TEAM) {
                    val teamBlueGreenFinished = tokens.count { (it.color == LudoColor.BLUE || it.color == LudoColor.GREEN) && it.isFinished }
                    val teamRedYellowFinished = tokens.count { (it.color == LudoColor.RED || it.color == LudoColor.YELLOW) && it.isFinished }
                    if (teamBlueGreenFinished >= 8) {
                        winnerAnnouncement = "🏆 টিম Green+Blue বিজয়ী হয়েছে!"
                        if (isSoundEnabled) LudoSoundManager.playTokenHomeSound(context)
                        showInterstitialAd(context)
                    } else if (teamRedYellowFinished >= 8) {
                        winnerAnnouncement = "🏆 টিম Red+Yellow বিজয়ী হয়েছে!"
                        if (isSoundEnabled) LudoSoundManager.playTokenHomeSound(context)
                        showInterstitialAd(context)
                    }
                } else {
                    players.forEach { p ->
                        val finished = tokens.count { it.color == p && it.isFinished }
                        if (finished >= 4 && winnerAnnouncement == null) {
                            winnerAnnouncement = "🏆 ${p.bengaliName} প্লেয়ার বিজয়ী হয়েছে!"
                            if (isSoundEnabled) LudoSoundManager.playTokenHomeSound(context)
                            showInterstitialAd(context)
                        }
                    }
                }

                // Turn rotation (if not 6 and didn't cut)
                if (diceValue != 6 && !cutOpponent) {
                    currentPlayerIndex = (currentPlayerIndex + 1) % players.size
                }
                isDiceRolled = false
            }
        }

        if (selectedMode == GameMode.ONLINE_MULTIPLAYER && isRoomJoined) {
            val tokenData = tokens.map {
                mapOf("id" to it.id, "color" to it.color.name, "position" to it.position, "isFinished" to it.isFinished)
            }
            FirebaseManager.syncLudoTokens(roomId, tokenData, activeColor.name, isDiceRolled)
        }
    }

    // Computer AI Automation for VS_COMPUTER Mode (Blue is human "You", Red/Green/Yellow are AI)
    LaunchedEffect(selectedMode, currentPlayerIndex, isDiceRolled, isDiceRollingAnimation) {
        if (selectedMode == GameMode.VS_COMPUTER && winnerAnnouncement == null && !isDiceRollingAnimation) {
            val activeColor = players.getOrNull(currentPlayerIndex) ?: players[0]
            val isComputerTurn = activeColor != LudoColor.BLUE

            if (isComputerTurn) {
                if (!isDiceRolled) {
                    delay(700)
                    rollDice()
                } else {
                    delay(600)
                    val moveableTokens = tokens.filter {
                        it.color == activeColor && canTokenMove(it, diceValue)
                    }
                    if (moveableTokens.isNotEmpty()) {
                        val finishToken = moveableTokens.firstOrNull { tok ->
                            when {
                                tok.position >= 52 -> tok.position + diceValue == 57
                                tok.position >= 0 -> {
                                    val steps = (tok.position - tok.color.startIndex + 52) % 52
                                    steps + diceValue > 50 && (52 + (steps + diceValue - 51)) == 57
                                }
                                else -> false
                            }
                        }
                        val bestToken = finishToken ?: moveableTokens.firstOrNull { tok ->
                            val targetPos = if (tok.position == -1) tok.color.startIndex else (tok.position + diceValue) % 52
                            targetPos in 0..51 && targetPos !in safeCells && tokens.any { it.color != activeColor && it.position == targetPos }
                        } ?: moveableTokens.firstOrNull { it.position == -1 && diceValue == 6 }
                          ?: moveableTokens.maxByOrNull { it.position }
                          ?: moveableTokens.first()

                        onTokenClick(bestToken)
                    } else {
                        currentPlayerIndex = (currentPlayerIndex + 1) % players.size
                        isDiceRolled = false
                    }
                }
            }
        }
    }

    BackHandler {
        if (selectedMode != null) {
            selectedMode = null
        } else {
            onDismiss?.invoke()
        }
    }

    // =========================================================================
    // SCREEN 1: GAME MODES MENU (First Screen before board)
    // =========================================================================
    if (selectedMode == null) {
        LudoModeSelectionScreen(
            onSelectMode = { mode -> resetGame(mode) },
            onDismiss = onDismiss
        )
        return
    }

    // =========================================================================
    // SCREEN 2: ACTIVE LUDO KING BOARD SCREEN
    // =========================================================================
    val currentMode = selectedMode!!
    val activeColor = players.getOrNull(currentPlayerIndex) ?: players[0]
    val isComputerTurn = currentMode == GameMode.VS_COMPUTER && activeColor != LudoColor.BLUE

    val activePlayerLabel = when (currentMode) {
        GameMode.VS_COMPUTER -> if (activeColor == LudoColor.BLUE) "আপনার চাল (You)" else "${activeColor.displayName} (Computer AI)"
        GameMode.TWO_JOR_TEAM -> if (activeColor == LudoColor.BLUE || activeColor == LudoColor.GREEN) "টিম Blue+Green (${activeColor.bengaliName})" else "টিম Red+Yellow (${activeColor.bengaliName})"
        else -> "Player ${currentPlayerIndex + 1} (${activeColor.bengaliName})"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F2B48), Color(0xFF0A192F), Color(0xFF07111E))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Top Header Bar
        Surface(
            color = Color(0xFF0D1B2A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back to Mode Menu selector button
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E3A8A).copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, Color(0xFFFFD700)),
                    modifier = Modifier.clickable { selectedMode = null }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currentMode.icon, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(currentMode.tag, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        Text(" ▾ মেনু", fontSize = 10.sp, color = Color.White)
                    }
                }

                // Game Title
                Text(
                    text = "Ludo King Pro 🎲",
                    color = Color(0xFFFFD700),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )

                // Right actions: Agora Voice + Sound Toggle + Restart + Close
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isAgoraLudoVoiceActive) Color(0xFF2E7D32) else Color(0xFF1E3A5F),
                        border = BorderStroke(1.dp, if (isAgoraLudoVoiceActive) Color(0xFF00E676) else Color(0xFF64B5F6)),
                        modifier = Modifier.clickable { toggleLudoAgoraVoice() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAgoraLudoVoiceActive) Icons.Default.Mic else Icons.Default.MicOff,
                                contentDescription = "Agora Voice",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isAgoraLudoVoiceActive) "ভয়েস ON" else "ভয়েস",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    IconButton(
                        onClick = { isSoundEnabled = !isSoundEnabled },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Sound Toggle",
                            tint = if (isSoundEnabled) Color(0xFFFFD700) else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = { resetGame(currentMode) },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Restart", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                    IconButton(
                        onClick = { onDismiss?.invoke() },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Online Room info pill if multiplayer (with Copy Code & Invite Friends)
        if (currentMode == GameMode.ONLINE_MULTIPLAYER) {
            Surface(
                color = Color(0xFF1B5E20),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(roomId))
                            Toast.makeText(context, "রুম কোড কপি হয়েছে: $roomId", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF00E676)))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("রুম: $roomId ($roomPlayersCount/4)", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code", tint = Color(0xFFFFD700), modifier = Modifier.size(13.dp))
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFD700),
                        modifier = Modifier.clickable { showOnlineInviteDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = "Invite", tint = Color.Black, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("বন্ধু ডাকুন", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // 2. Status Notice Bar
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF132F52),
            border = BorderStroke(1.dp, activeColor.color),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(activeColor.color)
                            .border(1.dp, Color.White, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = activePlayerLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Text(
                    text = statusText,
                    fontSize = 10.sp,
                    color = if (isDiceRolled && diceValue == 6) Color(0xFFFFD700) else Color(0xFFE2E8F0),
                    fontWeight = if (isDiceRolled && diceValue == 6) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // 3. TOP PLAYER CARDS WITH AVATAR PHOTO & AUDIO VOICE CHAT (Red on Left, Green on Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Top-Left: Red (Computer 2 / Player 2)
            LudoProfilePlayerCard(
                color = LudoColor.RED,
                name = if (currentMode == GameMode.VS_COMPUTER) "Computer 2" else "Player 2",
                avatarUrl = playerAvatars[LudoColor.RED] ?: "",
                diceValue = if (activeColor == LudoColor.RED) displayedTumbleValue else 1,
                isTurn = activeColor == LudoColor.RED,
                isMicOn = voiceStates[LudoColor.RED] ?: true,
                finishedCount = tokens.count { it.color == LudoColor.RED && it.isFinished },
                activeEmoji = activePlayerEmojis[LudoColor.RED],
                onToggleMic = {
                    voiceStates = voiceStates + (LudoColor.RED to !(voiceStates[LudoColor.RED] ?: true))
                }
            )

            // Top-Right: Green (Computer 3 / Player 3)
            LudoProfilePlayerCard(
                color = LudoColor.GREEN,
                name = if (currentMode == GameMode.VS_COMPUTER) "Computer 3" else "Player 3",
                avatarUrl = playerAvatars[LudoColor.GREEN] ?: "",
                diceValue = if (activeColor == LudoColor.GREEN) displayedTumbleValue else 1,
                isTurn = activeColor == LudoColor.GREEN,
                isMicOn = voiceStates[LudoColor.GREEN] ?: true,
                finishedCount = tokens.count { it.color == LudoColor.GREEN && it.isFinished },
                activeEmoji = activePlayerEmojis[LudoColor.GREEN],
                onToggleMic = {
                    voiceStates = voiceStates + (LudoColor.GREEN to !(voiceStates[LudoColor.GREEN] ?: true))
                }
            )
        }

        Spacer(modifier = Modifier.height(3.dp))

        // 4. 15x15 LUDO BOARD CANVAS WITH TOKENS & STANDS ("এস্টান")
        val boardSize = 295.dp
        val cellStep = boardSize.value / 15f

        // Continuous 360-degree rotation when GUTI GURBE active
        val infiniteTransition = rememberInfiniteTransition(label = "ludo_animations")
        val spinAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "spinAngle"
        )
        val pulseScale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(380, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )

        Box(
            modifier = Modifier
                .size(boardSize)
                .background(Color.White)
                .border(2.5.dp, Color(0xFF1E293B), RoundedCornerShape(4.dp))
        ) {
            // Background Board Artwork with Circular Stands ("এস্টান") & Quadrant Labels
            LudoBoardBackground(
                boardSize = boardSize,
                gameMode = currentMode,
                onHomeQuadrantClick = { clickedColor ->
                    if (isDiceRolled && diceValue == 6 && clickedColor == activeColor) {
                        val homeToken = tokens.firstOrNull { it.color == clickedColor && it.position == -1 }
                        if (homeToken != null) {
                            onTokenClick(homeToken)
                        }
                    }
                }
            )

            // Render all Tokens - Both in Home Base and on the Board with Stand ("এস্টান")
            tokens.forEach { token ->
                val coord: Pair<Float, Float> = when {
                    token.position == -1 -> getHomePosition(token)
                    token.position in 0..51 -> {
                        val pt = outerPath.getOrNull(token.position) ?: Pair(7, 7)
                        Pair(pt.first.toFloat() + 0.5f, pt.second.toFloat() + 0.5f)
                    }
                    else -> getHomePathPosition(token)
                }

                val sameCellTokens = tokens.filter {
                    it.position == token.position && (it.position != -1 || it.color == token.color)
                }
                val stackIndex = sameCellTokens.indexOf(token).coerceAtLeast(0)
                val stackOffsetX = if (sameCellTokens.size > 1) ((stackIndex % 2) * 4 - 2f).dp else 0.dp
                val stackOffsetY = if (sameCellTokens.size > 1) ((stackIndex / 2) * 4 - 2f).dp else 0.dp

                val tokenCenterX = (coord.first * cellStep).dp + stackOffsetX
                val tokenCenterY = (coord.second * cellStep).dp + stackOffsetY

                val isInHome = token.position == -1
                val isMyTurn = token.color == activeColor
                val tokenKey = "${token.color.name}_${token.id}"
                val isLaunching = launchedTokenKey == tokenKey

                // GUTI GURBE strictly applied only to legally movable tokens:
                val canMoveThis = isDiceRolled && isMyTurn && canTokenMove(token, diceValue)
                val isSpinning = canMoveThis

                val effectiveAngle = when {
                    isLaunching -> launchSpinAnim.value
                    isSpinning -> spinAngle
                    else -> 0f
                }

                LudoTokenWithStand(
                    token = token,
                    isSpinning = isSpinning || isLaunching,
                    spinAngle = effectiveAngle,
                    canMoveThis = canMoveThis,
                    pulseScale = pulseScale,
                    modifier = Modifier.offset(tokenCenterX - 15.dp, tokenCenterY - 15.dp),
                    onClick = { onTokenClick(token) }
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        // 5. BOTTOM PLAYER CARDS WITH AVATAR PHOTO & AUDIO VOICE CHAT (Blue on Left, Yellow on Right)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Bottom-Left: Blue ("You")
            LudoProfilePlayerCard(
                color = LudoColor.BLUE,
                name = if (currentMode == GameMode.VS_COMPUTER) "You (আপনি)" else "Player 1",
                avatarUrl = playerAvatars[LudoColor.BLUE] ?: "",
                diceValue = if (activeColor == LudoColor.BLUE) displayedTumbleValue else 1,
                isTurn = activeColor == LudoColor.BLUE,
                isMicOn = voiceStates[LudoColor.BLUE] ?: true,
                finishedCount = tokens.count { it.color == LudoColor.BLUE && it.isFinished },
                activeEmoji = activePlayerEmojis[LudoColor.BLUE],
                onToggleMic = {
                    val nextMicOn = !(voiceStates[LudoColor.BLUE] ?: true)
                    voiceStates = voiceStates + (LudoColor.BLUE to nextMicOn)
                    if (!isAgoraLudoVoiceActive && nextMicOn) {
                        toggleLudoAgoraVoice()
                    } else if (isAgoraLudoVoiceActive) {
                        com.example.data.AgoraCallManager.setMicMuted(!nextMicOn)
                    }
                }
            )

            // Bottom-Right: Yellow (Computer 4 / Player 4)
            LudoProfilePlayerCard(
                color = LudoColor.YELLOW,
                name = if (currentMode == GameMode.VS_COMPUTER) "Computer 4" else "Player 4",
                avatarUrl = playerAvatars[LudoColor.YELLOW] ?: "",
                diceValue = if (activeColor == LudoColor.YELLOW) displayedTumbleValue else 1,
                isTurn = activeColor == LudoColor.YELLOW,
                isMicOn = voiceStates[LudoColor.YELLOW] ?: true,
                finishedCount = tokens.count { it.color == LudoColor.YELLOW && it.isFinished },
                activeEmoji = activePlayerEmojis[LudoColor.YELLOW],
                onToggleMic = {
                    voiceStates = voiceStates + (LudoColor.YELLOW to !(voiceStates[LudoColor.YELLOW] ?: true))
                }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 6. 5 DISTINCT EMOJIS REACTION BAR + QUICK CHAT TRIGGER (😂, 🔥, 😡, 🎉, 👏 + 💬)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF132F52).copy(alpha = 0.9f),
            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.6f)),
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ইমোজি:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFFD700)
                )
                listOf("😂", "🔥", "😡", "🎉", "👏").forEach { emoji ->
                    Surface(
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { triggerPlayerEmoji(activeColor, emoji) }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(emoji, fontSize = 18.sp)
                        }
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (showQuickChatBar) Color(0xFFFFD700) else Color(0xFF1E3A8A),
                    modifier = Modifier.clickable { showQuickChatBar = !showQuickChatBar }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Quick Chat",
                            tint = if (showQuickChatBar) Color.Black else Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "চ্যাট",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showQuickChatBar) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        AnimatedVisibility(visible = showQuickChatBar) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("সেরা চাল! 🔥", "জলদি দাও ⏱️", "সাবধান! 👀", "হাহা 😂", "ভালো খেলা 👏").forEach { phrase ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF1E3A8A),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.7f)),
                        modifier = Modifier.clickable {
                            triggerPlayerEmoji(LudoColor.BLUE, phrase)
                            showQuickChatBar = false
                        }
                    ) {
                        Text(
                            text = phrase,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 7. 🔥 MAIN LARGE 6/DICE MOVED MUCH LOWER DOWN ("মেইন ৬ কাক্কা আরো নিচে বড় করে দেন, চাপ দিলে পাউন্ড অইবে")
        val isHumanCanRoll = (activeColor == LudoColor.BLUE || currentMode != GameMode.VS_COMPUTER) &&
                !isDiceRolled && !isDiceRollingAnimation

        val dicePoundPulse by animateFloatAsState(
            targetValue = if (isDiceRollingAnimation) 1.25f else 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
            label = "dicePoundPulse"
        )
        val diceTumbleRotation by animateFloatAsState(
            targetValue = if (isDiceRollingAnimation) 720f else 0f,
            animationSpec = tween(400, easing = LinearEasing),
            label = "diceTumbleRotation"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Glowing Circular Pedestal for the Big Dice
            Surface(
                shape = CircleShape,
                color = if (isHumanCanRoll) Color(0xFF1E3A8A) else Color(0xFF0F172A),
                border = BorderStroke(3.dp, if (isHumanCanRoll) Color(0xFFFFD700) else Color(0xFF334155)),
                shadowElevation = if (isHumanCanRoll) 12.dp else 4.dp,
                modifier = Modifier
                    .size(80.dp)
                    .scale(dicePoundPulse)
                    .clickable(enabled = isHumanCanRoll) { rollDice() }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    // Big 3D Dice Cube (৬৪.dp)
                    Ludo3DDice(
                        value = displayedTumbleValue,
                        isRolled = isDiceRolled,
                        isRolling = isDiceRollingAnimation,
                        size = 56.dp,
                        modifier = Modifier.rotate(diceTumbleRotation)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Action Instructions with Pounding Sound Tag
            Column {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isHumanCanRoll) Color(0xFFF59E0B) else Color(0xFF1E293B),
                    modifier = Modifier.clickable(enabled = isHumanCanRoll) { rollDice() }
                ) {
                    Text(
                        text = if (isDiceRollingAnimation) "🎲 পাউন্ড হচ্ছে..." else if (isHumanCanRoll) "🎲 ৬ কক্কা চাপুন (পাউন্ড হবে)" else if (isDiceRolled) "👉 গুটিতে ক্লিক করুন" else "অপেক্ষা করুন...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isHumanCanRoll) Color.Black else Color.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isDiceRolled) "দান: $diceValue | গুটি কাটলে ফস, পাকলে জয়!" else "চাপ দিলে পাউন্ড শব্দ ও ভাইব্রেশন হবে 🔊",
                    fontSize = 10.sp,
                    color = Color(0xFFFFD54F)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 8. ADMOB BANNER AT BOTTOM (Production Ad Unit ID)
        AdMobBannerView(
            adUnitId = GoogleAdConfig.BANNER_ID,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Win Announcement Dialog
    winnerAnnouncement?.let { winnerText ->
        AlertDialog(
            onDismissRequest = { /* force action */ },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color(0xFF0F3460),
            title = {
                Text(
                    text = "অভিনন্দন! 🎉",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFFD700),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = winnerText,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "চমৎকার খেলা হয়েছে! আবার খেলতে চান?",
                        fontSize = 12.sp,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        winnerAnnouncement = null
                        resetGame(currentMode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676))
                ) {
                    Text("আবার খেলুন 🔄", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    winnerAnnouncement = null
                    selectedMode = null
                }) {
                    Text("মোড মেনু", color = Color.White)
                }
            }
        )
    }

    // Online Room Friend Invite Dialog
    if (showOnlineInviteDialog) {
        AlertDialog(
            onDismissRequest = { showOnlineInviteDialog = false },
            containerColor = Color(0xFF0F3460),
            title = {
                Text(
                    text = "অনলাইন লুডু রুমে বন্ধু ডাকুন 🎲",
                    color = Color(0xFFFFD700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "রুম কোড: $roomId — বন্ধুদের আমন্ত্রণ পাঠান:",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                    SampleData.profileFriends.take(5).forEach { friend ->
                        val isInvited = friend.id in invitedFriendIds
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF132F52))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = friend.avatarUrl,
                                contentDescription = friend.name,
                                modifier = Modifier.size(34.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = friend.name,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (!isInvited) {
                                        invitedFriendIds = invitedFriendIds + friend.id
                                        roomPlayersCount = (roomPlayersCount + 1).coerceAtMost(4)
                                        Toast.makeText(context, "${friend.name}-কে লুডু রুমে আমন্ত্রণ পাঠানো হয়েছে!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isInvited,
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isInvited) Color(0xFF334155) else Color(0xFF00E676)
                                )
                            ) {
                                Text(
                                    text = if (isInvited) "আমন্ত্রিত ✓" else "ডাকুন",
                                    color = if (isInvited) Color.White else Color.Black,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showOnlineInviteDialog = false }) {
                    Text("ঠিক আছে", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

/**
 * Enhanced Player Card with Circular Profile Avatar Photo, Audio Voice Chat indicator & Emoji Reaction Bubble
 */
@Composable
fun LudoProfilePlayerCard(
    color: LudoColor,
    name: String,
    avatarUrl: String,
    diceValue: Int,
    isTurn: Boolean,
    isMicOn: Boolean,
    finishedCount: Int = 0,
    activeEmoji: String? = null,
    onToggleMic: () -> Unit
) {
    val borderColor = if (isTurn) Color(0xFFFFD700) else Color(0xFF2A4365)
    val borderWidth = if (isTurn) 2.dp else 1.dp

    // Voice animation wave
    val infiniteTransition = rememberInfiniteTransition(label = "voice_wave")
    val waveHeight by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveHeight"
    )

    Box {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF132F52),
            border = BorderStroke(borderWidth, borderColor),
            shadowElevation = if (isTurn) 6.dp else 2.dp,
            modifier = Modifier
                .width(152.dp)
                .height(54.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Profile Avatar Photo with Color Ring
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        shape = CircleShape,
                        color = color.color,
                        border = BorderStroke(1.5.dp, Color.White),
                        modifier = Modifier.size(32.dp)
                    ) {
                        AsyncImage(
                            model = avatarUrl,
                            contentDescription = name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Online green dot
                    Box(
                        modifier = Modifier
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF00E676))
                            .border(1.dp, Color.White, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Name, Audio Voice Status & Finished-Token Progress Dots
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTurn) Color(0xFFFFD700) else Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier.padding(vertical = 1.dp)
                    ) {
                        repeat(4) { idx ->
                            val isHome = idx < finishedCount
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(if (isHome) Color(0xFFFFD700) else Color(0xFF0A192F))
                                    .border(
                                        0.8.dp,
                                        if (isHome) color.color else Color.White.copy(alpha = 0.45f),
                                        CircleShape
                                    )
                            )
                        }
                    }

                    // Audio Voice indicator with live pulsating waves
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onToggleMic() }
                    ) {
                        Icon(
                            imageVector = if (isMicOn) Icons.Default.Mic else Icons.Default.MicOff,
                            contentDescription = "Voice Mic",
                            tint = if (isMicOn) Color(0xFF00E676) else Color.Gray,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        if (isMicOn) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(1.dp)
                            ) {
                                Box(modifier = Modifier.width(2.dp).height((waveHeight * 0.5f).dp).background(Color(0xFF00E676), RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.width(2.dp).height(waveHeight.dp).background(Color(0xFF00E676), RoundedCornerShape(1.dp)))
                                Box(modifier = Modifier.width(2.dp).height((waveHeight * 0.7f).dp).background(Color(0xFF00E676), RoundedCornerShape(1.dp)))
                            }
                        } else {
                            Text("মিউট", fontSize = 8.sp, color = Color.Gray)
                        }
                    }
                }

                // Mini Dice Indicator
                Ludo3DDice(
                    value = diceValue,
                    isRolled = true,
                    size = 28.dp
                )
            }
        }

        // Floating Emoji or Quick Chat Bubble
        activeEmoji?.let { bubbleText ->
            val isLongPhrase = bubbleText.length > 3
            Surface(
                shape = if (isLongPhrase) RoundedCornerShape(12.dp) else CircleShape,
                color = Color.Black.copy(alpha = 0.9f),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-22).dp)
                    .zIndex(10f)
                    .then(if (isLongPhrase) Modifier.wrapContentSize() else Modifier.size(30.dp))
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = if (isLongPhrase) Modifier.padding(horizontal = 8.dp, vertical = 3.dp) else Modifier
                ) {
                    Text(
                        text = bubbleText,
                        fontSize = if (isLongPhrase) 10.sp else 16.sp,
                        fontWeight = if (isLongPhrase) FontWeight.Bold else FontWeight.Normal,
                        color = Color.White,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Screen 1: Mode Selection Menu (Rendered before entering board)
 */
@Composable
fun LudoModeSelectionScreen(
    onSelectMode: (GameMode) -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F2B48), Color(0xFF0A192F), Color(0xFF07111E))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Surface(
            color = Color(0xFF0D1B2A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎲", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Ludo King Pro",
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            color = Color(0xFFFFD700)
                        )
                        Text(
                            text = "FotoFilm ChatFilm Edition",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                onDismiss?.let { dismiss ->
                    IconButton(onClick = dismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E3A8A).copy(alpha = 0.5f),
                border = BorderStroke(1.5.dp, Color(0xFFFFD700)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👑 খেলার মোড নির্বাচন করুন", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "আপনার পছন্দের অপশন বেছে নিয়ে সরাসরি খেলা শুরু করুন!",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            GameMode.entries.forEach { mode ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF16213E),
                    border = BorderStroke(1.5.dp, Color(0xFF334155)),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 5.dp)
                        .clickable { onSelectMode(mode) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0F3460),
                            border = BorderStroke(1.dp, Color(0xFFFFD700)),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(mode.icon, fontSize = 22.sp)
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = mode.subtitle,
                                fontSize = 11.sp,
                                color = Color(0xFFFFD54F)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E3A8A),
                            border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = mode.tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        AdMobBannerView(
            adUnitId = GoogleAdConfig.BANNER_ID,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Authentic 3D Ludo Die Cube (ছক্কা) with real black pips (dots)
 */
@Composable
fun Ludo3DDice(
    value: Int,
    isRolled: Boolean,
    isRolling: Boolean = false,
    size: androidx.compose.ui.unit.Dp = 38.dp,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFFFFDF8),
        border = BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
        shadowElevation = 5.dp,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(3.dp)) {
            val w = this.size.width
            val h = this.size.height
            val dotR = w * 0.115f
            val dotColor = Color(0xFF0F172A)

            val left = w * 0.22f
            val midX = w * 0.5f
            val right = w * 0.78f

            val top = h * 0.22f
            val midY = h * 0.5f
            val bottom = h * 0.78f

            fun drawPip(x: Float, y: Float) {
                drawCircle(Color.Gray.copy(alpha = 0.35f), radius = dotR, center = Offset(x, y + 1f))
                drawCircle(dotColor, radius = dotR, center = Offset(x, y))
                drawCircle(Color.White.copy(alpha = 0.45f), radius = dotR * 0.35f, center = Offset(x - dotR * 0.3f, y - dotR * 0.3f))
            }

            when (value.coerceIn(1, 6)) {
                1 -> drawPip(midX, midY)
                2 -> {
                    drawPip(left, top)
                    drawPip(right, bottom)
                }
                3 -> {
                    drawPip(left, top)
                    drawPip(midX, midY)
                    drawPip(right, bottom)
                }
                4 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
                5 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(midX, midY)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
                6 -> {
                    drawPip(left, top)
                    drawPip(right, top)
                    drawPip(left, midY)
                    drawPip(right, midY)
                    drawPip(left, bottom)
                    drawPip(right, bottom)
                }
            }
        }
    }
}

/**
 * 3D Circular Base Stand ("এস্টান") drawn on the board for Home Base pedestals
 */
private fun drawLudoBaseStand(
    drawScope: DrawScope,
    center: Offset,
    radius: Float,
    baseColor: Color,
    darkColor: Color
) {
    with(drawScope) {
        drawCircle(Color.Black.copy(alpha = 0.3f), radius = radius, center = Offset(center.x, center.y + 2f))
        drawCircle(darkColor, radius = radius, center = center)
        drawCircle(baseColor, radius = radius * 0.84f, center = center)
        drawCircle(
            Color.White.copy(alpha = 0.65f),
            radius = radius * 0.58f,
            center = Offset(center.x, center.y - 1f),
            style = Stroke(width = 2.5f)
        )
        drawCircle(darkColor, radius = radius * 0.35f, center = center)
    }
}

/**
 * Authentic 5-pointed Star ("★ তারা এস্টান")
 */
private fun drawFivePointStar(
    drawScope: DrawScope,
    center: Offset,
    outerRadius: Float,
    strokeColor: Color = Color(0xFF546E7A),
    fillColor: Color? = Color.White,
    strokeWidth: Float = 2.4f
) {
    with(drawScope) {
        val innerRadius = outerRadius * 0.40f
        val path = Path()
        val step = Math.PI / 5.0
        var angle = -Math.PI / 2.0

        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = (center.x + r * Math.cos(angle)).toFloat()
            val y = (center.y + r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            angle += step
        }
        path.close()

        fillColor?.let {
            drawPath(path, it)
        }
        drawPath(path, strokeColor, style = Stroke(width = strokeWidth))
    }
}

/**
 * Directional Arrow on Starting Cells
 */
private fun drawTrackArrow(
    drawScope: DrawScope,
    center: Offset,
    size: Float,
    color: Color,
    direction: Int
) {
    with(drawScope) {
        val path = Path()
        val s = size * 0.5f
        when (direction) {
            0 -> {
                path.moveTo(center.x - s, center.y - s * 0.55f)
                path.lineTo(center.x + s * 0.4f, center.y - s * 0.55f)
                path.lineTo(center.x + s * 0.4f, center.y - s)
                path.lineTo(center.x + s, center.y)
                path.lineTo(center.x + s * 0.4f, center.y + s)
                path.lineTo(center.x + s * 0.4f, center.y + s * 0.55f)
                path.lineTo(center.x - s, center.y + s * 0.55f)
                path.close()
            }
            1 -> {
                path.moveTo(center.x - s * 0.55f, center.y - s)
                path.lineTo(center.x - s * 0.55f, center.y + s * 0.4f)
                path.lineTo(center.x - s, center.y + s * 0.4f)
                path.lineTo(center.x, center.y + s)
                path.lineTo(center.x + s, center.y + s * 0.4f)
                path.lineTo(center.x + s * 0.55f, center.y + s * 0.4f)
                path.lineTo(center.x + s * 0.55f, center.y - s)
                path.close()
            }
            2 -> {
                path.moveTo(center.x + s, center.y - s * 0.55f)
                path.lineTo(center.x - s * 0.4f, center.y - s * 0.55f)
                path.lineTo(center.x - s * 0.4f, center.y - s)
                path.lineTo(center.x - s, center.y)
                path.lineTo(center.x - s * 0.4f, center.y + s)
                path.lineTo(center.x - s * 0.4f, center.y + s * 0.55f)
                path.lineTo(center.x + s, center.y + s * 0.55f)
                path.close()
            }
            3 -> {
                path.moveTo(center.x - s * 0.55f, center.y + s)
                path.lineTo(center.x - s * 0.55f, center.y - s * 0.4f)
                path.lineTo(center.x - s, center.y - s * 0.4f)
                path.lineTo(center.x, center.y - s)
                path.lineTo(center.x + s, center.y - s * 0.4f)
                path.lineTo(center.x + s * 0.55f, center.y - s * 0.4f)
                path.lineTo(center.x + s * 0.55f, center.y + s)
                path.close()
            }
        }
        drawPath(path, color)
    }
}

/**
 * Ludo Token with 3D Circular Stand Pedestal ("এস্টান") and 3D Map-Pin Pawn
 * Rotates 360 degrees continuously when GUTI GURBE condition met!
 */
@Composable
fun LudoTokenWithStand(
    token: Token,
    isSpinning: Boolean,
    spinAngle: Float,
    canMoveThis: Boolean,
    pulseScale: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val baseColor = token.color.color
    val darkBaseColor = when (token.color) {
        LudoColor.RED -> Color(0xFF8B0000)
        LudoColor.GREEN -> Color(0xFF1B5E20)
        LudoColor.BLUE -> Color(0xFF0D47A1)
        LudoColor.YELLOW -> Color(0xFFE65100)
    }
    val lightBaseColor = when (token.color) {
        LudoColor.RED -> Color(0xFFFF8A80)
        LudoColor.GREEN -> Color(0xFFB9F6CA)
        LudoColor.BLUE -> Color(0xFF82B1FF)
        LudoColor.YELLOW -> Color(0xFFFFF59D)
    }

    Box(
        modifier = modifier
            .size(30.dp)
            .clickable(enabled = canMoveThis, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .scale(if (canMoveThis) pulseScale else 1f)
                .rotate(if (isSpinning) spinAngle else 0f),
            contentAlignment = Alignment.Center
        ) {
            // 1. 3D Circular Stand Pedestal ("এস্টান")
            Canvas(modifier = Modifier.size(24.dp)) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.width / 2f

                drawCircle(Color.Black.copy(alpha = 0.35f), radius = r, center = Offset(cx, cy + 2f))
                drawCircle(darkBaseColor, radius = r, center = Offset(cx, cy))
                drawCircle(baseColor, radius = r * 0.82f, center = Offset(cx, cy))
                drawCircle(
                    Color.White.copy(alpha = 0.55f),
                    radius = r * 0.55f,
                    center = Offset(cx, cy - 1f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(darkBaseColor, radius = r * 0.35f, center = Offset(cx, cy))
            }

            // 2. 3D Map-Pin Pawn resting upright on the stand
            Canvas(modifier = Modifier.size(18.dp, 22.dp)) {
                val w = size.width
                val h = size.height
                val cx = w / 2f
                val headR = w * 0.44f
                val headCy = headR + 1.5f

                val pinPath = Path().apply {
                    moveTo(cx, h - 1.5f)
                    lineTo(cx - headR * 0.82f, headCy + headR * 0.48f)
                    arcTo(
                        rect = Rect(
                            left = cx - headR,
                            top = headCy - headR,
                            right = cx + headR,
                            bottom = headCy + headR
                        ),
                        startAngleDegrees = 155f,
                        sweepAngleDegrees = 230f,
                        forceMoveTo = false
                    )
                    lineTo(cx, h - 1.5f)
                    close()
                }

                drawPath(pinPath, Color.Black.copy(alpha = 0.35f))
                drawPath(
                    pinPath,
                    Brush.verticalGradient(
                        colors = listOf(Color.White, Color(0xFFECEFF1), Color(0xFFB0BEC5))
                    )
                )
                drawPath(pinPath, darkBaseColor.copy(alpha = 0.55f), style = Stroke(width = 1.2.dp.toPx()))

                val innerR = headR * 0.65f
                drawCircle(
                    Brush.radialGradient(
                        colors = listOf(lightBaseColor, baseColor, darkBaseColor),
                        center = Offset(cx, headCy)
                    ),
                    radius = innerR,
                    center = Offset(cx, headCy)
                )
                drawCircle(
                    Color.White.copy(alpha = 0.7f),
                    radius = innerR * 0.85f,
                    center = Offset(cx, headCy),
                    style = Stroke(width = 1.dp.toPx())
                )
            }

            // Crown or number indicator
            if (token.isFinished) {
                Text(
                    text = "👑",
                    fontSize = 8.sp,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 2.dp)
                )
            } else {
                Text(
                    text = "${token.id + 1}",
                    fontSize = 7.5.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = 3.dp)
                )
            }

            // Golden glowing ring when token is movable
            if (canMoveThis) {
                Surface(
                    shape = CircleShape,
                    color = Color.Transparent,
                    border = BorderStroke(2.dp, Color(0xFFFFD700)),
                    modifier = Modifier.size(28.dp)
                ) {}
            }
        }
    }
}

/**
 * 15x15 Ludo Board Artwork matching Screenshot_20261031_174421 and Screenshot_20261101_144841
 */
@Composable
fun LudoBoardBackground(
    boardSize: androidx.compose.ui.unit.Dp,
    gameMode: GameMode,
    onHomeQuadrantClick: (LudoColor) -> Unit
) {
    Canvas(
        modifier = Modifier
            .size(boardSize)
            .clickable(onClick = { /* Quadrant click */ })
    ) {
        val w = size.width
        val h = size.height
        val cellW = w / 15f
        val cellH = h / 15f

        val redColor = Color(0xFFE53935)
        val greenColor = Color(0xFF43A047)
        val yellowColor = Color(0xFFFDD835)
        val blueColor = Color(0xFF1E88E5)
        val gridLineColor = Color(0xFFB0BEC5)

        // 1. Four 6x6 Home Quadrants
        drawRect(redColor, topLeft = Offset(0f, 0f), size = androidx.compose.ui.geometry.Size(cellW * 6, cellH * 6))
        drawRect(greenColor, topLeft = Offset(cellW * 9, 0f), size = androidx.compose.ui.geometry.Size(cellW * 6, cellH * 6))
        drawRect(yellowColor, topLeft = Offset(cellW * 9, cellH * 9), size = androidx.compose.ui.geometry.Size(cellW * 6, cellH * 6))
        drawRect(blueColor, topLeft = Offset(0f, cellH * 9), size = androidx.compose.ui.geometry.Size(cellW * 6, cellH * 6))

        // White inner squares inside 4 Home Quadrants
        val padX = cellW * 0.9f
        val padY = cellH * 0.9f
        val innerW = cellW * 4.2f
        val innerH = cellH * 4.2f

        drawRect(Color.White, topLeft = Offset(padX, padY), size = androidx.compose.ui.geometry.Size(innerW, innerH))
        drawRect(Color.White, topLeft = Offset(cellW * 9 + padX, padY), size = androidx.compose.ui.geometry.Size(innerW, innerH))
        drawRect(Color.White, topLeft = Offset(cellW * 9 + padX, cellH * 9 + padY), size = androidx.compose.ui.geometry.Size(innerW, innerH))
        drawRect(Color.White, topLeft = Offset(padX, cellH * 9 + padY), size = androidx.compose.ui.geometry.Size(innerW, innerH))

        // Draw 3D Circular Pedestal Base Stands ("এস্টান") inside each Home Quadrant
        val baseStandR = cellW * 0.62f
        val redCenters = listOf(
            Offset(cellW * 2.1f, cellH * 2.1f), Offset(cellW * 3.9f, cellH * 2.1f),
            Offset(cellW * 2.1f, cellH * 3.9f), Offset(cellW * 3.9f, cellH * 3.9f)
        )
        redCenters.forEach { drawLudoBaseStand(this, it, baseStandR, redColor, Color(0xFF8B0000)) }

        val greenCenters = listOf(
            Offset(cellW * 11.1f, cellH * 2.1f), Offset(cellW * 12.9f, cellH * 2.1f),
            Offset(cellW * 11.1f, cellH * 3.9f), Offset(cellW * 12.9f, cellH * 3.9f)
        )
        greenCenters.forEach { drawLudoBaseStand(this, it, baseStandR, greenColor, Color(0xFF1B5E20)) }

        val yellowCenters = listOf(
            Offset(cellW * 11.1f, cellH * 11.1f), Offset(cellW * 12.9f, cellH * 11.1f),
            Offset(cellW * 11.1f, cellH * 12.9f), Offset(cellW * 12.9f, cellH * 12.9f)
        )
        yellowCenters.forEach { drawLudoBaseStand(this, it, baseStandR, yellowColor, Color(0xFFE65100)) }

        val blueCenters = listOf(
            Offset(cellW * 2.1f, cellH * 11.1f), Offset(cellW * 3.9f, cellH * 11.1f),
            Offset(cellW * 2.1f, cellH * 12.9f), Offset(cellW * 3.9f, cellH * 12.9f)
        )
        blueCenters.forEach { drawLudoBaseStand(this, it, baseStandR, blueColor, Color(0xFF0D47A1)) }

        // 2. Center 3x3 Finish Triangle Quadrants
        val centerLeft = cellW * 6
        val centerTop = cellH * 6
        val centerRight = cellW * 9
        val centerBottom = cellH * 9
        val centerMidX = cellW * 7.5f
        val centerMidY = cellH * 7.5f

        val redTriangle = Path().apply {
            moveTo(centerLeft, centerTop)
            lineTo(centerMidX, centerMidY)
            lineTo(centerLeft, centerBottom)
            close()
        }
        drawPath(redTriangle, redColor)

        val greenTriangle = Path().apply {
            moveTo(centerLeft, centerTop)
            lineTo(centerRight, centerTop)
            lineTo(centerMidX, centerMidY)
            close()
        }
        drawPath(greenTriangle, greenColor)

        val yellowTriangle = Path().apply {
            moveTo(centerRight, centerTop)
            lineTo(centerRight, centerBottom)
            lineTo(centerMidX, centerMidY)
            close()
        }
        drawPath(yellowTriangle, yellowColor)

        val blueTriangle = Path().apply {
            moveTo(centerLeft, centerBottom)
            lineTo(centerRight, centerBottom)
            lineTo(centerMidX, centerMidY)
            close()
        }
        drawPath(blueTriangle, blueColor)

        // 3. Colored Home Run Paths
        for (i in 1..5) {
            drawRect(redColor, Offset(cellW * i, cellH * 7), androidx.compose.ui.geometry.Size(cellW, cellH))
            drawRect(greenColor, Offset(cellW * 7, cellH * i), androidx.compose.ui.geometry.Size(cellW, cellH))
            drawRect(yellowColor, Offset(cellW * (14 - i), cellH * 7), androidx.compose.ui.geometry.Size(cellW, cellH))
            drawRect(blueColor, Offset(cellW * 7, cellH * (14 - i)), androidx.compose.ui.geometry.Size(cellW, cellH))
        }

        // Starting colored track cells
        drawRect(redColor, Offset(cellW * 1, cellH * 6), androidx.compose.ui.geometry.Size(cellW, cellH))
        drawRect(greenColor, Offset(cellW * 8, cellH * 1), androidx.compose.ui.geometry.Size(cellW, cellH))
        drawRect(yellowColor, Offset(cellW * 13, cellH * 8), androidx.compose.ui.geometry.Size(cellW, cellH))
        drawRect(blueColor, Offset(cellW * 6, cellH * 13), androidx.compose.ui.geometry.Size(cellW, cellH))

        // Starting arrows
        val arrowSize = cellW * 0.72f
        drawTrackArrow(this, Offset(cellW * 0.5f, cellH * 6.5f), arrowSize, redColor, 0)
        drawTrackArrow(this, Offset(cellW * 7.5f, cellH * 0.5f), arrowSize, greenColor, 1)
        drawTrackArrow(this, Offset(cellW * 14.5f, cellH * 8.5f), arrowSize, yellowColor, 2)
        drawTrackArrow(this, Offset(cellW * 7.5f, cellH * 14.5f), arrowSize, blueColor, 3)

        // 4. Five-Pointed Star Safe Stands ("★ তারা এস্টান")
        val starRadius = cellW * 0.38f
        val starCellPositions = listOf(
            Pair(6, 2), Pair(8, 3), Pair(12, 6), Pair(11, 8),
            Pair(8, 12), Pair(6, 11), Pair(2, 8), Pair(3, 6)
        )
        starCellPositions.forEach { (col, row) ->
            val center = Offset(cellW * (col + 0.5f), cellH * (row + 0.5f))
            drawFivePointStar(this, center, starRadius, strokeColor = Color(0xFF475569), fillColor = Color.White)
        }

        // 5. Grid Cell Border Lines
        for (i in 0..15) {
            val x = i * cellW
            val y = i * cellH
            drawLine(gridLineColor, Offset(x, 0f), Offset(x, h), strokeWidth = 1f)
            drawLine(gridLineColor, Offset(0f, y), Offset(w, y), strokeWidth = 1f)
        }

        val thickDivider = 2.5f
        drawLine(Color(0xFF1E293B), Offset(cellW * 6, 0f), Offset(cellW * 6, h), strokeWidth = thickDivider)
        drawLine(Color(0xFF1E293B), Offset(cellW * 9, 0f), Offset(cellW * 9, h), strokeWidth = thickDivider)
        drawLine(Color(0xFF1E293B), Offset(0f, cellH * 6), Offset(w, cellH * 6), strokeWidth = thickDivider)
        drawLine(Color(0xFF1E293B), Offset(0f, cellH * 9), Offset(w, cellH * 9), strokeWidth = thickDivider)
    }

    // Quadrant Player Labels
    Box(modifier = Modifier.size(boardSize)) {
        val quarterW = boardSize / 15f * 6

        // Top-Left: Computer 2 (Red)
        Box(
            modifier = Modifier
                .size(quarterW, 20.dp)
                .align(Alignment.TopStart),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (gameMode == GameMode.VS_COMPUTER) "Computer 2" else "Player 2 (Red)",
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Top-Right: Computer 3 (Green)
        Box(
            modifier = Modifier
                .size(quarterW, 20.dp)
                .align(Alignment.TopEnd),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (gameMode == GameMode.VS_COMPUTER) "Computer 3" else "Player 3 (Green)",
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Bottom-Left: You (Blue)
        Box(
            modifier = Modifier
                .size(quarterW, 20.dp)
                .align(Alignment.BottomStart)
                .offset(y = (-3).dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (gameMode == GameMode.VS_COMPUTER) "You" else "Player 1 (Blue)",
                color = Color.White,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Bottom-Right: Computer 4 (Yellow)
        Box(
            modifier = Modifier
                .size(quarterW, 20.dp)
                .align(Alignment.BottomEnd)
                .offset(y = (-3).dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (gameMode == GameMode.VS_COMPUTER) "Computer 4" else "Player 4 (Yellow)",
                color = Color(0xFF1E293B),
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

/**
 * Standard 52 outer track path coordinates (15x15 board)
 */
fun generateOuterPath(): List<Pair<Int, Int>> {
    return listOf(
        Pair(1, 6), Pair(2, 6), Pair(3, 6), Pair(4, 6), Pair(5, 6),
        Pair(6, 5), Pair(6, 4), Pair(6, 3), Pair(6, 2), Pair(6, 1), Pair(6, 0),
        Pair(7, 0),
        Pair(8, 0), Pair(8, 1), Pair(8, 2), Pair(8, 3), Pair(8, 4), Pair(8, 5),
        Pair(9, 6), Pair(10, 6), Pair(11, 6), Pair(12, 6), Pair(13, 6), Pair(14, 6),
        Pair(14, 7),
        Pair(14, 8), Pair(13, 8), Pair(12, 8), Pair(11, 8), Pair(10, 8), Pair(9, 8),
        Pair(8, 9), Pair(8, 10), Pair(8, 11), Pair(8, 12), Pair(8, 13), Pair(8, 14),
        Pair(7, 14),
        Pair(6, 14), Pair(6, 13), Pair(6, 12), Pair(6, 11), Pair(6, 10), Pair(6, 9),
        Pair(5, 8), Pair(4, 8), Pair(3, 8), Pair(2, 8), Pair(1, 8), Pair(0, 8),
        Pair(0, 7),
        Pair(0, 6)
    )
}

/**
 * Coordinates for tokens in Home Base (-1)
 */
fun getHomePosition(token: Token): Pair<Float, Float> {
    val id = token.id
    val colOffset = (id % 2) * 1.8f
    val rowOffset = (id / 2) * 1.8f
    return when (token.color) {
        LudoColor.RED -> Pair(2.1f + colOffset, 2.1f + rowOffset)
        LudoColor.GREEN -> Pair(11.1f + colOffset, 2.1f + rowOffset)
        LudoColor.YELLOW -> Pair(11.1f + colOffset, 11.1f + rowOffset)
        LudoColor.BLUE -> Pair(2.1f + colOffset, 11.1f + rowOffset)
    }
}

/**
 * Coordinates for tokens in Home Stretch (52..57)
 */
fun getHomePathPosition(token: Token): Pair<Float, Float> {
    val step = (token.position - 52).coerceIn(0, 5)
    return when (token.color) {
        LudoColor.RED -> Pair((1 + step).toFloat() + 0.5f, 7.5f)
        LudoColor.GREEN -> Pair(7.5f, (1 + step).toFloat() + 0.5f)
        LudoColor.YELLOW -> Pair((13 - step).toFloat() + 0.5f, 7.5f)
        LudoColor.BLUE -> Pair(7.5f, (13 - step).toFloat() + 0.5f)
    }
}

/**
 * Check if a token is protected by Jor (2 or more tokens of same color/team on same cell)
 */
fun isJorSafe(allTokens: List<Token>, target: Token): Boolean {
    return allTokens.count { it.color == target.color && it.position == target.position } >= 2
}

/**
 * Google AdMob Banner View
 */
@Composable
fun AdMobBannerView(
    adUnitId: String = GoogleAdConfig.BANNER_ID,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            AndroidView(
                modifier = Modifier.fillMaxWidth().height(50.dp),
                factory = { ctx ->
                    AdView(ctx).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        try {
                            loadAd(AdRequest.Builder().build())
                        } catch (e: Exception) {
                            android.util.Log.e("AdMob", "Error loading banner", e)
                        }
                    }
                }
            )
        }
    }
}

/**
 * Google AdMob Interstitial Ad Helper (shows on Game Win)
 */
fun showInterstitialAd(context: Context, onDismiss: () -> Unit = {}) {
    try {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            GoogleAdConfig.INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            onDismiss()
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            onDismiss()
                        }
                    }
                    if (context is Activity) {
                        interstitialAd.show(context)
                    } else {
                        onDismiss()
                    }
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    onDismiss()
                }
            }
        )
    } catch (e: Exception) {
        android.util.Log.e("AdMob", "Interstitial load error", e)
        onDismiss()
    }
}

/**
 * Fullscreen Dialog wrapper for Ludo King used in Messenger
 */
@Composable
fun LudoKingGroupDialog(
    viewModel: FaceFeedViewModel,
    initialMode: GameMode? = null,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnBackPress = true)
    ) {
        LudoKingFullGame(
            viewModel = viewModel,
            initialMode = initialMode,
            onDismiss = onDismiss
        )
    }
}
