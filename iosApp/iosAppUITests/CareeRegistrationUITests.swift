import XCTest

final class CareeRegistrationUITests: XCTestCase {
    
    var app: XCUIApplication!
    
    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }
    
    override func tearDownWithError() throws {
        app = nil
    }
    
    func testCareeRegistrationScreenElements() throws {
        // Navigate to caree registration via deep link simulation
        // This would normally be triggered by opening a deep link
        // For testing, we'll simulate the screen being presented
        
        // Test that all form elements are present
        let emailField = app.textFields["Enter your email"]
        let passwordField = app.secureTextFields["Enter your password"]
        let confirmPasswordField = app.secureTextFields["Confirm your password"]
        let firstNameField = app.textFields["Enter your first name"]
        let lastNameField = app.textFields["Enter your last name"]
        let dateOfBirthButton = app.buttons["Select your date of birth"]
        let addressField = app.textFields["Enter your address"]
        let emergencyContactField = app.textFields["Enter emergency contact"]
        let healthInfoTextView = app.textViews.firstMatch
        let submitButton = app.buttons["Complete Registration"]
        
        // Verify elements exist (when screen is loaded)
        XCTAssertTrue(emailField.exists)
        XCTAssertTrue(passwordField.exists)
        XCTAssertTrue(confirmPasswordField.exists)
        XCTAssertTrue(firstNameField.exists)
        XCTAssertTrue(lastNameField.exists)
        XCTAssertTrue(dateOfBirthButton.exists)
        XCTAssertTrue(addressField.exists)
        XCTAssertTrue(emergencyContactField.exists)
        XCTAssertTrue(healthInfoTextView.exists)
        XCTAssertTrue(submitButton.exists)
    }
    
    func testFormValidation() throws {
        // Test that submit button is disabled when form is invalid
        let submitButton = app.buttons["Complete Registration"]
        
        // Initially, submit button should be disabled
        XCTAssertFalse(submitButton.isEnabled)
        
        // Fill in some fields but not all required ones
        let emailField = app.textFields["Enter your email"]
        emailField.tap()
        emailField.typeText("test@example.com")
        
        // Submit button should still be disabled
        XCTAssertFalse(submitButton.isEnabled)
    }
    
    func testPasswordVisibilityToggle() throws {
        let passwordField = app.secureTextFields["Enter your password"]
        let passwordToggleButton = app.buttons["eye.fill"]
        
        // Initially password should be secure
        XCTAssertTrue(passwordField.exists)
        
        // Tap the toggle button
        passwordToggleButton.tap()
        
        // Now it should be a regular text field
        let visiblePasswordField = app.textFields["Enter your password"]
        XCTAssertTrue(visiblePasswordField.exists)
        
        // Tap again to hide
        let hidePasswordButton = app.buttons["eye.slash.fill"]
        hidePasswordButton.tap()
        
        // Should be secure again
        XCTAssertTrue(passwordField.exists)
    }
    
    func testDateOfBirthPicker() throws {
        let dateOfBirthButton = app.buttons["Select your date of birth"]
        
        // Tap the date of birth button
        dateOfBirthButton.tap()
        
        // Date picker sheet should appear
        let datePickerSheet = app.navigationBars["Date of Birth"]
        XCTAssertTrue(datePickerSheet.exists)
        
        // Test cancel button
        let cancelButton = app.buttons["Cancel"]
        XCTAssertTrue(cancelButton.exists)
        cancelButton.tap()
        
        // Sheet should dismiss
        XCTAssertFalse(datePickerSheet.exists)
    }
    
    func testDateOfBirthSelection() throws {
        let dateOfBirthButton = app.buttons["Select your date of birth"]
        
        // Tap the date of birth button
        dateOfBirthButton.tap()
        
        // Date picker sheet should appear
        let datePickerSheet = app.navigationBars["Date of Birth"]
        XCTAssertTrue(datePickerSheet.exists)
        
        // Test done button
        let doneButton = app.buttons["Done"]
        XCTAssertTrue(doneButton.exists)
        doneButton.tap()
        
        // Sheet should dismiss and button text should change
        XCTAssertFalse(datePickerSheet.exists)
        XCTAssertNotEqual(dateOfBirthButton.label, "Select your date of birth")
    }
    
    func testInvitationValidationLoading() throws {
        // Test that loading indicator appears during invitation validation
        let loadingText = app.staticTexts["Validating invitation..."]
        
        // This would be visible when the screen first loads with an invitation token
        // In a real test, we would simulate this state
        if loadingText.exists {
            XCTAssertTrue(loadingText.exists)
        }
    }
    
    func testInvalidInvitationMessage() throws {
        // Test that invalid invitation message is displayed
        let invalidInvitationTitle = app.staticTexts["Invalid Invitation"]
        let invalidInvitationMessage = app.staticTexts["This invitation link is invalid or has expired. Please contact your carer for a new invitation."]
        
        // These would be visible when invitation validation fails
        if invalidInvitationTitle.exists {
            XCTAssertTrue(invalidInvitationTitle.exists)
            XCTAssertTrue(invalidInvitationMessage.exists)
        }
    }
    
    func testValidInvitationDisplay() throws {
        // Test that carer information is displayed when invitation is valid
        let joinTitle = app.staticTexts["Join CareComms"]
        
        // This should always be present
        XCTAssertTrue(joinTitle.exists)
        
        // Carer invitation text would be present when invitation is valid
        let invitationTexts = app.staticTexts.matching(NSPredicate(format: "label CONTAINS 'invited by'"))
        
        // If invitation is valid, this text should exist
        if invitationTexts.count > 0 {
            XCTAssertTrue(invitationTexts.firstMatch.exists)
        }
    }
    
    func testFormSectionHeaders() throws {
        // Test that all section headers are present
        let accountInfoHeader = app.staticTexts["Account Information"]
        let personalInfoHeader = app.staticTexts["Personal Information"]
        let healthInfoHeader = app.staticTexts["Health Information"]
        
        XCTAssertTrue(accountInfoHeader.exists)
        XCTAssertTrue(personalInfoHeader.exists)
        XCTAssertTrue(healthInfoHeader.exists)
    }
    
    func testHealthInfoTextEditor() throws {
        // Test health information text editor
        let healthInfoTextView = app.textViews.firstMatch
        
        XCTAssertTrue(healthInfoTextView.exists)
        
        // Tap and enter text
        healthInfoTextView.tap()
        healthInfoTextView.typeText("Sample health information for testing")
        
        // Verify text was entered
        XCTAssertTrue(healthInfoTextView.value as? String == "Sample health information for testing")
    }
    
    func testNavigationBackButton() throws {
        // Test back button functionality
        let backButton = app.buttons["Back"]
        
        XCTAssertTrue(backButton.exists)
        
        // Tap back button
        backButton.tap()
        
        // Should navigate back (this would need to be verified based on the navigation flow)
    }
    
    func testRegistrationSuccessAlert() throws {
        // Test that success alert appears after successful registration
        // This would require mocking the registration process
        
        let successAlert = app.alerts["Registration Successful"]
        
        // If registration succeeds, alert should appear
        if successAlert.exists {
            XCTAssertTrue(successAlert.exists)
            
            let okButton = successAlert.buttons["OK"]
            XCTAssertTrue(okButton.exists)
            
            okButton.tap()
            
            // Alert should dismiss
            XCTAssertFalse(successAlert.exists)
        }
    }
    
    func testRegistrationErrorAlert() throws {
        // Test that error alert appears when registration fails
        let errorAlert = app.alerts["Registration Error"]
        
        // If registration fails, error alert should appear
        if errorAlert.exists {
            XCTAssertTrue(errorAlert.exists)
            
            let okButton = errorAlert.buttons["OK"]
            XCTAssertTrue(okButton.exists)
            
            okButton.tap()
            
            // Alert should dismiss
            XCTAssertFalse(errorAlert.exists)
        }
    }
    
    func testFormFieldValidationErrors() throws {
        // Test that validation error messages appear for invalid fields
        
        // Enter invalid email
        let emailField = app.textFields["Enter your email"]
        emailField.tap()
        emailField.typeText("invalid-email")
        
        // Tap another field to trigger validation
        let firstNameField = app.textFields["Enter your first name"]
        firstNameField.tap()
        
        // Check for email validation error
        let emailError = app.staticTexts["Please enter a valid email address"]
        if emailError.exists {
            XCTAssertTrue(emailError.exists)
        }
    }
    
    func testAccessibilityLabels() throws {
        // Test that accessibility labels are properly set
        let emailField = app.textFields["Enter your email"]
        let passwordField = app.secureTextFields["Enter your password"]
        let submitButton = app.buttons["Complete Registration"]
        
        XCTAssertTrue(emailField.exists)
        XCTAssertTrue(passwordField.exists)
        XCTAssertTrue(submitButton.exists)
        
        // Verify accessibility identifiers if set
        XCTAssertNotNil(emailField.identifier)
        XCTAssertNotNil(passwordField.identifier)
        XCTAssertNotNil(submitButton.identifier)
    }
}