# BaKalendar ![GitHub](https://img.shields.io/github/license/AlvaroFalcon/bakalendar_android?style=plastic)

BaKalendar is an Android app to follow the current anime season: what is airing, when the
next episode comes out in your own timezone, and which of your favorites air today.

Data comes from [Tenrai](https://tenrai.org), a free, unofficial MyAnimeList API that
follows the Jikan v4 schema.

## Features

- **Season list**: every anime airing this season, including series continuing from previous
  seasons (new shows first). Each entry shows a countdown to its next episode.
- **Cards or list**: big cards, or a compact list with the cover on the left and the countdown
  to the next episode. The choice is remembered.
- **Search, filters and sorting**: search by title; the filters sheet sorts by popularity or
  by soonest next episode and filters by one or more genres (anime matching any of them).
- **Detail**: collapsing cover with the title, tinted with colours picked from the cover;
  score, rank and episodes at a glance, next episode at your local time, trailer (opens in
  YouTube), synopsis, related anime (prequels, sequels, side stories…) and manga, studio,
  source, rating and more, plus a link to MyAnimeList. Drag it down to close it, or use the
  back gesture.
- **Favorites**: star any anime. Favorites are kept even after their season ends.
- **Weekly calendar**: Monday to Sunday, what airs each day at your local time, optionally
  only your favorites. Open it from the menu in the top right corner.
- **Daily reminder**: a notification around 10:00 listing the favorites that air in the
  next 24 hours.
- **Works offline**: the whole season is cached locally and refreshed every 12 hours or on
  pull-to-refresh; covers are cached too.

## Tech stack

- Kotlin, coroutines and Flow
- MVVM with AndroidX ViewModel, Hilt for dependency injection
- Room as the single source of truth (Flow), refreshed by a `SeasonRefresher`
- Retrofit + OkHttp + Gson for the API, Picasso for images
- WorkManager for the daily notification
- View-based UI with ViewBinding and Material Components
- Tests: JUnit, Robolectric (UI, Room and migrations), MockWebServer

Minimum SDK 26, target SDK 36.

## Project structure

```
app/src/main/java/com/frostfel/animelist/
├── data/            API (Retrofit), Room database, DAOs, season refresh and caching
├── injection/       Hilt modules
├── model/           API/Room models and schedule logic (next episode, weekly calendar)
├── notifications/   daily reminder (WorkManager) and notification permission
└── views/           season list, anime detail and weekly calendar screens
```

## Building and running

Requirements: JDK 17 and the Android SDK (API 36).

```bash
./gradlew assembleDebug          # build the debug APK
./gradlew testDebugUnitTest      # run the unit and Robolectric tests
./gradlew lintDebug              # run Android Lint
```

Or open the project in Android Studio and run the `app` configuration.

## CI

- **Build Debug APK** builds the app for every pull request to `develop`.
- **Generate signed AAB** builds and signs the release bundle on pushes to `master`, when a
  pull request is closed, or manually.

## Notes

- `docs/MIGRATION_JIKAN_TO_TENRAI.md` documents the migration from the discontinued Jikan
  API to Tenrai.
- Tenrai's public limits are 4 requests per second and 120 per minute; the app refreshes
  the season with a handful of requests and retries once when rate limited.

## License

[MIT](LICENSE)
