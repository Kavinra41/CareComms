# Chat System Troubleshooting Guide

## How the Chat System Works

1. **Relationships First**: Users must be connected through invitation codes before they can chat
2. **Automatic Chat Creation**: Chat rooms are created automatically when users try to message each other
3. **Real-time Updates**: Chat lists and messages update in real-time using Firestore listeners

## Step-by-Step Testing Process

### 1. Create Carer Account
1. Register as a Carer
2. Complete the registration process
3. You should see the Dashboard

### 2. Generate Invitation Code
1. Go to Dashboard
2. Click "Invite" button
3. Note down the generated invitation code (e.g., "ABC123")
4. Optionally send email invitation

### 3. Create Caree Account
1. Register as Care Recipient
2. Enter the invitation code from step 2
3. Complete registration
4. Account should be created and connected

### 4. Verify Connection
1. **Carer Dashboard**: Should show the caree in "Your Care Recipients"
2. **Caree Dashboard**: Should show the carer in "Your Care Team"
3. **Messages Tab**: Both users should see each other in the chat list

### 5. Start Chatting
1. Go to Messages tab
2. Click on the connected user
3. Chat room should open
4. Send a message
5. Other user should see the message in real-time

## Common Issues and Solutions

### Issue: "No conversations yet" in Messages tab

**Possible Causes:**
1. Users are not connected through invitation codes
2. Firestore rules are blocking access
3. Network connectivity issues

**Solutions:**
1. Verify invitation code was used during registration
2. Check Firestore rules (see FIRESTORE_RULES_SETUP.md)
3. Try refreshing the Messages tab
4. Check internet connection

### Issue: Chat list shows users but clicking doesn't work

**Possible Causes:**
1. Chat room creation is failing
2. User authentication issues
3. Firestore permission issues

**Solutions:**
1. Check logs for chat creation errors
2. Verify user is authenticated
3. Update Firestore rules

### Issue: Messages not appearing in real-time

**Possible Causes:**
1. Firestore listeners not working
2. Network issues
3. App backgrounded (Android battery optimization)

**Solutions:**
1. Check network connection
2. Refresh the chat
3. Check Android battery optimization settings

## Debug Information

The app logs detailed information about:
- User relationships loading
- Chat preview generation
- Chat room creation
- Message sending/receiving

Check logcat for specific error messages.

## Firestore Collections Structure

```
carer_invitations/
  {invitationCode}/
    carerId: string
    carerName: string
    carerEmail: string
    invitationCode: string
    createdAt: timestamp

carer_caree_relationships/
  {carerId}_{careeId}/
    id: string
    carerId: string
    careeId: string
    carerName: string
    careeName: string
    invitationCode: string
    status: "ACTIVE"
    createdAt: timestamp

chat_rooms/
  {userId1}_{userId2}/
    participants: [userId1, userId2]
    participantNames: {userId1: "Name1", userId2: "Name2"}
    lastMessage: string
    lastMessageTimestamp: timestamp
    createdAt: timestamp
    lastActivity: timestamp

chat_rooms/{chatId}/messages/
  {messageId}/
    senderId: string
    senderName: string
    content: string
    timestamp: timestamp
    status: "SENT" | "READ"
```

## Testing Checklist

- [ ] Carer can register successfully
- [ ] Carer can generate invitation codes
- [ ] Caree can register with invitation code
- [ ] Dashboard shows connected users
- [ ] Messages tab shows connected users
- [ ] Can open individual chats
- [ ] Can send messages
- [ ] Messages appear in real-time
- [ ] Chat list updates with latest messages