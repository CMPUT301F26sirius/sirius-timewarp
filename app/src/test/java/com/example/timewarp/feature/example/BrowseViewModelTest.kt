package com.example.timewarp.feature.example

import com.example.timewarp.data.repository.MockCampsiteRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.CampsiteFilter
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.feature.browse.BrowseViewModel
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun viewModel(repo: CampsiteRepository = MockCampsiteRepository()) =
        BrowseViewModel(repo, FakeRatingRepository())

    @Test
    fun loads_showsTwoCampsites() {
        val vm = viewModel()
        assertFalse(vm.state.value.isLoading)
        assertEquals(2, vm.state.value.campsites.size)
    }

    @Test
    fun sevenGuests_returnEmpty() {
        val vm = viewModel()
        vm.onFiltersChanged(CampsiteFilter(guests = 7))
        assertTrue(vm.state.value.campsites.isEmpty())
    }

    @Test
    fun sixGuests_returnBoth() {
        val vm = viewModel()
        vm.onFiltersChanged(CampsiteFilter(guests = 6))
        assertEquals(2, vm.state.value.campsites.size)
    }

    @Test
    fun clearFilters_restoresList() {
        val vm = viewModel()
        vm.onFiltersChanged(CampsiteFilter(guests = 7))
        vm.onFiltersChanged(CampsiteFilter())
        assertEquals(2, vm.state.value.campsites.size)
    }

    @Test
    fun repositoryThrow_setsError() {
        val vm = viewModel(ThrowingCampsiteRepository())
        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

}

class FakeRatingRepository : RatingRepository {
    override suspend fun getCampsiteRatings(campsiteId: String): MutableList<Rating>? {
        TODO("Not yet implemented")
    }

    override suspend fun getUserRatings(userId: String): MutableList<Rating>? {
        TODO("Not yet implemented")
    }

    override suspend fun getRatings(): MutableList<Rating>? {
        TODO("Not yet implemented")
    }

    override suspend fun createRating(rating: Rating) {
        TODO("Not yet implemented")
    }

    override suspend fun getRating(ratingId: String): Rating? {
        TODO("Not yet implemented")
    }

    override fun observeCampsiteRatings(campsiteId: String): Flow<List<Rating>> {
        TODO("Not yet implemented")
    }

}


class ThrowingCampsiteRepository : CampsiteRepository {
    override suspend fun getCampsites(): MutableList<Campsite>? {
        TODO("Not yet implemented")
    }

    override suspend fun createCampsite(campsite: Campsite) {
        TODO("Not yet implemented")
    }

    override suspend fun editCampsite(
        campsiteId: String,
        campsite: Campsite
    ) {
        TODO("Not yet implemented")
    }

    override suspend fun getCampsite(campsiteId: String): Campsite? {
        TODO("Not yet implemented")
    }

    override suspend fun queryCampsites(query: String): MutableList<Campsite>? {
        TODO("Not yet implemented")
    }

    override fun observeCampsite(campsiteId: String): Flow<Campsite?> {
        TODO("Not yet implemented")
    }

    override suspend fun filterCampsites(location: String, guests: Int?): MutableList<Campsite>? =
        throw RuntimeException("boom")

}
