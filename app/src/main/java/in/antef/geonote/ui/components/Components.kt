package `in`.antef.geonote.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import `in`.antef.geonote.R
import `in`.antef.geonote.ui.theme.BORDER_COLOR
import `in`.antef.geonote.ui.theme.DELETE_COLOR
import `in`.antef.geonote.ui.theme.NO_DATA_TEXT_COLOR
import `in`.antef.geonote.ui.theme.TERTIARY
import `in`.antef.geonote.ui.theme.TEXT_DARK_COLOR
import `in`.antef.geonote.ui.theme.TEXT_FIELD_COLOR
import `in`.antef.geonote.ui.theme.inter_medium
import `in`.antef.geonote.ui.theme.inter_regular

@Composable
fun CustomButton(onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .padding(16.dp)
            .navigationBarsPadding()
            .fillMaxWidth()
            .background(TERTIARY, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = Color.Black,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            color = Color.Black,
            text = "Create new project",
            style = TextStyle(
                fontFamily = inter_medium,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp,
                fontSize = 16.sp
            ),
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopBar(
    onMenu: () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.Transparent
        ),
        title = {

        },
        navigationIcon = {
            IconButton(onClick = { onMenu() }) {
                CustomIcon(
                    icon = R.drawable.ic_menu,
                )
            }
        },
        actions = {
            IconButton(onClick = { }) {
                CustomIcon(
                    icon = R.drawable.ic_notification,
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataTopBar(
    text: String,
    onBack: () -> Unit = {},
    goToMap: () -> Unit = {}
) {
    androidx.compose.material3.TopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        ),
        title = {
            TextMedium(text, fontSize = 20.sp)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "onBack"
                )
            }
        },
        actions = {
            IconButton(onClick = goToMap) {
                CustomIcon(
                    icon = R.drawable.ic_maps_location,
                )
            }
        }
    )
}

@Composable
fun RecordingBox(
    modifier: Modifier = Modifier,
    recordingTime: String
) {

    Row(
        modifier = modifier
            .width(83.dp)
            .height(35.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(0.50f), shape = RoundedCornerShape(16.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly

    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_recording),
            contentDescription = "ic_recording",
            tint = Color.Unspecified,
            modifier = Modifier
                .size(16.dp)

        )
        TextRegular(
            text = recordingTime,
        )
    }
}


@Composable
fun MapListCoordinates(modifier: Modifier = Modifier, count: Int = 0) {
    Box(
        modifier = modifier
            .size(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(TERTIARY, shape = RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,

        ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_loc_count),
            contentDescription = "ic_loc",
            tint = Color.Black,
            modifier = Modifier
                .width(22.dp)
                .height(28.dp)
        )

        CircleCounter(
            modifier = Modifier
                .padding(top = 4.dp, end = 6.dp)
                .align(Alignment.TopEnd),
            count = count,
            size = 16.dp,
            bgColor = Color(0xFFEDC18C),
            textColor = Color.Black,
            textSize = 12.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapTopBar(
    text: String = "Map",
    onBack: () -> Unit = {},
    goToData: () -> Unit = {}
) {
    androidx.compose.material3.TopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        ),
        title = {
            TextMedium(text, fontSize = 20.sp)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "onBack"
                )
            }
        },
        actions = {
            IconButton(onClick = goToData) {
                CustomIcon(
                    icon = R.drawable.ic_data,
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioTopBar(
    text: String = "Audio Recorder",
    onBack: () -> Unit = {},
) {
    androidx.compose.material3.TopAppBar(
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White
        ),
        title = {
            TextMedium(text, fontSize = 20.sp)
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "onBack"
                )
            }
        }
    )
}

@Composable
fun MapZoom(
    modifier: Modifier = Modifier,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        FloatingActionButton(
            onClick = onZoomIn,
            modifier = Modifier.size(40.dp),
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp),
            contentColor = Color.Black

        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_add),
                contentDescription = "onZoomIn"
            )
        }

        FloatingActionButton(
            onClick = onZoomOut,
            modifier = Modifier.size(40.dp),
            containerColor = Color.White,
            shape = RoundedCornerShape(8.dp),
            contentColor = Color.Black

        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_minus),
                contentDescription = "onZoomOut"
            )
        }
    }
}


@Composable
fun NoDataMessage(modifier: Modifier = Modifier, message: String) {
    Box(
        modifier = modifier
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        TextMedium(
            textAlign = TextAlign.Center,
            text = message,
            color = NO_DATA_TEXT_COLOR,
            fontSize = 15.sp,
        )
    }
}

@Composable
fun CustomFloatingButton(
    icon: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = TERTIARY,
    contentColor: Color = Color.Black,
    onClick: () -> Unit = {}
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        containerColor = containerColor,
        shape = RoundedCornerShape(8.dp),
        contentColor = contentColor

    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = "Get Location"
        )
    }
}


@Composable
fun CircleCounter(
    modifier: Modifier = Modifier,
    count: Int = 0,
    size: Dp = 20.dp,
    bgColor: Color = Color.Black,
    textColor: Color = Color.White,
    textSize: TextUnit = 12.sp,

    ) {
    Box(
        modifier = modifier
            .size(size)
            .background(bgColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        TextRegular(
            text = "" + count,
            textAlign = TextAlign.Center,
            color = textColor,
            fontSize = textSize,
        )
    }
}

@Composable
fun CircleBorderCounter(
    modifier: Modifier = Modifier,
    count: Int = 0,
    size: Dp = 20.dp,
    bgColor: Color = Color.White,
    textColor: Color = Color.Black,
    textSize: TextUnit = 12.sp,
    ) {
    Box(
        modifier = modifier
            .size(size)
            .background(bgColor, shape = CircleShape)
            .border(1.dp, Color.Black, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        TextRegular(
            text = "" + count,
            textAlign = TextAlign.Center,
            color = textColor,
            fontSize = textSize,
        )
    }
}

@Composable
fun CustomIcon(
    modifier: Modifier = Modifier,
    icon: Int,
    size: Dp = 24.dp,
    tint: Color = Color.Black
) {
    Icon(
        modifier = modifier
            .size(size),
        painter = painterResource(
            id = icon
        ), contentDescription = null,
        tint = tint
    )
}

@Composable
fun CustomText(modifier: Modifier = Modifier, text: String) {
    Text(
        modifier = modifier,
        text = text,
        color = Color.Black,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun TextMedium(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    fontSize: TextUnit = 16.sp,
    textAlign: TextAlign? = null

) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        fontSize = fontSize,
        textAlign = textAlign,
        fontFamily = inter_medium,
        fontWeight = FontWeight.Medium
    )
}

@Composable
fun TextRegular(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Black,
    fontSize: TextUnit = 16.sp,
    textAlign: TextAlign? = null
) {
    Text(
        modifier = modifier,
        text = text,
        color = color,
        textAlign = textAlign,
        fontSize = fontSize,
        fontFamily = inter_regular,
        fontWeight = FontWeight.Normal
    )
}

@Composable
fun CustomEditButton(modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Row(
        modifier = modifier
            .padding(end = 16.dp)
            .border(1.dp, BORDER_COLOR, shape = RoundedCornerShape(4.dp))
            .background(Color.White, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClick() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_pencil_edit),
            modifier = Modifier
                .size(20.dp)
                .padding(end = 4.dp),
            tint = Color.Black,
            contentDescription = "Edit Icon"
        )
        TextRegular(
            text = "Edit",
            color = Color.Black,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}


@Composable
fun CustomField(
    text: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Project Name",
    height: Dp = 50.dp,
    isMaxLines: Boolean = false,
    validationLength: Int = 25
) {
    BasicTextField(
        value = text,
        onValueChange = { newValue ->
            if (newValue.length <= if (isMaxLines) 100 else validationLength) {
                onValueChange(newValue)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, BORDER_COLOR, shape = RoundedCornerShape(8.dp))
            .background(color = TEXT_FIELD_COLOR, shape = RoundedCornerShape(8.dp)),
        singleLine = !isMaxLines,
        textStyle = TextStyle.Default,
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = if (isMaxLines) Alignment.TopStart else Alignment.CenterStart,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (text.isEmpty()) {
                    TextRegular(placeholder, color = TEXT_DARK_COLOR)
                }
                innerTextField()
            }
        }
    )
}


@Composable
fun DeleteConfirmationDialog(
    showDialog: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { TextMedium("Delete") },
            text = { TextRegular("Do you want to delete?") },
            confirmButton = {
                Button(onClick = onConfirm) {
                    TextMedium("Yes")
                }
            },
            dismissButton = {
                Button(onClick = onDismiss) {
                    TextMedium("No")
                }
            }
        )
    }
}

@Composable
fun CustomAlertDialog(
    showDialog: Boolean,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(dismissOnClickOutside = true)
        ) {
            // Custom dialog content
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.99f)
                    .wrapContentHeight()
                    .background(Color.Transparent), // Transparent background for Card
                shape = RoundedCornerShape(16.dp), // Rounded corners
                colors = CardDefaults.cardColors(
                    containerColor = Color.White // Dialog background color
                ),
                elevation = CardDefaults.cardElevation(8.dp) // Shadow for depth
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    TextMedium(
                        text = title,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Message
                    TextRegular(
                        text = message,
                        textAlign = TextAlign.Start,
                        fontSize = 14.sp,
                        color = TEXT_DARK_COLOR,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            ),
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(35.dp),
                            border = BorderStroke(1.dp, BORDER_COLOR),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            TextMedium(
                                text = "Cancel",
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DELETE_COLOR
                            ),
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(35.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            TextMedium(
                                text = "Delete",
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}