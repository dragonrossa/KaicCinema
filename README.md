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

Trenutno pokrivene kolekcije: `users`, `screenings`, `reservations`, `purchases`, `news`, `screeningNotifications`.

## Push notifikacije (Cloud Functions)

`functions/` je zaseban Node.js modul (Cloud Functions, 2nd gen) koji šalje FCM push notifikaciju korisniku kad admin odobri/odbije njegovu rezervaciju. Trigeriran je na `onDocumentUpdated` za `reservations/{reservationId}` (vidi `functions/index.js`).

### Priprema

```
cd functions
npm install
```

### Testiranje

**Logika triggera (besplatno, lokalno, bez interneta):**

```
npm test
```

Jest testovi (`functions/index.test.js`) pokrivaju `buildReservationDecisionMessage` i `handleReservationStatusChange` kroz injektirane fakeove za Firestore/Messaging — ne zahtijevaju stvarni Firebase projekt.

Za integracijsku provjeru da se trigger stvarno okida na promjenu statusa (i čita ispravne dokumente), koristi Firebase Local Emulator Suite iz root mape repozitorija:

```
firebase emulators:start --only functions,firestore --project=demo-cinema
```

⚠️ **Bitno ograničenje:** Emulator Suite **nema FCM emulator** — sam `getMessaging().send()` poziv unutar funkcije uvijek pokušava kontaktirati stvarni Google servis i treba prava produkcijska vjerodajnica, pa će u emulatoru visjeti ~60s i timeoutirati na tom koraku. Emulator je koristan samo za potvrdu da se trigger okida i čita prave podatke (`before`/`after` status, `userId`, `screeningId`) — ne za stvarnu isporuku notifikacije.

**Stvarna isporuka na uređaj (bez deploya, potpuno besplatno):** FCM slanje ne zahtijeva Blaze plan, samo Cloud Functions to traže. Da provjeriš da Android klijent (`CinemaMessagingService`, dopuštenje, notifikacijski kanal) stvarno prima i prikazuje notifikaciju:

1. Prijavi se u app na uređaju/emulatoru — token se sprema u `users/{uid}.fcmToken` u Firestore
2. Firebase Console → Firestore Database → `users/{uid}` → kopiraj `fcmToken`
3. Firebase Console → Messaging → New notification → **Firebase Notification messages** (ne "In-App messages", to je drugi SDK koji nije integriran) → "Test on device" → zalijepi token, pritisni Enter/"+" da se doda na listu → Test

### Deploy (zahtijeva Blaze plan)

Cloud Functions rade samo na Firebase **Blaze** (pay-as-you-go) planu — provjeri to u Firebase Console prije deploya. Sam FCM je besplatan; Cloud Functions imaju generozan free tier, pa je stvarni trošak za ovakav mali projekt tipično $0, ali Blaze i dalje zahtijeva dodanu karticu na projekt.

```
firebase login
firebase deploy --only functions
```

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
