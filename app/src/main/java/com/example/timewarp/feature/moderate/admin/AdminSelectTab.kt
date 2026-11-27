package com.example.timewarp.feature.moderate.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.timewarp.feature.moderate.admin.messages.AdminMessagesSection
import com.example.timewarp.feature.moderate.admin.notifications.AdminNotificationSection
import com.example.timewarp.ui.theme.PitchForest

/**
 * This function will display a tab bar for switching between the admin alert section.
 *
 * @param modifier Modifier applied to the column
 * @param tabs List of tabs to display. It is on AdminTab.all as default
 */

@Composable
fun AdminSelectTab(
    modifier: Modifier = Modifier,
    tabs: List<AdminTab> = AdminTab.all
){
    var selectedIndex by rememberSaveable { mutableIntStateOf(0) }
    val index = selectedIndex.coerceIn(0, tabs.lastIndex)

    Column(
        modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ScrollableTabRow(
            selectedTabIndex = index,
            edgePadding = 4.dp,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            indicator = {},
            divider = {},
            modifier = Modifier.clip(RoundedCornerShape(15.dp))
        ) {
            tabs.forEachIndexed {
                t, tab ->
                val selected = t == index
                Tab(
                    selected = t == index,
                    onClick = {selectedIndex = t},
                    text = { Text(
                        tab.label,
                        fontWeight = if (selected) FontWeight.Bold
                        else FontWeight.Normal,
                        color = if (selected) PitchForest
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    ) },
                    modifier = Modifier.testTag("admin_tab_${tab.key}")
                        .padding(vertical = 4.dp, horizontal = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (selected) MaterialTheme.colorScheme.surface
                            else Color.Transparent
                        )
                )
            }
        }

        // Active tabs under the admin alert section
        when (tabs[index]) {
            AdminTab.Profiles -> AdminProfileList()
            AdminTab.Images -> AdminPlaceholder("Images")
            AdminTab.Messages -> AdminMessages()
            AdminTab.Notifications -> AdminNotificationSection()
        }
    }
}


/*
 * This function is a temporary placeholder for the admin tab screen. It will show the section is
 * currently under development for now. Will remove it after the sections are completed
 */

@Composable
internal fun AdminPlaceholder(name: String) {
    Box(
        Modifier.fillMaxSize().testTag("admin_placeholder_$name"),
        contentAlignment = Alignment.Center
    ) {
        Text("$name: Coming Soon")
    }
}