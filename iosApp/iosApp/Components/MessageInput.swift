import SwiftUI

struct MessageInput: View {
    @Binding var message: String
    let onSend: () -> Void
    let isSending: Bool
    
    @FocusState private var isTextFieldFocused: Bool
    
    private var canSend: Bool {
        !message.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !isSending
    }
    
    var body: some View {
        HStack(spacing: 12) {
            TextField("Type a message...", text: $message, axis: .vertical)
                .textFieldStyle(.plain)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(.systemGray6))
                .clipShape(RoundedRectangle(cornerRadius: 20))
                .focused($isTextFieldFocused)
                .lineLimit(1...4)
                .onSubmit {
                    if canSend {
                        onSend()
                    }
                }
            
            Button(action: onSend) {
                Group {
                    if isSending {
                        ProgressView()
                            .scaleEffect(0.8)
                    } else {
                        Image(systemName: "arrow.up.circle.fill")
                            .font(.title2)
                    }
                }
                .foregroundColor(canSend ? Color(red: 0.4, green: 0.2, blue: 0.6) : .secondary)
            }
            .disabled(!canSend)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
        .background(Color(.systemBackground))
    }
}

#Preview {
    VStack {
        Spacer()
        MessageInput(
            message: .constant("Hello world"),
            onSend: {},
            isSending: false
        )
    }
}