package com.example.timewarp.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Period
import com.example.timewarp.ui.theme.PitchBorder
import com.example.timewarp.ui.theme.PitchForest
import com.example.timewarp.ui.theme.PitchMutedText
import com.example.timewarp.ui.theme.PitchSurface
import com.example.timewarp.ui.theme.PitchText


/*
* "Your days outside" calendar for the reservation screens (US 01.04.01).
* Booked days are crossed out and can't be tapped.
* Outstanding: month label only shows the first month.
*
* Row of days in [bookingPeriod].
* @param availableDays days that can be booked; others are crossed out.
* @param selectedStart check-in day, or null.
* @param selectedEnd check-out day, or null.
* @param onDayClick called when an available day is tapped.
*/

@Composable
fun AvailabilityCalendar(
    bookingPeriod: Period,
    availableDays: List<Int>,
    selectedStart: Int?,
    selectedEnd: Int?,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        CalendarHeader(firstDay = bookingPeriod.start)

        Spacer(modifier = Modifier.height(12.dp))

        // Invalid period: nothing to show
        if (bookingPeriod.end < bookingPeriod.start) {
            Text(text = "No dates available", fontSize = 13.sp, color = PitchMutedText)
            return@Column
        }

        val allDays = (bookingPeriod.start..bookingPeriod.end).toList()

        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(allDays) { day ->
                DayCell(
                    day = day,
                    isAvailable = availableDays.contains(day),
                    isSelected = isDaySelected(day, selectedStart, selectedEnd),
                    onClick = { onDayClick(day) }
                )
            }
        }
    }
}

// Title on the left, month on the right.
@Composable
private fun CalendarHeader(firstDay: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Your days outside",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = PitchText
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = DayFormatter.monthYear(firstDay),
            fontSize = 12.sp,
            color = PitchMutedText
        )
    }
}

// One day box: weekday letter on top, day number below.
@Composable
private fun DayCell(
    day: Int,
    isAvailable: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)

    var backgroundColor = PitchSurface
    var textColor = PitchText
    var textDecoration = TextDecoration.None

    if (isSelected) {
        backgroundColor = PitchForest
        textColor = PitchSurface
    }

    // Booked days are grey and crossed out
    if (!isAvailable) {
        textColor = PitchMutedText
        textDecoration = TextDecoration.LineThrough
    }

    var cellModifier = Modifier
        .size(width = 40.dp, height = 56.dp)
        .background(backgroundColor, shape)
        .border(1.dp, PitchBorder, shape)
        .testTag("calendar_day_$day")

    // Only available days can be tapped
    if (isAvailable) {
        cellModifier = cellModifier.clickable { onClick() }
    }

    Column(
        modifier = cellModifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = DayFormatter.weekdayLetter(day),
            fontSize = 10.sp,
            color = textColor,
            textDecoration = textDecoration
        )
        Text(
            text = DayFormatter.dayOfMonth(day).toString(),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            textDecoration = textDecoration
        )
    }
}

// True if [day] is between check-in and check-out (inclusive).
private fun isDaySelected(day: Int, selectedStart: Int?, selectedEnd: Int?): Boolean {
    if (selectedStart == null) {
        return false
    }

    // Only check-in picked
    if (selectedEnd == null) {
        return day == selectedStart
    }

    return day >= selectedStart && day <= selectedEnd
}

// November 10–16, 2027: November 12–13 booked, November 14–15 selected.
@Preview(showBackground = true)
@Composable
private fun AvailabilityCalendarPreview() {
    val nov10 = 21132

    AvailabilityCalendar(
        bookingPeriod = Period(start = nov10, end = nov10 + 6),
        availableDays = listOf(nov10, nov10 + 1, nov10 + 4, nov10 + 5, nov10 + 6),
        selectedStart = nov10 + 4,
        selectedEnd = nov10 + 5,
        onDayClick = {}
    )
}