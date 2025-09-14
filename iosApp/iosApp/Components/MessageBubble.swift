import SwiftUI
import shared

struct MessageBubble: View {
    let message: Message
    let isCurrentUser: Bool
    let currentUserId: String
    
    private var isFromCurrentUser: Bool {
        message.senderId == currentUserId
    }
    
    private var bubbleColor: Color {
        isFromCurrentUser ? Color(red: 0.4, green: 0.2, blue: 0.6) : Color(.systemGray5)
    }
    
    private var textColor: Color {
        isFromCurrentUser ? .white : .primary
    }
    
    private var alignment: HorizontalAlignment {
        isFromCurrentUser ? .trailing : .leading
    }
    
    var body: some View {
        VStack(alignment: alignment, spacing: 4) {
            HStack {
                if isFromCurrentUser {
                    Spacer(minLength: 60)
                }
                
                VStack(alignment: isFromCurrentUser ? .trailing : .leading, spacing: 2) {
                    Text(message.content)
                        .font(.body)
                        .foregroundColor(textColor)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)
                        .background(bubbleColor)
                        .clipShape(RoundedRectangle(cornerRadius: 18))
                    
                    HStack(spacing: 4) {
                        Text(formatTimestamp(message.timestamp))
                            .font(.caption2)
                            .foregroundColor(.secondary)
                        
                        if isFromCurrentUser {
                            MessageStatusView(status: message.status)
                        }
                    }
                }
                
                if !isFromCurrentUser {
                    Spacer(minLength: 60)
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 2)
    }
    
    private func formatTimestamp(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
        let formatter = DateFormatter()
        
        if Calendar.current.isDateInToday(date) {
            formatter.timeStyle = .short
            return formatter.string(from: date)
        } else if Calendar.current.isDateInYesterday(date) {
            return "Yesterday"
        } else {
            formatter.dateStyle = .short
            return formatter.string(from: date)
        }
    }
}

struct MessageStatusView: View {
    let status: MessageStatus
    
    var body: some View {
        Group {
            switch status {
            case .sent:
                Image(systemName: "checkmark")
                    .font(.caption2)
                    .foregroundColor(.secondary)
            case .delivered:
                Image(systemName: "checkmark.circle")
                    .font(.caption2)
                    .foregroundColor(.secondary)
            case .read:
                Image(systemName: "checkmark.circle.fill")
                    .font(.caption2)
                    .foregroundColor(.blue)
            default:
                EmptyView()
            }
        }
    }
}

#Preview {
    VStack(spacing: 8) {
        MessageBubble(
            message: Message(
                id: "1",
                senderId: "user1",
                content: "Hello! How are you doing today?",
                timestamp: Int64(Date().timeIntervalSince1970 * 1000),
                status: .read,
                type: .text
            ),
            isCurrentUser: false,
            currentUserId: "user2"
        )
        
        MessageBubble(
            message: Message(
                id: "2",
                senderId: "user2",
                content: "I'm doing well, thank you for asking! How about you?",
                timestamp: Int64(Date().timeIntervalSince1970 * 1000),
                status: .delivered,
                type: .text
            ),
            isCurrentUser: true,
            currentUserId: "user2"
        )
    }
    .padding()
}