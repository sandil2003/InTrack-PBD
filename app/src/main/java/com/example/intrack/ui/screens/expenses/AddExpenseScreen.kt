package com.example.intrack.ui.screens.expenses

import com.example.intrack.viewmodel.ExpenseViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.ui.theme.PlusJakartaSansFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

// Data models for categories and accounts
data class ExpenseCategoryItem(
    val key: String,
    val displayName: String,
    val icon: ImageVector
)

data class ExpenseAccountItem(
    val key: String,
    val title: String,
    val balanceLabel: String,
    val icon: ImageVector
)

/**
 * Full screen version of "Add an expense" matching Figma frame 2010:240.
 */
@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    onExpenseSaved: () -> Unit = onNavigateBack,
    expenseViewModel: ExpenseViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .background(MaterialTheme.colorScheme.surface)
        ) {
            AddExpenseContent(
                onDismiss = onNavigateBack,
                onSubmit = { newExpense ->
                    expenseViewModel.addExpense(newExpense)
                    onExpenseSaved()
                }
            )
        }
    }
}

/**
 * Modal Bottom Sheet version of "Add an expense" matching Figma frame 2010:303.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        containerColor = Color.White,
        dragHandle = null,
        modifier = modifier
    ) {
        AddExpenseContent(
            onDismiss = onDismiss,
            onSubmit = onSubmit
        )
    }
}

/**
 * Complete UI and UX matching Figma node 2010:303 ("New Transaction").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseContent(
    onDismiss: () -> Unit,
    onSubmit: (ExpenseEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    // State
    var amountInput by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var commitmentType by remember { mutableStateOf("DISCRETIONARY") }
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showNewTagDialog by remember { mutableStateOf(false) }
    var newTagInput by remember { mutableStateOf("") }
    var amountErrorMessage by remember { mutableStateOf<String?>(null) }

    // Categories
    val categories = remember {
        mutableStateListOf(
            ExpenseCategoryItem("DINING_OUT", "Dining", Icons.Rounded.Restaurant),
            ExpenseCategoryItem("TRANSPORT", "Transport", Icons.Rounded.DirectionsCar),
            ExpenseCategoryItem("MISC", "Groceries", Icons.Rounded.ShoppingCart),
            ExpenseCategoryItem("UTILITIES", "Utilities", Icons.Rounded.Bolt),
            ExpenseCategoryItem("RENT", "Rent", Icons.Rounded.Home),
            ExpenseCategoryItem("LIFESTYLE", "Lifestyle", Icons.Rounded.Spa),
            ExpenseCategoryItem("ATM_WITHDRAWAL", "ATM / Cash", Icons.Rounded.Payments)
        )
    }
    var selectedCategoryKey by remember { mutableStateOf("DINING_OUT") }

    // Accounts ("Paid From")
    val accounts = remember {
        listOf(
            ExpenseAccountItem("CARD_PRIMARY", "Card Primary", "Card", Icons.Rounded.CreditCard),
            ExpenseAccountItem("CASH", "Cash Wallet", "Cash", Icons.Rounded.AccountBalanceWallet),
            ExpenseAccountItem("ACCOUNT_SECONDARY", "Secondary", "Bank", Icons.Rounded.AccountBalance)
        )
    }
    var selectedAccountKey by remember { mutableStateOf("CARD_PRIMARY") }

    // Blinking cursor transition for hero amount display
    val infiniteTransition = rememberInfiniteTransition(label = "cursorTransition")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        // 1. Drag Handle Indicator (Height 22dp, pill 48x6dp, color #E4E2DE)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(22.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }

        // 2. Sheet Header Bar (Padding: Top 8dp, Bottom 12dp, Horiz 20dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Peach circular badge with terracotta icon (32x32dp, fill #F5DED4)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = "New Transaction Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Title: "New Transaction" (20sp, Bold 700, #1B1C1A)
                Text(
                    text = "New Transaction",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Close button (40x40dp circle, fill #EFEEEA)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close modal",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 3. Hero Amount Entry Display
        // Background #FBF9F5, border 1dp #E3BEB6, corner radius 16dp, padding 20dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header Label: "ENTER OUTFLOW AMOUNT" (11sp, SemiBold 600, #585F6B)
                Text(
                    text = "ENTER OUTFLOW AMOUNT",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Currency + Value Row + Blinking Cursor
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Currency: "LKR" (17sp, Bold 700, #6B5B53)
                    Text(
                        text = "LKR",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Value: e.g. "4,850.00" (32sp, ExtraBold 800, #1B1C1A)
                    BasicTextField(
                        value = amountInput,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                            amountInput = filtered
                            amountErrorMessage = null
                        },
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        cursorBrush = SolidColor(Color.Transparent),
                        decorationBox = { innerTextField ->
                            if (amountInput.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(3.dp))

                    // Blinking vertical cursor divider (2x28dp, fill #B52705)
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(28.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = cursorAlpha))
                    )
                }

                if (amountErrorMessage != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = amountErrorMessage!!,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Category Selector Chips
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // "Select Category" (12sp, SemiBold 600, #5B413B)
                Text(
                    text = "Select Category",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // "+ New Tag" button (11sp, SemiBold 600, #B52705)
                Text(
                    text = "+ New Tag",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showNewTagDialog = true }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontally scrollable chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategoryKey == category.key
                    val chipBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    val chipBorder = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    val chipContentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                    Row(
                        modifier = Modifier
                            .height(38.dp)
                            .clip(CircleShape)
                            .background(chipBg)
                            .border(1.dp, chipBorder, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedCategoryKey = category.key
                                // Smart auto-defaulting based on category
                                when (category.key) {
                                    "RENT", "UTILITIES", "MISC" -> {
                                        commitmentType = "COMMITTED"
                                    }
                                    "ATM_WITHDRAWAL" -> {
                                        commitmentType = "TRANSFER"
                                        selectedAccountKey = "CARD_PRIMARY"
                                    }
                                    "DINING_OUT", "LIFESTYLE", "TRANSPORT" -> {
                                        commitmentType = "DISCRETIONARY"
                                    }
                                }
                            }
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = category.icon,
                            contentDescription = category.displayName,
                            tint = iconColor,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = category.displayName,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = chipContentColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4a. ATM / Cash Withdrawal notice if selected
        if (selectedCategoryKey == "ATM_WITHDRAWAL") {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapHoriz,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Internal Transfer (Non-Expense)",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Transfers to Cash wallet • Excluded from expense outflows",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // 4b. Explicit "Commitment Type" Toggle Card (Essentials/Committed vs Lifestyle/Discretionary)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "COMMITMENT TYPE",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = when (commitmentType) {
                        "COMMITTED" -> "Bills & Essentials"
                        "TRANSFER" -> "Transfer"
                        else -> "Dining & Lifestyle"
                    },
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (commitmentType == "COMMITTED") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Option 1: Essentials / Committed
                val isCommittedSelected = commitmentType == "COMMITTED"
                val committedBg = if (isCommittedSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                val committedBorder = if (isCommittedSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                val committedText = if (isCommittedSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(committedBg)
                        .border(if (isCommittedSelected) 1.5.dp else 1.dp, committedBorder, RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { commitmentType = "COMMITTED" }
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Security,
                            contentDescription = "Committed",
                            tint = committedText,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Committed",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isCommittedSelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = committedText
                            )
                            Text(
                                text = "Rent, bills, groceries",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Option 2: Lifestyle / Discretionary
                val isDiscretionarySelected = commitmentType == "DISCRETIONARY"
                val discBg = if (isDiscretionarySelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                val discBorder = if (isDiscretionarySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                val discText = if (isDiscretionarySelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(discBg)
                        .border(if (isDiscretionarySelected) 1.5.dp else 1.dp, discBorder, RoundedCornerShape(12.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { commitmentType = "DISCRETIONARY" }
                        .padding(vertical = 10.dp, horizontal = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocalCafe,
                            contentDescription = "Discretionary",
                            tint = discText,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "Discretionary",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                fontWeight = if (isDiscretionarySelected) FontWeight.Bold else FontWeight.SemiBold,
                                color = discText
                            )
                            Text(
                                text = "Dining, leisure, wants",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Account Destination Selector ("Paid From")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // Header: "Paid From" (12sp, SemiBold 600, #5B413B)
            Text(
                text = "Paid From",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 3 Cards side-by-side
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                accounts.forEach { account ->
                    val isSelected = selectedAccountKey == account.key
                    val cardBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                    val cardBorder = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    val borderWidth = if (isSelected) 2.dp else 1.dp
                    val iconColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    val titleColor = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(cardBg)
                            .border(borderWidth, cardBorder, RoundedCornerShape(12.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedAccountKey = account.key }
                            .padding(vertical = 12.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = account.icon,
                            contentDescription = account.title,
                            tint = iconColor,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = account.title,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = titleColor,
                            maxLines = 1
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = account.balanceLabel,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Date Picker Pill Bar
        // Background #FBF9F5, stroke 1dp #E4E2DE, corner radius 14dp, padding 12dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left: Calendar icon in circular gray container (32x32dp, fill #E4E2DE)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = "Calendar",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        // Sub-label: "TRANSACTION DATE" (10sp, Bold 700, #585F6B)
                        Text(
                            text = "TRANSACTION DATE",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 0.5.sp
                        )

                        // Date: e.g. "Today, Oct 11, 2026" (14sp, SemiBold 600, #1B1C1A)
                        Text(
                            text = formatTransactionDisplayDate(selectedDateMillis),
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Right action: "Change" button (12sp, SemiBold 600, #B52705)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { showDatePicker = true }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Change",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 7. Description / Note Input
        // Background #FBF9F5, stroke 1dp #E4E2DE, corner radius 14dp, padding 14dp vert, 16dp horiz
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Edit pencil icon (#585F6B)
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Note",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Input field
                BasicTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (noteText.isEmpty()) {
                            Text(
                                text = "Add note (optional)",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 8. Subscription / Recurrence Toggle Card
        // Background #F5F3EF, stroke 1dp #E4E2DE, corner radius 14dp, padding 12dp
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isRecurring = !isRecurring }
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left circular peach badge with recurring sync/repeat icon (#B52705)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sync,
                            contentDescription = "Recurring",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        // Title: "Recurring monthly expense?" (12sp, SemiBold 600, #1B1C1A)
                        Text(
                            text = "Monthly recurring",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Subtitle: "Auto-tracked in Subscriptions" (12sp, Regular 400, #585F6B)
                        Text(
                            text = "Auto-tracked in Subscriptions",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Custom Styled Toggle Switch (44x24dp track, 20x20dp thumb)
                val trackColor by animateColorAsState(
                    targetValue = if (isRecurring) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "trackColor"
                )
                val thumbOffset by animateDpAsState(
                    targetValue = if (isRecurring) 22.dp else 2.dp,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing),
                    label = "thumbOffset"
                )

                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(24.dp)
                        .clip(CircleShape)
                        .background(trackColor)
                        .padding(vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = thumbOffset)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 9. Primary Action Button ("Save Expense")
        // Height 56dp, pill corner radius 9999dp, fill #FF5C38
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .clickable {
                    val rawAmount = amountInput.replace(",", "").trim()
                    val parsedAmount = rawAmount.toDoubleOrNull()
                    if (parsedAmount == null || parsedAmount <= 0.0) {
                        amountErrorMessage = "Please enter a valid amount"
                        return@clickable
                    }

                    val selectedCat = categories.find { it.key == selectedCategoryKey }
                    val categoryName = selectedCat?.displayName ?: "Dining"
                    val defaultTitle = if (selectedCategoryKey == "ATM_WITHDRAWAL") "ATM Cash Withdrawal" else categoryName
                    val finalTitle = noteText.trim().ifBlank { defaultTitle }

                    val finalCommitmentType = if (selectedCategoryKey == "ATM_WITHDRAWAL") "TRANSFER" else commitmentType

                    val newExpense = ExpenseEntity(
                        id = UUID.randomUUID().toString(),
                        userId = "",
                        title = finalTitle,
                        amountLkr = parsedAmount,
                        category = selectedCategoryKey,
                        paymentAccount = selectedAccountKey,
                        commitmentType = finalCommitmentType,
                        dateSpentMillis = selectedDateMillis,
                        isRecurring = isRecurring,
                        notes = noteText.trim().ifBlank { null }
                    )

                    onSubmit(newExpense)
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Checkmark in circle icon (#FFFFFF)
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )

                // Text: "Save Expense" (17sp, Bold 700, #FFFFFF)
                Text(
                    text = "Save Expense",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDateMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(
                        text = "Done",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = PlusJakartaSansFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Dialog for adding a custom category tag
    if (showNewTagDialog) {
        AlertDialog(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onDismissRequest = { showNewTagDialog = false },
            title = {
                Text(
                    text = "Add Category Tag",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter tag name:",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newTagInput,
                        onValueChange = { newTagInput = it },
                        singleLine = true,
                        placeholder = { Text("e.g. Health, Pets, Education") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = newTagInput.trim()
                        if (trimmed.isNotEmpty()) {
                            val key = trimmed.uppercase().replace(" ", "_")
                            if (categories.none { it.key == key }) {
                                categories.add(
                                    ExpenseCategoryItem(
                                        key = key,
                                        displayName = trimmed,
                                        icon = Icons.Rounded.Edit
                                    )
                                )
                            }
                            selectedCategoryKey = key
                            newTagInput = ""
                            showNewTagDialog = false
                        }
                    }
                ) {
                    Text(
                        text = "Add",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewTagDialog = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = PlusJakartaSansFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

/**
 * Format timestamp into Figma-style date representation:
 * E.g.: "Today, Oct 11, 2026", "Yesterday, Oct 10, 2026", or "Sunday, Oct 11, 2026".
 */
private fun formatTransactionDisplayDate(timeMillis: Long): String {
    val cal = Calendar.getInstance()
    val todayYear = cal.get(Calendar.YEAR)
    val todayDay = cal.get(Calendar.DAY_OF_YEAR)

    cal.timeInMillis = timeMillis
    val selYear = cal.get(Calendar.YEAR)
    val selDay = cal.get(Calendar.DAY_OF_YEAR)

    val dateMonthDay = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(timeMillis))

    return when {
        todayYear == selYear && todayDay == selDay -> "Today, $dateMonthDay"
        todayYear == selYear && todayDay - selDay == 1 -> "Yesterday, $dateMonthDay"
        else -> SimpleDateFormat("EEE, MMM d, yyyy", Locale.US).format(Date(timeMillis))
    }
}
