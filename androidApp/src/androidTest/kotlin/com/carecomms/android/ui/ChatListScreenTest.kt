package com.carecomms.android.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.carecomms.android.ui.screens.ChatListScreen
import com.carecomms.android.ui.theme.CareCommsTheme
import com.carecomms.data.models.ChatPreview
import com.carecomms.presentation.chat.ChatListAction
import com.carecomms.presentation.chat.ChatListState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockChatPreviews = listOf(
        ChatPreview(
            chatId = "chat1",
            careeName = "John Doe",
            lastMessage = "Hello, how are you today?",
            lastMessageTime = System.currentTimeMillis() - 3600000, // 1 hour ago
            unreadCount = 2,
            isOnline = true
        ),
        ChatPreview(
            chatId = "chat2",
            careeName = "Jane Smith",
            lastMessage = "Thank you for checking in",
            lastMessageTime = System.currentTimeMillis() - 7200000, // 2 hours ago
            unreadCount = 0,
            isOnline = false
        ),
        ChatPreview(
            chatId = "chat3",
            careeName = "Bob Johnson",
            lastMessage = "I'm feeling much better today",
            lastMessageTime = System.currentTimeMillis() - 86400000, // 1 day ago
            unreadCount = 1,
            isOnline = true
        )
    )

    @Test
    fun chatListScreen_displaysTitle() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Chats")
            .assertIsDisplayed()
    }

    @Test
    fun chatListScreen_displaysInviteButton() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Invite Caree")
            .assertIsDisplayed()
            .assertHasClickAction()
    }

    @Test
    fun chatListScreen_displaysSearchBar() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        composeTestRule
            .onNodeWithText("Search chats...")
            .assertIsDisplayed()
    }

    @Test
    fun chatListScreen_displaysChatPreviews() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(
                        chatPreviews = mockChatPreviews,
                        filteredChats = mockChatPreviews
                    ),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        // Check that all chat previews are displayed
        composeTestRule
            .onNodeWithText("John Doe")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Jane Smith")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Bob Johnson")
            .assertIsDisplayed()

        // Check that messages are displayed
        composeTestRule
            .onNodeWithText("Hello, how are you today?")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Thank you for checking in")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("I'm feeling much better today")
            .assertIsDisplayed()
    }

    @Test
    fun chatListScreen_displaysUnreadBadges() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(
                        chatPreviews = mockChatPreviews,
                        filteredChats = mockChatPreviews
                    ),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        // Check unread count badges
        composeTestRule
            .onNodeWithText("2")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("1")
            .assertIsDisplayed()
    }

    @Test
    fun chatListScreen_displaysOnlineStatus() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(
                        chatPreviews = mockChatPreviews,
                        filteredChats = mockChatPreviews
                    ),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        // Check online/offline status
        composeTestRule
            .onAllNodesWithText("Online")
            .assertCountEquals(2) // John and Bob are online
        
        composeTestRule
            .onNodeWithText("Offline")
            .assertIsDisplayed() // Jane is offline
    }

    @Test
    fun chatListScreen_handlesSearchInput() {
        var lastAction: ChatListAction? = null
        
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(
                        chatPreviews = mockChatPreviews,
                        filteredChats = mockChatPreviews
                    ),
                    onAction = { action -> lastAction = action },
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        // Type in search field
        composeTestRule
            .onNodeWithText("Search chats...")
            .performTextInput("John")

        // Verify search action was called
        assert(lastAction is ChatListAction.SearchChats)
        assert((lastAction as ChatListAction.SearchChats).query == "John")
    }

    @Test
    fun chatListScreen_handlesSearchClear() {
        var lastAction: ChatListAction? = null
        
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(
                        searchQuery = "John",
                        chatPreviews = mockChatPreviews,
                        filteredChats = mockChatPreviews.filter { it.careeName.contains("John") }
                    ),
                    onAction = { action -> lastAction = action },
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        // Click clear button
        composeTestRule
            .onNodeWithContentDescription("Clear search")
            .performClick()

        // Verify search was cleared
        assert(lastAction is ChatListAction.SearchChats)
        assert((lastAction as ChatListAction.SearchChats).query == "")
    }

    @Test
    fun chatListScreen_handlesChatClick() {
        var clickedChatId: String? = null
        
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(
                        chatPreviews = mockChatPreviews,
                        filteredChats = mockChatPreviews
                    ),
                    onAction = {},
                    onChatClick = { chatId -> clickedChatId = chatId },
                    onInviteClick = {}
                )
            }
        }

        // Click on first chat
        composeTestRule
            .onNodeWithText("John Doe")
            .performClick()

        // Verify correct chat was clicked
        assert(clickedChatId == "chat1")
    }

    @Test
    fun chatListScreen_handlesInviteClick() {
        var inviteClicked = false
        
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = { inviteClicked = true }
                )
            }
        }

        // Click invite button
        composeTestRule
            .onNodeWithContentDescription("Invite Caree")
            .performClick()

        // Verify invite was clicked
        assert(inviteClicked)
    }

    @Test
    fun chatListScreen_displaysEmptyState() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(
                        chatPreviews = emptyList(),
                        filteredChats = emptyList()
                    ),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        // Check empty state elements
        composeTestRule
            .onNodeWithText("No chats yet")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Invite carees to start chatting")
            .assertIsDisplayed()
        
        composeTestRule
            .onNodeWithText("Invite Caree")
            .assertIsDisplayed()
    }

    @Test
    fun chatListScreen_displaysErrorBanner() {
        var errorDismissed = false
        
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(
                        error = "Failed to load chats"
                    ),
                    onAction = { action ->
                        if (action is ChatListAction.ClearError) {
                            errorDismissed = true
                        }
                    },
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        // Check error banner is displayed
        composeTestRule
            .onNodeWithText("Failed to load chats")
            .assertIsDisplayed()

        // Click dismiss button
        composeTestRule
            .onNodeWithContentDescription("Dismiss")
            .performClick()

        // Verify error was dismissed
        assert(errorDismissed)
    }

    @Test
    fun chatListScreen_displaysLoadingState() {
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(isLoading = true),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        // The loading state is handled by SwipeRefresh, so we just verify
        // the screen renders without crashing when loading
        composeTestRule
            .onNodeWithText("Chats")
            .assertIsDisplayed()
    }

    @Test
    fun chatListScreen_filtersChatsCorrectly() {
        val filteredChats = mockChatPreviews.filter { it.careeName.contains("John") }
        
        composeTestRule.setContent {
            CareCommsTheme {
                ChatListScreen(
                    state = ChatListState(
                        searchQuery = "John",
                        chatPreviews = mockChatPreviews,
                        filteredChats = filteredChats
                    ),
                    onAction = {},
                    onChatClick = {},
                    onInviteClick = {}
                )
            }
        }

        // Only John Doe should be visible
        composeTestRule
            .onNodeWithText("John Doe")
            .assertIsDisplayed()
        
        // Jane Smith and Bob Johnson should not be visible
        composeTestRule
            .onNodeWithText("Jane Smith")
            .assertDoesNotExist()
        
        composeTestRule
            .onNodeWithText("Bob Johnson")
            .assertIsDisplayed() // Bob Johnson contains "John"
    }
}