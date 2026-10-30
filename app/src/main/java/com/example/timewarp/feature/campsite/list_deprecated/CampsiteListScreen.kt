package com.example.timewarp.feature.campsite.list_deprecated

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

@Composable
fun CampsiteListScreen(
    modifier: Modifier = Modifier,
    viewModel: CampsiteListViewModel = hiltViewModel()
) {
//    val state by viewModel.uiState.collectAsStateWithLifecycle()
//
//    if (state.isLoading) {
//        CircularProgressIndicator()
//    } else {
//        LazyColumn(
//            modifier = modifier,
//            verticalArrangement = Arrangement.spacedBy(6.dp),
//            contentPadding = PaddingValues(15.dp)
//        ) {
//            items(state.campsites, key ={ it.id }) {
//                campsite -> CampsiteSummary(campsite = campsite)
//            }
//        }
//    }
}