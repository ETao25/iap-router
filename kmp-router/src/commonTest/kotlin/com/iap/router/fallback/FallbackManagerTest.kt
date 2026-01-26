package com.iap.router.fallback

import com.iap.router.core.ProtocolParser
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * FallbackManager 测试
 * 验证全局降级和 pattern 降级规则
 */
class FallbackManagerTest {

    private fun createRouteContext(path: String): RouteContext {
        val parsedRoute = ProtocolParser.parseOrNull("iap://$path")!!
        return RouteContext(
            url = "iap://$path",
            parsedRoute = parsedRoute,
            params = emptyMap(),
            source = RouteSource.INTERNAL,
            timestamp = 0L // 测试用常量
        )
    }

    // ==================== 全局降级测试 ====================

    @Test
    fun `default fallback should return Ignore`() {
        val manager = FallbackManager()
        val context = createRouteContext("some/unknown/path")

        val action = manager.onRouteNotFound(context)

        assertIs<FallbackAction.Ignore>(action)
    }

    @Test
    fun `setGlobalFallback should change default action`() {
        val manager = FallbackManager()
        manager.setGlobalFallback(FallbackAction.NavigateTo("iap://error/404"))

        val context = createRouteContext("some/unknown/path")
        val action = manager.onRouteNotFound(context)

        assertIs<FallbackAction.NavigateTo>(action)
        assertEquals("iap://error/404", action.url)
    }

    @Test
    fun `setGlobalErrorFallback should change error action`() {
        val manager = FallbackManager()
        manager.setGlobalErrorFallback(FallbackAction.ShowError("Something went wrong"))

        val context = createRouteContext("some/path")
        val action = manager.onRouteError(context, RuntimeException("test error"))

        assertIs<FallbackAction.ShowError>(action)
        assertEquals("Something went wrong", action.message)
    }

    // ==================== Pattern 降级测试 ====================

    @Test
    fun `addPatternFallback should match exact pattern`() {
        val manager = FallbackManager()
        manager.addPatternFallback("user/profile", FallbackAction.NavigateTo("iap://login"))

        val context = createRouteContext("user/profile")
        val action = manager.onRouteNotFound(context)

        assertIs<FallbackAction.NavigateTo>(action)
        assertEquals("iap://login", action.url)
    }

    @Test
    fun `addPatternFallback should match wildcard pattern`() {
        val manager = FallbackManager()
        manager.addPatternFallback("user/*", FallbackAction.NavigateTo("iap://login"))

        val context = createRouteContext("user/settings/security")
        val action = manager.onRouteNotFound(context)

        assertIs<FallbackAction.NavigateTo>(action)
        assertEquals("iap://login", action.url)
    }

    @Test
    fun `addPatternFallback should match path param pattern`() {
        val manager = FallbackManager()
        manager.addPatternFallback("order/:orderId", FallbackAction.NavigateTo("iap://order/list"))

        val context = createRouteContext("order/123")
        val action = manager.onRouteNotFound(context)

        assertIs<FallbackAction.NavigateTo>(action)
        assertEquals("iap://order/list", action.url)
    }

    @Test
    fun `pattern fallback should take priority over global`() {
        val manager = FallbackManager()
        manager.setGlobalFallback(FallbackAction.NavigateTo("iap://error/404"))
        manager.addPatternFallback("payment/*", FallbackAction.NavigateTo("iap://h5/payment"))

        // 匹配 pattern 规则
        val paymentContext = createRouteContext("payment/checkout")
        val paymentAction = manager.onRouteNotFound(paymentContext)
        assertIs<FallbackAction.NavigateTo>(paymentAction)
        assertEquals("iap://h5/payment", paymentAction.url)

        // 不匹配 pattern，使用全局
        val otherContext = createRouteContext("other/path")
        val otherAction = manager.onRouteNotFound(otherContext)
        assertIs<FallbackAction.NavigateTo>(otherAction)
        assertEquals("iap://error/404", otherAction.url)
    }

    @Test
    fun `first matching pattern should win`() {
        val manager = FallbackManager()
        manager.addPatternFallback("user/*", FallbackAction.NavigateTo("iap://login"))
        manager.addPatternFallback("user/vip/*", FallbackAction.NavigateTo("iap://vip/upgrade"))

        // user/vip/xxx 会先匹配 user/*
        val context = createRouteContext("user/vip/settings")
        val action = manager.onRouteNotFound(context)

        assertIs<FallbackAction.NavigateTo>(action)
        assertEquals("iap://login", action.url)
    }

    // ==================== 条件降级测试 ====================

    @Test
    fun `conditional pattern fallback should check condition`() {
        val manager = FallbackManager()
        var isLoggedIn = false

        manager.addPatternFallback(
            pattern = "user/*",
            condition = { !isLoggedIn },
            action = FallbackAction.NavigateTo("iap://login")
        )

        val context = createRouteContext("user/profile")

        // 未登录，触发降级
        isLoggedIn = false
        val actionNotLoggedIn = manager.onRouteNotFound(context)
        assertIs<FallbackAction.NavigateTo>(actionNotLoggedIn)
        assertEquals("iap://login", actionNotLoggedIn.url)

        // 已登录，不触发降级（返回全局默认）
        isLoggedIn = true
        val actionLoggedIn = manager.onRouteNotFound(context)
        assertIs<FallbackAction.Ignore>(actionLoggedIn)
    }

    @Test
    fun `multiple conditional patterns should evaluate in order`() {
        val manager = FallbackManager()
        var isVip = false
        var isLoggedIn = true

        manager.addPatternFallback(
            pattern = "vip/*",
            condition = { !isVip },
            action = FallbackAction.NavigateTo("iap://vip/upgrade")
        )
        manager.addPatternFallback(
            pattern = "vip/*",
            condition = { !isLoggedIn },
            action = FallbackAction.NavigateTo("iap://login")
        )

        val context = createRouteContext("vip/exclusive")

        // 非 VIP，第一个条件满足
        isVip = false
        isLoggedIn = true
        val action1 = manager.onRouteNotFound(context)
        assertIs<FallbackAction.NavigateTo>(action1)
        assertEquals("iap://vip/upgrade", action1.url)

        // VIP 但未登录，第一个条件不满足，第二个满足
        isVip = true
        isLoggedIn = false
        val action2 = manager.onRouteNotFound(context)
        assertIs<FallbackAction.NavigateTo>(action2)
        assertEquals("iap://login", action2.url)

        // VIP 且已登录，都不满足
        isVip = true
        isLoggedIn = true
        val action3 = manager.onRouteNotFound(context)
        assertIs<FallbackAction.Ignore>(action3)
    }

    // ==================== 规则管理测试 ====================

    @Test
    fun `removePatternFallback should remove specific pattern`() {
        val manager = FallbackManager()
        manager.addPatternFallback("user/*", FallbackAction.NavigateTo("iap://login"))
        manager.addPatternFallback("payment/*", FallbackAction.NavigateTo("iap://h5/payment"))

        manager.removePatternFallback("user/*")

        // user/* 规则已移除
        val userContext = createRouteContext("user/profile")
        val userAction = manager.onRouteNotFound(userContext)
        assertIs<FallbackAction.Ignore>(userAction)

        // payment/* 规则仍存在
        val paymentContext = createRouteContext("payment/checkout")
        val paymentAction = manager.onRouteNotFound(paymentContext)
        assertIs<FallbackAction.NavigateTo>(paymentAction)
    }

    @Test
    fun `clearPatternFallbacks should remove all patterns`() {
        val manager = FallbackManager()
        manager.addPatternFallback("user/*", FallbackAction.NavigateTo("iap://login"))
        manager.addPatternFallback("payment/*", FallbackAction.NavigateTo("iap://h5/payment"))
        manager.setGlobalFallback(FallbackAction.NavigateTo("iap://error/404"))

        manager.clearPatternFallbacks()

        // 所有 pattern 规则已清除，使用全局
        val userContext = createRouteContext("user/profile")
        val userAction = manager.onRouteNotFound(userContext)
        assertIs<FallbackAction.NavigateTo>(userAction)
        assertEquals("iap://error/404", userAction.url)
    }

    // ==================== 错误处理测试 ====================

    @Test
    fun `onRouteError should also check pattern rules`() {
        val manager = FallbackManager()
        manager.addPatternFallback("payment/*", FallbackAction.NavigateTo("iap://h5/payment"))

        val context = createRouteContext("payment/checkout")
        val action = manager.onRouteError(context, RuntimeException("payment failed"))

        assertIs<FallbackAction.NavigateTo>(action)
        assertEquals("iap://h5/payment", action.url)
    }
}
