import XCTest

final class LoginScreenUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
        
        // Navigate to login screen
        navigateToLoginScreen()
    }
    
    private func navigateToLoginScreen() {
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
        
        // Tap login button
        let loginButton = app.buttons["Login"]
        XCTAssertTrue(loginButton.waitForExistence(timeout: 3.0))
        loginButton.tap()
        
        // Verify we're on login screen
        let loginTitle = app.staticTexts["Welcome Back"]
        XCTAssertTrue(loginTitle.waitForExistence(timeout: 3.0))
    }
    
    func testLoginScreenDisplaysTitle() throws {
        // Test that login screen shows the correct title
        let title = app.staticTexts["Welcome Back"]
        XCTAssertTrue(title.exists)
        
        let subtitle = app.staticTexts["Sign in to your account"]
        XCTAssertTrue(subtitle.exists)
    }
    
    func testLoginScreenDisplaysLogo() throws {
        // Test that login screen shows the person icon
        let logoImage = app.images["person.circle.fill"]
        XCTAssertTrue(logoImage.exists)
    }
    
    func testEmailFieldExists() throws {
        // Test that email field exists and is functional
        let emailField = app.textFields["Enter your email"]
        XCTAssertTrue(emailField.exists)
        
        // Test typing in email field
        emailField.tap()
        emailField.typeText("test@example.com")
        
        XCTAssertEqual(emailField.value as? String, "test@example.com")
    }
    
    func testPasswordFieldExists() throws {
        // Test that password field exists and is secure
        let passwordField = app.secureTextFields["Enter your password"]
        XCTAssertTrue(passwordField.exists)
        
        // Test typing in password field
        passwordField.tap()
        passwordField.typeText("password123")
    }
    
    func testPasswordVisibilityToggle() throws {
        // Test password visibility toggle functionality
        let passwordField = app.secureTextFields["Enter your password"]
        passwordField.tap()
        passwordField.typeText("password123")
        
        // Find and tap the eye icon to toggle visibility
        let eyeButton = app.buttons["eye.fill"]
        if eyeButton.exists {
            eyeButton.tap()
            
            // After tapping, it should become a regular text field
            let visiblePasswordField = app.textFields["Enter your password"]
            XCTAssertTrue(visiblePasswordField.exists)
        }
    }
    
    func testSignInButtonInitiallyDisabled() throws {
        // Test that sign in button is initially disabled
        let signInButton = app.buttons["Sign In"]
        XCTAssertTrue(signInButton.exists)
        XCTAssertFalse(signInButton.isEnabled)
    }
    
    func testSignInButtonEnabledWithValidInput() throws {
        // Test that sign in button becomes enabled with valid input
        let emailField = app.textFields["Enter your email"]
        emailField.tap()
        emailField.typeText("test@example.com")
        
        let passwordField = app.secureTextFields["Enter your password"]
        passwordField.tap()
        passwordField.typeText("password123")
        
        let signInButton = app.buttons["Sign In"]
        XCTAssertTrue(signInButton.isEnabled)
    }
    
    func testForgotPasswordButton() throws {
        // Test that forgot password button exists and is functional
        let forgotPasswordButton = app.buttons["Forgot Password?"]
        XCTAssertTrue(forgotPasswordButton.exists)
        XCTAssertTrue(forgotPasswordButton.isEnabled)
        
        // Tap forgot password button
        forgotPasswordButton.tap()
        
        // Should show an alert
        let alert = app.alerts["Login Status"]
        XCTAssertTrue(alert.waitForExistence(timeout: 2.0))
        
        let okButton = alert.buttons["OK"]
        okButton.tap()
    }
    
    func testBackButtonNavigation() throws {
        // Test back button navigation
        let backButton = app.buttons["Back"]
        XCTAssertTrue(backButton.exists)
        
        backButton.tap()
        
        // Should navigate back to landing screen
        let welcomeText = app.staticTexts["Welcome to CareComms"]
        XCTAssertTrue(welcomeText.waitForExistence(timeout: 3.0))
    }
    
    func testFormValidation() throws {
        // Test form validation with invalid email
        let emailField = app.textFields["Enter your email"]
        emailField.tap()
        emailField.typeText("invalid-email")
        
        let passwordField = app.secureTextFields["Enter your password"]
        passwordField.tap()
        passwordField.typeText("password123")
        
        let signInButton = app.buttons["Sign In"]
        // Button should still be disabled with invalid email
        XCTAssertFalse(signInButton.isEnabled)
    }
    
    func testLoadingState() throws {
        // Test loading state when signing in
        let emailField = app.textFields["Enter your email"]
        emailField.tap()
        emailField.typeText("test@example.com")
        
        let passwordField = app.secureTextFields["Enter your password"]
        passwordField.tap()
        passwordField.typeText("password123")
        
        let signInButton = app.buttons["Sign In"]
        XCTAssertTrue(signInButton.isEnabled)
        
        signInButton.tap()
        
        // Should show loading state
        let loadingButton = app.buttons["Signing In..."]
        XCTAssertTrue(loadingButton.waitForExistence(timeout: 2.0))
    }
}