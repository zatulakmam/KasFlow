package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.model.Transaction
import com.example.model.TransactionCategory
import com.example.model.TransactionType
import com.example.ui.theme.LocalCashflowColors
import com.example.util.defaultMillisForYearMonth
import com.example.util.formatDateShort
import com.example.util.formatRupiah
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionEditorSheet(
    editingRecord: Transaction?,
    selectedYearMonth: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        editingId: String?,
        title: String,
        type: TransactionType,
        category: TransactionCategory,
        amount: Long,
        note: String,
        dateMillis: Long
    ) -> Unit,
    onDelete: ((String) -> Unit)? = null
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val semantic = LocalCashflowColors.current

    var selectedType by remember(editingRecord) {
        mutableStateOf(editingRecord?.parsedType ?: TransactionType.EXPENSE)
    }
    var selectedCategory by remember(editingRecord) {
        mutableStateOf(
            editingRecord?.parsedCategory
                ?: TransactionCategory.forType(TransactionType.EXPENSE).first()
        )
    }
    var title by remember(editingRecord) {
        mutableStateOf(editingRecord?.title ?: "")
    }
    var amountText by remember(editingRecord) {
        mutableStateOf(editingRecord?.amount?.toString() ?: "")
    }
    var note by remember(editingRecord) {
        mutableStateOf(editingRecord?.note ?: "")
    }
    var dateMillis by remember(editingRecord, selectedYearMonth) {
        mutableLongStateOf(
            editingRecord?.date ?: defaultMillisForYearMonth(selectedYearMonth)
        )
    }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val availableCategories = remember(selectedType) {
        TransactionCategory.forType(selectedType)
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = semantic.primaryAction,
        unfocusedBorderColor = semantic.borderSubtle,
        focusedContainerColor = semantic.subtleSurface,
        unfocusedContainerColor = semantic.subtleSurface
    )

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        sheetState = sheetState,
        containerColor = semantic.cardSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingRecord == null) "Catat transaksi" else "Edit transaksi",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (editingRecord != null && onDelete != null) {
                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        enabled = !isSaving,
                        modifier = Modifier.testTag("delete_transaction_button")
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "Hapus transaksi",
                            tint = semantic.expenseText
                        )
                    }
                }
            }

            MinimalSegmentedTabs(
                options = listOf(TransactionType.EXPENSE, TransactionType.INCOME),
                selectedOption = selectedType,
                labelFor = { it.label },
                onSelect = { type ->
                    selectedType = type
                    if (selectedCategory.type != type) {
                        selectedCategory = TransactionCategory.forType(type).first()
                    }
                    validationError = null
                },
                testTagPrefix = "type_toggle"
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val parsedAmount = amountText.toLongOrNull() ?: 0L
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        val digits = raw.filter { it.isDigit() }.take(12)
                        amountText = digits
                        validationError = null
                    },
                    label = { Text("Nominal (Rp)") },
                    prefix = { Text("Rp ", fontWeight = FontWeight.SemiBold) },
                    supportingText = {
                        if (parsedAmount > 0L) {
                            Text(
                                text = formatRupiah(parsedAmount),
                                color = if (selectedType == TransactionType.INCOME) semantic.incomeText else semantic.expenseText,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text("Masukkan angka nominal tanpa titik/koma")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = fieldColors,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("amount_input")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        25_000L to "+25rb",
                        50_000L to "+50rb",
                        100_000L to "+100rb",
                        500_000L to "+500rb"
                    ).forEach { (delta, label) ->
                        SuggestionChip(
                            onClick = {
                                val next = ((amountText.toLongOrNull() ?: 0L) + delta)
                                    .coerceAtMost(999_999_999_999L)
                                amountText = next.toString()
                                validationError = null
                            },
                            label = { Text(label, fontWeight = FontWeight.SemiBold) },
                            border = BorderStroke(1.dp, semantic.borderSubtle),
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = semantic.subtleSurface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it.take(100)
                    validationError = null
                },
                label = { Text("Judul / Keterangan Singkat") },
                placeholder = {
                    Text(
                        if (selectedType == TransactionType.EXPENSE) "Contoh: Makan siang, Bensin, Listrik"
                        else "Contoh: Gaji bulanan, Proyek freelance"
                    )
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("title_input")
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Kategori",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    availableCategories.forEach { category ->
                        val selected = selectedCategory == category
                        FilterChip(
                            selected = selected,
                            onClick = { selectedCategory = category },
                            label = { Text(category.label, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (selected) Icons.Rounded.Check else category.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            border = BorderStroke(
                                1.dp,
                                if (selected) semantic.primaryAction else semantic.borderSubtle
                            ),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = semantic.subtleSurface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = semantic.primaryAction,
                                selectedLabelColor = semantic.onPrimaryAction,
                                selectedLeadingIconColor = semantic.onPrimaryAction
                            ),
                            modifier = Modifier.testTag("category_chip_${category.name.lowercase()}")
                        )
                    }
                }
            }

            MinimalButton(
                onClick = { showDatePicker = true },
                containerColor = semantic.subtleSurface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                borderColor = semantic.borderSubtle,
                cornerRadius = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("date_picker_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Tanggal: ${formatDateShort(dateMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it.take(250)
                    validationError = null
                },
                label = { Text("Catatan Tambahan (Opsional)") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                maxLines = 3,
                colors = fieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("note_input")
            )

            if (validationError != null) {
                Text(
                    text = validationError.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MinimalButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    containerColor = semantic.cardSurface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    borderColor = semantic.borderSubtle,
                    cornerRadius = 12.dp,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Batal", fontWeight = FontWeight.SemiBold)
                }

                MinimalButton(
                    onClick = {
                        val cleanTitle = title.trim()
                        val amount = amountText.toLongOrNull() ?: 0L
                        val cleanNote = note.trim()
                        when {
                            amount <= 0L -> validationError = "Masukkan nominal lebih dari Rp 0."
                            cleanTitle.isEmpty() -> validationError = "Judul transaksi tidak boleh kosong."
                            cleanTitle.length > 100 -> validationError = "Judul maksimal 100 karakter."
                            cleanNote.length > 250 -> validationError = "Catatan maksimal 250 karakter."
                            else -> {
                                onSave(
                                    editingRecord?.id,
                                    cleanTitle,
                                    selectedType,
                                    selectedCategory,
                                    amount,
                                    cleanNote,
                                    dateMillis
                                )
                            }
                        }
                    },
                    enabled = !isSaving,
                    containerColor = semantic.primaryAction,
                    contentColor = semantic.onPrimaryAction,
                    cornerRadius = 12.dp,
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("save_transaction_button")
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = semantic.onPrimaryAction,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = if (editingRecord == null) "Simpan ke Cloud" else "Perbarui",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val utcInitMillis = remember(dateMillis) { localMillisToUtcPickerMillis(dateMillis) }
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = utcInitMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { selectedUtc ->
                            dateMillis = utcPickerMillisToLocalNoonMillis(selectedUtc)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showDeleteConfirm && editingRecord != null && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = semantic.cardSurface,
            title = { Text("Hapus Transaksi?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Transaksi \"${editingRecord.title}\" akan dihapus permanen dari cloud Anda.")
            },
            confirmButton = {
                MinimalButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete(editingRecord.id)
                    },
                    containerColor = semantic.expenseContainer,
                    contentColor = semantic.expenseText,
                    cornerRadius = 10.dp,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text("Hapus", fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Batal", color = MaterialTheme.colorScheme.onSurface)
                }
            }
        )
    }
}

private fun localMillisToUtcPickerMillis(localMillis: Long): Long {
    val localCal = Calendar.getInstance().apply { timeInMillis = localMillis }
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(
            localCal.get(Calendar.YEAR),
            localCal.get(Calendar.MONTH),
            localCal.get(Calendar.DAY_OF_MONTH)
        )
    }
    return utcCal.timeInMillis
}

private fun utcPickerMillisToLocalNoonMillis(utcMillis: Long): Long {
    val utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        timeInMillis = utcMillis
    }
    val localCal = Calendar.getInstance().apply {
        clear()
        set(
            utcCal.get(Calendar.YEAR),
            utcCal.get(Calendar.MONTH),
            utcCal.get(Calendar.DAY_OF_MONTH),
            12,
            0,
            0
        )
    }
    return localCal.timeInMillis
}
