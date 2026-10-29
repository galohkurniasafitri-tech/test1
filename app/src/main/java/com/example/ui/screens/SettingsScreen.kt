package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DarkBackground
import com.example.ui.dialogs.ManageCategoriesDialog
import com.example.ui.dialogs.ManageLabelsDialog
import com.example.ui.dialogs.RecurringTransactionDialog
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSubtle
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.FinanceViewModel

@Composable
fun SettingsScreen(
    viewModel: FinanceViewModel,
    onBackupRestoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    var sliderPosition by remember { mutableFloatStateOf(settings.textSizePercent.toFloat()) }
    var showManageLabelsDialog by remember { mutableStateOf(false) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var showRecurringDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Header
        Text(
            text = "Pengaturan",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Pengaturan Aplikasi
            item {
                Text(
                    text = "Pengaturan Aplikasi",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItemClickable(
                            icon = Icons.Default.Language,
                            title = "Bahasa",
                            subtitle = "Ubah bahasa aplikasi",
                            badgeValue = "Indonesia",
                            onClick = { }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.DarkMode,
                            title = "Mode Gelap",
                            subtitle = "Beralih antara tema terang dan gelap",
                            checked = settings.isDarkMode,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(isDarkMode = it)) }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Palette,
                            title = "Warna Tema",
                            subtitle = "Sesuaikan warna utama aplikasi Anda",
                            onClick = { }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.CurrencyExchange,
                            title = "Mata Uang Utama",
                            subtitle = "Tetapkan mata uang pilihan Anda",
                            badgeValue = settings.primaryCurrency,
                            onClick = { }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Repeat,
                            title = "Kurs mata uang",
                            subtitle = "Tetapkan kurs Anda sendiri untuk pasangan mata uang",
                            onClick = { }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.Event,
                            title = "Tanggal Mulai Bulanan",
                            subtitle = "Sesuaikan siklus bulanan dengan tanggal gajian Anda",
                            badgeValue = "Hari ${settings.monthlyStartDay}",
                            onClick = { }
                        )

                        // Ukuran teks slider
                        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.FormatSize,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Ukuran teks", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                        Text("Perbesar atau perkecil teks di seluruh aplikasi", fontSize = 11.sp, color = TextMuted)
                                    }
                                }
                                Text("${sliderPosition.toInt()}%", fontSize = 12.sp, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Slider(
                                value = sliderPosition,
                                onValueChange = { sliderPosition = it },
                                valueRange = 80f..140f,
                                colors = SliderDefaults.colors(
                                    thumbColor = AccentBlue,
                                    activeTrackColor = AccentBlue,
                                    inactiveTrackColor = DarkSurfaceElevated
                                )
                            )
                        }

                        SettingsItemSwitch(
                            icon = Icons.Default.AccountBalanceWallet,
                            title = "Akun tunai saja",
                            subtitle = "Kecualikan transaksi kartu kredit dari laporan arus kas.",
                            checked = settings.cashAccountsOnly,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(cashAccountsOnly = it)) }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.ShowChart,
                            title = "Tampilkan uang terencana di saldo",
                            subtitle = "Menambahkan angka proyeksi 30 hari ke depan pada nilai bersih.",
                            checked = settings.showPlannedInBalance,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(showPlannedInBalance = it)) }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.Savings,
                            title = "Pengingat menabung",
                            subtitle = if (settings.saveReminder) "Aktif" else "Nonaktif",
                            checked = settings.saveReminder,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(saveReminder = it)) }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.Notifications,
                            title = "Pengingat tagihan",
                            subtitle = if (settings.billReminder) "Aktif" else "Nonaktif",
                            checked = settings.billReminder,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(billReminder = it)) }
                        )

                        SettingsItemSwitch(
                            icon = Icons.Default.Warning,
                            title = "Peringatan anggaran",
                            subtitle = "Pemberitahuan saat anggaran melewati 80%, dan lagi saat habis.",
                            checked = settings.budgetAlert,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(budgetAlert = it)) }
                        )

                        SettingsItemClickable(
                            icon = Icons.Default.AutoAwesome,
                            title = "Smart notifications",
                            subtitle = "Pemberitahuan wawasan pintar berkala",
                            badgeValue = "Off",
                            onClick = { }
                        )
                    }
                }
            }

            // Section 2: Atur
            item {
                Text(
                    text = "Atur",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItemClickable(
                            icon = Icons.Default.Label,
                            title = "Label Transaksi",
                            subtitle = "Atur dengan label khusus",
                            onClick = { showManageLabelsDialog = true }
                        )
                        SettingsItemClickable(
                            icon = Icons.Default.Category,
                            title = "Kategori Transaksi",
                            subtitle = "Kelola kategori pengeluaran dan pemasukan",
                            onClick = { showManageCategoriesDialog = true }
                        )
                        SettingsItemClickable(
                            icon = Icons.Default.Repeat,
                            title = "Transaksi Berulang",
                            subtitle = "Atur dan kelola entri berulang",
                            onClick = { showRecurringDialog = true }
                        )
                        SettingsItemClickable(
                            icon = Icons.Default.ViewAgenda,
                            title = "Atur menu utama",
                            subtitle = "Kustomisasi urutan tab navigasi",
                            onClick = { }
                        )
                    }
                }
            }

            // Section 3: Privasi & Data
            item {
                Text(
                    text = "Privasi & Data",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItemSwitch(
                            icon = Icons.Default.Lock,
                            title = "Kode Sandi Aplikasi",
                            subtitle = "Amankan aplikasi Anda dengan kode sandi",
                            checked = settings.passcodeEnabled,
                            onCheckedChange = { viewModel.updateSettings(settings.copy(passcodeEnabled = it)) }
                        )
                        SettingsItemClickable(
                            icon = Icons.Default.CloudUpload,
                            title = "Cadangkan & Pulihkan",
                            subtitle = "Ekspor atau impor data keuangan Anda",
                            onClick = onBackupRestoreClick
                        )
                    }
                }
            }

            // Section 4: Dukungan
            item {
                Text(
                    text = "Dukungan",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        SettingsItemClickable(
                            icon = Icons.Default.Feedback,
                            title = "Saran Perbaikan",
                            subtitle = "Bagikan ide dan masukan Anda",
                            onClick = { }
                        )
                        SettingsItemClickable(
                            icon = Icons.Default.Share,
                            title = "Bagikan ke Teman",
                            subtitle = "Bagikan tautan aplikasi atau undang orang lain",
                            onClick = { }
                        )
                    }
                }
            }

            // Section 5: Tentang
            item {
                Text(
                    text = "Tentang",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSubtle,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = DarkSurfaceElevated,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Versi Aplikasi", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                                Text("Versi aplikasi saat ini", fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = DarkSurfaceElevated
                        ) {
                            Text(
                                text = "v2.33.0",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        if (showManageLabelsDialog) {
            ManageLabelsDialog(
                viewModel = viewModel,
                onDismiss = { showManageLabelsDialog = false }
            )
        }

        if (showManageCategoriesDialog) {
            ManageCategoriesDialog(
                viewModel = viewModel,
                onDismiss = { showManageCategoriesDialog = false }
            )
        }

        if (showRecurringDialog) {
            RecurringTransactionDialog(
                viewModel = viewModel,
                onDismiss = { showRecurringDialog = false }
            )
        }
    }
}

@Composable
fun SettingsItemClickable(
    icon: ImageVector,
    title: String,
    subtitle: String,
    badgeValue: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                Text(subtitle, fontSize = 11.sp, color = TextMuted)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (badgeValue != null) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = DarkSurfaceElevated
                ) {
                    Text(
                        text = badgeValue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TextSubtle,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
fun SettingsItemSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AccentBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextWhite)
                Text(subtitle, fontSize = 11.sp, color = TextMuted)
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentBlue,
                uncheckedThumbColor = TextMuted,
                uncheckedTrackColor = DarkSurfaceElevated
            )
        )
    }
}
