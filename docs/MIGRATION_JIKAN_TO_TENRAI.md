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
| Rate limit público | 3 req/s, 60 req/min | 4 req/s, 120 req/min, 40.000 req/día (por IP) |
| Exceso de límite | HTTP 429 | HTTP 429 + cabecera `Retry-After` |
| Clave opcional | — | `X-Server-Key` (300/min, 5/s; **no usable en apps móviles** según la doc) |
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

### Fase 0 — Validación con la API real ✅
Hecha contra `api.tenrai.org` el 06‑10‑2026 (las 5 páginas de `seasons/now`, 120 animes, y `anime/{id}`):
- [x] Mismo formato que Jikan: `{data, pagination}` y `{data}`; 404 devuelve JSON de error.
- [x] `broadcast.day` = `"Fridays"`, `"Sundays"`…; `broadcast.timezone` = `"Asia/Tokyo"` o `null`.
- [x] 25 elementos por página, `has_next_page` correcto, sin duplicados.
- [x] Nulos observados: `episodes`, `score`, `rank` (Gson deja 0 en primitivos), `synopsis`,
      `title_english`, `season`, `year`, todo `broadcast.*` (49/120) y `aired.from` (2/120).
      Las listas y objetos anidados nunca son `null` (la doc garantiza estructuras vacías).
- [x] Conclusión: **no hace falta cambiar el esquema de Room ni migrar la BD**; solo hay que
      cubrir `aired.from` y un `broadcast` con día pero sin zona horaria.

### Fase 1 — Cambio mínimo (el "drop-in") ✅
- [x] URL base en `buildConfigField "API_BASE_URL"` (`https://api.tenrai.org/v1/`).
- [ ] Comprobar en dispositivo: lista de temporada, scroll infinito, detalle, favoritos, notificación diaria.

### Fase 2 — Robustez ✅
- [x] `RateLimitRetryInterceptor`: ante 429 espera `Retry-After` (máx. 10 s, 1 s por defecto) y reintenta una vez.
- [x] `Aired.from` nullable; `Broadcast` usa `Asia/Tokyo` si `timezone` es `null`.
- [x] `HttpLoggingInterceptor.Level.BODY` solo en `debug`.
- [x] Cabecera `User-Agent: Bakalendar-Android/<versión>`.
- [x] Tests unitarios: deserialización de una respuesta real de Tenrai (`tenrai_seasons_now.json`)
      y el interceptor con MockWebServer.

### Fase 3 — Opcional
- [ ] `X-Server-Key`: descartada. La doc dice explícitamente que no sirve para apps cliente (el límite es por clave, no por IP).
- [ ] Valorar `GET schedules` como alternativa para "qué se emite hoy" en `AnimeAlertAlarm`.
- [ ] Eliminar `AnimePagingSource` si sigue sin usarse.
- [ ] Bump de versión (`versionCode`/`versionName`) y nota de release.

### Fase 4 — Release
- [ ] Merge a `develop`, prueba interna, merge `develop` → `master` (que lleva 7 commits de retraso).

## 4. Riesgos

| Riesgo | Mitigación |
|---|---|
| Diferencias sutiles de formato | Verificado en fase 0 + test con JSON real de Tenrai |
| API en beta, con caídas posibles | URL configurable; caché local de Room |
| 429 por ráfagas de paginación | Interceptor con `Retry-After` |
| Usuarios con versiones antiguas seguirán llamando a Jikan | Publicar cuanto antes; la caché local de Room (12 h) da algo de margen pero no evita el fallo |
| Tenrai es otro proyecto comunitario sin SLA | URL base configurable; endpoint de estado `https://tenrai.org/status/api/status` |
