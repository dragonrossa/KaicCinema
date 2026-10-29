package hr.foi.air.cinema.loginemail

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import org.junit.Assert.assertEquals
import org.junit.Test

class AuthErrorMapperTest {

    @Test
    fun invalidCredentials_mapsToWrongEmailOrPasswordMessage() {
        val error = FirebaseAuthInvalidCredentialsException(
            "ERROR_INVALID_CREDENTIAL",
            "The supplied auth credential is incorrect, malformed or has expired.",
        )

        assertEquals("Pogrešan email ili lozinka", mapAuthErrorMessage(error))
    }

    @Test
    fun invalidUser_mapsToAccountDisabledOrMissingMessage() {
        val error = FirebaseAuthInvalidUserException("ERROR_USER_DISABLED", "The user account has been disabled.")

        assertEquals("Korisnički račun je onemogućen ili ne postoji", mapAuthErrorMessage(error))
    }

    @Test
    fun tooManyRequests_mapsToTryAgainLaterMessage() {
        val error = FirebaseTooManyRequestsException("We have blocked all requests from this device.")

        assertEquals("Previše pokušaja prijave. Pokušajte ponovno kasnije", mapAuthErrorMessage(error))
    }

    @Test
    fun networkError_mapsToCheckConnectionMessage() {
        val error = FirebaseNetworkException("A network error has occurred.")

        assertEquals("Provjerite internetsku vezu i pokušajte ponovno", mapAuthErrorMessage(error))
    }

    @Test
    fun unknownError_mapsToGenericFallbackMessage() {
        val error = RuntimeException("something unexpected")

        assertEquals("Prijava nije uspjela", mapAuthErrorMessage(error))
    }
}
