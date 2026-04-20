package `in`.antef.geonote.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.SecureFlagPolicy
import `in`.antef.geonote.ui.theme.BORDER_COLOR
import `in`.antef.geonote.ui.theme.TERTIARY

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomBottomSheet(
    header: String = "Project Details",
    placeholder1: String = "Project Name",
    placeholder2: String = "Description",
    isPoint: Boolean = false,
    title: String = "",
    description: String = "",
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onCreateClick: (String, String) -> Unit = { _, _ -> }
) {
    var _title by remember { mutableStateOf(title) }
    var _description by remember { mutableStateOf(description) }
    val isEditing = title.isNotEmpty()
    val minLength = 1

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false,
        confirmValueChange = { it != SheetValue.Hidden }
    )

    LaunchedEffect(title, description) {
        _title = title
        _description = description
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = sheetState,
            containerColor = Color.White,
            properties = ModalBottomSheetProperties(
                shouldDismissOnBackPress = false,
                securePolicy = SecureFlagPolicy.Inherit,
            )
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextMedium(
                        text = header,
                        fontSize = 20.sp
                    )

                    CustomField(
                        text = _title,
                        onValueChange = { _title = it },
                        placeholder = placeholder1,
                        height = 50.dp
                    )

                    CustomField(
                        text = _description,
                        onValueChange = { _description = it },
                        placeholder = placeholder2,
                        height = 100.dp,
                        isMaxLines = true
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(51.dp),
                            border = BorderStroke(1.dp, BORDER_COLOR),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            TextMedium(
                                text = "Cancel",
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = TERTIARY
                            ),
                            onClick = { onCreateClick(_title, _description) },
                            enabled = _title.length >= minLength,
                            modifier = Modifier
                                .weight(1f)
                                .height(51.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            TextMedium(
                                text = if (isEditing) "Update" else (if (isPoint) "Add" else "Create"),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}