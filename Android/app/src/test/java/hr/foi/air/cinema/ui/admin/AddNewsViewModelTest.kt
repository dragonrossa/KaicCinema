package hr.foi.air.cinema.ui.admin

import hr.foi.air.cinema.data.FakeNewsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class AddNewsViewModelTest {

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
    fun initialState_isIdle() {
        val viewModel = AddNewsViewModel(FakeNewsRepository())

        assertEquals(AddNewsUiState.Idle, viewModel.uiState.value)
    }

    @Test
    fun publishNews_success_updatesStateToSuccessAndSavesToRepository() = runTest {
        val repository = FakeNewsRepository()
        val viewModel = AddNewsViewModel(repository)

        viewModel.publishNews(title = "Nova sezona filmova", content = "Uskoro stižu nove projekcije.")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddNewsUiState.Success)
        val saved = (state as AddNewsUiState.Success).news
        assertEquals("Nova sezona filmova", saved.title)
        assertEquals("Uskoro stižu nove projekcije.", saved.content)
        assertEquals(1, repository.addNewsCallCount)
    }

    @Test
    fun publishNews_blankTitle_updatesStateToErrorWithoutCallingRepository() {
        val repository = FakeNewsRepository()
        val viewModel = AddNewsViewModel(repository)

        viewModel.publishNews(title = "  ", content = "Sadržaj")

        assertTrue(viewModel.uiState.value is AddNewsUiState.Error)
        assertEquals(0, repository.addNewsCallCount)
    }

    @Test
    fun publishNews_blankContent_updatesStateToErrorWithoutCallingRepository() {
        val repository = FakeNewsRepository()
        val viewModel = AddNewsViewModel(repository)

        viewModel.publishNews(title = "Naslov", content = " ")

        assertTrue(viewModel.uiState.value is AddNewsUiState.Error)
        assertEquals(0, repository.addNewsCallCount)
    }

    @Test
    fun publishNews_repositoryError_updatesStateToError() = runTest {
        val repository = FakeNewsRepository(
            addNewsResult = { Result.failure(RuntimeException("Greška pri objavi vijesti")) },
        )
        val viewModel = AddNewsViewModel(repository)

        viewModel.publishNews(title = "Naslov", content = "Sadržaj")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is AddNewsUiState.Error)
        assertEquals("Greška pri objavi vijesti", (state as AddNewsUiState.Error).message)
    }
}
