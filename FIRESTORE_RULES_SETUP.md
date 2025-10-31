# Firestore Security Rules Setup

## Quick Fix for Development

To fix the PERMISSION_DENIED error immediately:

1. Go to the [Firebase Console](https://console.firebase.google.com/)
2. Select your CareComms project
3. Go to **Firestore Database** in the left sidebar
4. Click on the **Rules** tab
5. Replace the existing rules with this temporary development rule:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Temporary development rules - REMOVE IN PRODUCTION
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

6. Click **Publish** to deploy the rules

## Production Rules (Use Later)

For production, use these more secure rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow users to read and write their own user document
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Allow authenticated users to read carer invitations (needed for validation)
    match /carer_invitations/{invitationCode} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == resource.data.carerId;
    }
    
    // Allow users to read and write carer-caree relationships they're part of
    match /carer_caree_relationships/{relationshipId} {
      allow read, write: if request.auth != null && 
        (request.auth.uid == resource.data.carerId || 
         request.auth.uid == resource.data.careeId);
    }
    
    // Allow users to read and write chat rooms they're participants in
    match /chat_rooms/{chatId} {
      allow read, write: if request.auth != null && 
        request.auth.uid in resource.data.participants;
      allow create: if request.auth != null && 
        request.auth.uid in request.resource.data.participants;
    }
    
    // Allow users to read and write messages in chat rooms they're participants in
    match /chat_rooms/{chatId}/messages/{messageId} {
      allow read, write: if request.auth != null && 
        request.auth.uid in get(/databases/$(database)/documents/chat_rooms/$(chatId)).data.participants;
      allow create: if request.auth != null && 
        request.auth.uid in get(/databases/$(database)/documents/chat_rooms/$(chatId)).data.participants;
    }
  }
}
```

## What This Fixes

1. **Invitation Code Validation**: Allows authenticated users to read from `carer_invitations` collection
2. **Chat List Errors**: Allows users to read chat rooms they're participants in
3. **User Data Access**: Allows users to read/write their own user documents
4. **Relationship Management**: Allows users to manage carer-caree relationships they're part of

## Testing

After updating the rules:

1. Try the "Test Invitation Validation" button in the signup screen
2. Check if the chat list error disappears
3. Test creating and validating invitation codes
4. Test the deep link flow

## Security Note

The development rules (`match /{document=**}`) are very permissive and should only be used for development and testing. Make sure to switch to the production rules before launching your app.