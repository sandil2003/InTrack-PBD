package com.example.intrack.ui.screens.subscriptions

import com.example.intrack.viewmodel.SubscriptionViewModel
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
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.intrack.data.model.SubscriptionEntity
import com.example.intrack.ui.theme.PlusJakartaSansFontFamily
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SubCategoryOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Full screen version of "Add subscription" matching Figma frame 2011:423.
 */
@Composable
fun AddSubscriptionScreen(
    initialSubscription: SubscriptionEntity? = null,
    onNavigateBack: () -> Unit,
    onSubscriptionSaved: (SubscriptionEntity) -> Unit = { onNavigateBack() },
    viewModel: SubscriptionViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F3EF))
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp))
                .background(Color.White)
        ) {
            AddSubscriptionContent(
                initialSubscription = initialSubscription,
                onDismiss = onNavigateBack,
                onSubmit = { sub ->
                    if (initialSubscription != null) {
                        viewModel.updateSubscription(sub)
                    } else {
                        viewModel.addSubscription(sub)
                    }
                    onSubscriptionSaved(sub)
                }
            )
        }
    }
}

/**
 * Modal Bottom Sheet version of "Add subscription" matching Figma frame 2011:493 ("New Recurring Charge").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionBottomSheet(
    initialSubscription: SubscriptionEntity? = null,
    onDismiss: () -> Unit,
    onSubmit: (SubscriptionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
        containerColor = Color.White,
        dragHandle = null,
        modifier = modifier.imePadding()
    ) {
        AddSubscriptionContent(
            initialSubscription = initialSubscription,
            onDismiss = onDismiss,
            onSubmit = onSubmit
        )
    }
}

/**
 * Complete UI and UX matching Figma node 2011:493 ("New Recurring Charge").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSubscriptionContent(
    initialSubscription: SubscriptionEntity? = null,
    onDismiss: () -> Unit,
    onSubmit: (SubscriptionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    // Form states
    var providerName by remember { mutableStateOf(initialSubscription?.name ?: "") }
    var amountInput by remember {
        mutableStateOf(
            if (initialSubscription != null && initialSubscription.amountLkr > 0) {
                String.format(Locale.US, "%.2f", initialSubscription.amountLkr)
            } else ""
        )
    }
    var billingFrequency by remember { mutableStateOf(initialSubscription?.billingCycle ?: "Monthly") }
    var selectedDateMillis by remember {
        mutableLongStateOf(
            initialSubscription?.nextBillingMillis ?: (System.currentTimeMillis() + 9L * 24 * 60 * 60 * 1000L)
        )
    }
    var selectedAccountKey by remember { mutableStateOf(initialSubscription?.paymentAccount ?: "CARD_PRIMARY") }
    var selectedCategoryKey by remember { mutableStateOf(initialSubscription?.category ?: "ENTERTAINMENT") }
    var remindAutoDebit by remember { mutableStateOf(initialSubscription?.isAutoPayment ?: true) }

    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = remember {
        listOf(
            SubCategoryOption("ENTERTAINMENT", "Entertainment", Icons.Rounded.LiveTv),
            SubCategoryOption("HOUSING", "Housing", Icons.Rounded.Home),
            SubCategoryOption("HEALTH", "Health", Icons.Rounded.FitnessCenter),
            SubCategoryOption("TECH", "Tech", Icons.Rounded.Devices)
        )
    }

    val frequencies = listOf("Monthly", "Yearly", "Weekly", "Quarterly")

    // Blinking cursor transition for amount field
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
            .imePadding()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Drag Handle Pill (48x6dp, #E4E2DE)
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
                    .background(Color(0xFFE4E2DE))
            )
        }

        // Header Bar: Peach Badge + "New Recurring Charge" + Close Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Peach circular badge with sync/recurring icon (32x32dp, fill #F5DED4)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5DED4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Sync,
                        contentDescription = "New Recurring Charge",
                        tint = Color(0xFFB52705),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = if (initialSubscription != null) "Edit Recurring Charge" else "New Recurring Charge",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1C1A)
                )
            }

            // Close button (40x40dp circle, fill #EFEEEA)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFEEEA))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Close modal",
                    tint = Color(0xFF585F6B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 1. Service or Provider Name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Service or Provider Name",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B5B53)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFBF9F5))
                    .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                BasicTextField(
                    value = providerName,
                    onValueChange = {
                        providerName = it
                        errorMessage = null
                    },
                    textStyle = TextStyle(
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1B1C1A)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        if (providerName.isEmpty()) {
                            Text(
                                text = "e.g. Netflix, Spotify, Gym, Rent",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        innerTextField()
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Billing Amount Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFBF9F5))
                .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "BILLING AMOUNT",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF585F6B),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LKR",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B5B53)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val amountFontSize = when {
                        amountInput.length > 9 -> 22.sp
                        amountInput.length > 6 -> 26.sp
                        else -> 32.sp
                    }

                    BasicTextField(
                        value = amountInput,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                            amountInput = filtered
                            errorMessage = null
                        },
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = amountFontSize,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1C1A)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        cursorBrush = SolidColor(Color.Transparent),
                        decorationBox = { innerTextField ->
                            if (amountInput.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = amountFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFC4BBB5)
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
                            .background(Color(0xFFB52705).copy(alpha = cursorAlpha))
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Billing Frequency Segmented Tabs
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Billing Frequency",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B5B53)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Segmented container: background #F5F3EF, border 1dp #E3BEB6, corner radius 12dp
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F3EF))
                    .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                frequencies.forEach { freq ->
                    val isSelected = billingFrequency.equals(freq, ignoreCase = true)
                    val bg = if (isSelected) Color.White else Color.Transparent
                    val textColor = if (isSelected) Color(0xFFB52705) else Color(0xFF585F6B)
                    val fontWt = if (isSelected) FontWeight.SemiBold else FontWeight.Medium

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { billingFrequency = freq },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = freq,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = fontWt,
                            color = textColor,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Subscription End Date / Next Due Date
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Subscription End Date",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B5B53)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFFBF9F5))
                    .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(14.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { showDatePicker = true }
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = "Renewal Date",
                            tint = Color(0xFFFF5C38),
                            modifier = Modifier.size(20.dp)
                        )

                        Column {
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date(selectedDateMillis)),
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1B1C1A)
                            )
                            Text(
                                text = "Renews automatically",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF585F6B)
                            )
                        }
                    }

                    // Days remaining badge
                    val daysRemaining = ((selectedDateMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFF5DED4))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (daysRemaining == 0L) "Today" else "In $daysRemaining Days",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB52705)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Payment Account Source
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Payment Account Source",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B5B53)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Card Primary
                val isCardSelected = selectedAccountKey == "CARD_PRIMARY"
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCardSelected) Color(0xFFFFF8F5) else Color(0xFFFBF9F5))
                        .border(
                            if (isCardSelected) 2.dp else 1.dp,
                            if (isCardSelected) Color(0xFFFF5C38) else Color(0xFFE3BEB6),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedAccountKey = "CARD_PRIMARY" }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CreditCard,
                            contentDescription = "Card Primary",
                            tint = if (isCardSelected) Color(0xFFFF5C38) else Color(0xFF6B5B53),
                            modifier = Modifier.size(18.dp)
                        )

                        // Radio indicator
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(
                                    1.5.dp,
                                    if (isCardSelected) Color(0xFFFF5C38) else Color(0xFFE3BEB6),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCardSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF5C38))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Card Primary",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B1C1A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Visa ••4242",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF585F6B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Secondary Acct
                val isSecondarySelected = selectedAccountKey == "ACCOUNT_SECONDARY"
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSecondarySelected) Color(0xFFFFF8F5) else Color(0xFFFBF9F5))
                        .border(
                            if (isSecondarySelected) 2.dp else 1.dp,
                            if (isSecondarySelected) Color(0xFFFF5C38) else Color(0xFFE3BEB6),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { selectedAccountKey = "ACCOUNT_SECONDARY" }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AccountBalance,
                            contentDescription = "Secondary Acct",
                            tint = if (isSecondarySelected) Color(0xFFFF5C38) else Color(0xFF6B5B53),
                            modifier = Modifier.size(18.dp)
                        )

                        // Radio indicator
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(
                                    1.5.dp,
                                    if (isSecondarySelected) Color(0xFFFF5C38) else Color(0xFFE3BEB6),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSecondarySelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF5C38))
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Secondary Acct",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B1C1A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "Current ••8890",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF585F6B),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Category Selection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Category",
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B5B53)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryKey == cat.key
                    val chipBg = if (isSelected) Color(0xFFF5DED4) else Color(0xFFFBF9F5)
                    val chipBorder = if (isSelected) Color(0xFFE3BEB6) else Color(0xFFE4E2DE)
                    val contentColor = if (isSelected) Color(0xFFB52705) else Color(0xFF6B5B53)

                    Row(
                        modifier = Modifier
                            .height(32.dp)
                            .clip(CircleShape)
                            .background(chipBg)
                            .border(1.dp, chipBorder, CircleShape)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { selectedCategoryKey = cat.key }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = cat.icon,
                            contentDescription = cat.label,
                            tint = contentColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = cat.label,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 7. Remind before auto-debit Notification Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFFBF9F5))
                .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(14.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { remindAutoDebit = !remindAutoDebit }
                .padding(14.dp)
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
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5DED4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Notifications,
                            contentDescription = "Notification",
                            tint = Color(0xFFB52705),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Remind before auto-debit",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1B1C1A)
                        )
                        Text(
                            text = "Notify 3 days before renewal",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF585F6B)
                        )
                    }
                }

                // Custom Switch
                val trackColor by animateColorAsState(
                    targetValue = if (remindAutoDebit) Color(0xFFFF5C38) else Color(0xFFE4E2DE),
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    label = "trackColor"
                )
                val thumbOffset by animateDpAsState(
                    targetValue = if (remindAutoDebit) 26.dp else 2.dp,
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    label = "thumbOffset"
                )

                Box(
                    modifier = Modifier
                        .width(48.dp)
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
                            .background(Color.White)
                    )
                }
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = errorMessage!!,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFB52705),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 8. Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "+ Add Subscription" Primary Button (fill #FF5C38)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFF5C38))
                    .clickable {
                        val rawAmount = amountInput.replace(",", "").trim()
                        val parsedAmount = rawAmount.toDoubleOrNull()
                        if (providerName.isBlank()) {
                            errorMessage = "Please enter a service or provider name"
                            return@clickable
                        }
                        if (parsedAmount == null || parsedAmount <= 0.0) {
                            errorMessage = "Please enter a valid billing amount"
                            return@clickable
                        }

                        val newSub = SubscriptionEntity(
                            id = initialSubscription?.id ?: UUID.randomUUID().toString(),
                            userId = initialSubscription?.userId ?: "",
                            name = providerName.trim(),
                            amountLkr = parsedAmount,
                            billingCycle = billingFrequency.uppercase(),
                            paymentAccount = selectedAccountKey,
                            category = selectedCategoryKey,
                            status = initialSubscription?.status ?: "ACTIVE",
                            nextBillingMillis = selectedDateMillis,
                            isAutoPayment = remindAutoDebit,
                            isFlaggedForReview = initialSubscription?.isFlaggedForReview ?: false,
                            totalPaidLkr = initialSubscription?.totalPaidLkr ?: parsedAmount,
                            billingCyclesCount = initialSubscription?.billingCyclesCount ?: 1
                        )

                        onSubmit(newSub)
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AddCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (initialSubscription != null) "Update Subscription" else "Add Subscription",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Cancel Button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color(0xFF6B5B53)
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
                        color = Color(0xFFB52705)
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(
                        text = "Cancel",
                        fontFamily = PlusJakartaSansFontFamily,
                        color = Color(0xFF585F6B)
                    )
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
