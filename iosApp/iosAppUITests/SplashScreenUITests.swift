import XCTest

final class SplashScreenUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    func testSplashScreenDisplaysLogo() throws {
        // Test that splash screen shows the app logo
        let logoImage = app.images["heart.text.square.fill"]
        XCTAssertTrue(logoImage.waitForExistence(timeout: 2.0))
    }
    
    func testSplashScreenDisplaysAppName() throws {
        // Test that splash screen shows the app name
        let appNameText = app.staticTexts["CareComms"]
        XCTAssertTrue(appNameText.waitForExistence(timeout: 2.0))
    }
    
    func testSplashScreenNavigatesToTerms() throws {
        // Test that splash screen automatically navigates to terms after 5 seconds
        let termsTitle = app.staticTexts["Terms and Conditions"]
        XCTAssertTrue(termsTitle.waitForExistence(timeout: 6.0))
    }
    
    func testSplashScreenLogoAnimation() throws {
        // Test that logo appears with animation
        let logoImage = app.images["heart.text.square.fill"]
        XCTAssertTrue(logoImage.waitForExistence(timeout: 2.0))
        
        // Verify logo is visible
        XCTAssertTrue(logoImage.isHittable)
    }
}