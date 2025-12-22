package com.example.mobile_final.ui.screen.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.mobile_final.data.local.entity.ActivityType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.showWeightDialog) {
        WeightDialog(
            currentWeight = uiState.settings.weight,
            useMetric = uiState.settings.useMetricUnits,
            onDismiss = { viewModel.hideWeightDialog() },
            onConfirm = { weight -> viewModel.updateWeight(weight) }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Activity Preferences Section
                SettingsSection(title = "Activity Preferences") {
                    ActivityTypeSelector(
                        selectedType = uiState.settings.preferredActivityType,
                        onTypeSelected = { viewModel.updateActivityType(it) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Measurement Section
                SettingsSection(title = "Measurement") {
                    SettingsCard {
                        // Unit System
                        SettingsToggleItem(
                            icon = Icons.Default.Straighten,
                            title = "Use Metric Units",
                            subtitle = if (uiState.settings.useMetricUnits) "Kilometers, Kilograms" else "Miles, Pounds",
                            checked = uiState.settings.useMetricUnits,
                            onCheckedChange = { viewModel.updateUseMetricUnits(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                        // Weight
                        SettingsClickItem(
                            icon = Icons.Default.Scale,
                            title = "Weight",
                            subtitle = formatWeight(
                                uiState.settings.weight,
                                uiState.settings.useMetricUnits
                            ),
                            onClick = { viewModel.showWeightDialog() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notifications Section
                SettingsSection(title = "Notifications") {
                    SettingsCard {
                        SettingsToggleItem(
                            icon = Icons.Default.Notifications,
                            title = "Enable Notifications",
                            subtitle = "Weekly summaries and reminders",
                            checked = uiState.settings.notificationsEnabled,
                            onCheckedChange = { viewModel.updateNotificationsEnabled(it) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // About Section
                SettingsSection(title = "About") {
                    SettingsCard {
                        SettingsInfoItem(
                            title = "Version",
                            value = "1.0.0"
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        SettingsInfoItem(
                            title = "Developer",
                            value = "Mobile Final Project"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsCard(
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            content()
        }
    }
}

@Composable
private fun ActivityTypeSelector(
    selectedType: ActivityType,
    onTypeSelected: (ActivityType) -> Unit
) {
    SettingsCard {
        ActivityType.entries.forEachIndexed { index, type ->
            ActivityTypeItem(
                type = type,
                isSelected = selectedType == type,
                onClick = { onTypeSelected(type) }
            )
            if (index < ActivityType.entries.size - 1) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun ActivityTypeItem(
    type: ActivityType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = getActivityIcon(type),
            contentDescription = null,
            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = type.displayName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f)
        )
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
    }
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun WeightDialog(
    currentWeight: Float,
    useMetric: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit
) {
    var weightText by remember {
        val displayWeight = if (useMetric) currentWeight else currentWeight * 2.20462f
        mutableStateOf(String.format("%.1f", displayWeight))
    }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter Weight") },
        text = {
            Column {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = {
                        weightText = it
                        isError = it.toFloatOrNull() == null
                    },
                    label = { Text(if (useMetric) "Weight (kg)" else "Weight (lbs)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isError) {
                    Text(
                        text = "Please enter a valid number",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    weightText.toFloatOrNull()?.let { inputWeight ->
                        val weightInKg = if (useMetric) inputWeight else inputWeight / 2.20462f
                        onConfirm(weightInKg)
                    }
                },
                enabled = !isError && weightText.isNotEmpty()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun getActivityIcon(type: ActivityType): ImageVector {
    return when (type) {
        ActivityType.RUNNING -> Icons.AutoMirrored.Filled.DirectionsRun
        ActivityType.WALKING -> Icons.Default.DirectionsWalk
        ActivityType.CYCLING -> Icons.Default.DirectionsBike
    }
}

private fun formatWeight(weightKg: Float, useMetric: Boolean): String {
    return if (useMetric) {
        String.format("%.1f kg", weightKg)
    } else {
        String.format("%.1f lbs", weightKg * 2.20462f)
    }
}
