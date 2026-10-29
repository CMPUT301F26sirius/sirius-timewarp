package com.example.timewarp.domain.model

@kotlin.uuid.ExperimentalUuidApi
class Campsite(
    val id: String,
    val ownerId: String,

    var name: String,
    var description: String,

    val amenities: List<String> = emptyList<String>(),
    val tags: List<String> = emptyList<String>(),

    var location: Location = Location(),

    val occupancy: Occupancy = Occupancy(),
    val pricing: Pricing = Pricing(),

    val rules: List<String> = emptyList(),
    val cancellationPolicy: CancellationPolicy = CancellationPolicy(),

    val images: List<String> = emptyList<String>()
) {

}
