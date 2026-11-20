package com.foldcinema.ai.ui.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.foldcinema.ai.ui.icons.EditorIcons
import com.foldcinema.ai.ui.theme.Amber
import com.foldcinema.ai.ui.theme.ErrorRed
import com.foldcinema.ai.ui.theme.TextPrimary

@Composable
fun EditorToolbar(
    canSplit: Boolean,
    hasSelection: Boolean,
    canMoveLeft: Boolean,
    canMoveRight: Boolean,
    trimActive: Boolean,
    onAdd: () -> Unit,
    onSplit: () -> Unit,
    onTrim: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        ToolbarButton(Icons.Default.Add, "Add", true, false, Amber, onAdd)
        ToolbarButton(EditorIcons.Split, "Split", canSplit, false, Amber, onSplit)
        ToolbarButton(EditorIcons.Trim, "Trim", hasSelection, trimActive, Amber, onTrim)
        ToolbarButton(EditorIcons.MoveLeft, "Left", canMoveLeft, false, Amber, onMoveLeft)
        ToolbarButton(EditorIcons.MoveRight, "Right", canMoveRight, false, Amber, onMoveRight)
        ToolbarButton(Icons.Default.Delete, "Delete", hasSelection, false, ErrorRed, onDelete)
    }
}

@Composable
private fun RowScope.ToolbarButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    active: Boolean,
    tint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(if (active) Amber.copy(alpha = 0.18f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled) 1f else 0.35f)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(24.dp))
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
    }
}
