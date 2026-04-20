package `in`.antef.geonote.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import `in`.antef.geonote.R
import `in`.antef.geonote.ui.theme.TEXT_COLOR

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun CustomBottomSheet(
    onDataClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(Color.White)
            .padding(start = 8.dp, end = 8.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomSheetItem(
            icon = R.drawable.ic_data,
            text = "Data",
            onClick = onDataClick
        )

        BottomSheetItem(
            icon = R.drawable.ic_pencil_edit,
            text = "Edit",
            onClick = onEditClick
        )

        BottomSheetItem(
            icon = R.drawable.ic_share,
            text = "Share",
            onClick = onShareClick
        )

        BottomSheetItem(
            icon = R.drawable.ic_delete,
            text = "Delete",
            onClick = onDeleteClick
        )
    }
}

@Composable
private fun BottomSheetItem(
    icon: Int,
    text: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .wrapContentWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painterResource(id = icon),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = TEXT_COLOR,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCustomBottomSheet(
    showBottomSheet: Boolean,
    onDismiss: () -> Unit,
    onDataClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onShareClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = Color.White
        ) {
            CustomBottomSheet(
                onDataClick = {
                    onDataClick()
                    onDismiss()
                },
                onEditClick = {
                    onEditClick()
                    onDismiss()
                },
                onShareClick = {
                    onShareClick()
                    onDismiss()
                },
                onDeleteClick = {
                    onDeleteClick()
                    onDismiss()
                }
            )
        }
    }
}