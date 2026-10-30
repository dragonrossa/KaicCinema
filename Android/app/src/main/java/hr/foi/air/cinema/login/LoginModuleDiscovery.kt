package hr.foi.air.cinema.login

import hr.foi.air.cinema.core.LoginModuleProvider
import java.util.ServiceLoader

fun discoverLoginModuleProviders(): List<LoginModuleProvider> =
    ServiceLoader.load(LoginModuleProvider::class.java).toList()
