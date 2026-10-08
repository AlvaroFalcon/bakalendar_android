package com.frostfel.animelist.data.interceptors

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class RateLimitRetryInterceptorTest {

    private val server = MockWebServer()
    private val sleeps = mutableListOf<Long>()
    private lateinit var client: OkHttpClient

    @Before
    fun setUp() {
        server.start()
        client = OkHttpClient.Builder()
            .addInterceptor(RateLimitRetryInterceptor(sleeper = { sleeps.add(it) }))
            .build()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun call() = client.newCall(Request.Builder().url(server.url("/v1/seasons/now")).build()).execute()

    @Test
    fun successIsPassedThrough() {
        server.enqueue(MockResponse().setBody("ok"))

        call().use { assertEquals(200, it.code) }
        assertEquals(1, server.requestCount)
        assertEquals(emptyList<Long>(), sleeps)
    }

    @Test
    fun retriesOnceAfterRetryAfter() {
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "2"))
        server.enqueue(MockResponse().setBody("ok"))

        call().use { assertEquals(200, it.code) }
        assertEquals(2, server.requestCount)
        assertEquals(listOf(2_000L), sleeps)
    }

    @Test
    fun usesDefaultWaitWithoutRetryAfter() {
        server.enqueue(MockResponse().setResponseCode(429))
        server.enqueue(MockResponse().setBody("ok"))

        call().use { assertEquals(200, it.code) }
        assertEquals(listOf(1_000L), sleeps)
    }

    @Test
    fun onlyRetriesOnce() {
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "1"))
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "1"))

        call().use { assertEquals(429, it.code) }
        assertEquals(2, server.requestCount)
    }

    @Test
    fun givesUpWhenWaitIsTooLong() {
        server.enqueue(MockResponse().setResponseCode(429).setHeader("Retry-After", "60"))

        call().use { assertEquals(429, it.code) }
        assertEquals(1, server.requestCount)
        assertEquals(emptyList<Long>(), sleeps)
    }
}
