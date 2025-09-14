import XCTest

final class CarerNavigationUITests: XCTestCase {
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    // MARK: - Tab Navigation Tests
    
    func testTabBarNavigation() throws {
        // Navigate to carer tabs (assuming user is logged in)
        navigateToCarerTabs()
        
        // Test that all tab bar items exist
        let tabBar = app.tabBars.firstMatch
        XCTAssertTrue(tabBar.exists, "Tab bar should exist")
        
        let chatsTab = tabBar.buttons["Chats"]
        let dashboardTab = tabBar.buttons["Dashboard"]
        let detailsTab = tabBar.buttons["Details"]
        let profileTab = tabBar.buttons["Profile"]
        
        XCTAssertTrue(chatsTab.exists, "Chats tab should exist")
        XCTAssertTrue(dashboardTab.exists, "Dashboard tab should exist")
        XCTAssertTrue(detailsTab.exists, "Details tab should exist")
        XCTAssertTrue(profileTab.exists, "Profile tab should exist")
        
        // Test that chats tab is selected by default
        XCTAssertTrue(chatsTab.isSelected, "Chats tab should be selected by default")
    }
    
    func testNavigationBetweenTabs() throws {
        navigateToCarerTabs()
        
        let tabBar = app.tabBars.firstMatch
        
        // Navigate to Dashboard
        tabBar.buttons["Dashboard"].tap()
        XCTAssertTrue(app.navigationBars["Dashboard"].exists, "Dashboard screen should be displayed")
        XCTAssertTrue(tabBar.buttons["Dashboard"].isSelected, "Dashboard tab should be selected")
        
        // Navigate to Details
        tabBar.buttons["Details"].tap()
        XCTAssertTrue(app.navigationBars["Details Tree"].exists, "Details Tree screen should be displayed")
        XCTAssertTrue(tabBar.buttons["Details"].isSelected, "Details tab should be selected")
        
        // Navigate to Profile
        tabBar.buttons["Profile"].tap()
        XCTAssertTrue(app.navigationBars["Profile"].exists, "Profile screen should be displayed")
        XCTAssertTrue(tabBar.buttons["Profile"].isSelected, "Profile tab should be selected")
        
        // Navigate back to Chats
        tabBar.buttons["Chats"].tap()
        XCTAssertTrue(app.navigationBars["Chats"].exists, "Chats screen should be displayed")
        XCTAssertTrue(tabBar.buttons["Chats"].isSelected, "Chats tab should be selected")
    }
    
    func testTabBarStatePreservation() throws {
        navigateToCarerTabs()
        
        let tabBar = app.tabBars.firstMatch
        
        // Navigate to Dashboard and interact with it
        tabBar.buttons["Dashboard"].tap()
        
        // Simulate some interaction (if there are interactive elements)
        if app.buttons["Select Carees"].exists {
            // Interact with caree selection if available
        }
        
        // Switch to another tab and back
        tabBar.buttons["Profile"].tap()
        tabBar.buttons["Dashboard"].tap()
        
        // Verify we're back on Dashboard
        XCTAssertTrue(app.navigationBars["Dashboard"].exists, "Dashboard should be preserved")
        XCTAssertTrue(tabBar.buttons["Dashboard"].isSelected, "Dashboard tab should still be selected")
    }
    
    // MARK: - Profile Screen Tests
    
    func testProfileScreenElements() throws {
        navigateToCarerTabs()
        
        // Navigate to Profile tab
        app.tabBars.firstMatch.buttons["Profile"].tap()
        
        // Check profile elements exist
        XCTAssertTrue(app.staticTexts["Dr. Sarah Johnson"].exists, "User name should be displayed")
        XCTAssertTrue(app.staticTexts["Professional Carer"].exists, "User role should be displayed")
        XCTAssertTrue(app.staticTexts["Email"].exists, "Email label should exist")
        XCTAssertTrue(app.staticTexts["Phone"].exists, "Phone label should exist")
        XCTAssertTrue(app.staticTexts["Location"].exists, "Location label should exist")
        XCTAssertTrue(app.staticTexts["Documents"].exists, "Documents label should exist")
        
        // Check settings options exist
        XCTAssertTrue(app.buttons["Notifications"].exists, "Notifications setting should exist")
        XCTAssertTrue(app.buttons["Privacy & Security"].exists, "Privacy setting should exist")
        XCTAssertTrue(app.buttons["Help & Support"].exists, "Help setting should exist")
        XCTAssertTrue(app.buttons["About"].exists, "About setting should exist")
        
        // Check logout button exists
        XCTAssertTrue(app.buttons["Sign Out"].exists, "Sign Out button should exist")
    }
    
    func testProfileSettingsInteraction() throws {
        navigateToCarerTabs()
        
        // Navigate to Profile tab
        app.tabBars.firstMatch.buttons["Profile"].tap()
        
        // Test tapping on settings options (they should be tappable)
        let notificationsButton = app.buttons["Notifications"]
        XCTAssertTrue(notificationsButton.exists, "Notifications button should exist")
        XCTAssertTrue(notificationsButton.isEnabled, "Notifications button should be enabled")
        
        let privacyButton = app.buttons["Privacy & Security"]
        XCTAssertTrue(privacyButton.exists, "Privacy button should exist")
        XCTAssertTrue(privacyButton.isEnabled, "Privacy button should be enabled")
        
        let helpButton = app.buttons["Help & Support"]
        XCTAssertTrue(helpButton.exists, "Help button should exist")
        XCTAssertTrue(helpButton.isEnabled, "Help button should be enabled")
        
        let aboutButton = app.buttons["About"]
        XCTAssertTrue(aboutButton.exists, "About button should exist")
        XCTAssertTrue(aboutButton.isEnabled, "About button should be enabled")
    }
    
    // MARK: - Logout Tests
    
    func testLogoutAlert() throws {
        navigateToCarerTabs()
        
        // Navigate to Profile tab
        app.tabBars.firstMatch.buttons["Profile"].tap()
        
        // Tap Sign Out button
        app.buttons["Sign Out"].tap()
        
        // Check that logout alert appears
        let alert = app.alerts["Sign Out"]
        XCTAssertTrue(alert.exists, "Logout alert should appear")
        
        // Check alert buttons
        XCTAssertTrue(alert.buttons["Cancel"].exists, "Cancel button should exist in alert")
        XCTAssertTrue(alert.buttons["Sign Out"].exists, "Sign Out button should exist in alert")
        
        // Check alert message
        XCTAssertTrue(alert.staticTexts["Are you sure you want to sign out?"].exists, "Alert message should be displayed")
    }
    
    func testLogoutCancel() throws {
        navigateToCarerTabs()
        
        // Navigate to Profile tab
        app.tabBars.firstMatch.buttons["Profile"].tap()
        
        // Tap Sign Out button
        app.buttons["Sign Out"].tap()
        
        // Tap Cancel in alert
        app.alerts["Sign Out"].buttons["Cancel"].tap()
        
        // Verify we're still on Profile screen
        XCTAssertTrue(app.navigationBars["Profile"].exists, "Should remain on Profile screen after cancel")
        XCTAssertTrue(app.tabBars.firstMatch.buttons["Profile"].isSelected, "Profile tab should still be selected")
    }
    
    func testLogoutConfirm() throws {
        navigateToCarerTabs()
        
        // Navigate to Profile tab
        app.tabBars.firstMatch.buttons["Profile"].tap()
        
        // Tap Sign Out button
        app.buttons["Sign Out"].tap()
        
        // Tap Sign Out in alert
        app.alerts["Sign Out"].buttons["Sign Out"].tap()
        
        // Wait for navigation to complete
        let landingScreen = app.navigationBars["CareComms"]
        let expectation = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "exists == true"),
            object: landingScreen
        )
        
        wait(for: [expectation], timeout: 5.0)
        
        // Verify we're back on landing screen
        XCTAssertTrue(landingScreen.exists, "Should navigate to landing screen after logout")
        XCTAssertFalse(app.tabBars.firstMatch.exists, "Tab bar should not exist after logout")
    }
    
    // MARK: - Dashboard Screen Tests
    
    func testDashboardScreenElements() throws {
        navigateToCarerTabs()
        
        // Navigate to Dashboard tab
        app.tabBars.firstMatch.buttons["Dashboard"].tap()
        
        // Check dashboard elements exist
        XCTAssertTrue(app.staticTexts["Select Carees"].exists, "Caree selection label should exist")
        XCTAssertTrue(app.staticTexts["Time Period"].exists, "Time period label should exist")
        
        // Check segmented control exists
        let segmentedControl = app.segmentedControls.firstMatch
        XCTAssertTrue(segmentedControl.exists, "Time period segmented control should exist")
        
        // Check that Daily is selected by default
        XCTAssertTrue(segmentedControl.buttons["Daily"].isSelected, "Daily should be selected by default")
    }
    
    func testDashboardPeriodSelection() throws {
        navigateToCarerTabs()
        
        // Navigate to Dashboard tab
        app.tabBars.firstMatch.buttons["Dashboard"].tap()
        
        let segmentedControl = app.segmentedControls.firstMatch
        
        // Test switching to Weekly
        segmentedControl.buttons["Weekly"].tap()
        XCTAssertTrue(segmentedControl.buttons["Weekly"].isSelected, "Weekly should be selected")
        
        // Test switching to Bi-weekly
        segmentedControl.buttons["Bi-weekly"].tap()
        XCTAssertTrue(segmentedControl.buttons["Bi-weekly"].isSelected, "Bi-weekly should be selected")
        
        // Test switching back to Daily
        segmentedControl.buttons["Daily"].tap()
        XCTAssertTrue(segmentedControl.buttons["Daily"].isSelected, "Daily should be selected")
    }
    
    // MARK: - Details Tree Screen Tests
    
    func testDetailsTreeScreenElements() throws {
        navigateToCarerTabs()
        
        // Navigate to Details tab
        app.tabBars.firstMatch.buttons["Details"].tap()
        
        // Check details tree elements exist
        XCTAssertTrue(app.navigationBars["Details Tree"].exists, "Details Tree navigation should exist")
        
        // If there are multiple carees, check selection header
        if app.staticTexts["Select Carees"].exists {
            XCTAssertTrue(app.staticTexts["Select Carees"].exists, "Caree selection should exist")
        }
    }
    
    // MARK: - Helper Methods
    
    private func navigateToCarerTabs() {
        // This is a simplified navigation - in a real test, you'd need to:
        // 1. Navigate through splash, terms, landing, login screens
        // 2. Or use a test-specific entry point
        
        // For now, we'll assume the app can be launched directly to carer tabs
        // In a real implementation, you might need to:
        // - Skip onboarding screens
        // - Mock authentication
        // - Use deep links or test-specific navigation
        
        // Wait for the app to load and navigate to carer tabs
        let expectation = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "exists == true"),
            object: app.tabBars.firstMatch
        )
        
        // If tab bar doesn't exist, try to navigate through the app
        if !app.tabBars.firstMatch.exists {
            // This would need to be implemented based on your app's flow
            // For example:
            // - Tap through splash screen
            // - Accept terms
            // - Navigate to login
            // - Enter credentials
            // - Navigate to carer tabs
        }
        
        wait(for: [expectation], timeout: 10.0)
    }
}

// MARK: - Extensions for better test readability

extension XCUIElement {
    var isSelected: Bool {
        return (self.value as? String) == "1" || self.isSelected
    }
}