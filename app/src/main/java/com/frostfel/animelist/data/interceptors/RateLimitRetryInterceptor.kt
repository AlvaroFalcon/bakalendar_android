package com.frostfel.animelist.data.interceptors

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Tenrai answers 429 when the public quota (4 req/s, 120 req/min per IP) is exceeded and
 * tells us how long to wait in the Retry-After header. Waits that long and retries once.
 */
class RateLimitRetryInterceptor(
    private val maxWaitMillis: Long = MAX_WAIT_MILLIS,
    private val sleeper: (Long) -> Unit = { Thread.sleep(it) }
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code != HTTP_TOO_MANY_REQUESTS) return response

        val waitMillis = response.header("Retry-After")?.trim()?.toLongOrNull()
            ?.times(1000)
            ?: DEFAULT_WAIT_MILLIS
        if (waitMillis > maxWaitMillis) return response

        response.close()
        sleeper(waitMillis)
        return chain.proceed(request)
    }

    companion object {
        private const val HTTP_TOO_MANY_REQUESTS = 429
        private const val DEFAULT_WAIT_MILLIS = 1_000L
        private const val MAX_WAIT_MILLIS = 10_000L
    }
}
