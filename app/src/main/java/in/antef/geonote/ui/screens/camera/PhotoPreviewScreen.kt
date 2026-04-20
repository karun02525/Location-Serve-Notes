package `in`.antef.geonote.ui.screens.camera

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import `in`.antef.geonote.R


@Preview(showSystemUi = true, showBackground = true)
@Composable
fun PhotoPreviewScreen(
    url: String="https://geonote-dev-mobile.s3.ap-south-1.amazonaws.com/8307c4aa-57ac-45cd-adf0-5e67bbbcb096.jpg",
    onBackPressed: () -> Unit={}
) {
    Box(
        modifier = Modifier
            .background(Color.Black)
            .fillMaxSize(),
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(url)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit,
            error = painterResource(id = R.drawable.ic_launcher_foreground),
            placeholder = painterResource(id = R.drawable.ic_launcher_foreground)
        )

        Icon(
            painter = painterResource(id = R.drawable.ic_arrow),
            contentDescription = "back",
            tint = Color.White,
            modifier = Modifier
                .padding(top = 16.dp,start = 16.dp)
                .align(Alignment.TopStart)
                .clickable { onBackPressed() }
                .size(24.dp)
        )
    }

}