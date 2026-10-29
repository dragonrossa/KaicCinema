package hr.foi.air.cinema.loginemail

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

internal fun mapAuthErrorMessage(error: Throwable): String = when (error) {
    is FirebaseAuthInvalidCredentialsException -> "Pogrešan email ili lozinka"
    is FirebaseAuthInvalidUserException -> "Korisnički račun je onemogućen ili ne postoji"
    is FirebaseTooManyRequestsException -> "Previše pokušaja prijave. Pokušajte ponovno kasnije"
    is FirebaseNetworkException -> "Provjerite internetsku vezu i pokušajte ponovno"
    else -> "Prijava nije uspjela"
}
