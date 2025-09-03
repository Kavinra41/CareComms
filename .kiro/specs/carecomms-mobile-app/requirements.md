# Requirements Document

## Introduction

CareComms is a Kotlin Multiplatform mobile application designed to facilitate communication and care coordination between professional carers and their care recipients (carees). The app enables carers to monitor, communicate with, and analyze the routines of elderly or disabled individuals under their care, while providing carees with a simple interface to stay connected and share updates.

The application features role-based access with distinct experiences for carers (professional caregivers) and carees (care recipients), emphasizing simplicity and accessibility for users who may not be familiar with mobile technology.

## Requirements

### Requirement 1: User Authentication and Role Management

**User Story:** As a user, I want to register and login with different roles (carer or caree), so that I can access the appropriate interface and features for my needs.

#### Acceptance Criteria

1. WHEN a user opens the app THEN the system SHALL display a splash screen with the app logo for maximum 5 seconds
2. WHEN the splash screen completes THEN the system SHALL display a terms and conditions page
3. WHEN a user accepts terms and conditions THEN the system SHALL display a landing screen with login and signup options
4. WHEN a user selects signup THEN the system SHALL present only carer signup option (caree signup is invitation-only)
5. WHEN a carer registers THEN the system SHALL collect professional documents, age, phone number, and location details
6. WHEN a user provides valid credentials THEN the system SHALL authenticate using Firebase email authentication
7. WHEN authentication succeeds THEN the system SHALL redirect users to their role-appropriate home screen

### Requirement 2: Caree Invitation System

**User Story:** As a carer, I want to invite carees to join the platform through a secure invitation link, so that I can establish care relationships with my clients.

#### Acceptance Criteria

1. WHEN a carer taps the invite button on chat list page THEN the system SHALL generate a unique invitation link with the carer's ID
2. WHEN a carer sends an invitation THEN the system SHALL provide options to share the link via messaging, email, or other apps
3. WHEN a caree opens the invitation link THEN the system SHALL redirect them to a hidden caree signup page
4. WHEN a caree accesses the signup via invitation link THEN the system SHALL pre-populate the carer relationship in the background
5. WHEN a caree completes signup through invitation THEN the system SHALL collect health information and basic personal details
6. WHEN caree signup is successful THEN the system SHALL automatically add the caree to the inviting carer's chat list
7. WHEN a caree logs in after invitation signup THEN the system SHALL direct them to the chat page with their inviting carer

### Requirement 3: Carer Chat Management Interface

**User Story:** As a carer, I want to view and manage conversations with multiple carees, so that I can efficiently coordinate care for all my clients.

#### Acceptance Criteria

1. WHEN a carer logs in THEN the system SHALL display a chat list screen showing all associated carees
2. WHEN a carer views the chat list THEN the system SHALL display each caree's name and last message preview
3. WHEN a carer taps on a caree THEN the system SHALL navigate to the one-on-one chat page for that caree
4. WHEN a carer uses the search function THEN the system SHALL filter the caree list based on the search query
5. WHEN a carer taps the invite button THEN the system SHALL generate and provide sharing options for caree invitation links
6. WHEN a carer navigates using bottom navigation THEN the system SHALL provide access to chat list, profile, data dashboard, and details tree pages
7. WHEN a caree completes invitation signup THEN the system SHALL automatically display the new caree in the carer's chat list

### Requirement 4: Caree Simple Chat Interface

**User Story:** As a caree, I want to have direct access to chat with my assigned carer, so that I can easily communicate updates and respond to check-ins.

#### Acceptance Criteria

1. WHEN a caree logs in THEN the system SHALL navigate directly to the chat page with the carer who invited them
2. WHEN a caree is in the chat interface THEN the system SHALL display the same chat functionality as the carer's chat page
3. WHEN a caree sends a message THEN the system SHALL deliver it to the connected carer in real-time
4. WHEN a caree receives a message THEN the system SHALL display it immediately in the chat interface

### Requirement 5: Real-time Chat Communication

**User Story:** As both a carer and caree, I want to exchange messages in real-time, so that we can maintain effective communication for care coordination.

#### Acceptance Criteria

1. WHEN a user sends a message THEN the system SHALL deliver it to the recipient in real-time
2. WHEN a user receives a message THEN the system SHALL display it immediately in the chat interface
3. WHEN a user is typing THEN the system SHALL show typing indicators to the other participant
4. WHEN messages are sent THEN the system SHALL display delivery and read status indicators
5. WHEN the app is in background THEN the system SHALL send push notifications for new messages

### Requirement 6: Data Dashboard and Analytics

**User Story:** As a carer, I want to view analytics and insights about my carees' patterns and routines, so that I can provide better care based on data-driven insights.

#### Acceptance Criteria

1. WHEN a carer accesses the data dashboard THEN the system SHALL display mock analytics data for carees
2. WHEN a carer selects single or multiple carees THEN the system SHALL filter dashboard data accordingly
3. WHEN viewing analytics THEN the system SHALL present daily, weekly, and bi-weekly data with charts and short notes
4. WHEN data is displayed THEN the system SHALL use visual charts and graphs for easy comprehension
5. IF no real data is available THEN the system SHALL display mock data as placeholder content

### Requirement 7: Details Tree Navigation

**User Story:** As a carer, I want to explore detailed information about my carees in an organized hierarchical structure, so that I can quickly access specific care-related information. This too would be mock data for now since we are not yet collecting data from the chat.

#### Acceptance Criteria

1. WHEN a carer accesses the details tree page THEN the system SHALL display tile-style boxes for all carees
2. WHEN a carer taps on a caree tile THEN the system SHALL expand to show data categories in accordion format
3. WHEN a carer taps on a category THEN the system SHALL expand to show detailed information
4. WHEN a carer taps on a detail item THEN the system SHALL show comprehensive information about that item
5. WHEN navigating the tree structure THEN the system SHALL maintain smooth accordion-style animations
6. IF no real data is available THEN the system SHALL display mock data as placeholder content

### Requirement 8: User Interface and Experience

**User Story:** As a user (especially elderly or technology-unfamiliar), I want a simple, accessible interface with clear navigation, so that I can use the app effectively without confusion.

#### Acceptance Criteria

1. WHEN the app displays any interface THEN the system SHALL use a minimal design with deep purple, light purple, and white color scheme
2. WHEN users navigate between screens THEN the system SHALL provide smooth transitions and appropriate animations
3. WHEN displaying content THEN the system SHALL ensure proper spacing without padding or margin errors
4. WHEN users interact with elements THEN the system SHALL provide clear visual feedback and professional appearance
5. WHEN elderly users access the app THEN the system SHALL present large, clear text and intuitive navigation patterns
6. WHEN users need to logout THEN the system SHALL provide easily accessible logout functionality appropriate to their role

### Requirement 9: Cross-Platform Compatibility

**User Story:** As a user, I want to access the app on both iOS and Android devices, so that I can use it regardless of my mobile platform preference.

#### Acceptance Criteria

1. WHEN the app is built THEN the system SHALL support both iOS and Android platforms using Kotlin Multiplatform
2. WHEN running on different platforms THEN the system SHALL maintain consistent functionality and appearance
3. WHEN platform-specific features are needed THEN the system SHALL implement appropriate native integrations
4. WHEN users switch between devices THEN the system SHALL maintain data synchronization across platforms