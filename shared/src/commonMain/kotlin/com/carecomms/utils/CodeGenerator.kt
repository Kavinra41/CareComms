package com.carecomms.utils

import kotlin.random.Random

object CodeGenerator {
    
    /**
     * Generates a consistent invitation code for a carer based on their ID
     * This ensures the same carer always gets the same code
     */
    fun generateCarerCode(carerId: String): String {
        // Use carer ID as seed for consistent code generation
        val seed = carerId.hashCode().toLong()
        val random = Random(seed)
        
        // Generate 6-character alphanumeric code
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val code = (1..6)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
        
        println("CodeGenerator: Generated code '$code' for carer ID: $carerId")
        return code
    }
    
    /**
     * Generates deep link for invitation
     */
    fun generateDeepLink(invitationCode: String): String {
        return "carecomms://invite/$invitationCode"
    }
}