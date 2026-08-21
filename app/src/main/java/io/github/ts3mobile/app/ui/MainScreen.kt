package io.github.ts3mobile.app.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeOff
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Dns
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Headphones
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.ts3mobile.app.ConnectionFormState
import io.github.ts3mobile.app.service.MicrophoneMode
import io.github.ts3mobile.app.service.audioControlKey
import io.github.ts3mobile.app.service.TeamSpeakServiceState
import io.github.ts3mobile.app.storage.Bookmark
import io.github.ts3mobile.audio.opus.AudioRoutingState
import io.github.ts3mobile.protocol.ChannelTree
import io.github.ts3mobile.protocol.ConnectionPhase
import io.github.ts3mobile.protocol.Ts3Participant
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    form: ConnectionFormState,
    bookmarks: List<Bookmark>,
    serviceState: TeamSpeakServiceState,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onNicknameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDefaultChannelChanged: (String) -> Unit,
    onSaveBookmark: () -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onApplyBookmark: (Bookmark) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
    onPlaybackMutedChange: (Boolean) -> Unit,
    onAudioRouteSelected: (Int) -> Unit,
    onMicrophoneModeChanged: (MicrophoneMode) -> Unit,
    onPushToTalkChanged: (Boolean) -> Unit,
    onJoinChannel: (Int, String) -> Unit,
    onUpdateNickname: (String) -> Unit,
    onSetInputMuted: (Boolean) -> Unit,
    onSetOutputMuted: (Boolean) -> Unit,
    onSetAway: (String?) -> Unit,
    onSendChat: (String) -> Unit,
    onSetMasterVolume: (Float) -> Unit,
    onChatOpened: () -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ColdTs Client",
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                        )
                        Text(
                            text = "Ice-cold TeamSpeak",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                actions = {
                    if (serviceState.status.phase == ConnectionPhase.CONNECTED &&
                        serviceState.unreadChat > 0
                    ) {
                        BadgedBox(badge = { Badge { Text(serviceState.unreadChat.toString()) } }) {
                            Icon(Icons.Outlined.Edit, null)
                        }
                        Spacer(Modifier.width(8.dp))
                    }
                    StatusIndicator(serviceState.status.phase)
                    Spacer(Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background,
                        ),
                    ),
                )
                .padding(padding),
        ) {
            listOfNotNull(
                serviceState.status.detail,
                serviceState.microphoneError,
                serviceState.channelError,
                serviceState.audioRouting.error,
            ).forEach { detail ->
                StatusMessage(
                    phase = serviceState.status.phase,
                    detail = detail,
                    isError = detail === serviceState.microphoneError ||
                        detail === serviceState.channelError ||
                        detail === serviceState.audioRouting.error,
                )
            }

            if (serviceState.status.phase == ConnectionPhase.CONNECTED) {
                ConnectedContent(
                    state = serviceState,
                    onDisconnect = onDisconnect,
                    onPlaybackMutedChange = onPlaybackMutedChange,
                    onAudioRouteSelected = onAudioRouteSelected,
                    onMicrophoneModeChanged = onMicrophoneModeChanged,
                    onPushToTalkChanged = onPushToTalkChanged,
                    onJoinChannel = onJoinChannel,
                    onUpdateNickname = onUpdateNickname,
                    onSetInputMuted = onSetInputMuted,
                    onSetOutputMuted = onSetOutputMuted,
                    onSetAway = onSetAway,
                    onSendChat = onSendChat,
                    onSetMasterVolume = onSetMasterVolume,
                    onChatOpened = onChatOpened,
                )
            } else {
                ConnectionForm(
                    form = form,
                    bookmarks = bookmarks,
                    phase = serviceState.status.phase,
                    onHostChanged = onHostChanged,
                    onPortChanged = onPortChanged,
                    onNicknameChanged = onNicknameChanged,
                    onPasswordChanged = onPasswordChanged,
                    onDefaultChannelChanged = onDefaultChannelChanged,
                    onSaveBookmark = onSaveBookmark,
                    onDeleteBookmark = onDeleteBookmark,
                    onApplyBookmark = onApplyBookmark,
                    onConnect = onConnect,
                    onDisconnect = onDisconnect,
                )
            }
        }
    }
}

@Composable
private fun ConnectionForm(
    form: ConnectionFormState,
    bookmarks: List<Bookmark>,
    phase: ConnectionPhase,
    onHostChanged: (String) -> Unit,
    onPortChanged: (String) -> Unit,
    onNicknameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onDefaultChannelChanged: (String) -> Unit,
    onSaveBookmark: () -> Unit,
    onDeleteBookmark: (String) -> Unit,
    onApplyBookmark: (Bookmark) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val isConnecting = phase == ConnectionPhase.CONNECTING ||
        phase == ConnectionPhase.RECONNECTING ||
        phase == ConnectionPhase.DISCONNECTING
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var showBookmarks by rememberSaveable { mutableStateOf(false) }

    val invalidHost = form.submitted && form.host.isBlank()
    val invalidPort = form.submitted && (form.port.toIntOrNull() !in 1..65535)
    val invalidNickname = form.submitted && form.nickname.trim().length !in 2..30

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Connect to a server",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = "Enter a domain, IP, or host:port. SRV records are resolved automatically.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = form.host,
                onValueChange = onHostChanged,
                modifier = Modifier.weight(1f),
                enabled = !isConnecting,
                singleLine = true,
                label = { Text("Server address") },
                placeholder = { Text("voice.example.com") },
                leadingIcon = { Icon(Icons.Outlined.Dns, null) },
                isError = invalidHost,
                supportingText = if (invalidHost) {
                    { Text("Enter the server address") }
                } else null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next,
                ),
            )
            OutlinedTextField(
                value = form.port,
                onValueChange = onPortChanged,
                modifier = Modifier.width(104.dp),
                enabled = !isConnecting,
                singleLine = true,
                label = { Text("Port") },
                isError = invalidPort,
                supportingText = if (invalidPort) {
                    { Text("1–65535") }
                } else {
                    { Text("9987") }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                ),
            )
        }

        OutlinedTextField(
            value = form.nickname,
            onValueChange = onNicknameChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isConnecting,
            singleLine = true,
            label = { Text("Nickname") },
            leadingIcon = { Icon(Icons.Outlined.AlternateEmail, null) },
            isError = invalidNickname,
            supportingText = if (invalidNickname) {
                { Text("Nickname must be 2–30 characters") }
            } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            value = form.password,
            onValueChange = onPasswordChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isConnecting,
            singleLine = true,
            label = { Text("Server password (optional)") },
            leadingIcon = { Icon(Icons.Outlined.Lock, null) },
            trailingIcon = {
                IconButton({ passwordVisible = !passwordVisible }) {
                    Icon(
                        if (passwordVisible) Icons.Outlined.VisibilityOff
                        else Icons.Outlined.Visibility,
                        if (passwordVisible) "Hide password" else "Show password",
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        )

        OutlinedTextField(
            value = form.defaultChannel,
            onValueChange = onDefaultChannelChanged,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isConnecting,
            singleLine = true,
            label = { Text("Default channel (optional)") },
            placeholder = { Text("Lobby / Support") },
            leadingIcon = { Icon(Icons.Outlined.Tag, null) },
        )

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onSaveBookmark,
                enabled = form.host.isNotBlank() && !isConnecting,
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Outlined.BookmarkAdd, null)
                Spacer(Modifier.width(8.dp))
                Text("Save bookmark")
            }
            OutlinedButton(
                onClick = { showBookmarks = !showBookmarks },
                enabled = bookmarks.isNotEmpty(),
                modifier = Modifier.weight(1f),
            ) {
                Icon(Icons.Outlined.Bookmark, null)
                Spacer(Modifier.width(8.dp))
                Text("Bookmarks (${bookmarks.size})")
            }
        }

        if (showBookmarks) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                if (bookmarks.isEmpty()) {
                    Text(
                        "No bookmarks yet.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    bookmarks.forEach { bookmark ->
                        BookmarkRow(
                            bookmark = bookmark,
                            enabled = !isConnecting,
                            onApply = { onApplyBookmark(bookmark) },
                            onDelete = { onDeleteBookmark(bookmark.id) },
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }

        Spacer(Modifier.height(4.dp))

        if (isConnecting) {
            OutlinedButton(
                onClick = onDisconnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = phase != ConnectionPhase.DISCONNECTING,
            ) {
                if (phase != ConnectionPhase.DISCONNECTING) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Icon(Icons.Default.PowerSettingsNew, null)
                Spacer(Modifier.width(8.dp))
                Text(
                    when (phase) {
                        ConnectionPhase.RECONNECTING -> "Cancel reconnect"
                        ConnectionPhase.DISCONNECTING -> "Disconnecting"
                        else -> "Cancel connection"
                    },
                )
            }
        } else {
            Button(
                onClick = onConnect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
            ) {
                Icon(Icons.Default.Link, null)
                Spacer(Modifier.width(10.dp))
                Text(
                    if (phase == ConnectionPhase.ERROR) "Reconnect" else "Connect",
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        if (bookmarks.isNotEmpty()) {
            Text(
                "Quick connect",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            bookmarks.take(6).forEach { bookmark ->
                BookmarkRow(
                    bookmark = bookmark,
                    enabled = !isConnecting,
                    onApply = {
                        onApplyBookmark(bookmark)
                        onConnect()
                    },
                    onDelete = { onDeleteBookmark(bookmark.id) },
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        Text(
            "Powered by ColdGame · coldgame.ir",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
    }
}

@Composable
private fun BookmarkRow(
    bookmark: Bookmark,
    enabled: Boolean,
    onApply: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(bookmark.label, fontWeight = FontWeight.SemiBold)
            Text(
                bookmark.displayHost +
                    (bookmark.nickname.takeIf { it.isNotBlank() }?.let { " · $it" } ?: ""),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onApply, enabled = enabled) { Text("Use") }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, "Delete bookmark", tint = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ConnectedContent(
    state: TeamSpeakServiceState,
    onDisconnect: () -> Unit,
    onPlaybackMutedChange: (Boolean) -> Unit,
    onAudioRouteSelected: (Int) -> Unit,
    onMicrophoneModeChanged: (MicrophoneMode) -> Unit,
    onPushToTalkChanged: (Boolean) -> Unit,
    onJoinChannel: (Int, String) -> Unit,
    onUpdateNickname: (String) -> Unit,
    onSetInputMuted: (Boolean) -> Unit,
    onSetOutputMuted: (Boolean) -> Unit,
    onSetAway: (String?) -> Unit,
    onSendChat: (String) -> Unit,
    onSetMasterVolume: (Float) -> Unit,
    onChatOpened: () -> Unit = {},
) {
    var nicknameEditorOpen by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            tonalElevation = 2.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = state.serverLabel.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${state.snapshot.channels.size} channels · " +
                            "${state.snapshot.participants.size} online · " +
                            state.audioRouting.selectedRoute.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                AudioRouteMenu(state.audioRouting, onAudioRouteSelected)
                IconButton({ nicknameEditorOpen = true }) {
                    Icon(Icons.Outlined.Edit, "Profile and away")
                }
                IconButton({ onSetInputMuted(!state.inputMuted) }) {
                    Icon(
                        if (state.inputMuted) Icons.Outlined.MicOff else Icons.Filled.Mic,
                        if (state.inputMuted) "Unmute microphone" else "Mute microphone",
                        tint = if (state.inputMuted) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface,
                    )
                }
                IconButton({
                    onSetOutputMuted(!state.outputMuted)
                    onPlaybackMutedChange(!state.outputMuted)
                }) {
                    Icon(
                        if (state.outputMuted) Icons.AutoMirrored.Outlined.VolumeOff
                        else Icons.AutoMirrored.Outlined.VolumeUp,
                        if (state.outputMuted) "Unmute" else "Mute",
                    )
                }
                IconButton(onDisconnect) {
                    Icon(Icons.Default.PowerSettingsNew, "Disconnect")
                }
            }
        }

        ChannelList(state, onJoinChannel, Modifier.weight(1f))

        ChatPanel(
            messages = state.chatMessages,
            unread = state.unreadChat,
            onSend = onSendChat,
            onOpened = onChatOpened,
        )
        MicrophoneControl(
            mode = state.microphoneMode,
            isTransmitting = state.isTransmitting,
            onMicrophoneModeChanged = onMicrophoneModeChanged,
            onPushToTalkChanged = onPushToTalkChanged,
        )
    }

    if (nicknameEditorOpen) {
        NicknameEditor(
            initial = state.snapshot.participants
                .firstOrNull { it.id == state.snapshot.ownClientId }
                ?.nickname
                ?: "",
            onDismiss = { nicknameEditorOpen = false },
            initialAway = state.away,
            onConfirm = { nick, away, awayMessage ->
                if (nick.isNotBlank()) onUpdateNickname(nick)
                onSetAway(if (away) awayMessage.ifBlank { "Away" } else null)
                nicknameEditorOpen = false
            },
        )
    }
}

@Composable
private fun NicknameEditor(
    initial: String,
    initialAway: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (nick: String, away: Boolean, awayMessage: String) -> Unit,
) {
    var value by rememberSaveable(initial) { mutableStateOf(initial) }
    var away by rememberSaveable { mutableStateOf(initialAway) }
    var awayMessage by rememberSaveable { mutableStateOf("Away") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    singleLine = true,
                    label = { Text("Nickname") },
                    leadingIcon = { Icon(Icons.Outlined.Person, null) },
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.material3.Checkbox(checked = away, onCheckedChange = { away = it })
                    Text("Set yourself as away")
                }
                if (away) {
                    OutlinedTextField(
                        value = awayMessage,
                        onValueChange = { awayMessage = it },
                        singleLine = true,
                        label = { Text("Away message") },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(value.trim(), away, awayMessage.trim()) },
                enabled = value.trim().length in 2..30,
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChatPanel(
    messages: List<io.github.ts3mobile.protocol.ChatMessage>,
    unread: Int,
    onSend: (String) -> Unit,
    onOpened: () -> Unit,
) {
    var open by rememberSaveable { mutableStateOf(false) }
    var text by rememberSaveable { mutableStateOf("") }
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()

    LaunchedEffect(messages.size, open) {
        if (open) {
            onOpened()
            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(tonalElevation = 2.dp) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .combinedClickable(
                        onClick = { open = !open },
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (open) "Hide chat" else "Channel chat",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f),
                )
                if (unread > 0 && !open) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.error,
                    ) { Text(unread.toString()) }
                    Spacer(Modifier.width(8.dp))
                }
                Icon(
                    if (open) Icons.Outlined.KeyboardArrowDown else Icons.Outlined.KeyboardArrowRight,
                    if (open) "Hide" else "Show",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (open) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                ) {
                    if (messages.isEmpty()) {
                        Text(
                            "No messages yet.",
                            Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            items(messages) { message ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (message.isOwn)
                                        MaterialTheme.colorScheme.primaryContainer
                                    else
                                        MaterialTheme.colorScheme.surface,
                                    tonalElevation = 1.dp,
                                ) {
                                    Column(Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                                        Text(
                                            message.author,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (message.isOwn)
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.primary,
                                        )
                                        Text(
                                            message.text,
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Row(
                    Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Message this channel") },
                        shape = RoundedCornerShape(20.dp),
                    )
                    Button(
                        onClick = {
                            if (text.isNotBlank()) {
                                onSend(text.trim())
                                text = ""
                            }
                        },
                        enabled = text.isNotBlank(),
                        shape = CircleShape,
                    ) {
                        Text("Send")
                    }
                }
            }
        }
    }
}

@Composable
private fun AudioRouteMenu(routing: AudioRoutingState, onRouteSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton({ expanded = true }) {
            Icon(
                Icons.Outlined.Headphones,
                "Select audio device, currently ${routing.selectedRoute.label}",
            )
        }
        DropdownMenu(expanded, { expanded = false }) {
            routing.routes.forEach { route ->
                val selected = route.id == routing.selectedRouteId
                DropdownMenuItem(
                    text = {
                        Text(route.label, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    },
                    onClick = {
                        expanded = false
                        onRouteSelected(route.id)
                    },
                    leadingIcon = {
                        if (selected) Icon(Icons.Outlined.CheckCircle, null)
                        else Spacer(Modifier.size(24.dp))
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MicrophoneControl(
    mode: MicrophoneMode,
    isTransmitting: Boolean,
    onMicrophoneModeChanged: (MicrophoneMode) -> Unit,
    onPushToTalkChanged: (Boolean) -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                MicrophoneMode.entries.forEachIndexed { index, option ->
                    SegmentedButton(
                        selected = mode == option,
                        onClick = { onMicrophoneModeChanged(option) },
                        shape = SegmentedButtonDefaults.itemShape(
                            index,
                            MicrophoneMode.entries.size,
                        ),
                    ) {
                        Text(
                            when (option) {
                                MicrophoneMode.OFF -> "Off"
                                MicrophoneMode.PUSH_TO_TALK -> "Push"
                                MicrophoneMode.CONTINUOUS -> "Always on"
                            },
                        )
                    }
                }
            }
            when (mode) {
                MicrophoneMode.PUSH_TO_TALK -> PushToTalkButton(
                    isTransmitting,
                    onPushToTalkChanged,
                )
                MicrophoneMode.OFF,
                MicrophoneMode.CONTINUOUS,
                -> Row(
                    Modifier.height(58.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Icon(
                        imageVector = if (mode == MicrophoneMode.OFF) Icons.Outlined.MicOff
                        else Icons.Filled.Mic,
                        contentDescription = null,
                        tint = if (isTransmitting) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        when {
                            mode == MicrophoneMode.OFF -> "Microphone is off"
                            isTransmitting -> "Microphone is live"
                            else -> "Starting microphone"
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun PushToTalkButton(
    isTransmitting: Boolean,
    onPushToTalkChanged: (Boolean) -> Unit,
) {
    var pressed by remember { mutableStateOf(false) }
    val current by rememberUpdatedState(onPushToTalkChanged)
    val active = pressed || isTransmitting
    val color by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.error
        else MaterialTheme.colorScheme.primary,
        label = "ptt",
    )

    Surface(
        modifier = Modifier
            .size(72.dp)
            .semantics {
                role = Role.Button
                contentDescription = if (active) "Speaking" else "Hold to talk"
                onClick {
                    onPushToTalkChanged(!isTransmitting)
                    true
                }
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    pressed = true
                    current(true)
                    try { waitForUpOrCancellation() } finally {
                        pressed = false
                        current(false)
                    }
                }
            },
        shape = CircleShape,
        color = color,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shadowElevation = 6.dp,
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(Icons.Filled.Mic, null, Modifier.size(32.dp))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ChannelList(state: TeamSpeakServiceState, onJoinChannel: (Int, String) -> Unit, modifier: Modifier = Modifier) {
    var passwordChannel by remember { mutableStateOf<io.github.ts3mobile.protocol.Ts3Channel?>(null) }
    var channelPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var expandedIds by rememberSaveable { mutableStateOf(intArrayOf()) }
    val current = state.snapshot.currentChannelId
    val rows = remember(state.snapshot.channels) { ChannelTree.flatten(state.snapshot.channels) }
    val byChannel = remember(state.snapshot.participants) {
        state.snapshot.participants.groupBy { it.channelId }
    }
    // Channels are open by default so member counts and spacer channels
    // are visible immediately without tapping every row.
    LaunchedEffect(rows) {
        if (expandedIds.isEmpty()) {
            expandedIds = rows.map { it.channel.id }.toIntArray()
        }
    }

    LaunchedEffect(current) {
        if (current != null && current !in expandedIds) expandedIds += current
    }

    passwordChannel?.let { channel ->
        AlertDialog(
            onDismissRequest = {
                passwordChannel = null
                channelPassword = ""
            },
            title = { Text("Join \u201C${channel.name}\u201D") },
            text = {
                OutlinedTextField(
                    value = channelPassword,
                    onValueChange = { channelPassword = it },
                    singleLine = true,
                    label = { Text("Channel password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                    trailingIcon = {
                        IconButton({ passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Outlined.VisibilityOff
                                else Icons.Outlined.Visibility,
                                if (passwordVisible) "Hide" else "Show",
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                )
            },
            confirmButton = {
                TextButton({
                    onJoinChannel(channel.id, channelPassword)
                    passwordChannel = null
                    channelPassword = ""
                }) { Text("Join") }
            },
            dismissButton = {
                TextButton({
                    passwordChannel = null
                    channelPassword = ""
                }) { Text("Cancel") }
            },
        )
    }

    if (rows.isEmpty()) {
        EmptyState("No visible channels", Icons.Outlined.Tag)
        return
    }

    LazyColumn(modifier.fillMaxSize()) {
        items(rows, key = { it.channel.id }) { row ->
            val isCurrent = row.channel.id == current
            val isSwitching = row.channel.id == state.switchingChannelId
            val isExpanded = row.channel.id in expandedIds
            val participants = byChannel[row.channel.id].orEmpty()
            Column(Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.surface,
                        )
                        .combinedClickable(
                            onClickLabel = if (isExpanded) "Collapse" else "Expand",
                            onClick = {
                                expandedIds = if (isExpanded) {
                                    expandedIds.filterNot { it == row.channel.id }.toIntArray()
                                } else {
                                    expandedIds + row.channel.id
                                }
                            },
                            onDoubleClick = {
                                if (!isCurrent && state.switchingChannelId == null) {
                                    if (row.channel.hasPassword) {
                                        channelPassword = ""
                                        passwordChannel = row.channel
                                    } else {
                                        onJoinChannel(row.channel.id, "")
                                    }
                                }
                            },
                        )
                        .padding(
                            start = (8 + row.depth * 20).coerceAtMost(88).dp,
                            end = 16.dp,
                            top = 13.dp,
                            bottom = 13.dp,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (participants.isNotEmpty() || row.channel.clientCount > 0) {
                        Icon(
                            if (isExpanded) Icons.Outlined.KeyboardArrowDown
                            else Icons.Outlined.KeyboardArrowRight,
                            if (isExpanded) "Collapse" else "Expand",
                            Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Spacer(Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (row.channel.hasPassword) Icons.Outlined.Lock else Icons.Outlined.Tag,
                        null,
                        Modifier.size(20.dp),
                        tint = if (row.channel.isDefault || isCurrent) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(row.channel.name, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(
                        row.channel.clientCount.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (isCurrent) {
                        Spacer(Modifier.width(10.dp))
                        Icon(
                            Icons.Outlined.CheckCircle,
                            "Current channel",
                            Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    } else if (isSwitching) {
                        Spacer(Modifier.width(10.dp))
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                }
                if (isExpanded) {
                    participants.forEach { p ->
                        ChannelParticipantRow(
                            p,
                            isOwnClient = p.id == state.snapshot.ownClientId,
                            depth = row.depth,
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun ChannelParticipantRow(p: Ts3Participant, isOwnClient: Boolean, depth: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
            .padding(
                start = (48 + depth * 20).coerceAtMost(112).dp,
                end = 16.dp,
                top = 9.dp,
                bottom = 9.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ParticipantIcon(p, Modifier.size(19.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            p.nickname,
            Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isOwnClient) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (isOwnClient) {
            Text("Me", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ParticipantIcon(p: Ts3Participant, modifier: Modifier = Modifier) {
    Icon(
        imageVector = when {
            p.isTalking -> Icons.Outlined.GraphicEq
            p.isInputMuted -> Icons.Outlined.MicOff
            p.isOutputMuted -> Icons.AutoMirrored.Outlined.VolumeOff
            else -> Icons.Outlined.Person
        },
        contentDescription = when {
            p.isTalking -> "Speaking"
            p.isInputMuted -> "Microphone muted"
            p.isOutputMuted -> "Speaker muted"
            else -> null
        },
        modifier = modifier,
        tint = if (p.isTalking) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun EmptyState(label: String, icon: ImageVector) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                null,
                Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Spacer(Modifier.height(12.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StatusIndicator(phase: ConnectionPhase) {
    val (label, color) = when (phase) {
        ConnectionPhase.DISCONNECTED -> "Offline" to MaterialTheme.colorScheme.outline
        ConnectionPhase.CONNECTING -> "Connecting" to MaterialTheme.colorScheme.tertiary
        ConnectionPhase.RECONNECTING -> "Reconnecting" to MaterialTheme.colorScheme.tertiary
        ConnectionPhase.CONNECTED -> "Connected" to MaterialTheme.colorScheme.primary
        ConnectionPhase.DISCONNECTING -> "Disconnecting" to MaterialTheme.colorScheme.tertiary
        ConnectionPhase.ERROR -> "Failed" to MaterialTheme.colorScheme.error
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(Modifier.width(7.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatusMessage(phase: ConnectionPhase, detail: String, isError: Boolean = false) {
    val error = phase == ConnectionPhase.ERROR || isError
    val bg = if (error) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.surfaceVariant
    val fg = if (error) MaterialTheme.colorScheme.onErrorContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Text(
        detail,
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        color = fg,
        style = MaterialTheme.typography.bodySmall,
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
    )
}
