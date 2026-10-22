package com.example.intrack.ui.screens.goal

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.ui.graphics.vector.ImageVector

object GoalCategoryHelper {
    fun getCategoryLabel(categoryKey: String): String {
        return when (categoryKey.uppercase()) {
            "TECH" -> "Tech & Gadgets"
            "TRAVEL" -> "Travel & Vacation"
            "EMERGENCY" -> "Emergency Buffer"
            "VEHICLE" -> "Vehicle"
            "HOME" -> "Home & Living"
            "INVESTMENT" -> "Investment"
            else -> categoryKey.replace("_", " ").lowercase().replaceFirstChar { it.titlecase() }
        }
    }

    fun getCategoryIcon(categoryKey: String): ImageVector {
        return when (categoryKey.uppercase()) {
            "TECH" -> Icons.Rounded.LaptopMac
            "TRAVEL" -> Icons.Rounded.Flight
            "EMERGENCY" -> Icons.Rounded.Savings
            "VEHICLE" -> Icons.Rounded.DirectionsCar
            "HOME" -> Icons.Rounded.Home
            "INVESTMENT" -> Icons.AutoMirrored.Rounded.TrendingUp
            else -> Icons.Rounded.TrackChanges
        }
    }

    fun getFundingSourceTitle(sourceKey: String): String {
        return when (sourceKey.uppercase()) {
            "CARD_PRIMARY" -> "Card Primary"
            "ACCOUNT_SECONDARY" -> "Secondary Acct"
            else -> sourceKey.replace("_", " ")
        }
    }

    fun getFundingSourceSubtitle(sourceKey: String): String {
        return when (sourceKey.uppercase()) {
            "CARD_PRIMARY" -> "Visa ••4242"
            "ACCOUNT_SECONDARY" -> "Current ••8890"
            else -> "Linked Account"
        }
    }

    fun getFundingSourceIcon(sourceKey: String): ImageVector {
        return when (sourceKey.uppercase()) {
            "CARD_PRIMARY" -> Icons.Rounded.CreditCard
            "ACCOUNT_SECONDARY" -> Icons.Rounded.AccountBalance
            else -> Icons.Rounded.AccountBalance
        }
    }

    fun getPaceReminderIcon(isActive: Boolean): ImageVector {
        return if (isActive) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff
    }
}
