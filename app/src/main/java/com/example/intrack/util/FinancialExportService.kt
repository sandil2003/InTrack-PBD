package com.example.intrack.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.intrack.data.model.ExpenseEntity
import com.example.intrack.data.model.GoalEntity
import com.example.intrack.data.model.IncomeEntity
import com.example.intrack.data.model.SubscriptionEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FinancialExportService {

    private val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private fun getExportDir(context: Context): File {
        val dir = File(context.cacheDir, "exports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun buildCsvContent(
        incomes: List<IncomeEntity>,
        expenses: List<ExpenseEntity>,
        subscriptions: List<SubscriptionEntity>,
        goals: List<GoalEntity>
    ): String {
        val sb = StringBuilder()
        sb.append("InTrack Financial Management System - Exported Statement\n")
        sb.append("Generated At,${sdf.format(Date())}\n\n")

        // 1. INCOMES
        sb.append("--- INCOMES ---\n")
        sb.append("Date,Channel,Title,Original Currency,Original Amount,Exchange Rate,Realized Amount (LKR),Status,Client\n")
        incomes.forEach { inc ->
            val date = dateOnlyFormat.format(Date(inc.dateReceivedMillis))
            val title = "\"${inc.title.replace("\"", "\"\"")}\""
            val client = "\"${(inc.clientName ?: "").replace("\"", "\"\"")}\""
            sb.append("$date,${inc.channel},$title,${inc.originalCurrency},${inc.originalAmount},${inc.exchangeRate},${inc.realizedAmountLkr},${inc.status},$client\n")
        }
        sb.append("\n")

        // 2. EXPENSES
        sb.append("--- EXPENSES ---\n")
        sb.append("Date,Category,Title,Amount (LKR),Payment Account,Commitment Type,Notes\n")
        expenses.forEach { exp ->
            val date = dateOnlyFormat.format(Date(exp.dateSpentMillis))
            val title = "\"${exp.title.replace("\"", "\"\"")}\""
            val notes = "\"${(exp.notes ?: "").replace("\"", "\"\"")}\""
            sb.append("$date,${exp.category},$title,${exp.amountLkr},${exp.paymentAccount},${exp.commitmentType},$notes\n")
        }
        sb.append("\n")

        // 3. SUBSCRIPTIONS
        sb.append("--- RECURRING OBLIGATIONS & SUBSCRIPTIONS ---\n")
        sb.append("Name,Amount (LKR),Billing Cycle,Payment Account,Status,Flagged For Review\n")
        subscriptions.forEach { sub ->
            val name = "\"${sub.name.replace("\"", "\"\"")}\""
            sb.append("$name,${sub.amountLkr},${sub.billingCycle},${sub.paymentAccount},${sub.status},${sub.isFlaggedForReview}\n")
        }
        sb.append("\n")

        // 4. GOALS
        sb.append("--- SAVINGS GOALS ---\n")
        sb.append("Goal Title,Target (LKR),Saved (LKR),Remaining (LKR),Completion %,Completed\n")
        goals.forEach { g ->
            val title = "\"${g.title.replace("\"", "\"\"")}\""
            val remaining = (g.targetAmountLkr - g.currentSavedLkr).coerceAtLeast(0.0)
            val pct = if (g.targetAmountLkr > 0) String.format(Locale.US, "%.1f", (g.currentSavedLkr / g.targetAmountLkr) * 100) else "0.0"
            sb.append("$title,${g.targetAmountLkr},${g.currentSavedLkr},$remaining,$pct%,${g.isCompleted}\n")
        }

        return sb.toString()
    }

    fun exportCsv(
        context: Context,
        incomes: List<IncomeEntity>,
        expenses: List<ExpenseEntity>,
        subscriptions: List<SubscriptionEntity>,
        goals: List<GoalEntity>
    ): File {
        val exportDir = getExportDir(context)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(exportDir, "InTrack_Statement_$timestamp.csv")

        val csvContent = buildCsvContent(incomes, expenses, subscriptions, goals)

        FileOutputStream(file).use { out ->
            out.write(csvContent.toByteArray(Charsets.UTF_8))
        }

        return file
    }

    fun exportPdf(
        context: Context,
        userName: String,
        incomes: List<IncomeEntity>,
        expenses: List<ExpenseEntity>,
        subscriptions: List<SubscriptionEntity>,
        goals: List<GoalEntity>
    ): File {
        val exportDir = getExportDir(context)
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(exportDir, "InTrack_Statement_$timestamp.pdf")

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 at 72dpi
        val page = pdfDoc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val titlePaint = Paint().apply {
            color = Color.rgb(27, 31, 38)
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subtitlePaint = Paint().apply {
            color = Color.rgb(108, 114, 127)
            textSize = 10.5f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val sectionPaint = Paint().apply {
            color = Color.rgb(249, 115, 22) // Brand Tangerine
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyBoldPaint = Paint().apply {
            color = Color.rgb(33, 37, 41)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.rgb(55, 65, 81)
            textSize = 9f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(229, 231, 235)
            strokeWidth = 1f
        }

        var y = 45f

        // Document Title
        canvas.drawText("InTrack Financial Audit Statement", 36f, y, titlePaint)
        y += 16f
        canvas.drawText("User: ${userName.ifBlank { "Mr. Kavindu Silva" }}  •  Generated: ${sdf.format(Date())}  •  Base Currency: LKR", 36f, y, subtitlePaint)
        y += 16f
        canvas.drawLine(36f, y, 559f, y, linePaint)
        y += 24f

        // 1. Executive Summary KPIs
        val paidIncomes = incomes.filter { it.status.equals("PAID", ignoreCase = true) }
        val totalInflow = paidIncomes.sumOf { it.realizedAmountLkr }
        val nonTransferExpenses = expenses.filter { !it.isInternalTransfer() }
        val totalOutflow = nonTransferExpenses.sumOf { it.amountLkr }
        val netCashFlow = totalInflow - totalOutflow

        canvas.drawText("1. EXECUTIVE CASH FLOW OVERVIEW", 36f, y, sectionPaint)
        y += 16f
        canvas.drawText("Total Realized Inflows (Paid):", 44f, y, bodyBoldPaint)
        canvas.drawText(CurrencyFormatter.formatLkr(totalInflow), 300f, y, bodyPaint)
        y += 14f
        canvas.drawText("Total Recorded Outflows (Excl. Transfers):", 44f, y, bodyBoldPaint)
        canvas.drawText(CurrencyFormatter.formatLkr(totalOutflow), 300f, y, bodyPaint)
        y += 14f
        canvas.drawText("Net Cash Flow:", 44f, y, bodyBoldPaint)
        val netText = if (netCashFlow >= 0) "+${CurrencyFormatter.formatLkr(netCashFlow)}" else "-${CurrencyFormatter.formatLkr(-netCashFlow)}"
        canvas.drawText(netText, 300f, y, bodyBoldPaint)
        y += 22f

        // 2. Primary Goal: MacBook Pro M4 Fund
        val macBookGoal = goals.find { it.id == "macbook_m4_goal" } ?: goals.firstOrNull() ?: GoalEntity()
        canvas.drawText("2. MACBOOK PRO M4 SAVINGS RUN-RATE", 36f, y, sectionPaint)
        y += 16f
        canvas.drawText("Target Configuration:", 44f, y, bodyBoldPaint)
        canvas.drawText("Apple MacBook Pro M4 (${CurrencyFormatter.formatLkr(macBookGoal.targetAmountLkr)})", 300f, y, bodyPaint)
        y += 14f
        canvas.drawText("Current Saved Progress:", 44f, y, bodyBoldPaint)
        val goalPct = if (macBookGoal.targetAmountLkr > 0) (macBookGoal.currentSavedLkr / macBookGoal.targetAmountLkr) * 100 else 0.0
        canvas.drawText("${CurrencyFormatter.formatLkr(macBookGoal.currentSavedLkr)} (${String.format(Locale.US, "%.1f", goalPct)}% achieved)", 300f, y, bodyPaint)
        y += 14f
        canvas.drawText("Required Monthly Run-Rate:", 44f, y, bodyBoldPaint)
        canvas.drawText("LKR 40,833.33 / month (12 Months Target)", 300f, y, bodyPaint)
        y += 22f

        // 3. Multi-Channel Income Breakdown & Crypto YTD
        canvas.drawText("3. MULTI-CHANNEL INCOME STREAMS", 36f, y, sectionPaint)
        y += 16f
        val salaryTotal = paidIncomes.filter { it.channel.equals("SALARY", ignoreCase = true) }.sumOf { it.realizedAmountLkr }
        val freelanceTotal = paidIncomes.filter { it.channel.equals("FREELANCE", ignoreCase = true) }.sumOf { it.realizedAmountLkr }
        val adsenseTotal = paidIncomes.filter { it.channel.equals("ADSENSE", ignoreCase = true) }.sumOf { it.realizedAmountLkr }
        val cryptoTotal = paidIncomes.filter { it.channel.equals("CRYPTO", ignoreCase = true) }.sumOf { it.realizedAmountLkr }

        canvas.drawText("Monthly Salary (Base + Bonus):", 44f, y, bodyPaint)
        canvas.drawText(CurrencyFormatter.formatLkr(salaryTotal), 300f, y, bodyPaint)
        y += 13f
        canvas.drawText("Freelance Web Projects (Paid):", 44f, y, bodyPaint)
        canvas.drawText(CurrencyFormatter.formatLkr(freelanceTotal), 300f, y, bodyPaint)
        y += 13f
        canvas.drawText("Google AdSense (USD converted):", 44f, y, bodyPaint)
        canvas.drawText(CurrencyFormatter.formatLkr(adsenseTotal), 300f, y, bodyPaint)
        y += 13f
        canvas.drawText("Binance Crypto Realized YTD Net:", 44f, y, bodyPaint)
        val cryptoNetStr = if (cryptoTotal >= 0) "+${CurrencyFormatter.formatLkr(cryptoTotal)}" else "-${CurrencyFormatter.formatLkr(-cryptoTotal)}"
        canvas.drawText(cryptoNetStr, 300f, y, bodyBoldPaint)
        y += 22f

        // 4. Subscriptions & Zombie Leak Detection
        canvas.drawText("4. RECURRING OBLIGATIONS & ZOMBIE LEAK AUDIT", 36f, y, sectionPaint)
        y += 16f
        val zombieSubs = subscriptions.filter { it.isFlaggedForReview || it.status.equals("UNUSED_FLAGGED", ignoreCase = true) }
        val totalRecurring = subscriptions.filter { !it.status.equals("CANCELLED", ignoreCase = true) }.sumOf { it.amountLkr }
        canvas.drawText("Active Recurring Outflows:", 44f, y, bodyPaint)
        canvas.drawText("${CurrencyFormatter.formatLkr(totalRecurring)} / month", 300f, y, bodyPaint)
        y += 13f
        canvas.drawText("Zombie Subscriptions Detected:", 44f, y, bodyBoldPaint)
        canvas.drawText("${zombieSubs.size} services flagged (Leak: ${CurrencyFormatter.formatLkr(zombieSubs.sumOf { it.amountLkr })}/mo)", 300f, y, bodyBoldPaint)
        y += 22f

        // 5. Recent Ledger Entries Table
        canvas.drawText("5. RECENT TRANSACTION ENTRIES (SAMPLE LEDGER)", 36f, y, sectionPaint)
        y += 16f
        canvas.drawText("DATE", 44f, y, bodyBoldPaint)
        canvas.drawText("DESCRIPTION", 130f, y, bodyBoldPaint)
        canvas.drawText("CATEGORY / CHANNEL", 330f, y, bodyBoldPaint)
        canvas.drawText("AMOUNT (LKR)", 460f, y, bodyBoldPaint)
        y += 6f
        canvas.drawLine(36f, y, 559f, y, linePaint)
        y += 12f

        expenses.take(8).forEach { exp ->
            val date = dateOnlyFormat.format(Date(exp.dateSpentMillis))
            val title = if (exp.title.length > 28) exp.title.take(25) + "..." else exp.title
            canvas.drawText(date, 44f, y, bodyPaint)
            canvas.drawText(title, 130f, y, bodyPaint)
            canvas.drawText(exp.category, 330f, y, bodyPaint)
            canvas.drawText(CurrencyFormatter.formatLkr(exp.amountLkr), 460f, y, bodyPaint)
            y += 14f
        }

        // Footer
        canvas.drawLine(36f, 800f, 559f, 800f, linePaint)
        canvas.drawText("InTrack SE3092 Platform Based Development • Confidential Financial Record • Page 1 of 1", 36f, 816f, subtitlePaint)

        pdfDoc.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDoc.writeTo(out)
        }
        pdfDoc.close()

        return file
    }

    fun shareExportedFile(context: Context, file: File, mimeType: String, chooserTitle: String = "Share Financial Statement") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, chooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
