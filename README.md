# KaicCinema

## Git workflow

Naziv grane i prefiks commit poruke moraju biti `feature/SCRUM-<id>-kratak-opis` odnosno `SCRUM-<id>: opis`, gdje je `<id>` **točan** broj Jira ticketa.

**Prije pokretanja `git checkout -b feature/SCRUM-<id>-...`, obavezno potvrdi broj ticketa u Jiri** — ne kreiraj granu na temelju broja iz sažetka razgovora, chata ili sjećanja. Kriv broj znači naknadno preimenovanje grane i/ili rebase commita (vidi incident: grana i commit su prvo kreirani kao "SCRUM-69" umjesto ispravnog "SCRUM-93", što je zahtijevalo naknadan popravak).

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

## Rješavanje problema s Android emulatorom

Dva poznata problema s emulatorom nemaju veze s kodom aplikacije — ako naiđeš na njih, ne treba ih ponovno dijagnosticirati, samo primijeni fix ispod.

### "Network error" / DNS ne radi na emulatoru

**Simptom:** login ili bilo koji Firebase poziv puca s "A network error (such as timeout, interrupted connection or unreachable host) has occurred", iako računalo ima normalan internet. U logcatu se vidi `UnknownHostException: Unable to resolve host` za sve domene (npr. `www.google.com`), dok `adb shell ping -c 2 8.8.8.8` (sirovi IP) prolazi bez problema.

**Uzrok:** interni DNS proxy emulatora (`10.0.2.3`, dio QEMU virtualne mreže) povremeno zaglavi, obično nakon dužeg rada emulatora ili buđenja računala iz spavanja. Ne popravlja se togglanjem WiFi-a unutar emulatora (`svc wifi disable/enable`) jer je problem na nižem, QEMU sloju.

**Fix:** restartaj emulator (cold boot):
- Iz Android Studija: **Device Manager** → strelica dolje pored emulatora → **Cold Boot Now**
- Ili iz terminala: `adb -s <device> emu kill`, pa ponovno pokreni emulator

Nakon restarta provjeri DNS prije nego nastaviš testirati: `adb shell ping -c 2 www.google.com`.

### Emulator se ne pokreće / "Running multiple emulators with the same AVD"

**Simptom:** emulator se ugasi sam od sebe, ne pojavi se u `adb devices`, ili se u logu pojavi `FATAL | Running multiple emulators with the same AVD is an experimental feature` iako nijedan drugi emulator vidljivo ne radi.

**Uzrok:** nedostatak RAM-a/swapa na računalu (emulator je težak proces) — ne stvarni sukob dviju instanci. Provjeri swap: `sysctl vm.swapusage` — ako je gotovo pun, to je uzrok.

**Fix:** prije pokretanja emulatora zatvori memorijski zahtjevne aplikacije koje trenutno ne trebaš (puno Chrome tabova, Teams, Word, Remote Desktop Manager i sl.), zatim pokreni/restartaj emulator iz Android Studija (Device Manager).

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
