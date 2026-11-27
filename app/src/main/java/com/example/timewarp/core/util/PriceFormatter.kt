package com.example.timewarp.core.util







/** Formats an amount to two decimal places followed by its currency code. */
// Formats an amount with its currency, e.g. "45.00 CAD"
internal fun formatPrice(amount: Double, currencyCode: String): String {
    return "%.2f %s".format(amount, currencyCode)
}