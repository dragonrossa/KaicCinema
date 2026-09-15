# KaicCinema

## Potreban JDK za build

Projekt zahtijeva **JDK 21** za pokretanje Gradlea (ne samo za target/compile compatibility — sam Gradle daemon i Kotlin DSL kompajler moraju se pokretati na JDK-u koji podržavaju). Trenutno korišten Gradle (vidi `gradle/wrapper/gradle-wrapper.properties`) **ne podržava JDK 24+** — pokretanje na novijem JDK-u (npr. JDK 25) puca s kriptičnom greškom oblika `IllegalArgumentException: 25.0.3` iz Kotlin DSL kompajlera, umjesto jasne poruke o nekompatibilnoj verziji.

**Poznat problem:** Android Studio interno imenuje JDK profile (npr. "jbr-21"), ali nakon ažuriranja IDE-a taj isti naziv može tiho počne pokazivati na noviji, nepodržani JVM (vidi SCRUM-121 incident) — naziv profila se ne mijenja, ali stvarna putanja iza njega može.

**Nakon svakog ažuriranja Android Studija ili JDK-a, provjeri prije builda:**

```
./gradlew --version
```

U outputu provjeri red `Launcher JVM:` — treba pisati **21.x**. Ako pokazuje 24, 25 ili višu verziju:

1. U Android Studiju: **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK** — odaberi stvarni JDK 21 (po potrebi **Download JDK...** ako nemaš instaliran)
2. Provjeri da `.idea/gradle.xml` (`gradleJvm`) pokazuje na taj JDK, ne na stari/preimenovani profil

**Preporučeno (trajno rješenje po računalu):** postavi `org.gradle.java.home` u svoj **osobni**, necommitani `~/.gradle/gradle.properties` (ne u projektni `gradle.properties` — taj je zajednički i putanja bi bila specifična za tvoj OS/korisnika):

```
org.gradle.java.home=/apsolutna/putanja/do/tvog/jdk21
```

Ovo prisiljava Gradle da uvijek koristi taj JDK, neovisno o tome što IDE-ov "Gradle JDK" profil tiho promijeni ispod istog naziva.

## Firestore security rules

Sva pravila pristupa nalaze se u `firestore.rules` (root repozitorija) i moraju se ručno objaviti u Firebase Console (Firestore Database → Rules → Publish) — repozitorij nema Firebase CLI/`firebase.json` postavljen za automatski deploy.

**Kad dodaješ novu Firestore kolekciju, obavezno:**

1. Dodaj `match /<kolekcija>/{docId} { ... }` blok u `firestore.rules` s pravilima za `read`/`create`/`update`/`delete` — prati postojeće obrasce (npr. `screenings`, `news`) kao predložak.
2. Objavi izmjenu u Firebase Console (copy-paste sadržaja `firestore.rules`) — sama datoteka u repozitoriju ne utječe na produkciju dok se ručno ne objavi.
3. Ručno testiraj write/read operaciju na uređaju/emulatoru prije nego zatvoriš ticket — unit testovi s fake repozitorijima **ne** validiraju stvarna Firestore pravila, pa PERMISSION_DENIED greška ostaje neotkrivena dok se ne testira uživo (vidi SCRUM-95, SCRUM-122).

Trenutno pokrivene kolekcije: `users`, `screenings`, `reservations`, `purchases`, `news`.

## Seedanje test podataka u Firestore

`firestore-seed` je zaseban Gradle/Kotlin modul koji jednom naredbom puni `screenings` kolekciju sa setom test projekcija (različite kategorije, views, popularity, broj mjesta), umjesto ručnog unosa kroz Firebase konzolu.

### Priprema

1. U Firebase konzoli → Project settings → Service accounts → Generate new private key. Preuzmi JSON.
2. Spremi ga negdje **izvan gita** (npr. `firestore-seed/serviceAccount.json` — taj obrazac imena je već u `.gitignore`, ali svejedno pripazi da ga ne commitaš).
3. Postavi environment varijablu na tu putanju:
   ```
   export GOOGLE_APPLICATION_CREDENTIALS="/apsolutna/putanja/do/serviceAccount.json"
   ```

### Pokretanje

Iz `Android/` mape (koristi postojeći Gradle wrapper):

```
./gradlew :firestore-seed:run
```

Ovo **dodaje** 6 test projekcija u `screenings` kolekciju — svako pokretanje stvara nove dokumente (nije idempotentno, ponovljeno pokretanje duplicira podatke).

Za čisti reset prije seedanja (**briše sve postojeće dokumente** u `screenings`):

```
./gradlew :firestore-seed:run --args="--clear"
```

⚠️ `--clear` je destruktivno i briše SVE postojeće projekcije, uključujući stvarne podatke ako slučajno pokreneš protiv pogrešnog Firebase projekta — provjeri da service account key pokazuje na dev/test projekt, ne produkciju.
