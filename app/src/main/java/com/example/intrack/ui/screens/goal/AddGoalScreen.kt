package com.example.intrack.ui.screens.goal

import com.example.intrack.viewmodel.GoalViewModel
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.TrackChanges
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
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
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.ui.theme.FigmaLightOrange
import com.example.intrack.ui.theme.PlusJakartaSansFontFamily
import com.example.intrack.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

data class GoalCategoryOption(
    val key: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Full-screen destination of "Add Goal" matching Figma Node 2028:1720.
 */
@Composable
fun AddGoalScreen(
    onNavigateBack: () -> Unit,
    onGoalSaved: () -> Unit = onNavigateBack,
    goalViewModel: GoalViewModel = hiltViewModel(),
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
            AddGoalContent(
                onDismiss = onNavigateBack,
                onSubmit = { newGoal ->
                    goalViewModel.addGoal(newGoal)
                    onGoalSaved()
                }
            )
        }
    }
}

/**
 * Modal Bottom Sheet version of "Add Goal" matching Figma Node 2028:1720.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: (GoalEntity) -> Unit,
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
        AddGoalContent(
            onDismiss = onDismiss,
            onSubmit = onSubmit
        )
    }
}

/**
 * Shared Content implementation for Figma Node 2028:1720 ("New Financial Goal").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalContent(
    onDismiss: () -> Unit,
    onSubmit: (GoalEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    // Form states
    var title by remember { mutableStateOf("") }
    var targetAmountInput by remember { mutableStateOf("") }
    var initialDepositInput by remember { mutableStateOf("") }
    var selectedCategoryKey by remember { mutableStateOf("TECH") }
    var selectedAccountKey by remember { mutableStateOf("CARD_PRIMARY") }
    var remindPace by remember { mutableStateOf(true) }

    // Date state: Default 12 months ahead
    var selectedDateMillis by remember {
        val cal = Calendar.getInstance()
        cal.add(Calendar.MONTH, 12)
        mutableLongStateOf(cal.timeInMillis)
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Categories matching InTrack design system
    val categories = remember {
        listOf(
            GoalCategoryOption("TECH", "Tech & Gadgets", Icons.Rounded.LaptopMac),
            GoalCategoryOption("TRAVEL", "Travel & Vacation", Icons.Rounded.Flight),
            GoalCategoryOption("EMERGENCY", "Emergency Buffer", Icons.Rounded.Savings),
            GoalCategoryOption("VEHICLE", "Vehicle", Icons.Rounded.DirectionsCar),
            GoalCategoryOption("HOME", "Home & Living", Icons.Rounded.Home),
            GoalCategoryOption("INVESTMENT", "Investment", Icons.AutoMirrored.Rounded.TrendingUp)
        )
    }

    // Quick timeframe options
    val timeframeChips = listOf(
        "3 Months" to 3,
        "6 Months" to 6,
        "1 Year" to 12,
        "2 Years" to 24
    )

    // Calculate months remaining and estimated monthly run-rate
    val now = System.currentTimeMillis()
    val diffMillis = (selectedDateMillis - now).coerceAtLeast(0L)
    val monthsRemaining = (diffMillis / (30L * 24 * 60 * 60 * 1000L)).toDouble().coerceAtLeast(1.0)

    val cleanTarget = targetAmountInput.replace(",", "").toDoubleOrNull() ?: 0.0
    val cleanInitial = initialDepositInput.replace(",", "").toDoubleOrNull() ?: 0.0
    val amountToSave = (cleanTarget - cleanInitial).coerceAtLeast(0.0)
    val estimatedRunRate = amountToSave / monthsRemaining

    val targetAmountFocusRequester = remember { FocusRequester() }
    var isTargetAmountFocused by remember { mutableStateOf(false) }

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

        // Header Bar: Peach Badge + "New Financial Goal" + Close Button
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
                // Peach circular badge with target icon (32x32dp, fill #F5DED4)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5DED4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.TrackChanges,
                        contentDescription = "New Financial Goal",
                        tint = Color(0xFFB52705),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "New Financial Goal",
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

        // 1. Goal Title / Name
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Goal Title or Name",
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
                    value = title,
                    onValueChange = {
                        title = it
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
                        if (title.isEmpty()) {
                            Text(
                                text = "e.g. MacBook Pro M4, Japan Trip, Emergency Buffer",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                        innerTextField()
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Target Savings Amount Hero Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFBF9F5))
                .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(16.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    targetAmountFocusRequester.requestFocus()
                }
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "TARGET SAVINGS AMOUNT",
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
                        targetAmountInput.length > 9 -> 22.sp
                        targetAmountInput.length > 6 -> 26.sp
                        else -> 32.sp
                    }

                    BasicTextField(
                        value = targetAmountInput,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                            targetAmountInput = filtered
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
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(Color(0xFFB52705)),
                        modifier = Modifier
                            .focusRequester(targetAmountFocusRequester)
                            .onFocusChanged { focusState ->
                                isTargetAmountFocused = focusState.isFocused
                            }
                            .fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            if (targetAmountInput.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = amountFontSize,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Initial Deposit / Starting Saved (Optional)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Initial Deposit / Starting Saved (Optional)",
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "LKR",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6B5B53)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    BasicTextField(
                        value = initialDepositInput,
                        onValueChange = { newValue ->
                            val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
                            initialDepositInput = filtered
                        },
                        textStyle = TextStyle(
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1B1C1A)
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Target Completion Date & Timeframe
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Target Completion Date",
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
                            contentDescription = "Target Date",
                            tint = FigmaLightOrange,
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
                                text = "Target savings horizon",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF585F6B)
                            )
                        }
                    }

                    // Dynamic timeframe badge
                    val daysRemaining = ((selectedDateMillis - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)).coerceAtLeast(0)
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFF5DED4))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (daysRemaining >= 365) "${(daysRemaining / 365)} Year(s) left" else if (daysRemaining >= 30) "${(daysRemaining / 30)} Month(s) left" else "In $daysRemaining Days",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB52705)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quick timeframe preset pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF5F3EF))
                    .border(1.dp, Color(0xFFE3BEB6), RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                timeframeChips.forEach { (label, months) ->
                    val cal = Calendar.getInstance()
                    cal.add(Calendar.MONTH, months)
                    val isSelected = Math.abs(selectedDateMillis - cal.timeInMillis) < (15L * 24 * 60 * 60 * 1000L)
                    val bg = if (isSelected) Color.White else Color.Transparent
                    val textColor = if (isSelected) Color(0xFFB52705) else Color(0xFF585F6B)
                    val fontWt = if (isSelected) FontWeight.SemiBold else FontWeight.Medium

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .clickable {
                                val c = Calendar.getInstance()
                                c.add(Calendar.MONTH, months)
                                selectedDateMillis = c.timeInMillis
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
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

            Spacer(modifier = Modifier.height(10.dp))

            // Run-rate preview metric card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFFF8F5))
                    .border(1.dp, Color(0xFFF5DED4), RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5DED4)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                            contentDescription = "Pace",
                            tint = Color(0xFFB52705),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Estimated Monthly Run-Rate",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF585F6B)
                        )
                        Text(
                            text = "${CurrencyFormatter.formatLkr(estimatedRunRate)} / month",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB52705)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 5. Category Selection
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Goal Category",
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
                            .height(34.dp)
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
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = cat.label,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = contentColor
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 6. Funding Account Source
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = "Primary Funding Source",
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
                            if (isCardSelected) FigmaLightOrange else Color(0xFFE3BEB6),
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
                            tint = if (isCardSelected) FigmaLightOrange else Color(0xFF6B5B53),
                            modifier = Modifier.size(18.dp)
                        )

                        // Radio indicator
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(
                                    1.5.dp,
                                    if (isCardSelected) FigmaLightOrange else Color(0xFFE3BEB6),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCardSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(FigmaLightOrange)
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
                            if (isSecondarySelected) FigmaLightOrange else Color(0xFFE3BEB6),
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
                            contentDescription = "Commercial Bank",
                            tint = if (isSecondarySelected) FigmaLightOrange else Color(0xFF6B5B53),
                            modifier = Modifier.size(18.dp)
                        )

                        // Radio indicator
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .border(
                                    1.5.dp,
                                    if (isSecondarySelected) FigmaLightOrange else Color(0xFFE3BEB6),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSecondarySelected) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(FigmaLightOrange)
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

        // 7. Monthly Pace Tracking & Reminders
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
                ) { remindPace = !remindPace }
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
                            text = "Monthly Pace Reminders",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1B1C1A)
                        )
                        Text(
                            text = "Alert when falling behind run-rate",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF585F6B)
                        )
                    }
                }

                // Animated Custom Switch
                val trackColor by animateColorAsState(
                    targetValue = if (remindPace) FigmaLightOrange else Color(0xFFE4E2DE),
                    animationSpec = tween(200, easing = FastOutSlowInEasing),
                    label = "trackColor"
                )
                val thumbOffset by animateDpAsState(
                    targetValue = if (remindPace) 26.dp else 2.dp,
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
            // "+ Create Financial Goal" Primary Button (fill #FF5C38)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(CircleShape)
                    .background(FigmaLightOrange)
                    .clickable {
                        val rawTarget = targetAmountInput.replace(",", "").trim()
                        val parsedTarget = rawTarget.toDoubleOrNull()
                        val rawInitial = initialDepositInput.replace(",", "").trim()
                        val parsedInitial = rawInitial.toDoubleOrNull() ?: 0.0

                        if (title.isBlank()) {
                            errorMessage = "Please enter a goal title or select a suggestion."
                            return@clickable
                        }
                        if (parsedTarget == null || parsedTarget <= 0.0) {
                            errorMessage = "Please specify a valid target savings amount."
                            return@clickable
                        }

                        val runRate = (parsedTarget - parsedInitial).coerceAtLeast(0.0) / monthsRemaining
                        val newGoal = GoalEntity(
                            id = UUID.randomUUID().toString(),
                            title = title.trim(),
                            targetAmountLkr = parsedTarget,
                            currentSavedLkr = parsedInitial,
                            targetDateMillis = selectedDateMillis,
                            monthlyTargetRunRateLkr = runRate,
                            isCompleted = parsedInitial >= parsedTarget,
                            category = selectedCategoryKey,
                            initialDepositLkr = parsedInitial,
                            primaryFundingSource = selectedAccountKey,
                            remindMonthlyPace = remindPace,
                            createdAtMillis = System.currentTimeMillis()
                        )

                        onSubmit(newGoal)
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+ Create Financial Goal",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Dismiss Button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Cancel",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF585F6B)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }

    // Material3 Date Picker Dialog
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
                        text = "OK",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = FigmaLightOrange
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
