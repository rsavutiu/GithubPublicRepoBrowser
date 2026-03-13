package com.rsav.githubPublicRepoBrowser.ui.components.molecules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.rsav.githubPublicRepoBrowser.ui.components.atoms.SearchTextField
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit
) {
    SearchTextField(
        value = query,
        onValueChange = onQueryChanged,
        onSearch = onSearch,
        modifier = modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    MyApplicationTheme {
        SearchBar(
            query = "android",
            onQueryChanged = {},
            onSearch = {},
        )
    }
}
