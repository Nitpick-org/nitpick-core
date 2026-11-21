package dev.nitpick.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.nitpick.app.ui.theme.NitpickTheme

/**
 * Dense text field used in toolbars, settings and inline review forms. It is deliberately
 * built on BasicTextField: Material's outlined field is 56dp tall, which breaks the 32dp
 * control rhythm of a developer tool.
 */
@Composable
fun NitpickTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String = "",
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = NitpickTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val focused = interactionSource.collectIsFocusedAsState().value
    val borderColor = when {
        errorMessage != null -> colors.danger
        focused -> colors.primary
        else -> colors.border
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label != null) {
            Text(
                text = label,
                style = NitpickTheme.typography.micro,
                color = if (errorMessage != null) colors.danger else colors.textSecondary,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.55f),
            shape = RoundedCornerShape(NitpickTheme.radii.md),
            color = colors.surface,
            contentColor = colors.text,
            border = BorderStroke(1.dp, borderColor),
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().heightIn(min = 34.dp).padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                leading?.invoke()
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = NitpickTheme.typography.small,
                            color = colors.textMuted,
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        singleLine = singleLine,
                        maxLines = if (singleLine) 1 else 4,
                        textStyle = NitpickTheme.typography.body.copy(color = colors.text),
                        cursorBrush = SolidColor(colors.primary),
                        interactionSource = interactionSource,
                        visualTransformation = visualTransformation,
                        keyboardOptions = keyboardOptions,
                    )
                }
                trailing?.invoke()
            }
        }
        if (errorMessage != null) {
            Text(text = errorMessage, style = NitpickTheme.typography.micro, color = colors.danger)
        }
    }
}
