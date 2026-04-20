package `in`.antef.geonote.ui.screens.recording

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.random.Random

@Composable
fun AudioWaveform(isActive: Boolean, modifier: Modifier = Modifier) {
    val barsCount = 40
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barsCount) {
            AudioBar(
                isActive = isActive,
                height = when {
                    i % 3 == 0 -> Random.nextFloat() * 0.5f + 0.3f
                    i % 7 == 0 -> Random.nextFloat() * 0.7f + 0.2f
                    else -> Random.nextFloat() * 0.3f + 0.1f
                },
                animationDelay = i * 50
            )
        }
    }
}

@Composable
fun AudioBar(isActive: Boolean, height: Float, animationDelay: Int) {
    val infiniteTransition = rememberInfiniteTransition()
    val barHeight = if (isActive) {
        val animatedHeight by infiniteTransition.animateFloat(
            initialValue = height * 0.2f,
            targetValue = height,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1000,
                    delayMillis = animationDelay,
                    easing = FastOutSlowInEasing
                )
            )
        )
        animatedHeight
    } else {
        height
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 1.dp)
            .width(4.dp)
            .height((100 * barHeight).dp)
            .background(Color(0xFFFF8700), RoundedCornerShape(2.dp))
    )
}
