package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountWallet
import com.example.data.model.AssetItem
import com.example.data.model.DebtItem
import com.example.util.ChartDataHelper
import com.example.util.ChartPeriod
import com.example.ui.dialogs.EditDebtDialog
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkCardSubtle
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.ExpenseRedSoft
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.IncomeGreenSoft
import com.example.ui.theme.SegmentAccountBlue
import com.example.ui.theme.SegmentAssetGreen
import com.example.ui.theme.SegmentDebtRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.theme.TrophyYellow
import com.example.ui.theme.TrophyYellowLight
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyHelper

@Composable
fun AccountScreen(
    viewModel: FinanceViewModel,
    onAddWalletClick: () -> Unit,
    onAddAssetClick: () -> Unit,
    onAddDebtClick: () -> Unit,
    onPayDebtClick: (DebtItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedWalletIdForDetail by remember { mutableStateOf<String?>(null) }
    var selectedAssetIdForDetail by remember { mutableStateOf<String?>(null) }
    var debtToEdit by remember { mutableStateOf<DebtItem?>(null) }
    var debtToDelete by remember { mutableStateOf<DebtItem?>(null) }

    if (selectedWalletIdForDetail != null) {
        AccountDetailScreen(
            walletId = selectedWalletIdForDetail!!,
            viewModel = viewModel,
            onBack = { selectedWalletIdForDetail = null }
        )
        return
    }

    if (selectedAssetIdForDetail != null) {
        AssetDetailScreen(
            assetId = selectedAssetIdForDetail!!,
            viewModel = viewModel,
            onBack = { selectedAssetIdForDetail = null }
        )
        return
    }

    val subTab by viewModel.accountSubTab.collectAsState()
    val totalWalletBalance by viewModel.totalAllWalletsBalance.collectAsState()
    val totalAssets by viewModel.totalAssetsValue.collectAsState()
    val totalDebts by viewModel.totalDebtsValue.collectAsState()
    val netWorth by viewModel.netWorthValue.collectAsState()

    val wallets by viewModel.wallets.collectAsState()
    val assets by viewModel.assets.collectAsState()
    val debts by viewModel.debts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()

    var selectedTimePeriod by remember { mutableStateOf("1M") }

    val currentChartPeriod = when (selectedTimePeriod) {
        "7D" -> ChartPeriod.SEVEN_DAYS
        "3M" -> ChartPeriod.THREE_MONTHS
        "YTD" -> ChartPeriod.ONE_YEAR
        "ALL" -> ChartPeriod.ALL
        else -> ChartPeriod.ONE_MONTH
    }

    val chartData = remember(wallets, assets, debts, transactions, subTab, currentChartPeriod) {
        ChartDataHelper.getNetWorthTrend(wallets, assets, debts, transactions, subTab, currentChartPeriod)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Akun",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                when (subTab) {
                    1 -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentBlue,
                            modifier = Modifier
                                .clickable(onClick = onAddWalletClick)
                                .testTag("btn_header_add_wallet")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Buat Akun",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    2 -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentBlue,
                            modifier = Modifier
                                .clickable(onClick = onAddAssetClick)
                                .testTag("btn_header_add_asset")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Buat Aset",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    3 -> {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AccentBlue,
                            modifier = Modifier
                                .clickable(onClick = onAddDebtClick)
                                .testTag("btn_header_add_debt")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Tambah Utang",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Pengaturan Akun",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Top Metric & Chart based on Sub-Tab
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (subTab) {
                        0 -> {
                            // Nilai Bersih
                            Text(
                                text = CurrencyHelper.formatRupiah(netWorth),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+Rp2.918.480  +55.4%  1M",
                                    fontSize = 12.sp,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Nilai Bersih", fontSize = 13.sp, color = TextMuted)
                            Text(
                                text = "📈 Dalam 30 hari, sesuai rencana: Rp10.854.093",
                                fontSize = 11.sp,
                                color = TextSubtle
                            )
                        }
                        1 -> {
                            // Akun / Dompet
                            Text(
                                text = CurrencyHelper.formatRupiah(totalWalletBalance),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = ExpenseRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "-Rp213.010  -31.7%  1M",
                                    fontSize = 12.sp,
                                    color = ExpenseRed,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Total Saldo Tersedia · ${wallets.size} Dompet",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        }
                        2 -> {
                            // Aset
                            Text(
                                text = CurrencyHelper.formatRupiah(totalAssets),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "+Rp1.465.745  +10.2%  1M",
                                    fontSize = 12.sp,
                                    color = IncomeGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Total Nilai Aset", fontSize = 13.sp, color = TextMuted)
                        }
                        3 -> {
                            // Utang
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(ExpenseRed))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("DIPINJAM", fontSize = 10.sp, color = TextSubtle, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = CurrencyHelper.formatRupiah(totalDebts),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text("▲ 3.9%  1M", fontSize = 11.sp, color = ExpenseRed)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(IncomeGreen))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("DIPINJAMKAN", fontSize = 10.sp, color = TextSubtle, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "Rp0",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text("▼ 0.0%  1M", fontSize = 11.sp, color = IncomeGreen)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Time Period Selector
                    Row(
                        modifier = Modifier
                            .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(2.dp)
                    ) {
                        listOf("7D", "1M", "3M", "YTD", "ALL").forEach { period ->
                            val isSelected = selectedTimePeriod == period
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentBlue else Color.Transparent,
                                modifier = Modifier.clickable { selectedTimePeriod = period }
                            ) {
                                Text(
                                    text = period,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextMuted,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Trend Line Canvas
                    NetWorthTrendCanvas(
                        points = chartData.points,
                        subTab = subTab,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(chartData.startLabel, fontSize = 10.sp, color = TextSubtle)
                        Text(chartData.midLabel, fontSize = 10.sp, color = TextSubtle)
                        Text(chartData.endLabel, fontSize = 10.sp, color = TextSubtle)
                    }
                }
            }


            // Content based on selected Sub-Tab
            when (subTab) {
                0 -> {
                    // Nilai Bersih View
                    item {
                        // Segmented proportion bar & 3 totals
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total Saldo", fontSize = 11.sp, color = TextSubtle)
                                        Text(
                                            CurrencyHelper.formatRupiah(totalWalletBalance),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SegmentAccountBlue
                                        )
                                    }
                                    Column {
                                        Text("Total Aset", fontSize = 11.sp, color = TextSubtle)
                                        Text(
                                            CurrencyHelper.formatRupiah(totalAssets),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SegmentAssetGreen
                                        )
                                    }
                                    Column {
                                        Text("Total Utang", fontSize = 11.sp, color = TextSubtle)
                                        Text(
                                            "-${CurrencyHelper.formatRupiah(totalDebts)}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SegmentDebtRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Segmented Multi-Color Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                ) {
                                    Box(modifier = Modifier.weight(0.02f).fillMaxSize().background(SegmentAccountBlue))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Box(modifier = Modifier.weight(0.65f).fillMaxSize().background(SegmentAssetGreen))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Box(modifier = Modifier.weight(0.33f).fillMaxSize().background(SegmentDebtRed))
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SegmentAccountBlue))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Akun 2%", fontSize = 10.sp, color = TextSubtle)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SegmentAssetGreen))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Aset 65%", fontSize = 10.sp, color = TextSubtle)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SegmentDebtRed))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Utang 33%", fontSize = 10.sp, color = TextSubtle)
                                    }
                                }
                            }
                        }
                    }

                    // Monthly Net Worth Bar Chart Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Nilai Bersih",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text("Bulan", fontSize = 11.sp, color = TextSubtle)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Monthly bar chart
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Bottom
                                ) {
                                    val months = listOf(
                                        Triple("Mei", -15.6f, "-15.6M"),
                                        Triple("Jun", -5.3f, "-5.3M"),
                                        Triple("Jul", 0.44f, "447.8K"),
                                        Triple("Agu", 3.4f, "3.4M"),
                                        Triple("Sep", 5.0f, "5.0M"),
                                        Triple("Okt", 8.2f, "8.2M")
                                    )

                                    months.forEach { (m, v, label) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(text = label, fontSize = 8.sp, color = TextSubtle)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            val barHeight = ((Math.abs(v) / 16f) * 60).dp.coerceAtLeast(6.dp)
                                            Box(
                                                modifier = Modifier
                                                    .width(36.dp)
                                                    .height(barHeight)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(if (v >= 0) SegmentAssetGreen else Color(0xFF6B2D38))
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = m, fontSize = 10.sp, color = TextMuted)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = DarkSurfaceElevated,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = IncomeGreenSoft,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                                    contentDescription = null,
                                                    tint = IncomeGreen,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Nilai bersih cenderung naik",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = "Rata-rata naik +Rp12.340.345 dalam 3 bulan terakhir",
                                                fontSize = 11.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Total Utang Ratio Card
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
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.AccountBalance,
                                            contentDescription = null,
                                            tint = TrophyYellow,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Total Utang",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextWhite
                                        )
                                    }
                                    Text(
                                        text = "49.6%",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TrophyYellowLight
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { 0.496f },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = TrophyYellow,
                                    trackColor = DarkSurfaceElevated
                                )

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Moderate debt ratio — manageable but watch it",
                                    fontSize = 11.sp,
                                    color = TextSubtle
                                )
                            }
                        }
                    }

                    // Combined Breakdown List
                    item {
                        Text(
                            text = "Akun  ${CurrencyHelper.formatRupiah(totalWalletBalance)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(wallets) { wallet ->
                        WalletMiniRow(
                            wallet = wallet,
                            onClick = { selectedWalletIdForDetail = wallet.id }
                        )
                    }

                    item {
                        Text(
                            text = "Aset  ${CurrencyHelper.formatRupiah(totalAssets)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(assets) { asset ->
                        AssetMiniRow(asset, onClick = { selectedAssetIdForDetail = asset.id })
                    }

                    item {
                        Text(
                            text = "Utang  -${CurrencyHelper.formatRupiah(totalDebts)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    items(debts) { debt ->
                        DebtMiniRow(debt)
                    }

                    // Retirement Planner Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x332563EB),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Umbrella,
                                            contentDescription = null,
                                            tint = AccentBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Perencana pensiun",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextWhite
                                    )
                                    Text(
                                        text = "Proyeksikan tabunganmu hingga usia pensiun",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }
                1 -> {
                    // Akun (Wallets) Grid View
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Dompet",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentBlue,
                                modifier = Modifier
                                    .clickable(onClick = onAddWalletClick)
                                    .testTag("btn_add_wallet")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Buat Akun",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // 2x2 Grid of Wallets
                    items(wallets.chunked(2)) { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            pair.forEach { wallet ->
                                val percent = if (totalWalletBalance > 0) ((wallet.balance / totalWalletBalance) * 100).toInt() else 0
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedWalletIdForDetail = wallet.id },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = Color.White,
                                                modifier = Modifier.height(24.dp)
                                            ) {
                                                Text(
                                                    text = if (wallet.name.contains("BRI")) "BRI" else "Jago",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (wallet.name.contains("BRI")) Color(0xFF00529C) else Color(0xFFFF6600),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = wallet.name,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = wallet.bankType,
                                            fontSize = 10.sp,
                                            color = TextSubtle
                                        )

                                        Spacer(modifier = Modifier.height(10.dp))

                                        Text(
                                            text = CurrencyHelper.formatRupiah(wallet.balance),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = "$percent% dari total",
                                            fontSize = 10.sp,
                                            color = TextSubtle
                                        )

                                        if (wallet.lastAmount > 0) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "${wallet.lastCategory} · ${CurrencyHelper.formatRupiah(wallet.lastAmount)} · ${wallet.lastActivityTime}",
                                                fontSize = 9.sp,
                                                color = TextMuted,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
                2 -> {
                    // Aset Grid View
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Aset & Investasi",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentBlue,
                                modifier = Modifier
                                    .clickable(onClick = onAddAssetClick)
                                    .testTag("btn_add_asset")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Buat Aset",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Group Tunai & Bank
                    val cashAssets = assets.filter { it.group == "Tunai & Bank" }
                    val investAssets = assets.filter { it.group == "Investasi" }

                    item {
                        val sumCash = cashAssets.sumOf { it.value }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SegmentAccountBlue))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tunai & Bank  ${CurrencyHelper.formatRupiah(sumCash)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }
                    }
                    items(cashAssets.chunked(2)) { pair ->
                        AssetGridRow(pair, totalAssets, onAssetClick = { selectedAssetIdForDetail = it.id })
                    }

                    item {
                        val sumInvest = investAssets.sumOf { it.value }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SegmentAssetGreen))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Investasi  ${CurrencyHelper.formatRupiah(sumInvest)}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextWhite
                            )
                        }
                    }
                    items(investAssets.chunked(2)) { pair ->
                        AssetGridRow(pair, totalAssets, onAssetClick = { selectedAssetIdForDetail = it.id })
                    }
                }
                3 -> {
                    // Utang List View
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Daftar Pinjaman / Utang (${debts.size})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = AccentBlue,
                                modifier = Modifier.clickable(onClick = onAddDebtClick)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Tambah Utang",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    items(debts) { debt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { debtToEdit = debt },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = ExpenseRedSoft,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.CreditCard,
                                                    contentDescription = null,
                                                    tint = ExpenseRed,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = debt.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextWhite
                                            )
                                            Text(
                                                text = "Tempo: ${debt.dueDate}",
                                                fontSize = 11.sp,
                                                color = TextSubtle
                                            )
                                        }
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = CurrencyHelper.formatRupiah(debt.totalAmount - debt.paidAmount),
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextWhite
                                        )
                                        Text(
                                            text = "${debt.progressPercent}% lunas",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (debt.progressPercent > 50) IncomeGreen else ExpenseRed
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                LinearProgressIndicator(
                                    progress = { (debt.progressPercent / 100f).coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = IncomeGreen,
                                    trackColor = DarkSurfaceElevated
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = { debtToEdit = debt },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Edit Utang",
                                                tint = TextMuted,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = { debtToDelete = debt },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Hapus Utang",
                                                tint = ExpenseRed,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = DarkSurfaceElevated,
                                        modifier = Modifier.clickable { onPayDebtClick(debt) }
                                    ) {
                                        Text(
                                            text = "Bayar Cicilan >",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = AccentBlue,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Fixed Sub-Tabs Toolbar: Nilai Bersih, Akun, Aset, Utang
        // Positioned fixed at the bottom, directly above the bottom navigation bar
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = DarkSurface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val subTabs = listOf(
                    Triple(0, "Nilai Bersih", Icons.Default.ShowChart),
                    Triple(1, "Akun", Icons.Default.Wallet),
                    Triple(2, "Aset", Icons.Default.Diamond),
                    Triple(3, "Utang", Icons.Default.Handshake)
                )

                subTabs.forEach { (index, title, icon) ->
                    val isSelected = subTab == index
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) AccentBlue else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setAccountSubTab(index) }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else TextMuted
                            )
                        }
                    }
                }
            }
        }

        // Edit Debt Dialog
        debtToEdit?.let { debt ->
            EditDebtDialog(
                debt = debt,
                viewModel = viewModel,
                onDismiss = { debtToEdit = null },
                onDelete = {
                    debtToDelete = debt
                    debtToEdit = null
                }
            )
        }

        // Delete Debt Confirmation Dialog
        debtToDelete?.let { debt ->
            AlertDialog(
                onDismissRequest = { debtToDelete = null },
                containerColor = DarkSurface,
                title = { Text("Hapus Utang?", color = TextWhite, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Apakah Anda yakin ingin menghapus data utang \"${debt.name}\"?",
                        color = TextMuted
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteDebt(debt.id)
                            debtToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                    ) {
                        Text("Hapus", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { debtToDelete = null }) {
                        Text("Batal", color = TextSubtle)
                    }
                }
            )
        }
    }
}

@Composable
fun WalletMiniRow(wallet: AccountWallet, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkCardSubtle,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.White,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = if (wallet.name.contains("BRI")) "BRI" else "JAG",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00529C)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(wallet.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
            }
            Text(
                text = CurrencyHelper.formatRupiah(wallet.balance),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}

@Composable
fun AssetMiniRow(
    asset: AssetItem,
    onClick: () -> Unit = {}
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkCardSubtle,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0x3334D399),
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Diamond,
                            contentDescription = null,
                            tint = SegmentAssetGreen,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(asset.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                    Text(asset.platform, fontSize = 10.sp, color = TextMuted)
                }
            }
            Text(
                text = CurrencyHelper.formatRupiah(asset.value),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextWhite
            )
        }
    }
}

@Composable
fun DebtMiniRow(debt: DebtItem) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkCardSubtle,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ExpenseRedSoft,
                    modifier = Modifier.size(24.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(debt.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
            }
            Text(
                text = "-${CurrencyHelper.formatRupiah(debt.totalAmount - debt.paidAmount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ExpenseRed
            )
        }
    }
}

@Composable
fun AssetGridRow(
    pair: List<AssetItem>,
    totalAssets: Double,
    onAssetClick: (AssetItem) -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        pair.forEach { asset ->
            val percent = if (totalAssets > 0) String.format(java.util.Locale.US, "%.1f", (asset.value / totalAssets) * 100) else "0"
            Card(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onAssetClick(asset) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = asset.platform.take(1).uppercase(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00AA13)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(asset.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextWhite)
                    Text("$percent%", fontSize = 11.sp, color = AccentBlue)

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        CurrencyHelper.formatRupiah(asset.value),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                }
            }
        }
        if (pair.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun NetWorthTrendCanvas(
    points: List<Float> = emptyList(),
    subTab: Int,
    modifier: Modifier = Modifier
) {
    val effectivePoints = if (points.size >= 2) points else listOf(0.4f, 0.45f, 0.5f, 0.55f, 0.65f)

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val color = when (subTab) {
            0, 2 -> IncomeGreen
            else -> ExpenseRed
        }

        val stepX = width / (effectivePoints.size - 1).coerceAtLeast(1)

        val path = Path()
        val fillPath = Path()
        fillPath.moveTo(0f, height)

        effectivePoints.forEachIndexed { i, value ->
            val x = i * stepX
            val y = height - (value * height * 0.82f).coerceAtMost(height * 0.95f)
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
                colors = listOf(color.copy(alpha = 0.22f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
