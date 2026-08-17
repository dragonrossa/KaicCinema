plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation("com.google.firebase:firebase-admin:9.4.1")
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}

application {
    mainClass.set("hr.foi.air.cinema.seed.SeedFirestoreKt")
}