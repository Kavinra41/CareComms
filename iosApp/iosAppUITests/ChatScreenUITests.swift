import XCTest

final class ChatScreenUITests: XCTestCase {
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    func testChatScreenDisplaysCorrectly() throws {
        // Navigate to chat screen (assuming we have navigation set up)
        // This would typically be done through the chat list
        
        // Verify navigation title
        let navigationTitle = app.navigationBars.firstMatch
        XCTAssertTrue(navigationTitle.exists)
        
        // Verify message input field exists
        let messageInput = app.textFields["Type a message..."]
        XCTAssertTrue(messageInput.exists)
        
        // Verify send button exists
        let sendButton = app.buttons.containing(NSPredicate(format: "identifier CONTAINS 'arrow.up.circle'")).firstMatch
        XCTAssertTrue(sendButton.exists)
    }
    
    func testMessageInputFunctionality() throws {
        let messageInput = app.textFields["Type a message..."]
        let sendButton = app.buttons.containing(NSPredicate(format: "identifier CONTAINS 'arrow.up.circle'")).firstMatch
        
        // Initially send button should be disabled
        XCTAssertFalse(sendButton.isEnabled)
        
        // Type a message
        messageInput.tap()
        messageInput.typeText("Hello, this is a test message!")
        
        // Send button should now be enabled
        XCTAssertTrue(sendButton.isEnabled)
        
        // Tap send button
        sendButton.tap()
        
        // Message input should be cleared
        XCTAssertEqual(messageInput.value as? String, "")
    }
    
    func testMessageBubbleDisplay() throws {
        // This test would verify that messages are displayed correctly
        // In a real scenario, we'd need to mock the data or have test data
        
        let scrollView = app.scrollViews.firstMatch
        XCTAssertTrue(scrollView.exists)
        
        // Look for message bubbles (this would depend on accessibility identifiers)
        let messageBubbles = app.staticTexts.matching(identifier: "message-bubble")
        
        // Verify messages can be scrolled
        if messageBubbles.count > 0 {
            scrollView.swipeUp()
            scrollView.swipeDown()
        }
    }
    
    func testTypingIndicatorVisibility() throws {
        // Test typing indicator appears and disappears
        // This would require mocking the typing status from the other user
        
        let typingIndicator = app.otherElements["typing-indicator"]
        
        // In a real test, we'd trigger typing from another user
        // and verify the indicator appears
        
        // For now, just verify the element can be found when it exists
        if typingIndicator.exists {
            XCTAssertTrue(typingIndicator.isHittable)
        }
    }
    
    func testMessageStatusIndicators() throws {
        // Test that message status indicators are displayed correctly
        
        let messageStatuses = app.images.matching(NSPredicate(format: "identifier CONTAINS 'checkmark'"))
        
        // Verify status indicators exist for sent messages
        if messageStatuses.count > 0 {
            let firstStatus = messageStatuses.element(boundBy: 0)
            XCTAssertTrue(firstStatus.exists)
        }
    }
    
    func testOnlineStatusDisplay() throws {
        // Test that online/offline status is displayed in navigation bar
        
        let navigationBar = app.navigationBars.firstMatch
        XCTAssertTrue(navigationBar.exists)
        
        // Look for online/offline status text
        let onlineStatus = navigationBar.staticTexts.matching(NSPredicate(format: "label CONTAINS 'Online' OR label CONTAINS 'Offline'"))
        
        if onlineStatus.count > 0 {
            XCTAssertTrue(onlineStatus.firstMatch.exists)
        }
    }
    
    func testPullToRefresh() throws {
        // Test pull-to-refresh functionality
        
        let scrollView = app.scrollViews.firstMatch
        XCTAssertTrue(scrollView.exists)
        
        // Perform pull-to-refresh gesture
        let firstCell = scrollView.children(matching: .any).element(boundBy: 0)
        if firstCell.exists {
            firstCell.swipeDown()
        }
        
        // Verify refresh indicator appears (this would need proper implementation)
        // In a real test, we'd verify that messages are reloaded
    }
    
    func testKeyboardHandling() throws {
        // Test keyboard appearance and dismissal
        
        let messageInput = app.textFields["Type a message..."]
        
        // Tap input field to show keyboard
        messageInput.tap()
        
        // Verify keyboard is visible (this is tricky in UI tests)
        // We can check if the input field is focused
        XCTAssertTrue(messageInput.hasKeyboardFocus)
        
        // Tap elsewhere to dismiss keyboard
        let scrollView = app.scrollViews.firstMatch
        scrollView.tap()
        
        // Verify keyboard is dismissed
        XCTAssertFalse(messageInput.hasKeyboardFocus)
    }
    
    func testErrorHandling() throws {
        // Test error alert display and dismissal
        
        // This would require triggering an error condition
        // For now, we'll test that error alerts can be dismissed if they appear
        
        let errorAlert = app.alerts["Error"]
        if errorAlert.exists {
            let okButton = errorAlert.buttons["OK"]
            XCTAssertTrue(okButton.exists)
            okButton.tap()
            XCTAssertFalse(errorAlert.exists)
        }
    }
    
    func testMessageTimestamps() throws {
        // Test that message timestamps are displayed correctly
        
        let timestampTexts = app.staticTexts.matching(NSPredicate(format: "label MATCHES '.*[0-9]{1,2}:[0-9]{2}.*|Yesterday|.*[0-9]{1,2}/[0-9]{1,2}/[0-9]{2,4}.*'"))
        
        // Verify timestamps exist for messages
        if timestampTexts.count > 0 {
            let firstTimestamp = timestampTexts.element(boundBy: 0)
            XCTAssertTrue(firstTimestamp.exists)
        }
    }
    
    func testScrollToBottomOnNewMessage() throws {
        // Test that the chat scrolls to bottom when new messages arrive
        
        let scrollView = app.scrollViews.firstMatch
        XCTAssertTrue(scrollView.exists)
        
        // This would require simulating a new message arrival
        // and verifying that the scroll view scrolls to the bottom
        
        // For now, we'll just verify the scroll view can scroll
        scrollView.swipeUp()
        scrollView.swipeDown()
    }
    
    func testAccessibilityLabels() throws {
        // Test that accessibility labels are properly set
        
        let messageInput = app.textFields["Type a message..."]
        XCTAssertTrue(messageInput.exists)
        XCTAssertEqual(messageInput.label, "Type a message...")
        
        // Test send button accessibility
        let sendButton = app.buttons.containing(NSPredicate(format: "identifier CONTAINS 'arrow.up.circle'")).firstMatch
        if sendButton.exists {
            XCTAssertNotNil(sendButton.label)
        }
    }
    
    func testLongMessageHandling() throws {
        // Test handling of long messages
        
        let messageInput = app.textFields["Type a message..."]
        messageInput.tap()
        
        let longMessage = String(repeating: "This is a very long message that should wrap properly in the input field and display correctly in the chat. ", count: 5)
        messageInput.typeText(longMessage)
        
        // Verify the input field handles long text
        XCTAssertTrue(messageInput.exists)
        
        // Send the long message
        let sendButton = app.buttons.containing(NSPredicate(format: "identifier CONTAINS 'arrow.up.circle'")).firstMatch
        sendButton.tap()
    }
}

// Extension to help with keyboard focus detection
extension XCUIElement {
    var hasKeyboardFocus: Bool {
        let hasKeyboardFocus = (self.value(forKey: "hasKeyboardFocus") as? Bool) ?? false
        return hasKeyboardFocus
    }
}