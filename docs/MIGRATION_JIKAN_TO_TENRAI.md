# Plan de migración: Jikan v4 → Tenrai v1

Rama base: `develop` (7 commits por delante de `master`, que no tiene nada que `develop` no tenga).

## 1. Contexto

Jikan v4 (`https://api.jikan.moe/v4/`) se ha discontinuado. [Tenrai](https://tenrai.org) es su
sucesor directo: una API REST no oficial sobre MyAnimeList que **replica el esquema de Jikan v4**,
de modo que la migración consiste principalmente en cambiar la URL base.

| | Jikan v4 | Tenrai v1 |
|---|---|---|
| URL base | `https://api.jikan.moe/v4/` | `https://api.tenrai.org/v1/` |
| Temporada actual | `GET seasons/now?page=N` | `GET seasons/now?page=N` |
| Detalle | `GET anime/{id}` | `GET anime/{id}` |
| Rate limit público | 3 req/s, 60 req/min | 3 req/s, 60 req/min, 40.000 req/día (por IP) |
| Exceso de límite | HTTP 429 | HTTP 429 + cabecera `Retry-After` |
| Clave opcional | — | `X-Server-Key` (300/min, 5/s; pensada para servidores) |
| Docs | jikan.moe | https://api.tenrai.org/documentation · https://api.tenrai.org/llms.txt |

Endpoints que Tenrai **no** soporta: users, clubs, watch, forum topics, user updates.
La app no usa ninguno.

## 2. Inventario del uso actual de la API

Todo el acceso a red está concentrado en dos ficheros:

- `injection/ApiModule.kt` → `BASE_URL = "https://api.jikan.moe/v4/"`
- `data/ApiServices.kt` → `seasons/now` y `anime/{id}`

Consumidores (no cambian si el contrato se mantiene):

- `data/mediators/AnimeRemoteMediator.kt` — paginación con `pagination.has_next_page`
- `views/season_list/paging/AnimePagingSource.kt` — (no se usa en el `Pager` actual)
- `views/season_list/repository/AnimeRepositoryImpl.kt` — `getAnimeById`

### Verificación de campos

Comparado contra los modelos de [Tenrai.Net](https://github.com/Kareadita/tenrai.net)
(wrapper oficial, v3.1.0), todos los campos que deserializa la app existen con el mismo nombre:

| Modelo app | Campos | Estado en Tenrai |
|---|---|---|
| `Anime` | `mal_id`, `url`, `images`, `trailer`, `title*`, `type`, `source`, `episodes`, `status`, `airing`, `aired`, `duration`, `rating`, `score`, `scored_by`, `rank`, `popularity`, `members`, `favorites`, `synopsis`, `background`, `season`, `year`, `broadcast`, `producers`, `licensors`, `studios`, `genres`, `explicit_genres`, `themes`, `demographics` | ✅ idénticos (Tenrai añade `titles`, `approved`) |
| `Images` / `Image` | `jpg`, `webp` / `image_url`, `small_image_url`, `large_image_url` | ✅ (añade `medium_image_url`, `maximum_image_url`) |
| `Broadcast` | `day`, `time`, `timezone`, `string` | ✅ |
| `Aired` | `from`, `to`, `prop` | ✅ |
| `TrailerInfo` | `youtube_id`, `url`, `embed_url` | ✅ |
| `Pagination` | `last_visible_page`, `has_next_page`, `items{count,total,per_page}` | ✅ (añade `current_page`) |

Los campos nuevos se ignoran con Gson, así que **no hace falta tocar modelos ni el esquema de Room**
(sin migración de BD; los `malId` siguen siendo IDs de MAL, así que los favoritos se conservan).

## 3. Plan por fases

### Fase 0 — Validación manual (antes de tocar código)
- [ ] `curl 'https://api.tenrai.org/v1/seasons/now?page=1'` y `curl 'https://api.tenrai.org/v1/anime/52991'`
      y comparar con una respuesta guardada de Jikan.
- [ ] Confirmar valores de `broadcast.day` (la app espera `"Mondays"`, `"Tuesdays"`… en `DayOfWeek.from`)
      y `broadcast.timezone` (`"Asia/Tokyo"`, usado con `ZoneId.of`).
- [ ] Confirmar tamaño de página de `seasons/now` (app asume `PAGE_SIZE = 25`).
- [ ] Confirmar qué campos pueden venir `null` (`episodes`, `score`, `rank`, `broadcast`, `trailer`…).

### Fase 1 — Cambio mínimo (el "drop-in")
- [ ] `ApiModule.kt`: `BASE_URL = "https://api.tenrai.org/v1/"`.
- [ ] Mover la URL base a `buildConfigField` en `app/build.gradle` para poder cambiarla
      sin tocar código (y por si hiciese falta un fallback).
- [ ] Comprobar en dispositivo: lista de temporada, scroll infinito, detalle, favoritos, notificación diaria.

### Fase 2 — Robustez (recomendado, mismo PR o siguiente)
- [ ] **Rate limit**: interceptor de OkHttp que, ante 429, espere `Retry-After` y reintente una vez.
      El `RemoteMediator` hace ráfagas de páginas al hacer scroll rápido.
- [ ] **Nulabilidad**: `Anime` declara no-nulos `episodes: Int`, `score: Double`, `rank: Int`,
      `broadcast`, `trailer`, `aired`, `images`… Gson los rellena con `null` saltándose Kotlin y
      puede provocar NPE (riesgo ya existente con Jikan, pero conviene cerrarlo ahora que
      cambia el proveedor). Hacerlos nullables y ajustar los usos (`Broadcast.isAiringToday`,
      `getNextBroadcastString`, adapters, `DatabaseTypeConverters`).
- [ ] `HttpLoggingInterceptor.Level.BODY` solo en `debug` (hoy se loguea todo también en release).
- [ ] Cabecera `User-Agent` identificando la app (buena práctica con APIs comunitarias).

### Fase 3 — Opcional
- [ ] `X-Server-Key`: **no** embeberla en el APK (es extraíble). Solo tendría sentido con un backend propio.
- [ ] Valorar `GET schedules` como alternativa para "qué se emite hoy" en `AnimeAlertAlarm`.
- [ ] Eliminar `AnimePagingSource` si sigue sin usarse.
- [ ] Bump de versión (`versionCode`/`versionName`) y nota de release.

### Fase 4 — Release
- [ ] Merge a `develop`, prueba interna, merge `develop` → `master` (que lleva 7 commits de retraso).

## 4. Riesgos

| Riesgo | Mitigación |
|---|---|
| Diferencias sutiles de formato (p. ej. `broadcast.day`) | Fase 0 + test unitario de deserialización con un JSON real de Tenrai |
| 429 por ráfagas de paginación | Interceptor con `Retry-After` |
| Usuarios con versiones antiguas seguirán llamando a Jikan | Publicar cuanto antes; la caché local de Room (12 h) da algo de margen pero no evita el fallo |
| Tenrai es otro proyecto comunitario sin SLA | URL base configurable; endpoint de estado `https://tenrai.org/status/api/status` |
