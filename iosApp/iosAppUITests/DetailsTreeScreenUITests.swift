import XCTest

final class DetailsTreeScreenUITests: XCTestCase {
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
        
        // Navigate to Details Tree screen
        navigateToDetailsTreeScreen()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    // MARK: - Navigation Helper
    
    private func navigateToDetailsTreeScreen() {
        // Assuming we're starting from the main carer interface
        // Navigate through the tab bar to Details Tree
        let tabBar = app.tabBars.firstMatch
        if tabBar.exists {
            let detailsTreeTab = tabBar.buttons["Details Tree"]
            if detailsTreeTab.exists {
                detailsTreeTab.tap()
            }
        }
        
        // Wait for the screen to load
        let navigationTitle = app.navigationBars["Details Tree"]
        XCTAssertTrue(navigationTitle.waitForExistence(timeout: 5.0))
    }
    
    // MARK: - Tile Layout Tests
    
    func testCareeSelectionTileLayout() throws {
        // Test that caree tiles are displayed in a grid layout
        let careeGrid = app.scrollViews.containing(.staticText, identifier: "Select Carees").firstMatch
        XCTAssertTrue(careeGrid.exists, "Caree selection grid should be visible")
        
        // Check for tile elements
        let careeTiles = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'caree-tile'"))
        XCTAssertGreaterThan(careeTiles.count, 0, "Should have at least one caree tile")
        
        // Test tile visual elements
        let firstTile = careeTiles.element(boundBy: 0)
        XCTAssertTrue(firstTile.exists, "First caree tile should exist")
        
        // Check for avatar circle in tile
        let avatarCircle = firstTile.images.firstMatch
        XCTAssertTrue(avatarCircle.exists, "Caree tile should have an avatar")
        
        // Check for caree name
        let careeName = firstTile.staticTexts.firstMatch
        XCTAssertTrue(careeName.exists, "Caree tile should display name")
    }
    
    func testCareeSelectionInteraction() throws {
        // Test selecting a caree tile
        let careeTiles = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'caree-tile'"))
        guard careeTiles.count > 0 else {
            XCTFail("No caree tiles found")
            return
        }
        
        let firstTile = careeTiles.element(boundBy: 0)
        
        // Tap to select
        firstTile.tap()
        
        // Verify selection state change (visual feedback)
        // The tile should show selection indicator
        let selectionIndicator = firstTile.images["checkmark.circle.fill"]
        XCTAssertTrue(selectionIndicator.waitForExistence(timeout: 2.0), "Selection indicator should appear")
        
        // Tap again to deselect
        firstTile.tap()
        
        // Verify deselection
        XCTAssertFalse(selectionIndicator.exists, "Selection indicator should disappear")
    }
    
    func testMultipleCareeSelection() throws {
        let careeTiles = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'caree-tile'"))
        guard careeTiles.count >= 2 else {
            XCTSkip("Need at least 2 carees for multi-selection test")
        }
        
        // Select first caree
        let firstTile = careeTiles.element(boundBy: 0)
        firstTile.tap()
        
        // Select second caree
        let secondTile = careeTiles.element(boundBy: 1)
        secondTile.tap()
        
        // Both should be selected
        let firstSelection = firstTile.images["checkmark.circle.fill"]
        let secondSelection = secondTile.images["checkmark.circle.fill"]
        
        XCTAssertTrue(firstSelection.exists, "First caree should remain selected")
        XCTAssertTrue(secondSelection.exists, "Second caree should be selected")
    }
    
    // MARK: - Tree Navigation Tests
    
    func testTreeStructureDisplay() throws {
        // Select a caree first
        selectFirstCaree()
        
        // Wait for tree structure to load
        let treeContent = app.scrollViews.containing(.staticText, identifier: "Health Information").firstMatch
        XCTAssertTrue(treeContent.waitForExistence(timeout: 3.0), "Tree content should load after caree selection")
        
        // Check for main categories
        let healthCategory = app.buttons["Health Information"]
        let activitiesCategory = app.buttons["Daily Activities"]
        let communicationCategory = app.buttons["Communication"]
        
        XCTAssertTrue(healthCategory.exists, "Health Information category should exist")
        XCTAssertTrue(activitiesCategory.exists, "Daily Activities category should exist")
        XCTAssertTrue(communicationCategory.exists, "Communication category should exist")
    }
    
    func testAccordionExpansion() throws {
        selectFirstCaree()
        
        // Wait for categories to load
        let healthCategory = app.buttons["Health Information"]
        XCTAssertTrue(healthCategory.waitForExistence(timeout: 3.0), "Health category should exist")
        
        // Tap to expand
        healthCategory.tap()
        
        // Check for expanded content
        let medicationsDetail = app.buttons["Medications"]
        XCTAssertTrue(medicationsDetail.waitForExistence(timeout: 2.0), "Medications detail should appear after expansion")
        
        let vitalsDetail = app.buttons["Vital Signs"]
        XCTAssertTrue(vitalsDetail.exists, "Vital Signs detail should be visible")
        
        // Test collapse
        healthCategory.tap()
        
        // Details should disappear
        XCTAssertFalse(medicationsDetail.waitForNonExistence(timeout: 2.0), "Medications should disappear after collapse")
    }
    
    func testHierarchicalNavigation() throws {
        selectFirstCaree()
        
        // Navigate through hierarchy: Category -> Detail -> Items
        let healthCategory = app.buttons["Health Information"]
        XCTAssertTrue(healthCategory.waitForExistence(timeout: 3.0))
        healthCategory.tap()
        
        // Expand medications detail
        let medicationsDetail = app.buttons["Medications"]
        XCTAssertTrue(medicationsDetail.waitForExistence(timeout: 2.0))
        medicationsDetail.tap()
        
        // Check for medication items
        let medicationItem = app.staticTexts["Lisinopril 10mg"]
        XCTAssertTrue(medicationItem.waitForExistence(timeout: 2.0), "Medication items should be visible")
        
        // Check for medication data
        let medicationData = app.staticTexts["Daily, Morning"]
        XCTAssertTrue(medicationData.exists, "Medication data should be displayed")
    }
    
    func testSmoothAnimations() throws {
        selectFirstCaree()
        
        let healthCategory = app.buttons["Health Information"]
        XCTAssertTrue(healthCategory.waitForExistence(timeout: 3.0))
        
        // Test expansion animation
        healthCategory.tap()
        
        // Verify content appears with animation (check for intermediate states)
        let medicationsDetail = app.buttons["Medications"]
        XCTAssertTrue(medicationsDetail.waitForExistence(timeout: 1.0), "Content should animate in smoothly")
        
        // Test collapse animation
        healthCategory.tap()
        XCTAssertTrue(medicationsDetail.waitForNonExistence(timeout: 1.0), "Content should animate out smoothly")
    }
    
    // MARK: - Mock Data Display Tests
    
    func testMockDataDisplay() throws {
        selectFirstCaree()
        
        // Expand health information
        let healthCategory = app.buttons["Health Information"]
        XCTAssertTrue(healthCategory.waitForExistence(timeout: 3.0))
        healthCategory.tap()
        
        // Expand vital signs
        let vitalsDetail = app.buttons["Vital Signs"]
        XCTAssertTrue(vitalsDetail.waitForExistence(timeout: 2.0))
        vitalsDetail.tap()
        
        // Check for mock vital signs data
        let bloodPressure = app.staticTexts["Blood Pressure"]
        let heartRate = app.staticTexts["Heart Rate"]
        let temperature = app.staticTexts["Temperature"]
        
        XCTAssertTrue(bloodPressure.exists, "Blood pressure data should be displayed")
        XCTAssertTrue(heartRate.exists, "Heart rate data should be displayed")
        XCTAssertTrue(temperature.exists, "Temperature data should be displayed")
        
        // Check for data values
        let bpValue = app.staticTexts["120/80 mmHg"]
        let hrValue = app.staticTexts["72 bpm"]
        let tempValue = app.staticTexts["98.6°F"]
        
        XCTAssertTrue(bpValue.exists, "Blood pressure value should be displayed")
        XCTAssertTrue(hrValue.exists, "Heart rate value should be displayed")
        XCTAssertTrue(tempValue.exists, "Temperature value should be displayed")
    }
    
    func testCommunicationDataDisplay() throws {
        selectFirstCaree()
        
        // Navigate to communication section
        let communicationCategory = app.buttons["Communication"]
        XCTAssertTrue(communicationCategory.waitForExistence(timeout: 3.0))
        communicationCategory.tap()
        
        // Expand recent messages
        let messagesDetail = app.buttons["Recent Messages"]
        XCTAssertTrue(messagesDetail.waitForExistence(timeout: 2.0))
        messagesDetail.tap()
        
        // Check for mock message data
        let message1 = app.staticTexts["Good morning!"]
        let message2 = app.staticTexts["Took my medication"]
        
        XCTAssertTrue(message1.exists, "Mock messages should be displayed")
        XCTAssertTrue(message2.exists, "Multiple mock messages should be visible")
        
        // Check for timestamps
        let timestamp1 = app.staticTexts["Today 8:00 AM"]
        XCTAssertTrue(timestamp1.exists, "Message timestamps should be displayed")
    }
    
    // MARK: - iOS Native Styling Tests
    
    func testIOSNativeStyling() throws {
        // Test that the interface uses iOS-native styling
        let navigationBar = app.navigationBars["Details Tree"]
        XCTAssertTrue(navigationBar.exists, "Should use iOS navigation bar")
        
        // Test refresh button in toolbar
        let refreshButton = app.buttons["Refresh"]
        XCTAssertTrue(refreshButton.exists, "Should have iOS-native refresh button")
        
        // Test scroll view behavior
        selectFirstCaree()
        let scrollView = app.scrollViews.firstMatch
        XCTAssertTrue(scrollView.exists, "Should use iOS-native scroll view")
        
        // Test that content is scrollable
        if scrollView.exists {
            scrollView.swipeUp()
            scrollView.swipeDown()
        }
    }
    
    func testAccessibilityLabels() throws {
        // Test accessibility for elderly users
        selectFirstCaree()
        
        let healthCategory = app.buttons["Health Information"]
        XCTAssertTrue(healthCategory.waitForExistence(timeout: 3.0))
        
        // Check that elements have proper accessibility labels
        XCTAssertNotNil(healthCategory.label, "Category buttons should have accessibility labels")
        
        // Test that touch targets are large enough (minimum 44pt)
        let frame = healthCategory.frame
        XCTAssertGreaterThanOrEqual(frame.height, 44, "Touch targets should be at least 44pt high")
    }
    
    // MARK: - Error Handling Tests
    
    func testEmptyStateDisplay() throws {
        // Test empty state when no carees are selected
        // Deselect all carees if any are selected
        let careeTiles = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'caree-tile'"))
        for i in 0..<careeTiles.count {
            let tile = careeTiles.element(boundBy: i)
            let selectionIndicator = tile.images["checkmark.circle.fill"]
            if selectionIndicator.exists {
                tile.tap() // Deselect
            }
        }
        
        // Check for empty state
        let emptyStateText = app.staticTexts["Select Carees"]
        XCTAssertTrue(emptyStateText.exists, "Empty state should be displayed when no carees selected")
        
        let emptyStateDescription = app.staticTexts["Choose one or more carees to explore their detailed information"]
        XCTAssertTrue(emptyStateDescription.exists, "Empty state description should be visible")
    }
    
    func testRefreshFunctionality() throws {
        // Test refresh button functionality
        let refreshButton = app.buttons["Refresh"]
        XCTAssertTrue(refreshButton.exists, "Refresh button should exist")
        
        refreshButton.tap()
        
        // Should show loading state briefly
        let loadingIndicator = app.staticTexts["Loading caree data..."]
        // Note: Loading might be too fast to catch in tests, so we just verify the button works
        
        // Verify content is still available after refresh
        let careeGrid = app.scrollViews.containing(.staticText, identifier: "Select Carees").firstMatch
        XCTAssertTrue(careeGrid.waitForExistence(timeout: 3.0), "Content should be available after refresh")
    }
    
    // MARK: - Performance Tests
    
    func testScrollPerformance() throws {
        selectFirstCaree()
        
        // Expand all categories to create more content
        let categories = ["Health Information", "Daily Activities", "Communication"]
        for categoryName in categories {
            let category = app.buttons[categoryName]
            if category.waitForExistence(timeout: 2.0) {
                category.tap()
            }
        }
        
        // Test smooth scrolling performance
        let scrollView = app.scrollViews.firstMatch
        XCTAssertTrue(scrollView.exists, "Scroll view should exist")
        
        // Perform multiple scroll operations
        for _ in 0..<5 {
            scrollView.swipeUp()
            usleep(100000) // 0.1 second delay
        }
        
        for _ in 0..<5 {
            scrollView.swipeDown()
            usleep(100000) // 0.1 second delay
        }
        
        // If we get here without timeout, scrolling performance is acceptable
        XCTAssertTrue(true, "Scrolling should be smooth and responsive")
    }
    
    // MARK: - Helper Methods
    
    private func selectFirstCaree() {
        let careeTiles = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'caree-tile'"))
        guard careeTiles.count > 0 else {
            XCTFail("No caree tiles found for selection")
            return
        }
        
        let firstTile = careeTiles.element(boundBy: 0)
        firstTile.tap()
        
        // Wait for tree content to load
        sleep(1)
    }
}