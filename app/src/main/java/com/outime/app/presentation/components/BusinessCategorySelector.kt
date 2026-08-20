package com.outime.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.outime.app.presentation.model.BusinessCategory

/**
 * Selector de categoría de negocio con una única fuente canónica ([BusinessCategory]).
 * Reutilizado en la creación y edición del negocio.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BusinessCategorySelector(
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        BusinessCategory.categories.forEach { cat ->
            FilterChip(
                selected = cat.label.equals(selected.trim(), ignoreCase = true),
                onClick = { onSelected(cat.label) },
                label = { Text(cat.label) }
            )
        }
    }
}