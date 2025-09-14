import XCTest

final class LandingScreenUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
        
        // Navigate through splash and terms to reach landing screen
        let termsTitle = app.staticTexts["Terms and Conditions"]
        XCTAssertTrue(termsTitle.waitForExistence(timeout: 6.0))
        
        // Scroll to bottom and accept terms
        let scrollView = app.scrollViews.firstMatch
        for _ in 0..<10 {
            scrollView.swipeUp()
        }
        
        Thread.sleep(forTimeInterval: 1.0)
        
        let acceptButton = app.buttons["Accept and Continue"]
        if acceptButton.isEnabled {
            acceptButton.tap()
        }
        
        // Wait for landing screen
        let welcomeText = app.staticTexts["Welcome to CareComms"]
        XCTAssertTrue(welcomeText.waitForExistence(timeout: 3.0))
    }
    
    func testLandingScreenDisplaysWelcomeMessage() throws {
        // Test that landing screen shows welcome message
        let welcomeTitle = app.staticTexts["Welcome to CareComms"]
        XCTAssertTrue(welcomeTitle.exists)
        
        let welcomeSubtitle = app.staticTexts["Connecting carers and care recipients with compassionate communication"]
        XCTAssertTrue(welcomeSubtitle.exists)
    }
    
    func testLandingScreenDisplaysLogo() throws {
        // Test that landing screen shows the app logo
        let logoImage = app.images["heart.text.square.fill"]
        XCTAssertTrue(logoImage.exists)
    }
    
    func testLoginButtonExists() throws {
        // Test that login button exists and is functional
        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.exists)
        XCTAssertTrue(loginButton.isEnabled)
    }
    
    func testSignUpButtonExists() throws {
        // Test that sign up button exists
        let signUpButton = app.buttons["Sign Up as Carer"]
        XCTAssertTrue(signUpButton.exists)
        XCTAssertTrue(signUpButton.isEnabled)
    }
    
    func testCareeSignupInfoText() throws {
        // Test that info text about caree signup is displayed
        let infoText1 = app.staticTexts["Care recipients (carees) can only join through"]
        XCTAssertTrue(infoText1.exists)
        
        let infoText2 = app.staticTexts["invitation links from their carers"]
        XCTAssertTrue(infoText2.exists)
    }
    
    func testNavigationToLoginScreen() throws {
        // Test navigation to login screen
        let loginButton = app.buttons["Login"]
        loginButton.tap()
        
        // Verify navigation to login screen
        let loginTitle = app.staticTexts["Welcome Back"]
        XCTAssertTrue(loginTitle.waitForExistence(timeout: 3.0))
    }
    
    func testButtonStyling() throws {
        // Test that buttons have proper styling and are accessible
        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.exists)
        
        let signUpButton = app.buttons["Sign Up as Carer"]
        XCTAssertTrue(signUpButton.exists)
        
        // Both buttons should be hittable (properly sized for accessibility)
        XCTAssertTrue(loginButton.isHittable)
        XCTAssertTrue(signUpButton.isHittable)
    }
}