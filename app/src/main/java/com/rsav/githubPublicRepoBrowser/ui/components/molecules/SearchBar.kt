package com.rsav.githubPublicRepoBrowser.ui.components.molecules

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.rsav.githubPublicRepoBrowser.R
import com.rsav.githubPublicRepoBrowser.ui.theme.MyApplicationTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = stringResource(R.string.search_placeholder),
    trailingIcons: @Composable (RowScope.() -> Unit) = {},
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    androidx.compose.material3.SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChanged,
                onSearch = {
                    onSearch()
                    expanded = false
                },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    if (expanded) {
                        IconButton(onClick = { expanded = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                    } else {
                        Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                    }
                },
                trailingIcon = {
                    Row {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChanged(""); onSearch() }) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear))
                            }
                        }
                        trailingIcons()
                    }
                },
            )
        },
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier,
        windowInsets = WindowInsets(0),
    ) {
        // No suggestion content
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarPreview() {
    MyApplicationTheme {
        SearchBar(
            query = "",
            onQueryChanged = {},
            onSearch = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SearchBarWithQueryPreview() {
    MyApplicationTheme {
        SearchBar(
            query = "android",
            onQueryChanged = {},
            onSearch = {},
        )
    }
}
