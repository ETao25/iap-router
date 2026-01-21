package com.iap.router.observer

import com.iap.router.model.RouteSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RouteEventTest {

    // ==================== RouteResultType 测试 ====================

    @Test
    fun `RouteResultType should have all expected values`() {
        val types = RouteResultType.entries

        assertEquals(5, types.size)
        assertTrue(types.contains(RouteResultType.SUCCESS))
        assertTrue(types.contains(RouteResultType.REDIRECT))
        assertTrue(types.contains(RouteResultType.BLOCKED))
        assertTrue(types.contains(RouteResultType.ERROR))
        assertTrue(types.contains(RouteResultType.NOT_FOUND))
    }

    // ==================== RouteEvent 创建测试 ====================

    @Test
    fun `RouteEvent success should create correct event`() {
        val event = RouteEvent.success(
            url = "iap://order/detail/123",
            source = RouteSource.INTERNAL,
            durationMs = 50L,
            interceptorChain = listOf("LoginInterceptor", "LogInterceptor"),
            timestamp = 1000L,
            pageId = "orderDetail"
        )

        assertEquals("iap://order/detail/123", event.url)
        assertEquals(RouteSource.INTERNAL, event.source)
        assertEquals(RouteResultType.SUCCESS, event.resultType)
        assertEquals(50L, event.durationMs)
        assertEquals(listOf("LoginInterceptor", "LogInterceptor"), event.interceptorChain)
        assertEquals(1000L, event.timestamp)
        assertEquals("orderDetail", event.pageId)
        assertNull(event.actionName)
        assertNull(event.errorMessage)
        assertNull(event.redirectUrl)
    }

    @Test
    fun `RouteEvent redirect should create correct event`() {
        val event = RouteEvent.redirect(
            url = "iap://payment/checkout",
            source = RouteSource.DEEPLINK,
            durationMs = 30L,
            interceptorChain = listOf("LoginInterceptor"),
            timestamp = 2000L,
            redirectUrl = "iap://auth/login"
        )

        assertEquals("iap://payment/checkout", event.url)
        assertEquals(RouteSource.DEEPLINK, event.source)
        assertEquals(RouteResultType.REDIRECT, event.resultType)
        assertEquals(30L, event.durationMs)
        assertEquals("iap://auth/login", event.redirectUrl)
        assertEquals(2000L, event.timestamp)
    }

    @Test
    fun `RouteEvent blocked should create correct event`() {
        val event = RouteEvent.blocked(
            url = "iap://admin/settings",
            source = RouteSource.INTERNAL,
            durationMs = 10L,
            interceptorChain = listOf("PermissionInterceptor"),
            timestamp = 3000L,
            reason = "No admin permission"
        )

        assertEquals("iap://admin/settings", event.url)
        assertEquals(RouteResultType.BLOCKED, event.resultType)
        assertEquals("No admin permission", event.errorMessage)
        assertEquals(3000L, event.timestamp)
    }

    @Test
    fun `RouteEvent error should create correct event`() {
        val event = RouteEvent.error(
            url = "iap://test/error",
            source = RouteSource.PUSH,
            durationMs = 100L,
            interceptorChain = emptyList(),
            timestamp = 4000L,
            errorMessage = "Navigation failed"
        )

        assertEquals("iap://test/error", event.url)
        assertEquals(RouteSource.PUSH, event.source)
        assertEquals(RouteResultType.ERROR, event.resultType)
        assertEquals("Navigation failed", event.errorMessage)
        assertEquals(4000L, event.timestamp)
    }

    @Test
    fun `RouteEvent notFound should create correct event`() {
        val event = RouteEvent.notFound(
            url = "iap://unknown/page",
            source = RouteSource.INTERNAL,
            durationMs = 5L,
            timestamp = 5000L
        )

        assertEquals("iap://unknown/page", event.url)
        assertEquals(RouteResultType.NOT_FOUND, event.resultType)
        assertEquals(5L, event.durationMs)
        assertEquals(emptyList<String>(), event.interceptorChain)
        assertEquals(5000L, event.timestamp)
    }

    // ==================== RouteEvent 属性测试 ====================

    @Test
    fun `RouteEvent should support action routes`() {
        val event = RouteEvent.success(
            url = "iap://action/share",
            source = RouteSource.INTERNAL,
            durationMs = 20L,
            interceptorChain = emptyList(),
            timestamp = 6000L,
            actionName = "share"
        )

        assertNull(event.pageId)
        assertEquals("share", event.actionName)
    }

    @Test
    fun `RouteEvent should support extras`() {
        val event = RouteEvent(
            url = "iap://test",
            source = RouteSource.INTERNAL,
            resultType = RouteResultType.SUCCESS,
            durationMs = 10L,
            interceptorChain = emptyList(),
            timestamp = 7000L,
            extras = mapOf("custom_key" to "custom_value", "count" to 42)
        )

        assertEquals("custom_value", event.extras["custom_key"])
        assertEquals(42, event.extras["count"])
    }

    private fun assertTrue(condition: Boolean) {
        kotlin.test.assertTrue(condition)
    }
}
