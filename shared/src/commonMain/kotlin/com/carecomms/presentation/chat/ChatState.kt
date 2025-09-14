package com.carecomms.presentation.chat

import com.carecomms.data.models.ChatPreview
import com.carecomms.data.models.Message
import com.carecomms.data.models.TypingStatus

data class ChatListState(
    val isLoading: Boolean = false,
    val chatPreviews: List<ChatPreview> = emptyList(),
    val searchQuery: String = "",
    val filteredChats: List<ChatPreview> = emptyList(),
    val error: String? = null,
    val unreadCount: Int = 0
)

data class ChatState(
    val isLoading: Boolean = false,
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val isTyping: Boolean = false,
    val otherUserTyping: TypingStatus? = null,
    val error: String? = null,
    val chatId: String = "",
    val otherUserName: String = "",
    val isOnline: Boolean = false,
    val isSendingMessage: Boolean = false
)

sealed class ChatListAction {
    object LoadChats : ChatListAction()
    data class SearchChats(val query: String) : ChatListAction()
    data class SelectChat(val chatId: String) : ChatListAction()
    object RefreshChats : ChatListAction()
    object ClearError : ChatListAction()
}

sealed class ChatAction {
    data class LoadMessages(val chatId: String) : ChatAction()
    data class SendMessage(val content: String) : ChatAction()
    data class UpdateCurrentMessage(val message: String) : ChatAction()
    data class SetTypingStatus(val isTyping: Boolean) : ChatAction()
    data class MarkMessageAsRead(val messageId: String) : ChatAction()
    object MarkAllAsRead : ChatAction()
    object ClearError : ChatAction()
    object RefreshMessages : ChatAction()
}

sealed class ChatEffect {
    object MessageSent : ChatEffect()
    object MessagesMarkedAsRead : ChatEffect()
    data class ShowError(val message: String) : ChatEffect()
    data class NavigateToChat(val chatId: String) : ChatEffect()
}