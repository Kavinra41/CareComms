import SwiftUI
import shared
import Charts

struct DashboardScreen: View {
    @StateObject private var viewModel: DashboardViewModel
    @State private var selectedCarees: Set<String> = []
    @State private var selectedPeriod: AnalyticsPeriodSwift = .daily
    
    init(carerId: String) {
        self._viewModel = StateObject(wrappedValue: DashboardViewModel(carerId: carerId))
    }
    
    var body: some View {
        NavigationView {
            ScrollView {
                VStack(spacing: 24) {
                    // Error Display
                    if let error = viewModel.error {
                        ErrorBanner(message: error) {
                            viewModel.clearError()
                        }
                        .padding(.horizontal, 20)
                    }
                    
                    // Caree Selection
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Select Carees")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 20)
                        
                        if viewModel.state.isLoadingCarees {
                            ProgressView("Loading carees...")
                                .frame(maxWidth: .infinity)
                                .padding(.horizontal, 20)
                        } else {
                            ScrollView(.horizontal, showsIndicators: false) {
                                HStack(spacing: 12) {
                                    ForEach(viewModel.state.availableCarees) { caree in
                                        CareeSelectionChip(
                                            caree: caree,
                                            isSelected: viewModel.state.selectedCareeIds.contains(caree.id)
                                        ) {
                                            toggleCareeSelection(caree.id)
                                        }
                                        .accessibilityIdentifier("caree-chip-\(caree.id)")
                                    }
                                }
                                .padding(.horizontal, 20)
                            }
                        }
                    }
                    
                    // Period Selection
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Time Period")
                            .font(.headline)
                            .fontWeight(.semibold)
                            .padding(.horizontal, 20)
                        
                        Picker("Period", selection: $selectedPeriod) {
                            ForEach(AnalyticsPeriodSwift.allCases, id: \.self) { period in
                                Text(period.rawValue).tag(period)
                            }
                        }
                        .pickerStyle(SegmentedPickerStyle())
                        .padding(.horizontal, 20)
                        .onChange(of: selectedPeriod) { newPeriod in
                            viewModel.changePeriod(newPeriod)
                        }
                    }
                    
                    // Analytics Content
                    if viewModel.state.isLoadingAnalytics {
                        LoadingView()
                            .frame(height: 200)
                    } else if viewModel.state.selectedCareeIds.isEmpty {
                        EmptySelectionView()
                            .frame(height: 200)
                    } else if let analyticsData = viewModel.state.analyticsData {
                        // Analytics Charts
                        VStack(spacing: 20) {
                            // Activity Chart
                            AnalyticsCard(title: "Activity Levels") {
                                ActivityChart(
                                    data: analyticsData,
                                    period: viewModel.state.selectedPeriod
                                )
                            }
                            
                            // Communication Chart
                            AnalyticsCard(title: "Communication Frequency") {
                                CommunicationChart(
                                    data: analyticsData,
                                    period: viewModel.state.selectedPeriod
                                )
                            }
                            
                            // Notes Section
                            AnalyticsCard(title: "Recent Notes") {
                                NotesSection(notes: analyticsData.notes)
                            }
                        }
                        .padding(.horizontal, 20)
                    }
                    
                    Spacer(minLength: 20)
                }
            }
            .navigationTitle("Dashboard")
            .navigationBarTitleDisplayMode(.large)
            .refreshable {
                viewModel.refreshData()
            }
        }
    }
    
    private func toggleCareeSelection(_ careeId: String) {
        if viewModel.state.selectedCareeIds.contains(careeId) {
            viewModel.deselectCaree(careeId)
        } else {
            viewModel.selectCaree(careeId)
        }
    }
}

// MARK: - Supporting Types

// MARK: - Components
struct CareeSelectionChip: View {
    let caree: CareeInfoSwift
    let isSelected: Bool
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            HStack(spacing: 8) {
                Circle()
                    .fill(isSelected ? Color(red: 0.4, green: 0.2, blue: 0.6) : Color(red: 0.8, green: 0.7, blue: 0.9))
                    .frame(width: 24, height: 24)
                    .overlay(
                        Text(String(caree.name.prefix(1)))
                            .font(.caption)
                            .fontWeight(.semibold)
                            .foregroundColor(isSelected ? .white : Color(red: 0.4, green: 0.2, blue: 0.6))
                    )
                
                VStack(alignment: .leading, spacing: 2) {
                    Text(caree.name)
                        .font(.subheadline)
                        .fontWeight(.medium)
                        .foregroundColor(isSelected ? .white : .primary)
                    
                    Text("Age \(caree.age)")
                        .font(.caption)
                        .foregroundColor(isSelected ? .white.opacity(0.8) : .secondary)
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 10)
            .background(
                isSelected ? Color(red: 0.4, green: 0.2, blue: 0.6) : Color(.systemGray6)
            )
            .cornerRadius(20)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct AnalyticsCard<Content: View>: View {
    let title: String
    let content: Content
    
    init(title: String, @ViewBuilder content: () -> Content) {
        self.title = title
        self.content = content()
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text(title)
                .font(.headline)
                .fontWeight(.semibold)
            
            content
        }
        .padding(20)
        .background(Color(.systemBackground))
        .cornerRadius(12)
        .shadow(color: .black.opacity(0.05), radius: 5, x: 0, y: 2)
    }
}

struct ActivityChart: View {
    let data: AnalyticsDataSwift
    let period: AnalyticsPeriodSwift
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            switch period {
            case .daily:
                if #available(iOS 16.0, *) {
                    Chart(data.dailyData) { dataPoint in
                        BarMark(
                            x: .value("Date", dataPoint.date),
                            y: .value("Activity", dataPoint.activityLevel)
                        )
                        .foregroundStyle(Color(red: 0.4, green: 0.2, blue: 0.6))
                    }
                    .frame(height: 150)
                    .chartYAxis {
                        AxisMarks(position: .leading)
                    }
                } else {
                    SimpleBarChart(
                        data: data.dailyData.map { ($0.date, Double($0.activityLevel)) },
                        title: "Daily Activity"
                    )
                }
                
            case .weekly:
                if #available(iOS 16.0, *) {
                    Chart(data.weeklyData) { dataPoint in
                        BarMark(
                            x: .value("Week", "\(dataPoint.weekStart) - \(dataPoint.weekEnd)"),
                            y: .value("Activity", dataPoint.averageActivityLevel)
                        )
                        .foregroundStyle(Color(red: 0.4, green: 0.2, blue: 0.6))
                    }
                    .frame(height: 150)
                    .chartYAxis {
                        AxisMarks(position: .leading)
                    }
                } else {
                    SimpleBarChart(
                        data: data.weeklyData.enumerated().map { (index, metric) in
                            ("Week \(index + 1)", Double(metric.averageActivityLevel))
                        },
                        title: "Weekly Activity"
                    )
                }
                
            case .biweekly, .monthly:
                if #available(iOS 16.0, *) {
                    Chart(data.biweeklyData) { dataPoint in
                        BarMark(
                            x: .value("Period", "\(dataPoint.periodStart) - \(dataPoint.periodEnd)"),
                            y: .value("Activity", dataPoint.averageActivityLevel)
                        )
                        .foregroundStyle(Color(red: 0.4, green: 0.2, blue: 0.6))
                    }
                    .frame(height: 150)
                    .chartYAxis {
                        AxisMarks(position: .leading)
                    }
                } else {
                    SimpleBarChart(
                        data: data.biweeklyData.enumerated().map { (index, metric) in
                            ("Period \(index + 1)", Double(metric.averageActivityLevel))
                        },
                        title: "Bi-weekly Activity"
                    )
                }
            }
            
            Text("Activity levels range from 1 (low) to 10 (high)")
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

struct CommunicationChart: View {
    let data: AnalyticsDataSwift
    let period: AnalyticsPeriodSwift
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            switch period {
            case .daily:
                if #available(iOS 16.0, *) {
                    Chart(data.dailyData) { dataPoint in
                        LineMark(
                            x: .value("Date", dataPoint.date),
                            y: .value("Messages", dataPoint.communicationCount)
                        )
                        .foregroundStyle(Color(red: 0.4, green: 0.2, blue: 0.6))
                        .lineStyle(StrokeStyle(lineWidth: 3))
                        
                        PointMark(
                            x: .value("Date", dataPoint.date),
                            y: .value("Messages", dataPoint.communicationCount)
                        )
                        .foregroundStyle(Color(red: 0.4, green: 0.2, blue: 0.6))
                    }
                    .frame(height: 150)
                    .chartYAxis {
                        AxisMarks(position: .leading)
                    }
                } else {
                    SimpleLineChart(
                        data: data.dailyData.map { ($0.date, Double($0.communicationCount)) },
                        title: "Daily Communication"
                    )
                }
                
            case .weekly:
                if #available(iOS 16.0, *) {
                    Chart(data.weeklyData) { dataPoint in
                        LineMark(
                            x: .value("Week", "\(dataPoint.weekStart) - \(dataPoint.weekEnd)"),
                            y: .value("Messages", dataPoint.totalCommunications)
                        )
                        .foregroundStyle(Color(red: 0.4, green: 0.2, blue: 0.6))
                        .lineStyle(StrokeStyle(lineWidth: 3))
                        
                        PointMark(
                            x: .value("Week", "\(dataPoint.weekStart) - \(dataPoint.weekEnd)"),
                            y: .value("Messages", dataPoint.totalCommunications)
                        )
                        .foregroundStyle(Color(red: 0.4, green: 0.2, blue: 0.6))
                    }
                    .frame(height: 150)
                    .chartYAxis {
                        AxisMarks(position: .leading)
                    }
                } else {
                    SimpleLineChart(
                        data: data.weeklyData.enumerated().map { (index, metric) in
                            ("Week \(index + 1)", Double(metric.totalCommunications))
                        },
                        title: "Weekly Communication"
                    )
                }
                
            case .biweekly, .monthly:
                if #available(iOS 16.0, *) {
                    Chart(data.biweeklyData) { dataPoint in
                        LineMark(
                            x: .value("Period", "\(dataPoint.periodStart) - \(dataPoint.periodEnd)"),
                            y: .value("Messages", dataPoint.totalCommunications)
                        )
                        .foregroundStyle(Color(red: 0.4, green: 0.2, blue: 0.6))
                        .lineStyle(StrokeStyle(lineWidth: 3))
                        
                        PointMark(
                            x: .value("Period", "\(dataPoint.periodStart) - \(dataPoint.periodEnd)"),
                            y: .value("Messages", dataPoint.totalCommunications)
                        )
                        .foregroundStyle(Color(red: 0.4, green: 0.2, blue: 0.6))
                    }
                    .frame(height: 150)
                    .chartYAxis {
                        AxisMarks(position: .leading)
                    }
                } else {
                    SimpleLineChart(
                        data: data.biweeklyData.enumerated().map { (index, metric) in
                            ("Period \(index + 1)", Double(metric.totalCommunications))
                        },
                        title: "Bi-weekly Communication"
                    )
                }
            }
            
            Text("Number of messages exchanged")
                .font(.caption)
                .foregroundColor(.secondary)
        }
    }
}

struct NotesSection: View {
    let notes: [AnalyticsNoteSwift]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            if notes.isEmpty {
                Text("No notes available")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding(.vertical, 20)
            } else {
                ForEach(notes.indices, id: \.self) { index in
                    let note = notes[index]
                    VStack(alignment: .leading, spacing: 4) {
                        HStack {
                            Text(formatTimestamp(note.timestamp))
                                .font(.caption)
                                .foregroundColor(.secondary)
                            
                            Spacer()
                            
                            Text(note.type.capitalized)
                                .font(.caption)
                                .padding(.horizontal, 8)
                                .padding(.vertical, 2)
                                .background(Color(red: 0.8, green: 0.7, blue: 0.9))
                                .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
                                .cornerRadius(8)
                        }
                        
                        Text(note.content)
                            .font(.subheadline)
                            .foregroundColor(.primary)
                    }
                    .padding(.vertical, 8)
                    
                    if index < notes.count - 1 {
                        Divider()
                    }
                }
            }
        }
    }
    
    private func formatTimestamp(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(timestamp / 1000))
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}

struct EmptySelectionView: View {
    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "chart.bar.xaxis")
                .font(.system(size: 50))
                .foregroundColor(Color(red: 0.8, green: 0.7, blue: 0.9))
            
            VStack(spacing: 8) {
                Text("Select Carees")
                    .font(.headline)
                    .fontWeight(.semibold)
                
                Text("Choose one or more carees to view their analytics")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(.horizontal, 40)
    }
}

// MARK: - Error Banner
struct ErrorBanner: View {
    let message: String
    let onDismiss: () -> Void
    
    var body: some View {
        HStack {
            Image(systemName: "exclamationmark.triangle.fill")
                .foregroundColor(.orange)
            
            Text(message)
                .font(.subheadline)
                .foregroundColor(.primary)
                .multilineTextAlignment(.leading)
            
            Spacer()
            
            Button("Dismiss") {
                onDismiss()
            }
            .font(.caption)
            .foregroundColor(Color(red: 0.4, green: 0.2, blue: 0.6))
        }
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
}

// MARK: - Fallback Charts for iOS 15
struct SimpleBarChart: View {
    let data: [(String, Double)]
    let title: String
    
    var body: some View {
        VStack {
            Text(title)
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            HStack(alignment: .bottom, spacing: 8) {
                ForEach(data.indices, id: \.self) { index in
                    let (label, value) = data[index]
                    VStack(spacing: 4) {
                        Rectangle()
                            .fill(Color(red: 0.4, green: 0.2, blue: 0.6))
                            .frame(width: 30, height: CGFloat(max(value * 15, 5)))
                        
                        Text(label)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
            }
        }
        .frame(height: 150)
    }
}

struct SimpleLineChart: View {
    let data: [(String, Double)]
    let title: String
    
    var body: some View {
        VStack {
            Text(title)
                .font(.subheadline)
                .foregroundColor(.secondary)
            
            HStack(alignment: .bottom, spacing: 8) {
                ForEach(data.indices, id: \.self) { index in
                    let (label, value) = data[index]
                    VStack(spacing: 4) {
                        Circle()
                            .fill(Color(red: 0.4, green: 0.2, blue: 0.6))
                            .frame(width: 8, height: 8)
                            .offset(y: -CGFloat(max(value * 8, 5)))
                        
                        Text(label)
                            .font(.caption2)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
            }
        }
        .frame(height: 150)
    }
}

// MARK: - Loading View
struct LoadingView: View {
    var body: some View {
        VStack(spacing: 16) {
            ProgressView()
                .scaleEffect(1.2)
            
            Text("Loading analytics...")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
    }
}

// MARK: - Preview
#Preview {
    DashboardScreen(carerId: "test-carer-id")
}