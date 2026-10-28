package com.example.timewarp.domain.model


// This data class tells what a campsite needs.
data class Campsite(
    val id: String = "", // This will be the Firestore document ID

    val name: String = "",

    val location: String = "" , // This will tell the location of the campsite

    val maximumOccupancy: Int = 0, // Total number of guests

    val rating: Double = 0.0 // over all rating for the campsite. example = 4.9 out of 5
)
