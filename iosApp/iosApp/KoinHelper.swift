import shared

class KoinHelper {
    static func getAuthUseCase() -> AuthUseCase {
        return KoinHelperKt.getAuthUseCase()
    }
    
    static func getChatUseCase() -> ChatUseCase {
        return KoinHelperKt.getChatUseCase()
    }
    
    static func getInvitationUseCase() -> InvitationUseCase {
        return KoinHelperKt.getInvitationUseCase()
    }
    
    static func createChatViewModel(currentUserId: String) -> ChatViewModel {
        return KoinHelperKt.createChatViewModel(currentUserId: currentUserId)
    }
}