import SwiftUI

struct TypingIndicator: View {
    @State private var animationOffset: CGFloat = 0
    
    var body: some View {
        HStack(spacing: 4) {
            ForEach(0..<3) { index in
                Circle()
                    .fill(Color.secondary)
                    .frame(width: 8, height: 8)
                    .scaleEffect(animationOffset == CGFloat(index) ? 1.2 : 0.8)
                    .animation(
                        Animation.easeInOut(duration: 0.6)
                            .repeatForever()
                            .delay(Double(index) * 0.2),
                        value: animationOffset
                    )
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
        .background(Color(.systemGray5))
        .clipShape(RoundedRectangle(cornerRadius: 18))
        .padding(.horizontal, 16)
        .padding(.vertical, 2)
        .onAppear {
            animationOffset = 2
        }
    }
}

struct TypingIndicatorContainer: View {
    let isVisible: Bool
    
    var body: some View {
        if isVisible {
            HStack {
                TypingIndicator()
                Spacer(minLength: 60)
            }
            .transition(.opacity.combined(with: .move(edge: .leading)))
        }
    }
}

#Preview {
    VStack {
        TypingIndicatorContainer(isVisible: true)
        Spacer()
    }
}