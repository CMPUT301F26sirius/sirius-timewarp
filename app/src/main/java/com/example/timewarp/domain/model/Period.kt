package com.example.timewarp.domain.model

/**
 * [Period] data class
 *
 * Inclusive integer-based date period. This will almost certainly need a refactor.
 * Data member of [Campsite].
 *
 * @property start - First day in the period.
 * @property end - Last day in the period.
 */
class Period (
    val start: Int = 0,
    val end: Int = 0
)