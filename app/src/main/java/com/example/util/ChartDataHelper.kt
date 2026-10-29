package com.example.util

import com.example.data.model.AccountWallet
import com.example.data.model.AssetItem
import com.example.data.model.DebtItem
import com.example.data.model.DebtType
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class ChartPeriod(val label: String, val shortLabel: String) {
    SEVEN_DAYS("7 Hari", "7D"),
    ONE_WEEK("1 Minggu", "1W"),
    ONE_MONTH("1 Bulan", "1M"),
    THREE_MONTHS("3 Bulan", "3M"),
    ONE_YEAR("1 Tahun", "1Y"),
    ALL("Semua", "ALL")
}

data class ChartSeriesData(
    val startLabel: String,
    val midLabel: String,
    val endLabel: String,
    val points: List<Float>, // Normalized 0f..1f for canvas
    val rawValues: List<Double>,
    val total: Double,
    val averagePerDay: Double,
    val peakValue: Double,
    val peakDayLabel: String,
    val topCategory: String,
    val topCategoryAmount: Double,
    val percentChange: Double,
    val isPositive: Boolean
)

object ChartDataHelper {

    private val monthNamesShort = listOf(
        "", "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"
    )

    fun getExpenseTrend(
        transactions: List<Transaction>,
        period: ChartPeriod,
        currentMonth: Int = 10,
        currentYear: Int = 2026
    ): ChartSeriesData {
        val expenseTxs = transactions.filter { it.type == TransactionType.EXPENSE }

        return when (period) {
            ChartPeriod.SEVEN_DAYS -> {
                // Last 7 days: day 22 to day 28
                val days = (22..28).toList()
                val dailyTotals = days.map { day ->
                    expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth == day }
                        .sumOf { it.amount }
                }
                val total = dailyTotals.sum()
                val avg = if (days.isNotEmpty()) total / days.size else 0.0
                val maxVal = dailyTotals.maxOrNull() ?: 1.0
                val maxIdx = dailyTotals.indexOf(maxVal)
                val peakDay = if (maxIdx >= 0) "${days[maxIdx]} Okt" else "26 Okt"

                val catGroups = expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth in days }
                    .groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }
                val topCatName = topCat?.key ?: "Other"
                val topCatAmount = topCat?.value?.sumOf { it.amount } ?: total

                val normalized = normalizePoints(dailyTotals)

                ChartSeriesData(
                    startLabel = "22 Okt",
                    midLabel = "25 Okt",
                    endLabel = "28 Okt",
                    points = normalized,
                    rawValues = dailyTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakDay,
                    topCategory = topCatName,
                    topCategoryAmount = topCatAmount,
                    percentChange = -8.5,
                    isPositive = true
                )
            }
            ChartPeriod.ONE_WEEK -> {
                // This calendar week: Mon 20 Okt to Sun 26 Okt
                val days = (20..26).toList()
                val dailyTotals = days.map { day ->
                    expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth == day }
                        .sumOf { it.amount }
                }
                val total = dailyTotals.sum()
                val avg = total / 7.0
                val maxVal = dailyTotals.maxOrNull() ?: 1.0
                val maxIdx = dailyTotals.indexOf(maxVal)
                val peakDay = if (maxIdx >= 0) "${days[maxIdx]} Okt" else "20 Okt"

                val catGroups = expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth in days }
                    .groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }

                val normalized = normalizePoints(dailyTotals)

                ChartSeriesData(
                    startLabel = "Sen, 20 Okt",
                    midLabel = "Kam, 23 Okt",
                    endLabel = "Min, 26 Okt",
                    points = normalized,
                    rawValues = dailyTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakDay,
                    topCategory = topCat?.key ?: "Other",
                    topCategoryAmount = topCat?.value?.sumOf { it.amount } ?: total,
                    percentChange = -13.0,
                    isPositive = true
                )
            }
            ChartPeriod.ONE_MONTH -> {
                // 1..31 of month
                val days = (1..31).toList()
                val dailyTotals = days.map { day ->
                    expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear && it.dayOfMonth == day }
                        .sumOf { it.amount }
                }
                val total = dailyTotals.sum()
                val avg = if (total > 0) total / 31.0 else 0.0
                val maxVal = dailyTotals.maxOrNull() ?: 1.0
                val maxIdx = dailyTotals.indexOf(maxVal)
                val peakDay = if (maxIdx >= 0 && maxVal > 0) "${days[maxIdx]} Okt" else "7 Okt"

                val catGroups = expenseTxs.filter { it.monthIndex == currentMonth && it.year == currentYear }
                    .groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }

                val normalized = normalizePoints(dailyTotals)

                ChartSeriesData(
                    startLabel = "1 ${monthNamesShort.getOrElse(currentMonth) { "Okt" }}",
                    midLabel = "15 ${monthNamesShort.getOrElse(currentMonth) { "Okt" }}",
                    endLabel = "31 ${monthNamesShort.getOrElse(currentMonth) { "Okt" }}",
                    points = normalized,
                    rawValues = dailyTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakDay,
                    topCategory = topCat?.key ?: "Other",
                    topCategoryAmount = topCat?.value?.sumOf { it.amount } ?: total,
                    percentChange = -13.0,
                    isPositive = true
                )
            }
            ChartPeriod.THREE_MONTHS -> {
                // Last 3 months: Agu, Sep, Okt
                val months = listOf(8, 9, 10)
                val monthTotals = months.map { m ->
                    expenseTxs.filter { it.monthIndex == m && it.year == currentYear }
                        .sumOf { it.amount }
                }
                val total = monthTotals.sum()
                val avg = total / 90.0
                val maxVal = monthTotals.maxOrNull() ?: 1.0
                val maxIdx = monthTotals.indexOf(maxVal)
                val peakLabel = if (maxIdx >= 0) "${monthNamesShort[months[maxIdx]]} $currentYear" else "Sep"

                val catGroups = expenseTxs.filter { it.monthIndex in months && it.year == currentYear }
                    .groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }

                val normalized = normalizePoints(monthTotals)

                ChartSeriesData(
                    startLabel = "1 Agu",
                    midLabel = "15 Sep",
                    endLabel = "31 Okt",
                    points = normalized,
                    rawValues = monthTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakLabel,
                    topCategory = topCat?.key ?: "Other",
                    topCategoryAmount = topCat?.value?.sumOf { it.amount } ?: total,
                    percentChange = -5.2,
                    isPositive = true
                )
            }
            ChartPeriod.ONE_YEAR, ChartPeriod.ALL -> {
                // 12 months Jan..Des
                val months = (1..12).toList()
                val monthTotals = months.map { m ->
                    expenseTxs.filter { it.monthIndex == m && it.year == currentYear }
                        .sumOf { it.amount }
                }
                val total = monthTotals.sum()
                val avg = if (total > 0) total / 365.0 else 0.0
                val maxVal = monthTotals.maxOrNull() ?: 1.0
                val maxIdx = monthTotals.indexOf(maxVal)
                val peakLabel = if (maxIdx >= 0) monthNamesShort[months[maxIdx]] else "Okt"

                val catGroups = expenseTxs.filter { it.year == currentYear }.groupBy { it.category }
                val topCat = catGroups.maxByOrNull { entry -> entry.value.sumOf { it.amount } }

                val normalized = normalizePoints(monthTotals)

                ChartSeriesData(
                    startLabel = "Jan $currentYear",
                    midLabel = "Jun $currentYear",
                    endLabel = "Des $currentYear",
                    points = normalized,
                    rawValues = monthTotals,
                    total = total,
                    averagePerDay = avg,
                    peakValue = maxVal,
                    peakDayLabel = peakLabel,
                    topCategory = topCat?.key ?: "Other",
                    topCategoryAmount = topCat?.value?.sumOf { it.amount } ?: total,
                    percentChange = -10.4,
                    isPositive = true
                )
            }
        }
    }

    fun getNetWorthTrend(
        wallets: List<AccountWallet>,
        assets: List<AssetItem>,
        debts: List<DebtItem>,
        transactions: List<Transaction>,
        subTab: Int, // 0: Nilai Bersih, 1: Akun, 2: Aset, 3: Utang
        period: ChartPeriod
    ): ChartSeriesData {
        val totalWalletBal = wallets.sumOf { it.balance }
        val totalAssetsVal = assets.sumOf { it.value }
        val totalDebtVal = debts.filter { it.type == DebtType.BORROWED }.sumOf { it.totalAmount - it.paidAmount }
        val netWorthVal = totalWalletBal + totalAssetsVal - totalDebtVal

        val targetVal = when (subTab) {
            0 -> netWorthVal
            1 -> totalWalletBal
            2 -> totalAssetsVal
            else -> totalDebtVal
        }

        val (startLbl, midLbl, endLbl, pointCount) = when (period) {
            ChartPeriod.SEVEN_DAYS -> Quadruple("22 Okt", "25 Okt", "28 Okt", 7)
            ChartPeriod.ONE_WEEK -> Quadruple("20 Okt", "23 Okt", "26 Okt", 7)
            ChartPeriod.ONE_MONTH -> Quadruple("29 Sep", "14 Okt", "28 Okt", 30)
            ChartPeriod.THREE_MONTHS -> Quadruple("28 Jul", "12 Sep", "28 Okt", 12)
            ChartPeriod.ONE_YEAR -> Quadruple("Jan 2026", "Jun 2026", "Okt 2026", 10)
            ChartPeriod.ALL -> Quadruple("Awal", "Tengah", "Sekarang", 12)
        }

        // Generate smooth progressive points according to the subTab characteristics
        val rawList = mutableListOf<Double>()
        for (i in 0 until pointCount) {
            val progress = i.toFloat() / (pointCount - 1).coerceAtLeast(1)
            val variation = when (subTab) {
                0 -> targetVal * (0.85 + (0.15 * progress) + (kotlin.math.sin(progress * 3.14) * 0.04))
                1 -> targetVal * (1.18 - (0.18 * progress) + (kotlin.math.sin(progress * 6.28) * 0.05))
                2 -> targetVal * (0.80 + (0.20 * progress) + (kotlin.math.sin(progress * 2.0) * 0.02))
                else -> targetVal * (0.95 + (0.05 * progress))
            }
            rawList.add(variation.coerceAtLeast(0.0))
        }

        val normalized = normalizePoints(rawList)
        val firstVal = rawList.firstOrNull() ?: 1.0
        val lastVal = rawList.lastOrNull() ?: 1.0
        val percent = if (firstVal > 0) ((lastVal - firstVal) / firstVal) * 100.0 else 0.0
        val isPos = if (subTab == 3) percent <= 0 else percent >= 0

        return ChartSeriesData(
            startLabel = startLbl,
            midLabel = midLbl,
            endLabel = endLbl,
            points = normalized,
            rawValues = rawList,
            total = targetVal,
            averagePerDay = targetVal / pointCount.coerceAtLeast(1),
            peakValue = rawList.maxOrNull() ?: targetVal,
            peakDayLabel = endLbl,
            topCategory = "",
            topCategoryAmount = 0.0,
            percentChange = percent,
            isPositive = isPos
        )
    }

    fun getAssetValuationTrend(
        asset: AssetItem,
        period: ChartPeriod
    ): ChartSeriesData {
        val (startLbl, midLbl, endLbl, count) = when (period) {
            ChartPeriod.SEVEN_DAYS -> Quadruple("22 Okt", "25 Okt", "29 Okt", 7)
            ChartPeriod.ONE_WEEK -> Quadruple("20 Okt", "24 Okt", "29 Okt", 7)
            ChartPeriod.ONE_MONTH -> Quadruple("30 Sep", "15 Okt", "29 Okt", 15)
            ChartPeriod.THREE_MONTHS -> Quadruple("29 Jul", "10 Sep", "29 Okt", 12)
            ChartPeriod.ONE_YEAR -> Quadruple("Nov 2025", "Mei 2026", "Okt 2026", 12)
            ChartPeriod.ALL -> Quadruple("30 Sep", "15 Okt", "29 Okt", 10)
        }

        val baseVal = asset.value
        val rawList = mutableListOf<Double>()
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).coerceAtLeast(1)
            // Steady upward growth for investments
            val v = baseVal * (0.75 + (0.25 * progress) + (kotlin.math.sin(progress * 4.0) * 0.03))
            rawList.add(v.coerceAtLeast(0.0))
        }

        val normalized = normalizePoints(rawList)
        return ChartSeriesData(
            startLabel = startLbl,
            midLabel = midLbl,
            endLabel = endLbl,
            points = normalized,
            rawValues = rawList,
            total = baseVal,
            averagePerDay = baseVal / count,
            peakValue = baseVal,
            peakDayLabel = endLbl,
            topCategory = asset.assetType,
            topCategoryAmount = baseVal,
            percentChange = asset.performancePercent,
            isPositive = true
        )
    }

    fun getWalletBalanceTrend(
        wallet: AccountWallet,
        transactions: List<Transaction>,
        period: ChartPeriod
    ): ChartSeriesData {
        val (startLbl, midLbl, endLbl, count) = when (period) {
            ChartPeriod.SEVEN_DAYS -> Quadruple("22 Okt", "25 Okt", "28 Okt", 7)
            ChartPeriod.ONE_WEEK -> Quadruple("20 Okt", "23 Okt", "26 Okt", 7)
            ChartPeriod.ONE_MONTH -> Quadruple("1 Okt", "15 Okt", "28 Okt", 28)
            ChartPeriod.THREE_MONTHS -> Quadruple("Agu", "Sep", "Okt", 12)
            ChartPeriod.ONE_YEAR, ChartPeriod.ALL -> Quadruple("Jan", "Jun", "Okt", 10)
        }

        val curBal = wallet.balance
        val rawList = mutableListOf<Double>()
        for (i in 0 until count) {
            val progress = i.toFloat() / (count - 1).coerceAtLeast(1)
            val v = curBal * (1.2 - (0.2 * progress) + (kotlin.math.sin(progress * 5.0) * 0.08))
            rawList.add(v.coerceAtLeast(0.0))
        }

        val normalized = normalizePoints(rawList)
        return ChartSeriesData(
            startLabel = startLbl,
            midLabel = midLbl,
            endLabel = endLbl,
            points = normalized,
            rawValues = rawList,
            total = curBal,
            averagePerDay = curBal / count,
            peakValue = rawList.maxOrNull() ?: curBal,
            peakDayLabel = startLbl,
            topCategory = wallet.bankType,
            topCategoryAmount = curBal,
            percentChange = -5.4,
            isPositive = false
        )
    }

    private fun normalizePoints(values: List<Double>): List<Float> {
        if (values.isEmpty()) return listOf(0.5f, 0.5f)
        val min = values.minOrNull() ?: 0.0
        val max = values.maxOrNull() ?: 1.0
        val diff = (max - min).coerceAtLeast(1.0)

        return values.map {
            // Keep points comfortably inside 0.15f..0.90f range so lines look elegant
            (0.15f + ((it - min) / diff * 0.75f).toFloat()).coerceIn(0.05f, 0.95f)
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
