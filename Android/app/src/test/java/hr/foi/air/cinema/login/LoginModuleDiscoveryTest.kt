package hr.foi.air.cinema.login

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginModuleDiscoveryTest {

    @Test
    fun discoverLoginModuleProviders_findsProviderRegisteredViaServiceLoader() {
        val providers = discoverLoginModuleProviders()

        val fakeProvider = providers.find { it.id == "fake-test-provider" }
        assertTrue("Expected FakeLoginModuleProvider to be discovered via ServiceLoader", fakeProvider != null)
        assertEquals("Fake Test Provider", fakeProvider?.displayName)
    }

    @Test
    fun discoverLoginModuleProviders_noDuplicateIds() {
        val providers = discoverLoginModuleProviders()

        val ids = providers.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }
}
