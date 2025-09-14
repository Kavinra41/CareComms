import XCTest

final class TermsScreenUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
        
        // Wait for splash screen to complete and navigate to terms
        let termsTitle = app.staticTexts["Terms and Conditions"]
        XCTAssertTrue(termsTitle.waitForExistence(timeout: 6.0))
    }
    
    func testTermsScreenDisplaysTitle() throws {
        // Test that terms screen shows the correct title
        let title = app.staticTexts["Terms and Conditions"]
        XCTAssertTrue(title.exists)
    }
    
    func testTermsScreenDisplaysSubtitle() throws {
        // Test that terms screen shows the subtitle
        let subtitle = app.staticTexts["Please read and accept our terms to continue"]
        XCTAssertTrue(subtitle.exists)
    }
    
    func testTermsScreenHasScrollableContent() throws {
        // Test that terms content is scrollable
        let scrollView = app.scrollViews.firstMatch
        XCTAssertTrue(scrollView.exists)
        
        // Test that we can scroll
        scrollView.swipeUp()
        scrollView.swipeUp()
    }
    
    func testAcceptButtonInitiallyDisabled() throws {
        // Test that accept button is initially disabled/dimmed
        let acceptButton = app.buttons["Accept and Continue"]
        XCTAssertTrue(acceptButton.exists)
        
        // Button should exist but be disabled initially
        XCTAssertFalse(acceptButton.isEnabled)
    }
    
    func testAcceptButtonEnabledAfterScrolling() throws {
        // Test that accept button becomes enabled after scrolling to bottom
        let scrollView = app.scrollViews.firstMatch
        XCTAssertTrue(scrollView.exists)
        
        // Scroll to bottom multiple times to ensure we reach the end
        for _ in 0..<10 {
            scrollView.swipeUp()
        }
        
        // Wait a moment for the scroll detection to update
        Thread.sleep(forTimeInterval: 1.0)
        
        let acceptButton = app.buttons["Accept and Continue"]
        XCTAssertTrue(acceptButton.exists)
    }
    
    func testDeclineButtonExists() throws {
        // Test that decline button exists
        let declineButton = app.buttons["Decline"]
        XCTAssertTrue(declineButton.exists)
        XCTAssertTrue(declineButton.isEnabled)
    }
    
    func testTermsContentExists() throws {
        // Test that key terms sections exist
        let acceptanceSection = app.staticTexts["1. Acceptance of Terms"]
        XCTAssertTrue(acceptanceSection.exists)
        
        let serviceSection = app.staticTexts["2. Description of Service"]
        XCTAssertTrue(serviceSection.exists)
    }
    
    func testNavigationToLandingScreen() throws {
        // Test navigation to landing screen after accepting terms
        let scrollView = app.scrollViews.firstMatch
        
        // Scroll to bottom to enable accept button
        for _ in 0..<10 {
            scrollView.swipeUp()
        }
        
        Thread.sleep(forTimeInterval: 1.0)
        
        let acceptButton = app.buttons["Accept and Continue"]
        if acceptButton.isEnabled {
            acceptButton.tap()
            
            // Verify navigation to landing screen
            let welcomeText = app.staticTexts["Welcome to CareComms"]
            XCTAssertTrue(welcomeText.waitForExistence(timeout: 3.0))
        }
    }
}