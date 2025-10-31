package com.carecomms.android.utils

import android.content.Intent
import android.net.Uri

object DeepLinkHandler {
    
    fun extractInvitationCode(intent: Intent?): String? {
        return when {
            intent?.action == Intent.ACTION_VIEW -> {
                val uri = intent.data
                println("DeepLinkHandler: Processing URI: $uri")
                val code = extractCodeFromUri(uri)
                println("DeepLinkHandler: Extracted code: $code")
                code
            }
            else -> {
                println("DeepLinkHandler: Intent action is not ACTION_VIEW: ${intent?.action}")
                null
            }
        }
    }
    
    private fun extractCodeFromUri(uri: Uri?): String? {
        println("DeepLinkHandler: URI details - scheme: ${uri?.scheme}, host: ${uri?.host}, path: ${uri?.path}, pathSegments: ${uri?.pathSegments}")
        
        return when {
            uri?.scheme == "carecomms" && uri.host == "invite" -> {
                // carecomms://invite/ABC123 - the code is in the path after /
                val code = uri.path?.removePrefix("/")?.takeIf { it.isNotBlank() }
                println("DeepLinkHandler: Custom scheme code: $code")
                code
            }
            uri?.scheme == "https" && uri.host == "carecomms.app" -> {
                // https://carecomms.app/invite/ABC123
                val code = uri.pathSegments?.getOrNull(1)
                println("DeepLinkHandler: HTTPS scheme code: $code")
                code
            }
            else -> {
                println("DeepLinkHandler: No matching scheme/host pattern")
                null
            }
        }
    }
    
    fun isInvitationLink(intent: Intent?): Boolean {
        return extractInvitationCode(intent) != null
    }
}