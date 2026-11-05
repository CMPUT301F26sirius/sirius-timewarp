package com.example.timewarp.feature.campsite.owner

import android.R.attr.top
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.ui.component.CampsiteCard
import com.example.timewarp.ui.theme.PitchError
import com.example.timewarp.ui.theme.PitchForest
import com.example.timewarp.ui.theme.PitchMutedText
import com.example.timewarp.ui.theme.TimewarpTheme

/*
 * Owner's campsite list, shown inside the Browse tab under "My campsites" (US 02.03.01).
 * Outstanding: "+ Register campsite" button goes here once Register Campsite (US 02.01.01) has a route.
 */

/**
 * "All campsites" / "My campsites" pills shown to owners on the Browse tab.
 *
 * @param showMyCampsites true when "My campsites" is selected.
 * @param onToggle called with true for "My campsites", false for "All campsites".
 */
@Composable
fun CampsiteListToggle(showMyCampsites: Boolean, onToggle: (Boolean) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 15.dp)
    ) {
        FilterChip(
            selected = !showMyCampsites,
            onClick = { onToggle(false) },
            label = { Text("All campsites") },
            modifier = Modifier.testTag("all_campsites_pill")
        )
        FilterChip(
            selected = showMyCampsites,
            onClick = { onToggle(true) },
            label = { Text("My campsites") },
            modifier = Modifier.testTag("my_campsites_pill")
        )
    }
}

/**
 * List of the signed-in owner's campsites, under a "Manage your campsites" heading.
 *
 * @param state what to show.
 * @param onCampsiteClick called with the campsite ID when a card is tapped.
 */
@Composable
fun MyCampsitesContent(
    state: MyCampsitesState,
    onCampsiteClick: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(top = 15.dp)
    ) {
        Text(
            text = "Manage your campsites",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        when (state) {
            MyCampsitesState.Loading -> Text("Loading your campsites...")
            is MyCampsitesState.Error -> Text("Error: ${state.message}", color = PitchError)

            is MyCampsitesState.Content -> {
                if (state.items.isEmpty()) {
                    Text(
                        text = "You haven't registered any campsites yet",
                        color = PitchMutedText,
                        modifier = Modifier.testTag("no_my_campsites")
                    )
                } else {
                    for (item in state.items) {
                        MyCampsiteRow(
                            item = item,
                            onClick = { onCampsiteClick(item.campsite.campsiteId) }
                        )
                    }
                }
            }
        }
    }
}

// One campsite card, plus a lottery label when one is open or coming up
@Composable
private fun MyCampsiteRow(item: MyCampsiteItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("my_campsite_${item.campsite.campsiteId}")
    ) {
        CampsiteCard(campsite = item.campsite, onClick = onClick)

        if (item.hasUpcomingLottery) {
            Text(
                text = "Lottery open or upcoming",
                style = MaterialTheme.typography.labelMedium,
                color = PitchForest
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MyCampsitesContentPreview() {
    val first = Campsite(campsiteId = "1", name = "Wapiti Camp", location = Location(address = "Jasper, Alberta"))
    val second = Campsite(campsiteId = "2", name = "Two Jack Main", location = Location(address = "Banff, Alberta"))
    val items = listOf(MyCampsiteItem(first, true), MyCampsiteItem(second, false))

    TimewarpTheme {
        MyCampsitesContent(state = MyCampsitesState.Content(items), onCampsiteClick = {})
    }
}