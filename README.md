# KaicCinema

## Odluka: arhitektura push notifikacija (FCM)

Aplikacija nema server-side komponentu — svi upisi u Firestore idu izravno iz Android klijenta. Push notifikacije (odluka o rezervaciji, nova projekcija, admin obavijest vezana uz projekciju) zahtijevaju nešto što detektira relevantan Firestore upis i pošalje FCM poruku, jer klijent koji je napisao promjenu ne može pouzdano poslati notifikaciju **drugom** korisniku (nema pristup njegovom FCM tokenu niti razlog da drži server-side kredencijale).

**Razmatrane opcije:**

1. **Cloud Functions (Firestore trigger)** — server-side funkcija koja sluša `onDocumentCreated`/`onDocumentUpdated` na relevantnoj kolekciji i šalje FCM poruku preko Firebase Admin SDK-a.
2. **Klijentski trigger** — admin uređaj izravno zove FCM Admin API/HTTP v1 kad izvrši akciju. Odbačeno: zahtijeva da klijent (mobilna aplikacija) drži server-side kredencijale/service account, što je sigurnosno neprihvatljivo (kredencijal bi bio dostupan svakom tko dekompilira APK).
3. **Scheduled job (periodično provjeravanje)** — polling najnovijih promjena u fiksnim intervalima. Odbačeno: uvodi kašnjenje (notifikacija ne stiže odmah nakon promjene) i nepotrebno je kompleksnije od direktnog triggera na sam upis.

**Odluka: Cloud Functions (Firestore trigger).** Ovo je standardan, preporučen Firebase obrazac za ovaj problem — nema sigurnosnih kompromisa klijentskog pristupa, a šalje notifikaciju odmah čim se dogodi relevantan upis (bez kašnjenja pollinga).

**Infrastrukturne/troškovne implikacije:**

- Cloud Functions zahtijevaju Firebase **Blaze plan** (pay-as-you-go, potrebna dodana kartica na projekt) — projekt je do sada bio na besplatnom Spark planu.
- Sam FCM (slanje notifikacija) je uvijek besplatan, bez obzira na plan — trošak dolazi isključivo od Cloud Functions izvršavanja.
- Cloud Functions imaju generozan free tier (milijuni pozivâ mjesečno) — za opseg ovog projekta stvarni trošak je u praksi $0, ali Blaze plan i dalje zahtijeva postavljenu karticu.
- Novi Node.js modul (`functions/`) u repozitoriju, sa zasebnim `package.json`/testovima (Jest) — ne utječe na Android build, ali zahtijeva Node.js i Firebase CLI instalirane lokalno za razvoj/testiranje/deploy.
- Deploy je ručan korak (`firebase deploy --only functions`), izvan Android CI/CD toka.

**Mapiranje triggerâ na mehanizam** (svaki je zasebna Cloud Function, `functions/index.js`):

| Notifikacija | Firestore trigger | Cilj slanja |
|---|---|---|
| Odluka o rezervaciji (odobreno/odbijeno) | `onDocumentUpdated` na `reservations/{reservationId}`, kad se `status` promijeni u APPROVED/REJECTED | Pojedinačni FCM token korisnika (`users/{uid}.fcmToken`) |
| Nova projekcija objavljena | `onDocumentCreated` na `screenings/{screeningId}` | FCM tema `new_screenings` (svi pretplaćeni korisnici) |
| Admin obavijest vezana uz projekciju | `onDocumentCreated` na `screeningNotifications/{notificationId}` | FCM tema `screening_notifications` (svi pretplaćeni korisnici) |

**Napomena o testiranju:** Firebase Local Emulator Suite nema FCM emulator — trigger logika (koja Firestore polja čita, koga cilja) testira se besplatno lokalno emulatorom, ali stvarna isporuka notifikacije zahtijeva pravi deploy ili slanje test poruke kroz Firebase Console. Ova odluka je provedena kroz SCRUM-101 (odluka o rezervaciji), SCRUM-102 (nova projekcija) i SCRUM-103 (admin obavijest) — detaljne upute za setup/testiranje/deploy `functions/` modula nalaze se u sekciji "Push notifikacije (Cloud Functions)" (dodanoj kroz te tickete).

## Odluka: FCM dopuštenje i upravljanje device tokenom

Slanje ciljanih push notifikacija zahtijeva spremljen FCM token po korisniku i zatraženo dopuštenje za notifikacije na Android 13+. Ova odluka definira očekivano ponašanje u oba slučaja, kako bi tickete za slanje notifikacija (SCRUM-101/102/103) bilo moguće implementirati na konzistentan način.

**Spremanje i osvježavanje tokena:**

- Token se sprema u **jedno** polje `fcmToken` na `users/{uid}` Firestore dokumentu (ne u zasebnu kolekciju/podkolekciju) — najjednostavniji oblik dovoljan za trenutan opseg aplikacije.
- Token se dohvaća i sprema pri **svakoj uspješnoj prijavi** (`LoginViewModel.setUpPushNotifications()`), i ponovno svaki put kad Firebase SDK interno osvježi token (`CinemaMessagingService.onNewToken()`), čime ostaje ažuran bez potrebe za ručnim osvježavanjem od strane korisnika.
- Firestore pravilo (`firestore.rules`) dopušta korisniku da upiše/ažurira **isključivo** vlastito `fcmToken` polje na vlastitom dokumentu (`request.auth.uid == userId` i `affectedKeys().hasOnly(['fcmToken'])`), ništa drugo — sprječava korisnika da mijenja npr. `role`.

**Multi-device: eksplicitno izvan opsega.** Trenutna shema podržava **jedan token po korisniku** — ako se isti korisnik prijavi na drugom uređaju, novi token prepisuje stari (posljednja prijava "pobjeđuje"), pa prethodni uređaj prestaje primati notifikacije bez ikakve obavijesti o tome. Ovo je svjesan kompromis radi jednostavnosti; podrška za više uređaja po korisniku (npr. podkolekcija `users/{uid}/deviceTokens/{token}` uz slanje na sve tokene) nije implementirana i trebala bi biti zaseban budući ticket ako postane potrebna.

**Ponašanje kod dopuštenja za notifikacije (Android 13+):**

- Dopuštenje `POST_NOTIFICATIONS` traži se jednom pri pokretanju `MainActivity` (`requestNotificationPermissionIfNeeded()`), samo ako još nije odobreno — ne traži se ponovno unutar iste sesije ako je već odobreno ili odbijeno.
- **Ako korisnik odbije:** aplikacija nastavlja raditi normalno, bez pada — poziv `NotificationManager.notify()` u pozadini jednostavno neće prikazati ništa na uređajima bez dopuštenja (Android to tiho ignorira, ne baca iznimku). Nema dodatne logike koja bi to posebno hvatala jer nije ni potrebna.
- **Ponovno pitanje nakon odbijanja:** ne implementira se ručno u kodu — Android sam po sebi (nakon jednog ili dva odbijanja, ovisno o verziji) prestaje prikazivati sistemski dijalog za to dopuštenje dok korisnik ručno ne omogući iz postavki, pa naš kod ne mora (i ne smije) pokušavati zaobići to ponašanje.
- **Re-request UI unutar aplikacije: trenutno ne postoji.** Nema gumba/postavke unutar aplikacije koja bi korisnika uputila natrag u sistemske postavke da ručno omogući notifikacije nakon odbijanja. Ovo je svjesna odluka radi opsega — ako se pokaže potrebnim (npr. korisnici se žale da ne dobivaju notifikacije), dodavanje takvog gumba (koji otvara `Settings.ACTION_APP_NOTIFICATION_SETTINGS`) je mali, izolirani budući ticket.

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
