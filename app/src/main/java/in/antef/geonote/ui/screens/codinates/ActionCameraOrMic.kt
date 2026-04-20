package `in`.antef.geonote.ui.screens.codinates

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import `in`.antef.geonote.R
import `in`.antef.geonote.ui.theme.TERTIARY

@Composable
fun ActionCameraOrMic(
    modifier: Modifier = Modifier,
    onClickCamera: () -> Unit,
    onClickMic: () -> Unit,
) {
    Box(
        modifier = modifier
            .padding(bottom = 32.dp)
            .background(Color.Black, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TERTIARY, RoundedCornerShape(12))
                    .clickable { onClickCamera() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = R
                            .drawable.ic_camera
                    ), contentDescription = null,
                    tint = Color.Black
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TERTIARY, RoundedCornerShape(10))
                    .clickable { onClickMic()},
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = R
                            .drawable.ic_mic_rec
                    ), contentDescription = null,
                    tint = Color.Black
                )
            }
        }

    }
}