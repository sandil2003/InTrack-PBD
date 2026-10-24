package com.example.intrack.ui.screens.expenses

import com.example.intrack.viewmodel.ExpenseViewModel
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.ui.common.UiState
import com.example.intrack.ui.theme.BrandTangerine
import com.example.intrack.ui.theme.PlusJakartaSansFontFamily
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Design tokens matching Figma frame 2015:802
private val FigmaBg = Color(0xFFFAF9F5)
private val FigmaDark = Color(0xFF1B1C1A)
private val FigmaMuted = Color(0xFF6B5B53)
private val FigmaSubtle = Color(0xFF756A63)
private val FigmaBorder = Color(0xFFEFE9E0)
private val FigmaPeachTint = Color(0xFFF5DED4)
private val FigmaRust = Color(0xFFB52705)
private val FigmaPrimary = Color(0xFFFF5C38)
private val FigmaSettledBg = Color(0xFFEAF7EE)
private val FigmaSettledBorder = Color(0xFFC6EACB)
private val FigmaSettledGreen = Color(0xFF219653)
private val FigmaActiveGreen = Color(0xFF27AE60)
private val FigmaDestructive = Color(0xFFE74C3C)

/**
 * Modal Bottom Sheet implementation of the Figma "Edit expense" screen (Node 2015:802).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseSheet(
    expense: ExpenseEntity,
    onDismiss: () -> Unit,
    onUpdateExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    initialEditingMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = null,
        modifier = modifier
    ) {
        EditExpenseContent(
            expense = expense,
            onDismiss = onDismiss,
            onUpdateExpense = onUpdateExpense,
            onDeleteExpense = onDeleteExpense,
            initialEditingMode = initialEditingMode
        )
    }
}

/**
 * Standalone Screen version of "Edit expense" screen for direct navigation routes.
 */
@Composable
fun EditExpenseScreen(
    expenseId: String,
    onNavigateBack: () -> Unit,
    expenseViewModel: ExpenseViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val expensesState by expenseViewModel.expensesState.collectAsState()
    val expense = (expensesState as? UiState.Success<List<ExpenseEntity>>)?.data?.find { it.id == expenseId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(FigmaBg)
            .statusBarsPadding()
    ) {
        if (expense != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .background(Color.White)
            ) {
                EditExpenseContent(
                    expense = expense,
                    onDismiss = onNavigateBack,
                    onUpdateExpense = { updated ->
                        expenseViewModel.updateExpense(updated)
                        onNavigateBack()
                    },
                    onDeleteExpense = { toDelete ->
                        val currentUid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: "guest_user"
                        expenseViewModel.deleteExpense(toDelete.userId.ifBlank { currentUid }, toDelete.id)
                        onNavigateBack()
                    },
                    initialEditingMode = true
                )
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandTangerine)
            }
        }
    }
}

/**
 * Main content layout for Edit Expense with both Detail View and In-Sheet Edit Form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseContent(
    expense: ExpenseEntity,
    onDismiss: () -> Unit,
    onUpdateExpense: (ExpenseEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit,
    initialEditingMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Toggle between View Mode and Edit Mode
    var isEditingMode by remember(initialEditingMode) { mutableStateOf(initialEditingMode) }
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Editable form state initialized from expense
    var editTitle by remember(expense) { mutableStateOf(expense.title) }
    var editAmountText by remember(expense) {
        val formatted = if (expense.amountLkr % 1.0 == 0.0) {
            String.format(Locale.US, "%.0f", expense.amountLkr)
        } else {
            String.format(Locale.US, "%.2f", expense.amountLkr)
        }
        mutableStateOf(formatted)
    }
    var amountError by remember { mutableStateOf<String?>(null) }
    var editCategory by remember(expense) { mutableStateOf(expense.category) }
    var editAccount by remember(expense) { mutableStateOf(expense.paymentAccount) }
    var editCommitment by remember(expense) { mutableStateOf(expense.commitmentType) }
    var editDateMillis by remember(expense) { mutableLongStateOf(expense.dateSpentMillis) }
    var editNotes by remember(expense) { mutableStateOf(expense.notes ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    // Category mappings
    val categoryDisplay = remember(expense.category) {
        when (expense.category.uppercase()) {
            "TRANSPORT" -> "Transport"
            "DINING_OUT" -> "Dining Out"
            "RENT" -> "Housing / Rent"
            "SUBSCRIPTION" -> "Subscription"
            "UTILITIES" -> "Utilities"
            "MISC" -> "Groceries & Misc"
            else -> expense.category
        }
    }

    val categoryIcon = remember(expense.category) {
        when (expense.category.uppercase()) {
            "TRANSPORT" -> Icons.Rounded.DirectionsCar
            "DINING_OUT" -> Icons.Rounded.Restaurant
            "RENT" -> Icons.Rounded.Home
            "SUBSCRIPTION" -> Icons.Rounded.Receipt
            "UTILITIES" -> Icons.Rounded.Payments
            else -> Icons.Rounded.ShoppingCart
        }
    }

    val accountDisplay = remember(expense.paymentAccount) {
        when (expense.paymentAccount.uppercase()) {
            "CARD_PRIMARY" -> "Card Primary (Visa ••4242)"
            "ACCOUNT_SECONDARY" -> "Secondary Bank Account"
            "CASH" -> "Cash Wallet"
            else -> expense.paymentAccount.replace("_", " ")
        }
    }

    val formattedDateTime = remember(expense.dateSpentMillis) {
        val cal = Calendar.getInstance()
        val expCal = Calendar.getInstance().apply { timeInMillis = expense.dateSpentMillis }
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(expense.dateSpentMillis))
        val isToday = cal.get(Calendar.YEAR) == expCal.get(Calendar.YEAR) &&
                cal.get(Calendar.DAY_OF_YEAR) == expCal.get(Calendar.DAY_OF_YEAR)
        if (isToday) {
            "Today, ${SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.dateSpentMillis))} • $timeFormat"
        } else {
            "${SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault()).format(Date(expense.dateSpentMillis))} • $timeFormat"
        }
    }

    val referenceId = remember(expense.id) {
        val clean = expense.id.replace(Regex("[^A-Za-z0-9]"), "").take(7).uppercase()
        "TXN-${clean.ifEmpty { "8942104" }}"
    }

    val isCommitted = expense.commitmentType.equals("COMMITTED", ignoreCase = true)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Handle bar at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(38.dp)
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFD8D0C9))
            )
        }

        AnimatedContent(
            targetState = isEditingMode,
            transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
            label = "EditExpenseModeTransition"
        ) { editing ->
            if (!editing) {
                // =============================================================
                // VIEW MODE: Exact Figma "Edit expense" Sheet (Node 2015:802)
                // =============================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Top Header Row with Category Pill and Close Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FigmaPeachTint
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = categoryIcon,
                                    contentDescription = null,
                                    tint = FigmaRust,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = categoryDisplay,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FigmaRust
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF4F0EB))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = FigmaDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Main Title & Subtitle
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = expense.title,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaDark,
                            lineHeight = 30.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                    }

                    // Hero Total Debited Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF9F5)),
                        border = BorderStroke(1.dp, FigmaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Top Row: Total Debited + Settled Badge
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Debited",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = FigmaSubtle,
                                    maxLines = 1
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isCommitted) FigmaPeachTint else Color(0xFFEDE8E3))
                                        .padding(horizontal = 8.dp, vertical = 2.5.dp)
                                ) {
                                    Text(
                                        text = if (isCommitted) "COMMITTED" else "DISCRETIONARY",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isCommitted) FigmaRust else FigmaDark
                                    )
                                }
                            }

                            // Amount Display (Baseline aligned like in ViewSubscriptionSheet)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    space = 6.dp,
                                    alignment = Alignment.CenterHorizontally
                                ),
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text(
                                    text = "-",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FigmaDark,
                                    modifier = Modifier.alignByBaseline()
                                )
                                Text(
                                    text = "LKR",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FigmaDark,
                                    modifier = Modifier.alignByBaseline()
                                )
                                Text(
                                    text = String.format(Locale.US, "%,.2f", expense.amountLkr),
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 30.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FigmaDark,
                                    letterSpacing = (-0.5).sp,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.alignByBaseline()
                                )
                            }

                            // Budget & Essentials Indicator
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(FigmaPrimary)
                                )
                                Text(
                                    text = if (isCommitted) "Essentials" else "Discretionary",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FigmaRust,
                                    maxLines = 1,
                                    softWrap = false
                                )
                                Text(
                                    text = "•",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    color = FigmaSubtle
                                )
                                Text(
                                    text = if (isCommitted) "Obligation" else "Flexible",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 12.sp,
                                    color = FigmaMuted,
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                            }
                        }
                    }

                    // Metadata Details Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, FigmaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Row 1: Date & Time
                            DetailRowItem(
                                icon = Icons.Rounded.Schedule,
                                label = "Date & Time",
                                value = formattedDateTime
                            )

                            HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

                            // Row 2: Account
                            DetailRowItem(
                                icon = Icons.Rounded.CreditCard,
                                label = "Account",
                                value = accountDisplay,
                                hasBullet = true
                            )

                            HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

                            // Row 3: Reference ID (with copy button)
                            DetailRowItem(
                                icon = Icons.AutoMirrored.Rounded.ReceiptLong,
                                label = "Reference ID",
                                value = referenceId,
                                isMonospace = true,
                                trailingContent = {
                                    IconButton(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                            clipboard.setPrimaryClip(ClipData.newPlainText("Reference ID", referenceId))
                                            Toast.makeText(context, "Copied ID: $referenceId", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ContentCopy,
                                            contentDescription = "Copy ID",
                                            tint = FigmaSubtle,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            )

                            HorizontalDivider(color = FigmaBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

                            // Row 4: Notes
                            DetailRowItem(
                                icon = Icons.Rounded.EditNote,
                                label = "Notes",
                                value = if (expense.notes?.isNotBlank() == true) expense.notes!! else "None"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Primary Action: Edit Transaction Button (Terracotta Pill)
                    Button(
                        onClick = { isEditingMode = true },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FigmaPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Edit Transaction",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Edit Transaction",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Destructive Action: Delete Button
                    TextButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = "Delete",
                                tint = FigmaDestructive,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delete",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaDestructive
                            )
                        }
                    }
                }
            } else {
                // =============================================================
                // EDIT MODE: In-Sheet Interactive Form
                // =============================================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 22.dp)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with back to view mode and cancel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isEditingMode = false }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = FigmaDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Edit Details",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaDark
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF4F0EB))
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "Close",
                                tint = FigmaDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // 1. Amount Input Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBF9F5)),
                        border = BorderStroke(1.dp, FigmaBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "ENTER TRANSACTION AMOUNT",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = FigmaSubtle
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "LKR",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FigmaDark,
                                    modifier = Modifier.alignByBaseline()
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                BasicTextField(
                                    value = editAmountText,
                                    onValueChange = { input ->
                                        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                                        val dotCount = filtered.count { it == '.' }
                                        if (dotCount <= 1) {
                                            editAmountText = filtered
                                            amountError = null
                                        }
                                    },
                                    textStyle = TextStyle(
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FigmaDark
                                    ),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Decimal,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    cursorBrush = SolidColor(FigmaPrimary),
                                    singleLine = true,
                                    modifier = Modifier.alignByBaseline()
                                )
                            }
                            if (amountError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = amountError!!,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FigmaDestructive
                                )
                            }
                        }
                    }

                    // 2. Title / Description
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Title / Merchant",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaSubtle
                        )
                        OutlinedTextField(
                            value = editTitle,
                            onValueChange = { editTitle = it },
                            placeholder = { Text("e.g. Ride to Office", color = FigmaSubtle) },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FigmaPrimary,
                                unfocusedBorderColor = FigmaBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAF8F5)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // 3. Category Selector Chips
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Category",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaSubtle
                        )
                        val categories = listOf(
                            "TRANSPORT" to ("Transport" to Icons.Rounded.DirectionsCar),
                            "DINING_OUT" to ("Dining" to Icons.Rounded.Restaurant),
                            "RENT" to ("Housing / Rent" to Icons.Rounded.Home),
                            "UTILITIES" to ("Utilities" to Icons.Rounded.Payments),
                            "SUBSCRIPTION" to ("Subscriptions" to Icons.Rounded.Receipt),
                            "MISC" to ("Groceries & Misc" to Icons.Rounded.ShoppingCart),
                            "ATM_WITHDRAWAL" to ("ATM / Cash" to Icons.Rounded.Payments)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEach { (catKey, pair) ->
                                val (name, icon) = pair
                                val isSelected = editCategory.equals(catKey, ignoreCase = true)
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        editCategory = catKey
                                        if (catKey == "ATM_WITHDRAWAL") {
                                            editCommitment = "TRANSFER"
                                            editAccount = "CARD_PRIMARY"
                                        } else if (editCommitment == "TRANSFER") {
                                            editCommitment = if (catKey in listOf("RENT", "UTILITIES", "MISC")) "COMMITTED" else "DISCRETIONARY"
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = name,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = null,
                                            tint = if (isSelected) FigmaRust else FigmaSubtle,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FigmaPeachTint,
                                        selectedLabelColor = FigmaRust,
                                        containerColor = Color(0xFFFBF8F5),
                                        labelColor = FigmaDark
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        borderColor = if (isSelected) FigmaRust else FigmaBorder,
                                        selectedBorderColor = FigmaRust
                                    )
                                )
                            }
                        }
                    }

                    // 4. Payment Account Selector
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Paid From",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaSubtle
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("CARD_PRIMARY", "Card Primary", Icons.Rounded.CreditCard),
                                Triple("CASH", "Cash Wallet", Icons.Rounded.AccountBalanceWallet),
                                Triple("ACCOUNT_SECONDARY", "Secondary", Icons.Rounded.AccountBalance)
                            ).forEach { (accKey, label, icon) ->
                                val isSelected = editAccount.equals(accKey, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isSelected) FigmaPeachTint else Color(0xFFFAF8F5),
                                    border = BorderStroke(1.dp, if (isSelected) FigmaRust else FigmaBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { editAccount = accKey }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) FigmaRust else FigmaSubtle,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = label,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.5.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) FigmaRust else FigmaDark,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 5. Commitment Type Toggle
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Commitment Type",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaSubtle
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(
                                "COMMITTED" to "Essentials",
                                "DISCRETIONARY" to "Discretionary"
                            ).forEach { (typeKey, label) ->
                                val isSelected = editCommitment.equals(typeKey, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) FigmaPeachTint else Color(0xFFFAF8F5),
                                    border = BorderStroke(1.dp, if (isSelected) FigmaRust else FigmaBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { editCommitment = typeKey }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = FigmaRust,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = label,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) FigmaRust else FigmaDark,
                                            maxLines = 1,
                                            softWrap = false
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 6. Transaction Date
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFFAF8F5),
                        border = BorderStroke(1.dp, FigmaBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CalendarToday,
                                    contentDescription = null,
                                    tint = FigmaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "TRANSACTION DATE",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FigmaSubtle,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(editDateMillis)),
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FigmaDark,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Text(
                                text = "Change",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = FigmaPrimary,
                                maxLines = 1
                            )
                        }
                    }

                    // 7. Notes Input
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Notes",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FigmaSubtle
                        )
                        OutlinedTextField(
                            value = editNotes,
                            onValueChange = { editNotes = it },
                            placeholder = { Text("Add note (optional)", color = FigmaSubtle) },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FigmaPrimary,
                                unfocusedBorderColor = FigmaBorder,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAF8F5)
                            ),
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Buttons: Save Changes and Cancel
                    Button(
                        onClick = {
                            val cleanRaw = editAmountText.replace(",", "").trim()
                            val parsedAmount = cleanRaw.toDoubleOrNull()
                            if (parsedAmount == null || parsedAmount <= 0.0) {
                                amountError = "Enter a valid amount"
                                return@Button
                            }
                            val updated = expense.copy(
                                title = editTitle.ifBlank { expense.title },
                                amountLkr = parsedAmount,
                                category = editCategory,
                                paymentAccount = editAccount,
                                commitmentType = editCommitment,
                                dateSpentMillis = editDateMillis,
                                notes = editNotes
                            )
                            isSaving = true
                            coroutineScope.launch {
                                delay(300)
                                onUpdateExpense(updated)
                                isSaving = false
                                isEditingMode = false
                                Toast.makeText(context, "Transaction updated", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FigmaPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                text = "Save Changes",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    TextButton(
                        onClick = { isEditingMode = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Text(
                            text = "Cancel",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = FigmaDark
                        )
                    }
                }
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = editDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { editDateMillis = it }
                        showDatePicker = false
                    }
                ) {
                    Text("Select", color = FigmaPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = FigmaDark)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White,
            title = {
                Text(
                    text = "Delete Transaction?",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = FigmaDark
                )
            },
            text = {
                Text(
                    text = "Permanently delete '${expense.title}'?",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 14.sp,
                    color = FigmaMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteExpense(expense)
                        onDismiss()
                    }
                ) {
                    Text("Delete", color = FigmaDestructive, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = FigmaDark, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

// =============================================================================
// HELPER COMPOSABLE FOR METADATA ROWS
// =============================================================================

@Composable
private fun DetailRowItem(
    icon: ImageVector,
    label: String,
    value: String,
    hasBullet: Boolean = false,
    isMonospace: Boolean = false,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(0.9f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = FigmaPrimary,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = FigmaSubtle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.weight(1.1f)
        ) {
            if (hasBullet) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(FigmaPrimary)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = value,
                fontFamily = if (isMonospace) FontFamily.Monospace else PlusJakartaSansFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = FigmaDark,
                textAlign = TextAlign.End,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            trailingContent?.let {
                Spacer(modifier = Modifier.width(4.dp))
                it()
            }
        }
    }
}
