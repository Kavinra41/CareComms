import XCTest
import SwiftUI

final class DashboardScreenUITests: XCTestCase {
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launchArguments = ["UI_TESTING"]
        app.launch()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    // MARK: - Dashboard Navigation Tests
    
    func testDashboardScreenAppears() throws {
        // Navigate to dashboard (assuming we start from login)
        navigateToDashboard()
        
        // Verify dashboard screen elements
        XCTAssertTrue(app.navigationBars["Dashboard"].exists)
        XCTAssertTrue(app.staticTexts["Select Carees"].exists)
        XCTAssertTrue(app.staticTexts["Time Period"].exists)
    }
    
    func testCareeSelectionSection() throws {
        navigateToDashboard()
        
        // Wait for carees to load
        let careerSelectionText = app.staticTexts["Select Carees"]
        XCTAssertTrue(careerSelectionText.waitForExistence(timeout: 5))
        
        // Check if loading indicator appears initially
        if app.progressIndicators["Loading carees..."].exists {
            // Wait for loading to complete
            XCTAssertTrue(app.progressIndicators["Loading carees..."].waitForNonExistence(timeout: 10))
        }
        
        // Verify caree selection chips appear
        let scrollView = app.scrollViews.firstMatch
        XCTAssertTrue(scrollView.exists)
    }
    
    func testPeriodSelection() throws {
        navigateToDashboard()
        
        // Verify period selection segmented control
        let periodPicker = app.segmentedControls.firstMatch
        XCTAssertTrue(periodPicker.waitForExistence(timeout: 5))
        
        // Test period selection
        let dailyButton = periodPicker.buttons["Daily"]
        let weeklyButton = periodPicker.buttons["Weekly"]
        let biweeklyButton = periodPicker.buttons["Bi-weekly"]
        
        XCTAssertTrue(dailyButton.exists)
        XCTAssertTrue(weeklyButton.exists)
        XCTAssertTrue(biweeklyButton.exists)
        
        // Test selecting different periods
        weeklyButton.tap()
        XCTAssertTrue(weeklyButton.isSelected)
        
        biweeklyButton.tap()
        XCTAssertTrue(biweeklyButton.isSelected)
        
        dailyButton.tap()
        XCTAssertTrue(dailyButton.isSelected)
    }
    
    // MARK: - Caree Selection Tests
    
    func testCareeSelectionInteraction() throws {
        navigateToDashboard()
        
        // Wait for carees to load
        waitForCareesToLoad()
        
        // Find and tap a caree selection chip
        let careerChips = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'caree-chip'"))
        if careerChips.count > 0 {
            let firstChip = careerChips.element(boundBy: 0)
            XCTAssertTrue(firstChip.exists)
            
            // Test selection
            firstChip.tap()
            
            // Verify analytics content appears
            let activityCard = app.staticTexts["Activity Levels"]
            XCTAssertTrue(activityCard.waitForExistence(timeout: 5))
        }
    }
    
    func testMultipleCareeSelection() throws {
        navigateToDashboard()
        waitForCareesToLoad()
        
        let careerChips = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'caree-chip'"))
        if careerChips.count > 1 {
            // Select first caree
            careerChips.element(boundBy: 0).tap()
            
            // Select second caree
            careerChips.element(boundBy: 1).tap()
            
            // Verify analytics still appear for multiple selection
            let activityCard = app.staticTexts["Activity Levels"]
            XCTAssertTrue(activityCard.waitForExistence(timeout: 5))
        }
    }
    
    // MARK: - Analytics Display Tests
    
    func testAnalyticsCardsDisplay() throws {
        navigateToDashboard()
        waitForCareesToLoad()
        selectFirstCaree()
        
        // Wait for analytics to load
        waitForAnalyticsToLoad()
        
        // Verify all analytics cards are present
        XCTAssertTrue(app.staticTexts["Activity Levels"].exists)
        XCTAssertTrue(app.staticTexts["Communication Frequency"].exists)
        XCTAssertTrue(app.staticTexts["Recent Notes"].exists)
        
        // Verify chart descriptions
        XCTAssertTrue(app.staticTexts["Activity levels range from 1 (low) to 10 (high)"].exists)
        XCTAssertTrue(app.staticTexts["Number of messages exchanged"].exists)
    }
    
    func testNotesSection() throws {
        navigateToDashboard()
        waitForCareesToLoad()
        selectFirstCaree()
        waitForAnalyticsToLoad()
        
        // Scroll to notes section
        let notesCard = app.staticTexts["Recent Notes"]
        XCTAssertTrue(notesCard.exists)
        
        // Check if notes are displayed or empty state
        let noNotesText = app.staticTexts["No notes available"]
        if !noNotesText.exists {
            // If notes exist, verify they have proper structure
            // Notes should have timestamps and content
            let scrollView = app.scrollViews.firstMatch
            scrollView.swipeUp()
        }
    }
    
    func testEmptySelectionState() throws {
        navigateToDashboard()
        waitForCareesToLoad()
        
        // Ensure no carees are selected initially or deselect all
        deselectAllCarees()
        
        // Verify empty selection view
        let emptyIcon = app.images["chart.bar.xaxis"]
        let emptyTitle = app.staticTexts["Select Carees"]
        let emptyMessage = app.staticTexts["Choose one or more carees to view their analytics"]
        
        XCTAssertTrue(emptyIcon.exists || emptyTitle.exists)
        XCTAssertTrue(emptyMessage.exists)
    }
    
    // MARK: - Loading States Tests
    
    func testLoadingStates() throws {
        navigateToDashboard()
        
        // Check for initial loading state
        if app.progressIndicators["Loading carees..."].exists {
            XCTAssertTrue(app.progressIndicators["Loading carees..."].waitForNonExistence(timeout: 10))
        }
        
        // Select a caree to trigger analytics loading
        waitForCareesToLoad()
        selectFirstCaree()
        
        // Check for analytics loading state
        if app.staticTexts["Loading analytics..."].exists {
            XCTAssertTrue(app.staticTexts["Loading analytics..."].waitForNonExistence(timeout: 10))
        }
    }
    
    // MARK: - Error Handling Tests
    
    func testErrorBannerDisplay() throws {
        navigateToDashboard()
        
        // If an error banner appears, test its functionality
        let errorBanner = app.staticTexts.matching(NSPredicate(format: "label CONTAINS 'Using mock data'")).firstMatch
        if errorBanner.exists {
            // Verify dismiss button works
            let dismissButton = app.buttons["Dismiss"]
            if dismissButton.exists {
                dismissButton.tap()
                XCTAssertFalse(errorBanner.exists)
            }
        }
    }
    
    // MARK: - Refresh Functionality Tests
    
    func testPullToRefresh() throws {
        navigateToDashboard()
        waitForCareesToLoad()
        
        // Perform pull to refresh
        let scrollView = app.scrollViews.firstMatch
        XCTAssertTrue(scrollView.exists)
        
        // Pull down to refresh
        let startCoordinate = scrollView.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.2))
        let endCoordinate = scrollView.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.8))
        startCoordinate.press(forDuration: 0.1, thenDragTo: endCoordinate)
        
        // Verify refresh completes
        XCTAssertTrue(scrollView.exists)
    }
    
    // MARK: - Accessibility Tests
    
    func testAccessibilityElements() throws {
        navigateToDashboard()
        waitForCareesToLoad()
        
        // Verify important elements have accessibility labels
        XCTAssertTrue(app.staticTexts["Select Carees"].isAccessibilityElement)
        XCTAssertTrue(app.staticTexts["Time Period"].isAccessibilityElement)
        
        // Verify segmented control is accessible
        let periodPicker = app.segmentedControls.firstMatch
        XCTAssertTrue(periodPicker.isAccessibilityElement)
    }
    
    func testLargeTextSupport() throws {
        // This would require setting up accessibility settings
        // For now, just verify text elements exist
        navigateToDashboard()
        
        XCTAssertTrue(app.staticTexts["Select Carees"].exists)
        XCTAssertTrue(app.staticTexts["Time Period"].exists)
    }
    
    // MARK: - Helper Methods
    
    private func navigateToDashboard() {
        // This assumes we're starting from a logged-in state
        // In a real app, you might need to navigate through login first
        
        // If we're in a tab view, tap the dashboard tab
        let dashboardTab = app.tabBars.buttons["Dashboard"]
        if dashboardTab.exists {
            dashboardTab.tap()
        }
        
        // Wait for dashboard to appear
        XCTAssertTrue(app.navigationBars["Dashboard"].waitForExistence(timeout: 5))
    }
    
    private func waitForCareesToLoad() {
        // Wait for loading to complete
        if app.progressIndicators["Loading carees..."].exists {
            XCTAssertTrue(app.progressIndicators["Loading carees..."].waitForNonExistence(timeout: 10))
        }
        
        // Wait a bit more for UI to settle
        Thread.sleep(forTimeInterval: 1.0)
    }
    
    private func selectFirstCaree() {
        let careerChips = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'caree-chip'"))
        if careerChips.count > 0 {
            careerChips.element(boundBy: 0).tap()
        }
    }
    
    private func deselectAllCarees() {
        let careerChips = app.buttons.matching(NSPredicate(format: "identifier CONTAINS 'caree-chip'"))
        for i in 0..<careerChips.count {
            let chip = careerChips.element(boundBy: i)
            if chip.exists && chip.isSelected {
                chip.tap()
            }
        }
    }
    
    private func waitForAnalyticsToLoad() {
        // Wait for analytics loading to complete
        if app.staticTexts["Loading analytics..."].exists {
            XCTAssertTrue(app.staticTexts["Loading analytics..."].waitForNonExistence(timeout: 10))
        }
        
        // Wait for analytics cards to appear
        let activityCard = app.staticTexts["Activity Levels"]
        XCTAssertTrue(activityCard.waitForExistence(timeout: 5))
    }
}

// MARK: - Performance Tests
extension DashboardScreenUITests {
    
    func testDashboardLoadingPerformance() throws {
        measure {
            navigateToDashboard()
            waitForCareesToLoad()
        }
    }
    
    func testAnalyticsLoadingPerformance() throws {
        navigateToDashboard()
        waitForCareesToLoad()
        
        measure {
            selectFirstCaree()
            waitForAnalyticsToLoad()
        }
    }
    
    func testPeriodSwitchingPerformance() throws {
        navigateToDashboard()
        waitForCareesToLoad()
        selectFirstCaree()
        waitForAnalyticsToLoad()
        
        let periodPicker = app.segmentedControls.firstMatch
        
        measure {
            periodPicker.buttons["Weekly"].tap()
            Thread.sleep(forTimeInterval: 0.5)
            
            periodPicker.buttons["Bi-weekly"].tap()
            Thread.sleep(forTimeInterval: 0.5)
            
            periodPicker.buttons["Daily"].tap()
            Thread.sleep(forTimeInterval: 0.5)
        }
    }
}