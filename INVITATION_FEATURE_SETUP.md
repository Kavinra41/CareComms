# CareComms Invitation Feature Setup Guide

## Overview
This feature allows carers to invite carees via email with unique invitation codes. When carees register using the invitation code, they are automatically connected to the specific carer who invited them.

## Key Features Implemented

### 1. **Invitation System**
- Auto-generated unique codes per carer (consistent across sessions)
- Email invitations with HTML templates
- Deep link support (`carecomms://invite/ABC123`)
- Carer-caree relationship management

### 2. **Privacy & Security**
- Only connected users can see each other
- Invitation codes are required for caree registration
- Relationships are stored in Firebase with proper validation

### 3. **User Interface**
- Invitation screen with code sharing and email sending
- Updated chat list showing only connected conversations
- Dashboard integration with "Invite Caree" button

## Setup Instructions

### 1. **Gmail Configuration** (REQUIRED)
You need to configure Gmail credentials for sending invitation emails:

1. **Enable 2-Factor Authentication** on your Gmail account
2. **Generate App Password**:
   - Go to Google Account Settings
   - Security → 2-Step Verification (enable if not already)
   - Security → App passwords
   - Generate password for "Mail"
   - Copy the 16-character password

3. **Update Configuration**:
   ```kotlin
   // File: androidApp/src/androidMain/kotlin/com/carecomms/android/config/EmailConfig.kt
   object EmailConfig {
       const val SENDER_EMAIL = "your-actual-email@gmail.com"
       const val SENDER_APP_PASSWORD = "your-16-character-app-password"
   }
   ```

### 2. **Firebase Collections**
The following collections will be automatically created:
- `carer_invitations` - Stores invitation codes and carer info
- `carer_caree_relationships` - Manages connections between users

### 3. **Deep Link Testing**
Test deep links using ADB:
```bash
adb shell am start \
  -W -a android.intent.action.VIEW \
  -d "carecomms://invite/ABC123" \
  com.carecomms.android
```

## How It Works

### 1. **Carer Flow**
1. Carer logs in and goes to Dashboard
2. Clicks "Invite Caree" button
3. Gets unique invitation code (same code every time)
4. Can share code directly or send email invitation
5. Email contains invitation code and deep link

### 2. **Caree Flow**
1. Receives email with invitation
2. Clicks deep link or manually opens app
3. Registers new account with invitation code pre-filled
4. Automatically connected to the inviting carer

### 3. **Chat System**
- Only shows conversations between connected users
- Chat list displays actual conversation previews
- No more "all users" visibility

## Files Added/Modified

### New Files
- `shared/src/commonMain/kotlin/com/carecomms/data/models/InvitationModels.kt`
- `shared/src/commonMain/kotlin/com/carecomms/data/repository/InvitationRepository.kt`
- `shared/src/commonMain/kotlin/com/carecomms/utils/CodeGenerator.kt`
- `androidApp/src/androidMain/kotlin/com/carecomms/android/data/repository/FirebaseInvitationRepository.kt`
- `androidApp/src/androidMain/kotlin/com/carecomms/android/data/repository/EmailService.kt`
- `androidApp/src/androidMain/kotlin/com/carecomms/android/ui/viewmodels/InvitationViewModel.kt`
- `androidApp/src/androidMain/kotlin/com/carecomms/android/ui/screens/InvitationScreen.kt`
- `androidApp/src/androidMain/kotlin/com/carecomms/android/utils/DeepLinkHandler.kt`
- `androidApp/src/androidMain/kotlin/com/carecomms/android/config/EmailConfig.kt`
- `shared/src/commonMain/kotlin/com/carecomms/data/models/ChatPreview.kt`

### Modified Files
- `androidApp/build.gradle.kts` - Added JavaMail dependencies
- `androidApp/src/androidMain/AndroidManifest.xml` - Deep link support already present
- `androidApp/src/androidMain/kotlin/com/carecomms/android/di/AndroidModule.kt` - DI setup
- `androidApp/src/androidMain/kotlin/com/carecomms/android/MainActivity.kt` - Deep link handling
- `androidApp/src/androidMain/kotlin/com/carecomms/android/navigation/AuthNavigation.kt` - Invitation code passing
- `androidApp/src/androidMain/kotlin/com/carecomms/android/navigation/CarerNavigation.kt` - Added invitation screen
- `androidApp/src/androidMain/kotlin/com/carecomms/android/ui/screens/SignupScreen.kt` - Invitation code support
- `androidApp/src/androidMain/kotlin/com/carecomms/android/ui/screens/DashboardScreen.kt` - Invite button
- `androidApp/src/androidMain/kotlin/com/carecomms/android/ui/screens/ChatListScreen.kt` - Chat previews
- `androidApp/src/androidMain/kotlin/com/carecomms/android/ui/viewmodels/ChatListViewModel.kt` - Updated for relationships
- `shared/src/commonMain/kotlin/com/carecomms/data/repository/ChatRepository.kt` - Added getChatPreviews
- `androidApp/src/androidMain/kotlin/com/carecomms/android/data/repository/FirestoreChatRepository.kt` - Relationship filtering
- `androidApp/src/androidMain/kotlin/com/carecomms/android/data/repository/LocalChatRepository.kt` - Interface compliance

## Testing Checklist

### Before Testing
- [ ] Configure Gmail credentials in `EmailConfig.kt`
- [ ] Build and install the app
- [ ] Ensure Firebase is properly configured

### Test Scenarios
1. **Carer Registration & Code Generation**
   - [ ] Register as carer
   - [ ] Navigate to invitation screen
   - [ ] Verify unique code is generated
   - [ ] Verify same code appears on subsequent visits

2. **Email Invitation**
   - [ ] Enter valid email address
   - [ ] Send invitation
   - [ ] Check recipient's email for invitation
   - [ ] Verify email contains code and deep link

3. **Deep Link Registration**
   - [ ] Click deep link from email
   - [ ] Verify app opens with code pre-filled
   - [ ] Complete caree registration
   - [ ] Verify automatic connection to carer

4. **Manual Code Entry**
   - [ ] Open app normally
   - [ ] Register as caree with invitation code
   - [ ] Verify connection to carer

5. **Chat Functionality**
   - [ ] Verify only connected users appear in chat list
   - [ ] Start conversation between carer and caree
   - [ ] Verify messages sync properly

## Troubleshooting

### Email Not Sending
- Check Gmail credentials in `EmailConfig.kt`
- Ensure 2FA is enabled and app password is used
- Check device internet connection
- Look for error logs in Android Studio

### Deep Links Not Working
- Verify manifest configuration
- Test with ADB command
- Check if app is set as default for the scheme

### Users Not Connecting
- Check Firebase console for relationship documents
- Verify invitation code validation
- Check error logs during registration

## Security Notes
- Invitation codes are deterministic but not easily guessable
- Email credentials should be secured (consider using environment variables in production)
- All Firebase operations include proper error handling
- Relationships are validated before creation

## Future Enhancements
- Invitation code expiry
- Multiple invitation codes per carer
- Invitation analytics
- Push notifications for new invitations
- Bulk invitation support