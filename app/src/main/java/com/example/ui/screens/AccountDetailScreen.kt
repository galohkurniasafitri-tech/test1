package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountWallet
import com.example.data.model.Transaction
import com.example.data.model.TransactionType
import com.example.util.ChartDataHelper
import com.example.util.ChartPeriod
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.dialogs.CreateWalletDialog
import com.example.ui.dialogs.EditTransactionDialog
import com.example.ui.dialogs.getCategoryIcon
import com.example.ui.dialogs.getWalletIconVector
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
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.CurrencyHelper

@Composable
fun AccountDetailScreen(
    walletId: String,
    viewModel: FinanceViewModel,
    onBack: () -> Unit
) {
    // Intercept back button to return to account list
    BackHandler(onBack = onBack)

    val allWallets by viewModel.wallets.collectAsState()
    val wallet = allWallets.find { it.id == walletId }

    if (wallet == null) {
        // If deleted, safely navigate back
        onBack()
        return
    }

    val allTransactions by viewModel.transactions.collectAsState()
    val accountTransactions = remember(allTransactions, wallet.name) {
        allTransactions.filter { 
            it.accountName.equals(wallet.name, ignoreCase = true) ||
            (it.type == TransactionType.TRANSFER && it.toAccountName.equals(wallet.name, ignoreCase = true))
        }
    }

    val totalIncome = remember(accountTransactions, wallet.name) {
        accountTransactions.filter { 
            it.type == TransactionType.INCOME || 
            (it.type == TransactionType.TRANSFER && it.toAccountName.equals(wallet.name, ignoreCase = true))
        }.sumOf { it.amount }
    }
    val totalExpense = remember(accountTransactions, wallet.name) {
        accountTransactions.filter { 
            it.type == TransactionType.EXPENSE || 
            (it.type == TransactionType.TRANSFER && it.accountName.equals(wallet.name, ignoreCase = true))
        }.sumOf { it.amount + it.transferFee }
    }

    var selectedPeriod by remember { mutableStateOf("1M") }

    val chartPeriod = when (selectedPeriod) {
        "7D" -> ChartPeriod.SEVEN_DAYS
        "3M" -> ChartPeriod.THREE_MONTHS
        "YTD" -> ChartPeriod.ONE_YEAR
        "ALL" -> ChartPeriod.ALL
        else -> ChartPeriod.ONE_MONTH
    }

    val chartData = remember(wallet, accountTransactions, chartPeriod) {
        ChartDataHelper.getWalletBalanceTrend(wallet, accountTransactions, chartPeriod)
    }

    var selectedTxFilter by remember { mutableStateOf("Semua") } // "Semua", "Pengeluaran", "Pemasukan"
    var showEditWalletDialog by remember { mutableStateOf(false) }
    var showAddTxDialog by remember { mutableStateOf(false) }
    var activeTxToEdit by remember { mutableStateOf<Transaction?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var txSearchQuery by remember { mutableStateOf("") }

    val filteredTransactions = remember(accountTransactions, selectedTxFilter, txSearchQuery) {
        accountTransactions.filter { tx ->
            val matchFilter = when (selectedTxFilter) {
                "Pengeluaran" -> tx.type == TransactionType.EXPENSE
                "Pemasukan" -> tx.type == TransactionType.INCOME
                else -> true
            }
            val matchQuery = txSearchQuery.isBlank() ||
                    tx.title.contains(txSearchQuery, ignoreCase = true) ||
                    tx.category.contains(txSearchQuery, ignoreCase = true) ||
                    tx.note.contains(txSearchQuery, ignoreCase = true)
            matchFilter && matchQuery
        }
    }

    val groupedTransactions = remember(filteredTransactions) {
        filteredTransactions.groupBy { tx -> "${tx.dayOfMonth} ${tx.dayOfWeekName} ${tx.monthIndex}/${tx.year % 100}" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
    ) {
        // 1. Toolbar on top: Back button, Account Name, Pencil Edit Icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_account_detail_back")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali ke Akun",
                        tint = TextWhite,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getWalletIconVector(wallet.iconName.ifBlank { wallet.bankType }),
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = wallet.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Text(
                        text = wallet.bankType,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentBlue,
                    modifier = Modifier
                        .clickable { showAddTxDialog = true }
                        .testTag("btn_top_add_tx")
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
                            text = "Tambah",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Top Right: Pencil Edit Icon
                IconButton(
                    onClick = { showEditWalletDialog = true },
                    modifier = Modifier.testTag("btn_edit_account_pencil")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Akun",
                        tint = TextWhite,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 2. Saldo & Period Filter
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = CurrencyHelper.formatRupiah(wallet.balance),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saldo Tersedia",
                        fontSize = 12.sp,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Period Selector Pills
                    Row(
                        modifier = Modifier
                            .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(2.dp)
                    ) {
                        listOf("7D", "1M", "3M", "YTD", "ALL").forEach { period ->
                            val isSelected = selectedPeriod == period
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentBlue else Color.Transparent,
                                modifier = Modifier.clickable { selectedPeriod = period }
                            ) {
                                Text(
                                    text = period,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextMuted,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Ringkasan Grafik (Summary Chart)
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
                                text = "Ringkasan Grafik",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )

                            val netChange = totalIncome - totalExpense
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (netChange >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (netChange >= 0) IncomeGreen else ExpenseRed,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${if (netChange >= 0) "+" else ""}${CurrencyHelper.formatRupiah(netChange)}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (netChange >= 0) IncomeGreen else ExpenseRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Dynamic Curve Chart for this account
                        AccountTrendChart(
                            points = chartData.points,
                            isPositive = totalIncome >= totalExpense,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

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
            }

            // 4. Ringkasan Pemasukan dan Pengeluaran (2 Cards)
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Ringkasan Arus Kas",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextWhite,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Pemasukan Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(IncomeGreenSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = null,
                                            tint = IncomeGreen,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pemasukan",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = CurrencyHelper.formatRupiah(totalIncome),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                val inCount = accountTransactions.count { it.type == TransactionType.INCOME }
                                Text(
                                    text = "$inCount transaksi",
                                    fontSize = 10.sp,
                                    color = TextSubtle
                                )
                            }
                        }

                        // Pengeluaran Card
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(ExpenseRedSoft),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                                            contentDescription = null,
                                            tint = ExpenseRed,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Pengeluaran",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = TextMuted
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = CurrencyHelper.formatRupiah(totalExpense),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ExpenseRed
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                val outCount = accountTransactions.count { it.type == TransactionType.EXPENSE }
                                Text(
                                    text = "$outCount transaksi",
                                    fontSize = 10.sp,
                                    color = TextSubtle
                                )
                            }
                        }
                    }
                }
            }

            // 5. Toolbar Transaksi & Filter
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Toolbar Transaksi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextWhite
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { isSearchActive = !isSearchActive }) {
                                Icon(
                                    imageVector = if (isSearchActive) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Cari Transaksi",
                                    tint = TextWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AccentBlue,
                                modifier = Modifier.clickable { showAddTxDialog = true }
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
                                        text = "Tambah",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Optional Search Bar
                    if (isSearchActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = txSearchQuery,
                            onValueChange = { txSearchQuery = it },
                            placeholder = { Text("Cari transaksi di akun ini...", color = TextSubtle, fontSize = 13.sp) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = TextWhite,
                                unfocusedTextColor = TextWhite,
                                focusedContainerColor = DarkSurfaceElevated,
                                unfocusedContainerColor = DarkSurfaceElevated,
                                focusedBorderColor = AccentBlue,
                                unfocusedBorderColor = DarkCardBorder
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Filter chips: Semua, Pengeluaran, Pemasukan
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("Semua", "Pengeluaran", "Pemasukan").forEach { filterName ->
                            val isSelected = selectedTxFilter == filterName
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) AccentBlue else DarkSurfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) AccentBlue else DarkCardBorder
                                ),
                                modifier = Modifier.clickable { selectedTxFilter = filterName }
                            ) {
                                Text(
                                    text = filterName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextMuted,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 6. List Transaksi
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Belum Ada Transaksi",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Belum ada transaksi tercatat untuk dompet '${wallet.name}'.",
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddTxDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                            ) {
                                Text("Tambah Transaksi Sekarang", fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                groupedTransactions.forEach { (dateHeader, txs) ->
                    item {
                        val netForDay = txs.sumOf { if (it.type == TransactionType.INCOME) it.amount else -it.amount }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateHeader,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextWhite
                            )
                            Text(
                                text = "${if (netForDay >= 0) "+" else ""}${CurrencyHelper.formatRupiah(netForDay)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (netForDay >= 0) IncomeGreen else ExpenseRed
                            )
                        }
                    }

                    items(txs) { tx ->
                        AccountTransactionRow(
                            transaction = tx,
                            walletName = wallet.name,
                            onClick = { activeTxToEdit = tx }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }

    // Edit Wallet Dialog (invoked by the pencil icon in the top right)
    if (showEditWalletDialog) {
        CreateWalletDialog(
            viewModel = viewModel,
            walletToEdit = wallet,
            onDismiss = { showEditWalletDialog = false }
        )
    }

    // Add Transaction Dialog (with this wallet pre-selected)
    if (showAddTxDialog) {
        AddTransactionDialog(
            viewModel = viewModel,
            onDismiss = { showAddTxDialog = false }
        )
    }

    // Edit Transaction Dialog
    activeTxToEdit?.let { tx ->
        EditTransactionDialog(
            transaction = tx,
            viewModel = viewModel,
            onDismiss = { activeTxToEdit = null }
        )
    }
}

@Composable
fun AccountTransactionRow(
    transaction: Transaction,
    walletName: String = "",
    onClick: () -> Unit
) {
    val isIncoming = transaction.type == TransactionType.INCOME ||
            (transaction.type == TransactionType.TRANSFER && transaction.toAccountName.equals(walletName, ignoreCase = true))

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = DarkCardSubtle,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isIncoming) IncomeGreenSoft else DarkSurfaceElevated),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(transaction.category),
                        contentDescription = transaction.category,
                        tint = if (isIncoming) IncomeGreen else TextWhite,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    val transferSubtext = if (transaction.type == TransactionType.TRANSFER) {
                        if (transaction.toAccountName.isNotBlank()) "Ke ${transaction.toAccountName}"
                        else if (transaction.targetAssetName.isNotBlank()) "Ke ${transaction.targetAssetName}"
                        else transaction.category
                    } else transaction.category
                    Text(
                        text = "$transferSubtext · ${transaction.time}",
                        fontSize = 10.sp,
                        color = TextSubtle
                    )
                }
            }

            // Amount
            Text(
                text = "${if (isIncoming) "+" else "-"}${CurrencyHelper.formatRupiah(transaction.amount)}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isIncoming) IncomeGreen else ExpenseRed
            )
        }
    }
}

@Composable
fun AccountTrendChart(
    points: List<Float> = emptyList(),
    isPositive: Boolean = true,
    modifier: Modifier = Modifier
) {
    val effectivePoints = if (points.size >= 2) points else listOf(0.3f, 0.5f, 0.45f, 0.65f, 0.6f)
    val lineColor = if (isPositive) IncomeGreen else ExpenseRed

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val stepX = width / (effectivePoints.size - 1).coerceAtLeast(1)

        val strokePath = Path()
        val fillPath = Path()
        fillPath.moveTo(0f, height)

        effectivePoints.forEachIndexed { i, value ->
            val x = i * stepX
            val y = height - (value * height * 0.85f).coerceAtMost(height * 0.95f)
            if (i == 0) {
                strokePath.moveTo(x, y)
                fillPath.lineTo(x, y)
            } else {
                strokePath.lineTo(x, y)
                fillPath.lineTo(x, y)
            }
        }

        fillPath.lineTo(width, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = strokePath,
            color = lineColor,
            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
