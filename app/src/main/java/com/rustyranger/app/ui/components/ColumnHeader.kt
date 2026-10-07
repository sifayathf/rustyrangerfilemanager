package com.rustyranger.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rustyranger.app.ui.screens.browser.SortConfig
import com.rustyranger.app.ui.screens.browser.SortField
import com.rustyranger.app.ui.theme.TextMuted

@Composable
fun SortableHeader(
    text: String,
    field: SortField,
    sortConfig: SortConfig,
    onSort: (SortField) -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    val isSelected = sortConfig.field == field
    Row(
        modifier = modifier
            .clickable { onSort(field) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (textAlign == TextAlign.End) Arrangement.End else if (textAlign == TextAlign.Center) Arrangement.Center else Arrangement.Start
    ) {
        if (textAlign == TextAlign.End && isSelected) {
            Icon(
                imageVector = if (sortConfig.ascending) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
            color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = textAlign
        )
        if (textAlign != TextAlign.End && isSelected) {
            Icon(
                imageVector = if (sortConfig.ascending) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ColumnHeader(
    sortConfig: SortConfig,
    onSort: (SortField) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(30.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SortableHeader(
                text = "Name",
                field = SortField.NAME,
                sortConfig = sortConfig,
                onSort = onSort,
                modifier = Modifier.weight(1f)
            )

            SortableHeader(
                text = "Size",
                field = SortField.SIZE,
                sortConfig = sortConfig,
                onSort = onSort,
                modifier = Modifier.width(64.dp),
                textAlign = TextAlign.End
            )

            SortableHeader(
                text = "Type",
                field = SortField.TYPE,
                sortConfig = sortConfig,
                onSort = onSort,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            SortableHeader(
                text = "Date Modified",
                field = SortField.DATE,
                sortConfig = sortConfig,
                onSort = onSort,
                modifier = Modifier.width(105.dp),
                textAlign = TextAlign.End
            )
        }

        HorizontalDivider(color = TextMuted.copy(alpha = 0.2f), thickness = 1.dp)
    }
}
