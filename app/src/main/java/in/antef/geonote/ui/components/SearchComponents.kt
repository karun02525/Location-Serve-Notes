package `in`.antef.geonote.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.antef.geonote.R
import `in`.antef.geonote.ui.theme.TEXT_DARK_COLOR
import `in`.antef.geonote.ui.theme.inter_regular

@Composable
fun SearchComponents(
    modifier: Modifier = Modifier,
    onSearchChanged: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    InjectedPaddingOutlinedTextField(
        value = searchQuery,
        maxLines = 1,
        isError = false,
        keyboardOptions = KeyboardOptions(
            autoCorrectEnabled = false,
            keyboardType = KeyboardType.Text,
            capitalization = KeyboardCapitalization.None,
            imeAction = ImeAction.Search
        ),
        keyboardActions = KeyboardActions(
            onSearch = { /* Optionally handle search action */ },
        ),
        textStyle = TextStyle(
            fontFamily = inter_regular,
            letterSpacing = 0.sp,
            fontSize = 16.sp
        ),
        placeholder = {
            Text(
                "Search",
                fontSize = 15.sp,
                color = TEXT_DARK_COLOR,
                style = TextStyle(
                    fontFamily = inter_regular,
                    letterSpacing = 0.sp,
                    fontSize = 16.sp
                ),
                modifier = Modifier.padding(start = 2.dp)
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search Icon",
            )
        },
        onValueChange = {
            searchQuery = it
            onSearchChanged(it) // Auto search: notify parent on every change
        },
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(44.dp),
        contentPaddingValues = PaddingValues(top = 8.dp, start = 4.dp)
    )
}