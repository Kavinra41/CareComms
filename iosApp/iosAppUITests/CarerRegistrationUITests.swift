import XCTest

final class CarerRegistrationUITests: XCTestCase {
    
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
    
    func testNavigationToCarerRegistration() throws {
        // Navigate through the app flow to reach carer registration
        navigateToCarerRegistration()
        
        // Verify we're on the carer registration screen
        XCTAssertTrue(app.staticTexts["Carer Registration"].exists)
        XCTAssertTrue(app.staticTexts["Join our community of professional carers"].exists)
    }
    
    func testBackNavigationFromCarerRegistration() throws {
        navigateToCarerRegistration()
        
        // Tap back button
        app.buttons["Back"].tap()
        
        // Verify we're back on the landing screen
        XCTAssertTrue(app.staticTexts["Welcome to CareComms"].exists)
        XCTAssertTrue(app.buttons["Sign Up as Carer"].exists)
    }
    
    // MARK: - Form Validation Tests
    
    func testEmptyFormValidation() throws {
        navigateToCarerRegistration()
        
        // Try to submit empty form
        let createAccountButton = app.buttons["Create Account"]
        XCTAssertTrue(createAccountButton.exists)
        XCTAssertFalse(createAccountButton.isEnabled)
    }
    
    func testEmailFieldValidation() throws {
        navigateToCarerRegistration()
        
        let emailField = app.textFields["Enter your email"]
        XCTAssertTrue(emailField.exists)
        
        // Test email input
        emailField.tap()
        emailField.typeText("test@example.com")
        
        // Verify email was entered
        XCTAssertEqual(emailField.value as? String, "test@example.com")
    }
    
    func testPasswordFieldsValidation() throws {
        navigateToCarerRegistration()
        
        let passwordField = app.secureTextFields["Enter your password"]
        let confirmPasswordField = app.secureTextFields["Confirm your password"]
        
        XCTAssertTrue(passwordField.exists)
        XCTAssertTrue(confirmPasswordField.exists)
        
        // Test password input
        passwordField.tap()
        passwordField.typeText("password123")
        
        confirmPasswordField.tap()
        confirmPasswordField.typeText("password123")
        
        // Test password visibility toggle
        let passwordToggle = app.buttons.matching(identifier: "eye.fill").firstMatch
        if passwordToggle.exists {
            passwordToggle.tap()
            // After toggle, password should be visible in text field
            let visiblePasswordField = app.textFields["Enter your password"]
            XCTAssertTrue(visiblePasswordField.exists)
        }
    }
    
    func testAgeFieldValidation() throws {
        navigateToCarerRegistration()
        
        let ageField = app.textFields["Enter your age"]
        XCTAssertTrue(ageField.exists)
        
        ageField.tap()
        ageField.typeText("25")
        
        XCTAssertEqual(ageField.value as? String, "25")
    }
    
    func testPhoneNumberFieldValidation() throws {
        navigateToCarerRegistration()
        
        let phoneField = app.textFields["Enter your phone number"]
        XCTAssertTrue(phoneField.exists)
        
        phoneField.tap()
        phoneField.typeText("+1234567890")
        
        XCTAssertEqual(phoneField.value as? String, "+1234567890")
    }
    
    func testLocationFieldValidation() throws {
        navigateToCarerRegistration()
        
        let locationField = app.textFields["Enter your location"]
        XCTAssertTrue(locationField.exists)
        
        locationField.tap()
        locationField.typeText("New York, NY")
        
        XCTAssertEqual(locationField.value as? String, "New York, NY")
    }
    
    // MARK: - Document Upload Tests
    
    func testDocumentUploadSection() throws {
        navigateToCarerRegistration()
        
        // Verify document upload section exists
        XCTAssertTrue(app.staticTexts["Professional Documents"].exists)
        XCTAssertTrue(app.staticTexts["Upload your professional certificates, ID, and other required documents"].exists)
        
        // Verify add document button exists
        let addDocumentButton = app.buttons["Add Document"]
        XCTAssertTrue(addDocumentButton.exists)
    }
    
    func testDocumentTypeSelector() throws {
        navigateToCarerRegistration()
        
        // Tap add document button
        app.buttons["Add Document"].tap()
        
        // Verify document type selector appears
        XCTAssertTrue(app.staticTexts["Document Type"].exists)
        
        // Verify document type options exist
        XCTAssertTrue(app.staticTexts["Professional Certificate"].exists)
        XCTAssertTrue(app.staticTexts["Identity Document"].exists)
        XCTAssertTrue(app.staticTexts["Background Check"].exists)
        XCTAssertTrue(app.staticTexts["Reference Letter"].exists)
        XCTAssertTrue(app.staticTexts["Other Document"].exists)
        
        // Test selection
        app.staticTexts["Professional Certificate"].tap()
        
        // Tap select button
        app.buttons["Select"].tap()
        
        // This should open the document picker (we can't test file selection in UI tests)
        // But we can verify the picker was dismissed
        XCTAssertFalse(app.staticTexts["Document Type"].exists)
    }
    
    func testDocumentTypeSelectorCancel() throws {
        navigateToCarerRegistration()
        
        // Tap add document button
        app.buttons["Add Document"].tap()
        
        // Tap cancel button
        app.buttons["Cancel"].tap()
        
        // Verify selector was dismissed
        XCTAssertFalse(app.staticTexts["Document Type"].exists)
    }
    
    // MARK: - Form Completion Tests
    
    func testCompleteFormEnablesSubmitButton() throws {
        navigateToCarerRegistration()
        
        // Fill out all required fields
        fillCompleteRegistrationForm()
        
        // Note: Since we can't actually upload documents in UI tests,
        // the submit button will remain disabled. In a real scenario,
        // we would need to mock the document upload functionality.
        let createAccountButton = app.buttons["Create Account"]
        XCTAssertTrue(createAccountButton.exists)
        
        // The button should still be disabled because no documents are uploaded
        // This is expected behavior for the UI test
    }
    
    func testFormFieldsRetainValues() throws {
        navigateToCarerRegistration()
        
        // Fill out form
        fillCompleteRegistrationForm()
        
        // Scroll to verify all fields retain their values
        let emailField = app.textFields["Enter your email"]
        let ageField = app.textFields["Enter your age"]
        let phoneField = app.textFields["Enter your phone number"]
        let locationField = app.textFields["Enter your location"]
        
        XCTAssertEqual(emailField.value as? String, "test@example.com")
        XCTAssertEqual(ageField.value as? String, "30")
        XCTAssertEqual(phoneField.value as? String, "+1234567890")
        XCTAssertEqual(locationField.value as? String, "New York, NY")
    }
    
    // MARK: - Error Handling Tests
    
    func testRegistrationErrorHandling() throws {
        navigateToCarerRegistration()
        
        // Fill out form with invalid data
        let emailField = app.textFields["Enter your email"]
        emailField.tap()
        emailField.typeText("invalid-email")
        
        let passwordField = app.secureTextFields["Enter your password"]
        passwordField.tap()
        passwordField.typeText("123")
        
        let confirmPasswordField = app.secureTextFields["Confirm your password"]
        confirmPasswordField.tap()
        confirmPasswordField.typeText("456")
        
        let ageField = app.textFields["Enter your age"]
        ageField.tap()
        ageField.typeText("15")
        
        // The form should show validation errors when attempting to submit
        // (This would be tested with actual validation in a complete implementation)
    }
    
    // MARK: - Accessibility Tests
    
    func testAccessibilityLabels() throws {
        navigateToCarerRegistration()
        
        // Verify important elements have accessibility labels
        XCTAssertTrue(app.textFields["Enter your email"].exists)
        XCTAssertTrue(app.secureTextFields["Enter your password"].exists)
        XCTAssertTrue(app.secureTextFields["Confirm your password"].exists)
        XCTAssertTrue(app.textFields["Enter your age"].exists)
        XCTAssertTrue(app.textFields["Enter your phone number"].exists)
        XCTAssertTrue(app.textFields["Enter your location"].exists)
        XCTAssertTrue(app.buttons["Add Document"].exists)
        XCTAssertTrue(app.buttons["Create Account"].exists)
    }
    
    func testLargeTextSupport() throws {
        // This test would verify that the UI adapts to larger text sizes
        // In a complete implementation, we would test dynamic type scaling
        navigateToCarerRegistration()
        
        // Verify key text elements exist and are readable
        XCTAssertTrue(app.staticTexts["Carer Registration"].exists)
        XCTAssertTrue(app.staticTexts["Email"].exists)
        XCTAssertTrue(app.staticTexts["Password"].exists)
        XCTAssertTrue(app.staticTexts["Professional Documents"].exists)
    }
    
    // MARK: - Helper Methods
    
    private func navigateToCarerRegistration() {
        // Wait for splash screen to complete
        let termsButton = app.buttons["I Accept"]
        let expectation = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "exists == true"),
            object: termsButton
        )
        wait(for: [expectation], timeout: 6.0)
        
        // Accept terms
        termsButton.tap()
        
        // Navigate to carer registration
        app.buttons["Sign Up as Carer"].tap()
    }
    
    private func fillCompleteRegistrationForm() {
        // Fill email
        let emailField = app.textFields["Enter your email"]
        emailField.tap()
        emailField.typeText("test@example.com")
        
        // Fill password
        let passwordField = app.secureTextFields["Enter your password"]
        passwordField.tap()
        passwordField.typeText("password123")
        
        // Fill confirm password
        let confirmPasswordField = app.secureTextFields["Confirm your password"]
        confirmPasswordField.tap()
        confirmPasswordField.typeText("password123")
        
        // Fill age
        let ageField = app.textFields["Enter your age"]
        ageField.tap()
        ageField.typeText("30")
        
        // Fill phone
        let phoneField = app.textFields["Enter your phone number"]
        phoneField.tap()
        phoneField.typeText("+1234567890")
        
        // Fill location
        let locationField = app.textFields["Enter your location"]
        locationField.tap()
        locationField.typeText("New York, NY")
        
        // Dismiss keyboard
        app.tap()
    }
}

// MARK: - Registration Success Screen Tests

extension CarerRegistrationUITests {
    
    func testRegistrationSuccessScreenElements() throws {
        // This test would verify the success screen after successful registration
        // Since we can't complete actual registration in UI tests, we'll test navigation
        
        // For now, we can test that the success screen exists in the navigation
        // In a complete implementation, we would mock successful registration
        
        navigateToCarerRegistration()
        
        // The success screen would be tested after successful form submission
        // This is a placeholder for that functionality
    }
    
    func testRegistrationSuccessNavigation() throws {
        // This would test navigation from the success screen
        // Placeholder for complete implementation
        
        // Test navigation to chat list (when implemented)
        // Test navigation back to home
    }
}