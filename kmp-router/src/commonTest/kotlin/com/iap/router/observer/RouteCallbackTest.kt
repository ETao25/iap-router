package com.iap.router.observer

import com.iap.router.model.RouteContext
import com.iap.router.model.RouteSource
import com.iap.router.model.ParsedRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RouteCallbackTest {

    private fun createTestContext(url: String = "iap://test/page"): RouteContext {
        return RouteContext(
            url = url,
            parsedRoute = ParsedRoute(
                scheme = "iap",
                path = "test/page",
                queryParams = emptyMap(),
                pathParams = mutableMapOf()
            ),
            params = emptyMap(),
            source = RouteSource.INTERNAL,
            timestamp = 1000L
        )
    }

    // ==================== RouteError 测试 ====================

    @Test
    fun `RouteError should contain all error information`() {
        val error = RouteError(
            code = RouteErrorCode.ROUTE_NOT_FOUND,
            message = "Route not found",
            url = "iap://unknown",
            cause = null
        )

        assertEquals(RouteErrorCode.ROUTE_NOT_FOUND, error.code)
        assertEquals("Route not found", error.message)
        assertEquals("iap://unknown", error.url)
        assertNull(error.cause)
    }

    @Test
    fun `RouteError should contain cause when provided`() {
        val exception = RuntimeException("Original error")
        val error = RouteError(
            code = RouteErrorCode.NAVIGATION_FAILED,
            message = "Navigation failed",
            url = "iap://test",
            cause = exception
        )

        assertEquals(exception, error.cause)
    }

    // ==================== SimpleRouteCallback 测试 ====================

    @Test
    fun `SimpleRouteCallback onSuccess should invoke callback`() {
        var successContext: RouteContext? = null

        val callback = SimpleRouteCallback(
            onSuccessCallback = { context ->
                successContext = context
            }
        )

        val context = createTestContext("iap://success")
        callback.onSuccess(context)

        assertEquals("iap://success", successContext?.url)
    }

    @Test
    fun `SimpleRouteCallback onError should invoke callback`() {
        var receivedError: RouteError? = null

        val callback = SimpleRouteCallback(
            onErrorCallback = { error ->
                receivedError = error
            }
        )

        val error = RouteError(
            code = RouteErrorCode.ROUTE_BLOCKED,
            message = "Blocked by interceptor",
            url = "iap://blocked"
        )
        callback.onError(error)

        assertEquals(RouteErrorCode.ROUTE_BLOCKED, receivedError?.code)
        assertEquals("iap://blocked", receivedError?.url)
    }

    @Test
    fun `SimpleRouteCallback should handle null callbacks gracefully`() {
        val callback = SimpleRouteCallback()

        // 不应抛出异常
        callback.onSuccess(createTestContext())
        callback.onError(RouteError(
            code = RouteErrorCode.UNKNOWN,
            message = "Error",
            url = "iap://test"
        ))
    }

    // ==================== RouteErrorCode 测试 ====================

    @Test
    fun `RouteErrorCode should have all expected values`() {
        val codes = RouteErrorCode.entries

        assertEquals(6, codes.size)
        assertTrue(codes.contains(RouteErrorCode.ROUTE_NOT_FOUND))
        assertTrue(codes.contains(RouteErrorCode.ROUTE_BLOCKED))
        assertTrue(codes.contains(RouteErrorCode.PARAM_VALIDATION_FAILED))
        assertTrue(codes.contains(RouteErrorCode.NAVIGATION_FAILED))
        assertTrue(codes.contains(RouteErrorCode.INTERCEPTOR_TIMEOUT))
        assertTrue(codes.contains(RouteErrorCode.UNKNOWN))
    }

    private fun assertTrue(condition: Boolean) {
        kotlin.test.assertTrue(condition)
    }
}
