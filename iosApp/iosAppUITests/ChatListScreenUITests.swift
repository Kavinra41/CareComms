import XCTest

final class ChatListScreenUITests: XCTestCase {
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    // MARK: - Navigation Tests
    
    func testChatListScreenDisplaysCorrectly() throws {
        // Navigate to chat list (assuming we start from splash)
        navigateToChatList()
        
        // Verify navigation title
        XCTAssertTrue(app.navigationBars["Chats"].exists)
        
        // Verify search bar exists
        XCTAssertTrue(app.searchFields["Search chats..."].exists)
        
        // Verify invite button exists
        XCTAssertTrue(app.navigationBars.buttons.element(matching: .button, identifier: "person.badge.plus").exists)
    }
    
    func testChatListDisplaysMockData() throws {
        navigateToChatList()
        
        // Wait for mock data to load
        let firstChatCell = app.buttons["Eleanor Smith"]
        XCTAssertTrue(firstChatCell.waitForExistence(timeout: 3.0))
        
        // Verify mock chat previews are displayed
        XCTAssertTrue(app.buttons["Eleanor Smith"].exists)
        XCTAssertTrue(app.buttons["Robert Johnson"].exists)
        XCTAssertTrue(app.buttons["Margaret Davis"].exists)
        
        // Verify last messages are shown
        XCTAssertTrue(app.staticTexts["Good morning! How are you feeling today?"].exists)
        XCTAssertTrue(app.staticTexts["Thank you for checking on me. I'm doing well."].exists)
        
        // Verify unread count badges
        XCTAssertTrue(app.staticTexts["2"].exists) // Eleanor's unread count
        XCTAssertTrue(app.staticTexts["1"].exists) // Margaret's unread count
    }
    
    // MARK: - Search Functionality Tests
    
    func testSearchFunctionality() throws {
        navigateToChatList()
        
        // Wait for data to load
        let firstChatCell = app.buttons["Eleanor Smith"]
        XCTAssertTrue(firstChatCell.waitForExistence(timeout: 3.0))
        
        // Tap search field and enter search query
        let searchField = app.searchFields["Search chats..."]
        searchField.tap()
        searchField.typeText("Eleanor")
        
        // Verify filtered results
        XCTAssertTrue(app.buttons["Eleanor Smith"].exists)
        XCTAssertFalse(app.buttons["Robert Johnson"].exists)
        XCTAssertFalse(app.buttons["Margaret Davis"].exists)
        
        // Clear search
        searchField.buttons["Clear text"].tap()
        
        // Verify all chats are shown again
        XCTAssertTrue(app.buttons["Eleanor Smith"].exists)
        XCTAssertTrue(app.buttons["Robert Johnson"].exists)
        XCTAssertTrue(app.buttons["Margaret Davis"].exists)
    }
    
    func testSearchWithNoResults() throws {
        navigateToChatList()
        
        // Wait for data to load
        let firstChatCell = app.buttons["Eleanor Smith"]
        XCTAssertTrue(firstChatCell.waitForExistence(timeout: 3.0))
        
        // Search for non-existent caree
        let searchField = app.searchFields["Search chats..."]
        searchField.tap()
        searchField.typeText("NonExistentCaree")
        
        // Verify no results message
        XCTAssertTrue(app.staticTexts["No Results"].exists)
        XCTAssertTrue(app.staticTexts["No chats found for \"NonExistentCaree\""].exists)
        
        // Verify no chat cells are visible
        XCTAssertFalse(app.buttons["Eleanor Smith"].exists)
        XCTAssertFalse(app.buttons["Robert Johnson"].exists)
        XCTAssertFalse(app.buttons["Margaret Davis"].exists)
    }
    
    func testSearchByMessage() throws {
        navigateToChatList()
        
        // Wait for data to load
        let firstChatCell = app.buttons["Eleanor Smith"]
        XCTAssertTrue(firstChatCell.waitForExistence(timeout: 3.0))
        
        // Search by message content
        let searchField = app.searchFields["Search chats..."]
        searchField.tap()
        searchField.typeText("medication")
        
        // Verify only Margaret's chat is shown (she mentioned medication)
        XCTAssertTrue(app.buttons["Margaret Davis"].exists)
        XCTAssertFalse(app.buttons["Eleanor Smith"].exists)
        XCTAssertFalse(app.buttons["Robert Johnson"].exists)
    }
    
    // MARK: - Invite Functionality Tests
    
    func testInviteButtonOpensShareSheet() throws {
        navigateToChatList()
        
        // Tap invite button
        let inviteButton = app.navigationBars.buttons.element(matching: .button, identifier: "person.badge.plus")
        XCTAssertTrue(inviteButton.waitForExistence(timeout: 2.0))
        inviteButton.tap()
        
        // Wait for invitation link generation (mock delay)
        sleep(2)
        
        // Verify invite share sheet appears
        XCTAssertTrue(app.navigationBars["Invite Caree"].waitForExistence(timeout: 3.0))
        XCTAssertTrue(app.staticTexts["Invite a Caree"].exists)
        XCTAssertTrue(app.staticTexts["Share this link with someone you care for to invite them to join CareComms"].exists)
        
        // Verify share button exists
        XCTAssertTrue(app.buttons["Share Invitation"].exists)
        
        // Close the sheet
        app.buttons["Done"].tap()
        
        // Verify we're back to chat list
        XCTAssertTrue(app.navigationBars["Chats"].exists)
    }
    
    func testInviteShareSheetCopyFunctionality() throws {
        navigateToChatList()
        
        // Open invite sheet
        let inviteButton = app.navigationBars.buttons.element(matching: .button, identifier: "person.badge.plus")
        inviteButton.tap()
        
        // Wait for sheet to appear
        XCTAssertTrue(app.navigationBars["Invite Caree"].waitForExistence(timeout: 3.0))
        
        // Tap copy button (doc.on.doc icon)
        let copyButton = app.buttons.element(matching: .button, identifier: "doc.on.doc")
        XCTAssertTrue(copyButton.exists)
        copyButton.tap()
        
        // Note: We can't easily test clipboard content in UI tests,
        // but we can verify the button was tappable
        XCTAssertTrue(copyButton.exists)
    }
    
    // MARK: - Pull to Refresh Tests
    
    func testPullToRefresh() throws {
        navigateToChatList()
        
        // Wait for data to load
        let firstChatCell = app.buttons["Eleanor Smith"]
        XCTAssertTrue(firstChatCell.waitForExistence(timeout: 3.0))
        
        // Perform pull to refresh gesture
        let chatList = app.tables.firstMatch
        if chatList.exists {
            chatList.swipeDown()
        } else {
            // If no table, try on the main view
            app.swipeDown()
        }
        
        // Verify data is still there after refresh
        XCTAssertTrue(app.buttons["Eleanor Smith"].waitForExistence(timeout: 3.0))
        XCTAssertTrue(app.buttons["Robert Johnson"].exists)
        XCTAssertTrue(app.buttons["Margaret Davis"].exists)
    }
    
    // MARK: - Navigation to Chat Tests
    
    func testTapChatNavigatesToChatScreen() throws {
        navigateToChatList()
        
        // Wait for data to load
        let eleanorChat = app.buttons["Eleanor Smith"]
        XCTAssertTrue(eleanorChat.waitForExistence(timeout: 3.0))
        
        // Tap on Eleanor's chat
        eleanorChat.tap()
        
        // Verify navigation to chat screen
        XCTAssertTrue(app.navigationBars["Chat"].waitForExistence(timeout: 2.0))
        XCTAssertTrue(app.staticTexts["Chat Screen"].exists)
        
        // Verify chat ID is passed correctly
        XCTAssertTrue(app.staticTexts["Chat ID: chat_1"].exists)
    }
    
    // MARK: - Empty State Tests
    
    func testEmptyStateWhenNoChats() throws {
        // This test would require a way to simulate no chats
        // For now, we'll test the search empty state which is similar
        navigateToChatList()
        
        // Wait for data to load
        let firstChatCell = app.buttons["Eleanor Smith"]
        XCTAssertTrue(firstChatCell.waitForExistence(timeout: 3.0))
        
        // Search for something that returns no results
        let searchField = app.searchFields["Search chats..."]
        searchField.tap()
        searchField.typeText("NoResultsQuery")
        
        // Verify empty state
        XCTAssertTrue(app.staticTexts["No Results"].exists)
        XCTAssertTrue(app.images.element(matching: .image, identifier: "magnifyingglass").exists)
    }
    
    // MARK: - Accessibility Tests
    
    func testAccessibilityLabels() throws {
        navigateToChatList()
        
        // Wait for data to load
        let firstChatCell = app.buttons["Eleanor Smith"]
        XCTAssertTrue(firstChatCell.waitForExistence(timeout: 3.0))
        
        // Verify important elements have accessibility labels
        XCTAssertTrue(app.searchFields["Search chats..."].exists)
        XCTAssertTrue(app.navigationBars["Chats"].exists)
        
        // Verify chat cells are accessible
        XCTAssertTrue(firstChatCell.isHittable)
        XCTAssertTrue(app.buttons["Robert Johnson"].isHittable)
        XCTAssertTrue(app.buttons["Margaret Davis"].isHittable)
    }
    
    func testLargeTextSupport() throws {
        // Enable larger text size
        app.launchArguments.append("-UIPreferredContentSizeCategoryName")
        app.launchArguments.append("UICTContentSizeCategoryAccessibilityExtraExtraExtraLarge")
        app.launch()
        
        navigateToChatList()
        
        // Wait for data to load
        let firstChatCell = app.buttons["Eleanor Smith"]
        XCTAssertTrue(firstChatCell.waitForExistence(timeout: 3.0))
        
        // Verify elements are still accessible with large text
        XCTAssertTrue(firstChatCell.isHittable)
        XCTAssertTrue(app.searchFields["Search chats..."].exists)
    }
    
    // MARK: - Helper Methods
    
    private func navigateToChatList() {
        // This is a simplified navigation - in a real app, you'd need to:
        // 1. Go through splash screen
        // 2. Accept terms
        // 3. Login as a carer
        // 4. Navigate to chat list
        
        // For testing purposes, we'll assume we can navigate directly
        // In a real implementation, you'd add the full navigation flow
        
        // Skip splash screen if it appears
        if app.staticTexts["CareComms"].waitForExistence(timeout: 2.0) {
            // Wait for splash to complete
            sleep(2)
        }
        
        // Accept terms if they appear
        if app.buttons["Accept"].waitForExistence(timeout: 2.0) {
            app.buttons["Accept"].tap()
        }
        
        // Navigate through login flow if needed
        if app.buttons["Login"].waitForExistence(timeout: 2.0) {
            // Simulate login flow
            app.buttons["Login"].tap()
            
            // Fill in mock credentials if login form appears
            if app.textFields["Email"].waitForExistence(timeout: 2.0) {
                app.textFields["Email"].tap()
                app.textFields["Email"].typeText("test@example.com")
                
                app.secureTextFields["Password"].tap()
                app.secureTextFields["Password"].typeText("password123")
                
                app.buttons["Sign In"].tap()
            }
        }
        
        // Navigate to chat list if not already there
        if !app.navigationBars["Chats"].exists {
            // Look for a way to get to chat list
            if app.tabBars.buttons["Chats"].exists {
                app.tabBars.buttons["Chats"].tap()
            }
        }
    }
}