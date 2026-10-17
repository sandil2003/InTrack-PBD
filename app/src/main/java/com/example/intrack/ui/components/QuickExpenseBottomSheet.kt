package com.example.intrack.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.intrack.data.model.ExpenseEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickExpenseBottomSheet(
    onDismiss: () -> Unit,
    onSubmitExpense: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var selectedPreset by remember { mutableStateOf<String?>(null) }
    var commitmentTypeIndex by remember { mutableIntStateOf(1) }
    var selectedAccountIndex by remember { mutableIntStateOf(0) }
    val focusManager = LocalFocusManager.current

    val presets = listOf(
        "☕ Coffee" to ("DINING_OUT" to "DISCRETIONARY"),
        "🚗 PickMe" to ("TRANSPORT" to "COMMITTED"),
        "🍔 Takeout" to ("DINING_OUT" to "DISCRETIONARY"),
        "🛒 Groceries" to ("MISC" to "COMMITTED"),
        "🏠 Rent & Utilities" to ("RENT" to "COMMITTED"),
        "📦 Snacks" to ("DINING_OUT" to "DISCRETIONARY")
    )

    val accounts = listOf("Card", "Secondary", "Cash")
    val accountKeys = listOf("CARD_PRIMARY", "ACCOUNT_SECONDARY", "CASH")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "Log Expense",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Numeric Display for Amount
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Amount (LKR)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                textStyle = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // One-Tap Presets
            Text(
                text = "Quick Presets",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                presets.forEach { (presetName, config) ->
                    val isSelected = selectedPreset == presetName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPreset = presetName
                            title = presetName.substringAfter(" ")
                            commitmentTypeIndex = if (config.second == "COMMITTED") 0 else 1
                        },
                        label = { Text(presetName, style = MaterialTheme.typography.labelSmall) },
                        shape = RoundedCornerShape(8.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Custom Title Field
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title / Note") },
                placeholder = { Text("e.g. Afternoon Coffee") },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Commitment Segmented Row
            Text(
                text = "Obligation Type",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = commitmentTypeIndex == 0,
                    onClick = { commitmentTypeIndex = 0 },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Essential (Fixed)", style = MaterialTheme.typography.labelSmall)
                }
                SegmentedButton(
                    selected = commitmentTypeIndex == 1,
                    onClick = { commitmentTypeIndex = 1 },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Lifestyle (Discretionary)", style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Account Selector
            Text(
                text = "Payment Account",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                accounts.forEachIndexed { index, account ->
                    SegmentedButton(
                        selected = selectedAccountIndex == index,
                        onClick = { selectedAccountIndex = index },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = accounts.size)
                    ) {
                        Text(account, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            val amountValue = amountText.toDoubleOrNull() ?: 0.0
            val isFormValid = amountValue > 0 && title.isNotEmpty()

            Button(
                onClick = {
                    if (isFormValid) {
                        val category = when {
                            title.contains("Rent", ignoreCase = true) -> "RENT"
                            title.contains("Ride", ignoreCase = true) || title.contains("PickMe", ignoreCase = true) || title.contains("Transport", ignoreCase = true) -> "TRANSPORT"
                            title.contains("Uber", ignoreCase = true) || title.contains("Coffee", ignoreCase = true) || title.contains("Cafe", ignoreCase = true) || title.contains("Dining", ignoreCase = true) -> "DINING_OUT"
                            title.contains("Grocery", ignoreCase = true) || title.contains("Supermarket", ignoreCase = true) -> "MISC"
                            else -> "MISC"
                        }
                        val expense = ExpenseEntity(
                            title = title,
                            amountLkr = amountValue,
                            category = category,
                            paymentAccount = accountKeys[selectedAccountIndex],
                            commitmentType = if (commitmentTypeIndex == 0) "COMMITTED" else "DISCRETIONARY"
                        )
                        onSubmitExpense(expense)
                        onDismiss()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (isFormValid) "Save Expense" else "Enter Amount & Title",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

