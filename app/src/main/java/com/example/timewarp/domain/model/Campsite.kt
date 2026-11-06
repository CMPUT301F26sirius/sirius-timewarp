package com.example.timewarp.domain.model

/**
 * [Availability] data class
 *
 * This class stores important information about campsites, which are stored
 * in the firebase `Campsite` collection.
 *
 * Issues:
 * - [Period], [Availability], [CancellationPolicy] and images will need large
 * refactors.
 * - images should point to Image via imageId.
 *
 * @property campsiteId - A universally unique identifier for this object
 * @property ownerUserId - A universally unique identifier that points to
 *  the user who owns this campsite.
 * @property name - A string, representing the campsite name
 * @property description - A owner set free-form string describing the camp
 * @property amenities - A list of amenities, to be listed in a list layout
 * @property tags - A list of tags that describe the campsite. Should be used
 *  to optimize search.
 * @property rating - A cached average of the ratings for this site.
 * @property location - Of type Location, describing address, long and lat
 *  of the site
 * @property occupancy - Of type Occupancy, describing the max number of
 *  occupants allowed.
 * @property pricing - Of type Pricing, describing the pricing scheme used
 *  for the campsite.
 * @property availability - Of type Availability, describing available
 *  dates that could be booked
 * @property rules - A list of sting rules defined by the owner.
 * @property cancellationPolicy - Of type CancellationPolicy, describes the
 *  terms of cancellation.
 * @property images - A list of string urls.
 */
data class Campsite(
    val campsiteId: String = "",
    val ownerUserId: String = "",

    var name: String = "",
    var description: String = "",

    val amenities: List<String> = emptyList<String>(),
    val tags: List<String> = emptyList<String>(),

    val rating: Double = 0.0,
    var location: Location = Location(),

    val occupancy: Occupancy = Occupancy(),
    val pricing: Pricing = Pricing(),
    val availability: Availability = Availability(),

    val rules: List<String> = emptyList<String>(),
    val cancellationPolicy: CancellationPolicy = CancellationPolicy(),

    val images: List<String> = emptyList<String>()
) {

}
