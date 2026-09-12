# KaicCinema

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
