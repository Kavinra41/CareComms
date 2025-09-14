import SwiftUI

/**
 * Accessibility settings screen for iOS
 */
struct AccessibilitySettingsScreen: View {
    @Environment(\.presentationMode) var presentationMode
    @StateObject private var accessibilityHelper = AccessibilityHelper()
    
    // Local state for settings (in a real app, this would be connected to the Kotlin shared module)
    @State private var textScale: Double = 1.0
    @State private var isHighContrastEnabled = false
    @State private var isLargeTouchTargetsEnabled = true
    @State private var isReducedMotionEnabled = false
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(alignment: .leading, spacing: 24) {
                    
                    // Text Size Section
                    VStack(alignment: .leading, spacing: 16) {
                        Text("Text Size")
                            .font(.headline)
                            .accessibilityAddTraits(.isHeader)
                        
                        Text("Adjust text size for better readability")
                            .font(.caption)
                            .foregroundColor(.secondary)
                        
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Current size: \(Int(textScale * 100))%")
                                .font(.caption)
                            
                            Slider(
                                value: $textScale,
                                in: AccessibilityConstants.minTextScale...AccessibilityConstants.maxTextScale,
                                step: 0.1
                            ) {
                                Text("Text Size")
                            } minimumValueLabel: {
                                Text("Small")
                                    .font(.caption2)
                            } maximumValueLabel: {
                                Text("Large")
                                    .font(.caption2)
                            }
                            .accessibilityLabel("Text size slider")
                            .accessibilityValue("\(Int(textScale * 100)) percent")
                        }
                    }
                    .padding()
                    .background(Color(.systemBackground))
                    .cornerRadius(12)
                    .shadow(radius: 2)
                    
                    // High Contrast Section
                    VStack(alignment: .leading, spacing: 16) {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("High Contrast")
                                    .font(.headline)
                                
                                Text("Increase contrast for better visibility")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            Toggle("", isOn: $isHighContrastEnabled)
                                .accessibilityLabel("High contrast")
                                .accessibilityValue(isHighContrastEnabled ? "enabled" : "disabled")
                        }
                    }
                    .padding()
                    .background(Color(.systemBackground))
                    .cornerRadius(12)
                    .shadow(radius: 2)
                    
                    // Large Touch Targets Section
                    VStack(alignment: .leading, spacing: 16) {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Large Touch Targets")
                                    .font(.headline)
                                
                                Text("Make buttons and interactive elements larger")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            Toggle("", isOn: $isLargeTouchTargetsEnabled)
                                .accessibilityLabel("Large touch targets")
                                .accessibilityValue(isLargeTouchTargetsEnabled ? "enabled" : "disabled")
                        }
                    }
                    .padding()
                    .background(Color(.systemBackground))
                    .cornerRadius(12)
                    .shadow(radius: 2)
                    
                    // Reduced Motion Section
                    VStack(alignment: .leading, spacing: 16) {
                        HStack {
                            VStack(alignment: .leading, spacing: 4) {
                                Text("Reduce Motion")
                                    .font(.headline)
                                
                                Text("Minimize animations and transitions")
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            
                            Spacer()
                            
                            Toggle("", isOn: $isReducedMotionEnabled)
                                .accessibilityLabel("Reduced motion")
                                .accessibilityValue(isReducedMotionEnabled ? "enabled" : "disabled")
                        }
                    }
                    .padding()
                    .background(Color(.systemBackground))
                    .cornerRadius(12)
                    .shadow(radius: 2)
                    
                    // System Accessibility Status
                    VStack(alignment: .leading, spacing: 16) {
                        Text("System Accessibility Status")
                            .font(.headline)
                            .accessibilityAddTraits(.isHeader)
                        
                        VStack(alignment: .leading, spacing: 8) {
                            AccessibilityStatusRow(
                                title: "VoiceOver",
                                isEnabled: accessibilityHelper.isVoiceOverRunning
                            )
                            
                            AccessibilityStatusRow(
                                title: "Switch Control",
                                isEnabled: accessibilityHelper.isSwitchControlRunning
                            )
                            
                            AccessibilityStatusRow(
                                title: "Reduce Motion",
                                isEnabled: accessibilityHelper.isReduceMotionEnabled
                            )
                            
                            AccessibilityStatusRow(
                                title: "Reduce Transparency",
                                isEnabled: accessibilityHelper.isReduceTransparencyEnabled
                            )
                            
                            AccessibilityStatusRow(
                                title: "Darker System Colors",
                                isEnabled: accessibilityHelper.isDarkerSystemColorsEnabled
                            )
                        }
                    }
                    .padding()
                    .background(Color(.systemBackground))
                    .cornerRadius(12)
                    .shadow(radius: 2)
                    
                    // Reset Button
                    AccessibleButton(
                        title: "Reset to Defaults",
                        action: resetToDefaults,
                        contentDescription: "Reset all accessibility settings to default values",
                        style: .secondary
                    )
                    .padding(.top, 16)
                }
                .padding()
            }
            .navigationTitle("Accessibility Settings")
            .navigationBarTitleDisplayMode(.large)
            .navigationBarBackButtonHidden(true)
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    AccessibleIconButton(
                        systemName: "arrow.left",
                        action: {
                            presentationMode.wrappedValue.dismiss()
                        },
                        contentDescription: AccessibilityConstants.ContentDescriptions.backButton
                    )
                }
            }
        }
        .dynamicTypeSupport(textScale: CGFloat(textScale))
        .highContrastSupport()
        .reducedMotionSupport()
    }
    
    private func resetToDefaults() {
        textScale = AccessibilityConstants.defaultTextScale
        isHighContrastEnabled = false
        isLargeTouchTargetsEnabled = true
        isReducedMotionEnabled = false
    }
}

/**
 * Row component for displaying accessibility status
 */
struct AccessibilityStatusRow: View {
    let title: String
    let isEnabled: Bool
    
    var body: some View {
        HStack {
            Text(title)
                .font(.body)
            
            Spacer()
            
            Image(systemName: isEnabled ? "checkmark.circle.fill" : "circle")
                .foregroundColor(isEnabled ? .green : .gray)
                .accessibilityLabel(isEnabled ? "Enabled" : "Disabled")
        }
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(title), \(isEnabled ? "enabled" : "disabled")")
    }
}

// MARK: - Preview

struct AccessibilitySettingsScreen_Previews: PreviewProvider {
    static var previews: some View {
        AccessibilitySettingsScreen()
            .previewLayout(.sizeThatFits)
    }
}