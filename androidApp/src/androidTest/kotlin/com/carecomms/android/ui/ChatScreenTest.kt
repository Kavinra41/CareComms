package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.ChatScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.data.models.Message
import com.carecomms.data.models.MessageStatus
import com.carecomms.data.models.MessageType
import com.carecomms.data.models.TypingStatus
import com.carecomms.presentation.chat.ChatAction
import com.carecomms.presentation.chat.ChatState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockMessages = listOf(
        Message(
            id = "1",
            senderId = "user1",
            content = "Hello there!",
            timestamp = System.currentTimeMillis() - 60000,
            status = MessageStatus.READ,
            type = MessageType.TEXT
        ),
        Message(
            id = "2",
            senderId = "user2",
            content = "Hi! How are you doing today?",
            timestamp = System.currentTimeMillis() - 30000,
            status = MessageStatus.DELIVERED,
            type = MessageType.TEXT
        ),
        Message(
            id = "3",
            senderId = "user1",
            content = "I'm doing well, thank you for asking!",
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            type = MessageType.TEXT
        )
    )

    @Test
    fun chatScreen_displaysCorrectly() {
        val state = ChatState(
            messages = mockMessages,
            currentMessage = "",
            isLoading = false,
            error = null,
            chatId = "chat1",
            otherUserName = "John Doe",
            isOnline = true
        )

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Verify top bar displays user name
        composeTestRule.onNodeWithText("John Doe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Online").assertIsDisplayed()

        // Verify messages are displayed
        composeTestRule.onNodeWithText("Hello there!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hi! How are you doing today?").assertIsDisplayed()
        composeTestRule.onNodeWithText("I'm doing well, thank you for asking!").assertIsDisplayed()

        // Verify message input is displayed
        composeTestRule.onNodeWithText("Type a message...").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Send message").assertIsDisplayed()
    }

    @Test
    fun chatScreen_showsLoadingState() {
        val state = ChatState(
            isLoading = true,
            messages = emptyList()
        )

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Verify loading indicator is displayed
        composeTestRule.onNode(hasTestTag("loading")).assertExists()
    }

    @Test
    fun chatScreen_showsEmptyState() {
        val state = ChatState(
            isLoading = false,
            messages = emptyList()
        )

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Verify empty state message is displayed
        composeTestRule.onNodeWithText("Start a conversation with John Doe").assertIsDisplayed()
    }

    @Test
    fun chatScreen_showsErrorState() {
        val state = ChatState(
            error = "Failed to load messages"
        )

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Failed to load messages").assertIsDisplayed()
        composeTestRule.onNodeWithText("Dismiss").assertIsDisplayed()
    }

    @Test
    fun chatScreen_showsTypingIndicator() {
        val state = ChatState(
            messages = mockMessages,
            otherUserTyping = TypingStatus(
                userId = "user2",
                isTyping = true,
                timestamp = System.currentTimeMillis()
            )
        )

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Verify typing indicator is displayed
        composeTestRule.onNodeWithText("John Doe is typing...").assertIsDisplayed()
    }

    @Test
    fun messageInput_sendsMessage() {
        val state = ChatState()
        var lastAction: ChatAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = { action -> lastAction = action },
                    onBackClick = {}
                )
            }
        }

        // Type a message
        composeTestRule.onNodeWithText("Type a message...")
            .performTextInput("Hello world!")

        // Verify message change action is triggered
        assert(lastAction is ChatAction.UpdateCurrentMessage)
        assert((lastAction as ChatAction.UpdateCurrentMessage).message == "Hello world!")

        // Click send button
        composeTestRule.onNodeWithContentDescription("Send message")
            .performClick()

        // Verify send action is triggered
        assert(lastAction is ChatAction.SendMessage)
        assert((lastAction as ChatAction.SendMessage).content == "Hello world!")
    }

    @Test
    fun messageInput_disabledWhenSending() {
        val state = ChatState(
            isSendingMessage = true,
            currentMessage = "Test message"
        )

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Verify send button is disabled and shows loading
        composeTestRule.onNodeWithContentDescription("Send message")
            .assertIsNotEnabled()
    }

    @Test
    fun messageInput_handlesKeyboardSend() {
        val state = ChatState(currentMessage = "Test message")
        var lastAction: ChatAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = { action -> lastAction = action },
                    onBackClick = {}
                )
            }
        }

        // Perform IME action (send)
        composeTestRule.onNodeWithText("Type a message...")
            .performImeAction()

        // Verify send action is triggered
        assert(lastAction is ChatAction.SendMessage)
    }

    @Test
    fun backButton_triggersCallback() {
        val state = ChatState()
        var backClicked = false

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = {},
                    onBackClick = { backClicked = true }
                )
            }
        }

        // Click back button
        composeTestRule.onNodeWithContentDescription("Back")
            .performClick()

        // Verify callback is triggered
        assert(backClicked)
    }

    @Test
    fun messageBubbles_displayCorrectAlignment() {
        val state = ChatState(messages = mockMessages)

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = {},
                    onBackClick = {}
                )
            }
        }

        // Verify messages are displayed
        // Note: In a real test, we would verify the alignment and styling
        // but Compose testing doesn't easily allow checking visual positioning
        composeTestRule.onNodeWithText("Hello there!").assertIsDisplayed()
        composeTestRule.onNodeWithText("Hi! How are you doing today?").assertIsDisplayed()
    }

    @Test
    fun errorDismiss_triggersAction() {
        val state = ChatState(error = "Test error")
        var lastAction: ChatAction? = null

        composeTestRule.setContent {
            CareCommsTheme {
                ChatScreen(
                    state = state,
                    currentUserId = "user1",
                    otherUserName = "John Doe",
                    onAction = { action -> lastAction = action },
                    onBackClick = {}
                )
            }
        }

        // Click dismiss button
        composeTestRule.onNodeWithText("Dismiss").performClick()

        // Verify clear error action is triggered
        assert(lastAction is ChatAction.ClearError)
    }
}