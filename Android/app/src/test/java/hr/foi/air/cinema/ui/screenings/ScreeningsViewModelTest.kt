package hr.foi.air.cinema.ui.screenings

import hr.foi.air.cinema.data.FakeScreeningRepository
import hr.foi.air.cinema.data.Screening
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ScreeningsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialState_isLoading() {
        val viewModel = ScreeningsViewModel(FakeScreeningRepository())

        assertEquals(ScreeningsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun screeningsLoaded_updatesStateToSuccessWithScreenings() = runTest {
        val screenings = listOf(
            Screening(id = "1", movieTitle = "Dune: Part Three"),
            Screening(id = "2", movieTitle = "Oppenheimer"),
        )
        val viewModel = ScreeningsViewModel(
            repository = FakeScreeningRepository(screeningsFlow = flowOf(screenings)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ScreeningsUiState.Success)
        assertEquals(screenings, (state as ScreeningsUiState.Success).allScreenings)
    }

    @Test
    fun repositoryError_updatesStateToError() = runTest {
        val viewModel = ScreeningsViewModel(
            repository = FakeScreeningRepository(
                screeningsFlow = flow { throw RuntimeException("Greška pri dohvaćanju projekcija") },
            ),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is ScreeningsUiState.Error)
        assertEquals("Greška pri dohvaćanju projekcija", (state as ScreeningsUiState.Error).message)
    }

    @Test
    fun categories_derivedFromLoadedScreenings_distinctAndSorted() = runTest {
        val screenings = listOf(
            Screening(id = "1", movieTitle = "A", category = "3D"),
            Screening(id = "2", movieTitle = "B", category = "Standard"),
            Screening(id = "3", movieTitle = "C", category = "3D"),
        )
        val viewModel = ScreeningsViewModel(
            repository = FakeScreeningRepository(screeningsFlow = flowOf(screenings)),
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as ScreeningsUiState.Success
        assertEquals(listOf("3D", "Standard"), state.categories)
    }

    @Test
    fun onCategorySelected_filtersDisplayedScreeningsToThatCategoryOnly() = runTest {
        val screenings = listOf(
            Screening(id = "1", movieTitle = "A", category = "3D"),
            Screening(id = "2", movieTitle = "B", category = "Standard"),
        )
        val viewModel = ScreeningsViewModel(
            repository = FakeScreeningRepository(screeningsFlow = flowOf(screenings)),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected("3D")

        val state = viewModel.uiState.value as ScreeningsUiState.Success
        assertEquals(listOf(screenings[0]), state.displayedScreenings)
    }

    @Test
    fun onCategorySelected_null_restoresFullList() = runTest {
        val screenings = listOf(
            Screening(id = "1", movieTitle = "A", category = "3D"),
            Screening(id = "2", movieTitle = "B", category = "Standard"),
        )
        val viewModel = ScreeningsViewModel(
            repository = FakeScreeningRepository(screeningsFlow = flowOf(screenings)),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected("3D")
        viewModel.onCategorySelected(null)

        val state = viewModel.uiState.value as ScreeningsUiState.Success
        assertEquals(screenings, state.displayedScreenings)
    }

    @Test
    fun categoryFilter_survivesRepeatedFirestoreEmissions() = runTest {
        val initial = listOf(
            Screening(id = "1", movieTitle = "A", category = "3D"),
            Screening(id = "2", movieTitle = "B", category = "Standard"),
        )
        val screeningsFlow = MutableStateFlow(initial)
        val viewModel = ScreeningsViewModel(
            repository = FakeScreeningRepository(screeningsFlow = screeningsFlow),
        )
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.onCategorySelected("3D")

        val updated = initial + Screening(id = "3", movieTitle = "C", category = "3D")
        screeningsFlow.value = updated
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as ScreeningsUiState.Success
        assertEquals("3D", state.selectedCategory)
        assertEquals(updated.filter { it.category == "3D" }, state.displayedScreenings)
    }
}
