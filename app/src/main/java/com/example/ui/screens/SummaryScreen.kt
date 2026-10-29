package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dialogs.MonthYearPickerDialog
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.AccentBlueBright
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenSoft
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.ChartDataHelper
import com.example.util.ChartPeriod
import com.example.util.CurrencyHelper

@Composable
fun SummaryScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedMonthIndex.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    val monthOptions by viewModel.monthOptions.collectAsState()
    val selectedWallet by viewModel.selectedWalletFilter.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val summaryTab by viewModel.summaryTab.collectAsState()
    val summaryPeriod by viewModel.summaryPeriod.collectAsState()
    val monthlyIncome by viewModel.monthlyIncome.collectAsState()
    val monthlyExpense by viewModel.monthlyExpense.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var showWalletDropdown by remember { mutableStateOf(false) }
    var isPlannedActive by remember { mutableStateOf(true) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    var chartPeriod by remember { mutableStateOf(ChartPeriod.ONE_MONTH) }

    val chartData = remember(transactions, chartPeriod, selectedMonth, selectedYear) {
        ChartDataHelper.getExpenseTrend(transactions, chartPeriod, selectedMonth, selectedYear)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Ringkasan",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            // Wallet Filter
            Box {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.clickable { showWalletDropdown = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedWallet,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextWhite
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = showWalletDropdown,
                    onDismissRequest = { showWalletDropdown = false },
                    modifier = Modifier.background(DarkSurfaceElevated)
                ) {
                    DropdownMenuItem(
                        text = { Text("Semua", color = TextWhite) },
                        onClick = {
                            viewModel.selectWalletFilter("Semua")
                            showWalletDropdown = false
                        }
                    )
                    wallets.forEach { wallet ->
                        DropdownMenuItem(
                            text = { Text(wallet.name, color = TextWhite) },
                            onClick = {
                                viewModel.selectWalletFilter(wallet.name)
                                showWalletDropdown = false
                            }
                        )
                    }
                }
            }
        }

        // Month Selector: Fixed Calendar Button on the left + Horizontally scrollable Month Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Fixed Calendar Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier
                    .padding(start = 16.dp, end = 8.dp)
                    .clickable { showMonthYearPicker = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Pilih Bulan dan Tahun",
                        tint = AccentBlueBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            // Scrollable Month Chips
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                monthOptions.forEach { (index, label) ->
                    val isSelected = selectedMonth == index
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = if (isSelected) AccentBlue else DarkSurfaceElevated,
                        modifier = Modifier.clickable { viewModel.selectMonth(index) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Sub-tabs row: Statistik, Laporan, Kalender
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val tabs = listOf(
                Triple(0, "Statistik", Icons.Default.BarChart),
                Triple(1, "Laporan", Icons.Default.Description),
                Triple(2, "Kalender", Icons.Default.CalendarMonth)
            )

            tabs.forEach { (index, title, icon) ->
                val isSelected = summaryTab == index
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AccentBlue else DarkSurfaceElevated,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setSummaryTab(index) }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) Color.White else TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextMuted
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // "Buku harian uang Anda" Banner Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF1B2238),
                                        Color(0xFF151928),
                                        Color(0xFF111420)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0x33F59E0B),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = Color(0xFFFBBF24),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Buku harian uang Anda",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = "Melihat kembali dengan lembut ke mana uang Anda pergi",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }

                                // Moon icon
                                Text(text = "🌙", fontSize = 24.sp)
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0x33FFFFFF)
                                ) {
                                    Text(
                                        text = "🌙 Okt 2026",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextWhite,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0x33FFFFFF)
                                ) {
                                    Text(
                                        text = "⭐ 7 hari lalu",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextWhite,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Period Selector (Bulanan / Tahunan / Kustom)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sekilas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )

                    Row(
                        modifier = Modifier
                            .background(DarkSurfaceElevated, RoundedCornerShape(14.dp))
                            .padding(2.dp)
                    ) {
                        listOf("Bulanan", "Tahunan", "Kustom").forEachIndexed { index, title ->
                            val isSelected = summaryPeriod == index
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) AccentBlue else Color.Transparent,
                                modifier = Modifier.clickable { viewModel.setSummaryPeriod(index) }
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextMuted,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Card "AKTIVITAS PENGELUARAN" (Daily Trend Chart)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AKTIVITAS PENGELUARAN",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = "${monthOptions.getOrElse(selectedMonth) { "Okt" }} $selectedYear",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }

                            // Period selector chips inside card
                            Row(
                                modifier = Modifier
                                    .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                                    .padding(2.dp)
                            ) {
                                listOf(
                                    ChartPeriod.SEVEN_DAYS,
                                    ChartPeriod.ONE_WEEK,
                                    ChartPeriod.ONE_MONTH,
                                    ChartPeriod.ONE_YEAR
                                ).forEach { p ->
                                    val isSelected = chartPeriod == p
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (isSelected) AccentBlue else Color.Transparent,
                                        modifier = Modifier.clickable { chartPeriod = p }
                                    ) {
                                        Text(
                                            text = p.shortLabel,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextMuted,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rp${CurrencyHelper.formatCompact(chartData.total)}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pengeluaran",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (chartData.percentChange <= 0) IncomeGreenSoft else Color(0x33F43F5E)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (chartData.percentChange <= 0) Icons.AutoMirrored.Filled.TrendingDown else Icons.Default.BarChart,
                                        contentDescription = null,
                                        tint = if (chartData.percentChange <= 0) IncomeGreen else ExpenseRed,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = "${if (chartData.percentChange > 0) "+" else ""}${chartData.percentChange}% vs periode lalu",
                                        fontSize = 10.sp,
                                        color = if (chartData.percentChange <= 0) IncomeGreen else ExpenseRed,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Custom Interactive Line Chart Canvas with dynamic points
                        DailyExpenseLineChart(
                            points = chartData.points,
                            peakIndex = chartData.rawValues.indexOf(chartData.peakValue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(chartData.startLabel, fontSize = 10.sp, color = TextSubtle)
                            Text(chartData.midLabel, fontSize = 10.sp, color = TextSubtle)
                            Text(chartData.endLabel, fontSize = 10.sp, color = TextSubtle)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // 3 Dynamic summary metrics
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Per hari", fontSize = 11.sp, color = TextSubtle)
                                Text("Rp${CurrencyHelper.formatCompact(chartData.averagePerDay)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                            }
                            Column {
                                Text("Hari terboros", fontSize = 11.sp, color = TextSubtle)
                                Text("Rp${CurrencyHelper.formatCompact(chartData.peakValue)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text(chartData.peakDayLabel, fontSize = 10.sp, color = TextSubtle)
                            }
                            Column {
                                Text("Kategori teratas", fontSize = 11.sp, color = TextSubtle)
                                Text(chartData.topCategory, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                                Text("Rp${CurrencyHelper.formatCompact(chartData.topCategoryAmount)}", fontSize = 10.sp, color = TextSubtle)
                            }
                        }
                    }
                }
            }

            // Card "Ringkasan: Arus Kas Bulanan"
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Ringkasan",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSubtle
                        )
                        Text(
                            text = "Arus Kas Bulanan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Income Bar
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(IncomeGreen)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = CurrencyHelper.formatRupiah(monthlyIncome),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pemasukan",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { 1.0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = IncomeGreen,
                            trackColor = DarkSurfaceElevated
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Expense Bar
                        val expenseRatio = if (monthlyIncome > 0) (monthlyExpense / monthlyIncome).toFloat().coerceIn(0f, 1f) else 0.7f
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(ExpenseRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = CurrencyHelper.formatRupiah(monthlyExpense),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pengeluaran",
                                fontSize = 11.sp,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { expenseRatio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = ExpenseRed,
                            trackColor = DarkSurfaceElevated
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Text analysis card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val net = monthlyIncome - monthlyExpense
                            Text(
                                text = "Pada Okt 2026, arus kas bulanan Anda positif. Pemasukan Anda +${CurrencyHelper.formatRupiah(net)} lebih besar dari pengeluaran.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                lineHeight = 18.sp,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }

            // Card "Terencana"
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Terencana",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextSubtle
                                )
                                Text(
                                    text = "Masih direncanakan",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextWhite
                                )
                            }
                            Switch(
                                checked = isPlannedActive,
                                onCheckedChange = { isPlannedActive = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AccentBlue,
                                    uncheckedThumbColor = TextMuted,
                                    uncheckedTrackColor = DarkSurfaceElevated
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Tagihan dan pemasukan dari daftar berulang Anda yang masih akan datang di periode ini.",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tidak ada lagi yang direncanakan untuk periode ini.",
                            fontSize = 12.sp,
                            color = TextSubtle
                        )
                    }
                }
            }

            // Card "Pendapatan vs Pengeluaran" (Cumulative)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pendapatan vs Pengeluaran",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "Kumulatif",
                                fontSize = 11.sp,
                                color = TextSubtle
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "+Rp2M",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                                Text(
                                    text = "Ditabung periode ini",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = DarkSurfaceElevated
                            ) {
                                Text(
                                    text = "30% pemasukan tersisa",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = IncomeGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp, 3.dp).background(IncomeGreen))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pemasukan Rp6.8M", fontSize = 11.sp, color = TextMuted)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(10.dp, 3.dp).background(ExpenseRed))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pengeluaran Rp4.8M", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Cumulative dual line chart
                        CumulativeComparisonChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            listOf("1", "9", "17", "25", "31").forEach {
                                Text(text = it, fontSize = 10.sp, color = TextSubtle)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "📈", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Pemasukan tetap unggul sepanjang periode",
                                fontSize = 11.sp,
                                color = IncomeGreen
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    if (showMonthYearPicker) {
        MonthYearPickerDialog(
            currentMonth = selectedMonth,
            currentYear = selectedYear,
            onDismiss = { showMonthYearPicker = false },
            onSelectMonthYear = { m, y ->
                viewModel.selectMonthAndYear(m, y)
            }
        )
    }
}

@Composable
fun DailyExpenseLineChart(
    points: List<Float> = emptyList(),
    peakIndex: Int = -1,
    modifier: Modifier = Modifier
) {
    val effectivePoints = if (points.size >= 2) points else listOf(0.2f, 0.35f, 0.25f, 0.4f, 0.3f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val stepX = width / (effectivePoints.size - 1).coerceAtLeast(1)

        // Draw baseline previous period dotted line
        val prevPath = Path()
        effectivePoints.forEachIndexed { i, value ->
            val x = i * stepX
            // Dotted comparison baseline slightly varied
            val prevVal = (value * 0.85f + 0.05f).coerceIn(0.05f, 0.95f)
            val y = height - (prevVal * height * 0.85f)
            if (i == 0) prevPath.moveTo(x, y) else prevPath.lineTo(x, y)
        }
        drawPath(
            path = prevPath,
            color = Color(0xFF64748B),
            style = Stroke(
                width = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        )

        // Draw current period filled gradient wave & solid line
        val path = Path()
        val fillPath = Path()
        fillPath.moveTo(0f, height)

        effectivePoints.forEachIndexed { i, value ->
            val x = i * stepX
            val y = height - (value * height * 0.85f).coerceAtMost(height * 0.95f)
            if (i == 0) {
                path.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                path.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x40F43F5E), Color(0x00F43F5E))
            )
        )

        drawPath(
            path = path,
            color = ExpenseRed,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Highlight the peak point
        val validPeakIndex = if (peakIndex in effectivePoints.indices) {
            peakIndex
        } else {
            effectivePoints.indices.maxByOrNull { effectivePoints[it] } ?: (effectivePoints.size / 2)
        }
        val peakX = validPeakIndex * stepX
        val peakY = height - (effectivePoints[validPeakIndex] * height * 0.85f).coerceAtMost(height * 0.95f)

        drawCircle(
            color = Color.White,
            radius = 5.dp.toPx(),
            center = Offset(peakX, peakY)
        )
        drawCircle(
            color = ExpenseRed,
            radius = 3.dp.toPx(),
            center = Offset(peakX, peakY)
        )
    }
}

@Composable
fun CumulativeComparisonChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Income cumulative curve (fast jump on day 1 to ~5.8M, small bumps)
        val incomePath = Path()
        val incomePoints = listOf(
            Offset(0f, height * 0.75f),
            Offset(width * 0.25f, height * 0.28f),
            Offset(width * 0.5f, height * 0.28f),
            Offset(width * 0.65f, height * 0.20f),
            Offset(width, height * 0.20f)
        )

        incomePoints.forEachIndexed { index, pt ->
            if (index == 0) incomePath.moveTo(pt.x, pt.y)
            else incomePath.lineTo(pt.x, pt.y)
        }

        drawPath(
            path = incomePath,
            color = IncomeGreen,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )

        // Expense cumulative curve (steady gradual rise from day 1 to day 31)
        val expensePath = Path()
        val expensePoints = listOf(
            Offset(0f, height * 0.88f),
            Offset(width * 0.15f, height * 0.85f),
            Offset(width * 0.3f, height * 0.55f),
            Offset(width * 0.55f, height * 0.52f),
            Offset(width * 0.75f, height * 0.44f),
            Offset(width, height * 0.42f)
        )

        expensePoints.forEachIndexed { index, pt ->
            if (index == 0) expensePath.moveTo(pt.x, pt.y)
            else expensePath.lineTo(pt.x, pt.y)
        }

        drawPath(
            path = expensePath,
            color = ExpenseRed,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
