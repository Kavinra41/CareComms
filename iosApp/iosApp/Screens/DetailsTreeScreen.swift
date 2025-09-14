import SwiftUI
import shared

struct DetailsTreeScreen: View {
    @StateObject private var viewModel = DetailsTreeViewModel(carerId: "carer1")
    @State private var showingError = false
    
    var body: some View {
        NavigationView {
            VStack(spacing: 0) {
                // Loading State
                if viewModel.isLoading {
                    ProgressView("Loading caree data...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    // Caree Selection Header with Tile Layout
                    if viewModel.availableCarees.count > 1 {
                        CareeSelectionTileView(
                            carees: viewModel.availableCarees,
                            selectedCarees: $viewModel.selectedCarees,
                            onCareeToggle: viewModel.toggleCareeSelection
                        )
                        .padding(.horizontal, 20)
                        .padding(.vertical, 16)
                        .background(Color(.systemGray6))
                    }
                    
                    // Tree Content
                    if viewModel.selectedCarees.isEmpty {
                        EmptyTreeSelectionView()
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    } else {
                        TreeContentView(
                            treeData: viewModel.treeData,
                            expandedCategories: $viewModel.expandedCategories,
                            expandedDetails: $viewModel.expandedDetails,
                            onExpandCategory: viewModel.expandCategory,
                            onCollapseCategory: viewModel.collapseCategory,
                            onExpandDetail: viewModel.expandDetail,
                            onCollapseDetail: viewModel.collapseDetail
                        )
                    }
                }
            }
            .navigationTitle("Details Tree")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button("Refresh") {
                        viewModel.refreshData()
                    }
                }
            }
            .alert("Error", isPresented: $showingError) {
                Button("OK") {
                    viewModel.clearError()
                }
            } message: {
                Text(viewModel.error ?? "An unknown error occurred")
            }
            .onChange(of: viewModel.error) { error in
                showingError = error != nil
            }
        }
    }
    

}

// MARK: - Supporting Types
struct TreeNode {
    let id: String
    let title: String
    let type: TreeNodeType
    let children: [TreeNode]
    let data: String?
    
    enum TreeNodeType {
        case caree, category, detail, item
    }
}

// MARK: - Components
struct CareeSelectionTileView: View {
    let carees: [CareeInfo]
    @Binding var selectedCarees: Set<String>
    let onCareeToggle: (String) -> Void
    
    private let columns = [
        GridItem(.adaptive(minimum: 150), spacing: 12)
    ]
    
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Select Carees")
                .font(.headline)
                .fontWeight(.semibold)
            
            LazyVGrid(columns: columns, spacing: 12) {
                ForEach(carees, id: \.id) { caree in
                    CareeTileView(
                        caree: caree,
                        isSelected: selectedCarees.contains(caree.id),
                        onTap: {
                            onCareeToggle(caree.id)
                        }
                    )
                    .accessibilityIdentifier("caree-tile-\(caree.id)")
                }
            }
        }
    }
}

struct CareeTileView: View {
    let caree: CareeInfo
    let isSelected: Bool
    let onTap: () -> Void
    
    var body: some View {
        Button(action: onTap) {
            VStack(spacing: 12) {
                // Avatar Circle
                ZStack {
                    Circle()
                        .fill(
                            LinearGradient(
                                colors: isSelected ? 
                                    [Color(red: 0.4, green: 0.2, blue: 0.6), Color(red: 0.6, green: 0.4, blue: 0.8)] :
                                    [Color(red: 0.8, green: 0.7, blue: 0.9), Color(red: 0.9, green: 0.85, blue: 0.95)],
                                startPoint: .topLeading,
                                endPoint: .bottomTrailing
                            )
                        )
                        .frame(width: 60, height: 60)
                    
                    Text(String(caree.name.prefix(1)))
                        .font(.title2)
                        .fontWeight(.bold)
                        .foregroundColor(isSelected ? .white : Color(red: 0.4, green: 0.2, blue: 0.6))
                    
                    // Selection indicator
                    if isSelected {
                        VStack {
                            HStack {
                                Spacer()
                                Image(systemName: "checkmark.circle.fill")
                                    .foregroundColor(.white)
                                    .background(Color(red: 0.4, green: 0.2, blue: 0.6))
                                    .clipShape(Circle())
                                    .accessibilityIdentifier("checkmark.circle.fill")
                            }
                            Spacer()
                        }
                        .frame(width: 60, height: 60)
                    }
                }
                
                // Caree Info
                VStack(spacing: 4) {
                    Text(caree.name)
                        .font(.subheadline)
                        .fontWeight(.semibold)
                        .foregroundColor(.primary)
                        .multilineTextAlignment(.center)
                        .lineLimit(2)
                    
                    Text("Age \(caree.age)")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    
                    if !caree.healthConditions.isEmpty {
                        Text("\(caree.healthConditions.count) condition\(caree.healthConditions.count == 1 ? "" : "s")")
                            .font(.caption2)
                            .foregroundColor(.secondary)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(Color(.systemGray5))
                            .cornerRadius(8)
                    }
                }
            }
            .padding(16)
            .background(
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color(.systemBackground))
                    .shadow(
                        color: isSelected ? Color(red: 0.4, green: 0.2, blue: 0.6).opacity(0.3) : Color.black.opacity(0.1),
                        radius: isSelected ? 8 : 4,
                        x: 0,
                        y: isSelected ? 4 : 2
                    )
            )
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(
                        isSelected ? Color(red: 0.4, green: 0.2, blue: 0.6) : Color.clear,
                        lineWidth: 2
                    )
            )
            .scaleEffect(isSelected ? 1.02 : 1.0)
            .animation(.easeInOut(duration: 0.2), value: isSelected)
        }
        .buttonStyle(PlainButtonStyle())
    }
}

struct TreeContentView: View {
    let treeData: [TreeNode]
    @Binding var expandedCategories: Set<String>
    @Binding var expandedDetails: Set<String>
    let onExpandCategory: (String) -> Void
    let onCollapseCategory: (String) -> Void
    let onExpandDetail: (String) -> Void
    let onCollapseDetail: (String) -> Void
    
    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(treeData, id: \.id) { node in
                    TreeNodeView(
                        node: node,
                        expandedCategories: $expandedCategories,
                        expandedDetails: $expandedDetails,
                        onExpandCategory: onExpandCategory,
                        onCollapseCategory: onCollapseCategory,
                        onExpandDetail: onExpandDetail,
                        onCollapseDetail: onCollapseDetail,
                        level: 0
                    )
                }
            }
            .padding(.vertical, 16)
        }
    }
}

struct TreeNodeView: View {
    let node: TreeNode
    @Binding var expandedCategories: Set<String>
    @Binding var expandedDetails: Set<String>
    let onExpandCategory: (String) -> Void
    let onCollapseCategory: (String) -> Void
    let onExpandDetail: (String) -> Void
    let onCollapseDetail: (String) -> Void
    let level: Int
    
    private var isExpanded: Bool {
        switch node.type {
        case .category:
            return expandedCategories.contains(node.id)
        case .detail:
            return expandedDetails.contains(node.id)
        default:
            return true
        }
    }
    
    private var hasChildren: Bool {
        !node.children.isEmpty
    }
    
    var body: some View {
        VStack(spacing: 0) {
            // Node Header
            Button(action: {
                toggleExpansion()
            }) {
                HStack(spacing: 12) {
                    // Indentation
                    HStack(spacing: 0) {
                        ForEach(0..<level, id: \.self) { _ in
                            Rectangle()
                                .fill(Color.clear)
                                .frame(width: 20)
                        }
                    }
                    
                    // Expansion Icon
                    if hasChildren {
                        Image(systemName: isExpanded ? "chevron.down" : "chevron.right")
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .frame(width: 12)
                    } else {
                        Rectangle()
                            .fill(Color.clear)
                            .frame(width: 12)
                    }
                    
                    // Node Icon
                    Image(systemName: iconForNodeType(node.type))
                        .font(.subheadline)
                        .foregroundColor(colorForNodeType(node.type))
                        .frame(width: 20)
                    
                    // Node Title
                    Text(node.title)
                        .font(fontForNodeType(node.type))
                        .fontWeight(fontWeightForNodeType(node.type))
                        .foregroundColor(.primary)
                        .multilineTextAlignment(.leading)
                    
                    Spacer()
                    
                    // Data Preview
                    if let data = node.data, !hasChildren {
                        Text(data)
                            .font(.caption)
                            .foregroundColor(.secondary)
                            .lineLimit(1)
                    }
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 12)
                .background(backgroundColorForNodeType(node.type))
            }
            .buttonStyle(PlainButtonStyle())
            .disabled(!hasChildren)
            
            // Children (with iOS-native accordion animation)
            if isExpanded && hasChildren {
                ForEach(node.children, id: \.id) { child in
                    TreeNodeView(
                        node: child,
                        expandedCategories: $expandedCategories,
                        expandedDetails: $expandedDetails,
                        onExpandCategory: onExpandCategory,
                        onCollapseCategory: onCollapseCategory,
                        onExpandDetail: onExpandDetail,
                        onCollapseDetail: onCollapseDetail,
                        level: level + 1
                    )
                }
                .transition(
                    .asymmetric(
                        insertion: .opacity.combined(with: .move(edge: .top)).combined(with: .scale(scale: 0.95)),
                        removal: .opacity.combined(with: .move(edge: .top)).combined(with: .scale(scale: 0.95))
                    )
                )
            }
        }
        .animation(.spring(response: 0.4, dampingFraction: 0.8, blendDuration: 0), value: isExpanded)
    }
    
    private func toggleExpansion() {
        withAnimation(.spring(response: 0.4, dampingFraction: 0.8, blendDuration: 0)) {
            switch node.type {
            case .category:
                if expandedCategories.contains(node.id) {
                    onCollapseCategory(node.id)
                } else {
                    onExpandCategory(node.id)
                }
            case .detail:
                if expandedDetails.contains(node.id) {
                    onCollapseDetail(node.id)
                } else {
                    onExpandDetail(node.id)
                }
            default:
                break
            }
        }
    }
    
    private func iconForNodeType(_ type: TreeNode.TreeNodeType) -> String {
        switch type {
        case .caree:
            return "person.circle.fill"
        case .category:
            return "folder.fill"
        case .detail:
            return "doc.text.fill"
        case .item:
            return "circle.fill"
        }
    }
    
    private func colorForNodeType(_ type: TreeNode.TreeNodeType) -> Color {
        switch type {
        case .caree:
            return Color(red: 0.4, green: 0.2, blue: 0.6) // Deep purple
        case .category:
            return Color(red: 0.6, green: 0.4, blue: 0.8) // Medium purple
        case .detail:
            return Color(red: 0.8, green: 0.7, blue: 0.9) // Light purple
        case .item:
            return Color.secondary
        }
    }
    
    private func fontForNodeType(_ type: TreeNode.TreeNodeType) -> Font {
        switch type {
        case .caree:
            return .headline
        case .category:
            return .subheadline
        case .detail:
            return .subheadline
        case .item:
            return .caption
        }
    }
    
    private func fontWeightForNodeType(_ type: TreeNode.TreeNodeType) -> Font.Weight {
        switch type {
        case .caree:
            return .bold
        case .category:
            return .semibold
        case .detail:
            return .medium
        case .item:
            return .regular
        }
    }
    
    private func backgroundColorForNodeType(_ type: TreeNode.TreeNodeType) -> Color {
        switch type {
        case .caree:
            return Color(red: 0.4, green: 0.2, blue: 0.6).opacity(0.1)
        case .category:
            return Color(red: 0.8, green: 0.7, blue: 0.9).opacity(0.3)
        default:
            return Color.clear
        }
    }
}

struct EmptyTreeSelectionView: View {
    var body: some View {
        VStack(spacing: 20) {
            Image(systemName: "tree")
                .font(.system(size: 60))
                .foregroundColor(Color(red: 0.8, green: 0.7, blue: 0.9))
            
            VStack(spacing: 8) {
                Text("Select Carees")
                    .font(.title2)
                    .fontWeight(.semibold)
                    .foregroundColor(.primary)
                
                Text("Choose one or more carees to explore their detailed information")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
                    .multilineTextAlignment(.center)
            }
        }
        .padding(.horizontal, 40)
    }
}



// MARK: - Preview
#Preview {
    DetailsTreeScreen()
}