package com.iap.router.core

import com.iap.router.model.ActionRouteConfig
import com.iap.router.model.PageRouteConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RouteTableTest {

    // ==================== 页面路由注册测试 ====================

    @Test
    fun `registerPage should add page route to table`() {
        val table = RouteTable()
        val config = PageRouteConfig(pageId = "orderDetail")

        table.registerPage("order/detail/:orderId", config)

        assertTrue(table.contains("order/detail/:orderId"))
        assertEquals(1, table.size())
    }

    @Test
    fun `registerPage should normalize pattern by removing leading slash`() {
        val table = RouteTable()
        val config = PageRouteConfig(pageId = "orderDetail")

        table.registerPage("/order/detail", config)

        assertTrue(table.contains("order/detail"))
    }

    @Test
    fun `registerPage should overwrite existing route`() {
        val table = RouteTable()
        val config1 = PageRouteConfig(pageId = "orderDetail1")
        val config2 = PageRouteConfig(pageId = "orderDetail2")

        table.registerPage("order/detail", config1)
        table.registerPage("order/detail", config2)

        assertEquals(1, table.size())
        assertEquals("orderDetail2", table.getPageConfig("order/detail")?.pageId)
    }

    // ==================== Action 路由注册测试 ====================

    @Test
    fun `registerAction should add action route to table`() {
        val table = RouteTable()
        val config = ActionRouteConfig(actionName = "showPopup")
        val handler = TestActionHandler()

        table.registerAction("showPopup", config, handler)

        assertTrue(table.contains("action/showPopup"))
    }

    @Test
    fun `registerAction should prefix action name with action slash`() {
        val table = RouteTable()
        val handler = TestActionHandler()

        table.registerAction("copyText", ActionRouteConfig("copyText"), handler)

        assertTrue(table.getActionPatterns().contains("action/copyText"))
    }

    // ==================== 查找测试 ====================

    @Test
    fun `lookup should find registered page route`() {
        val table = RouteTable()
        val config = PageRouteConfig(pageId = "orderDetail")
        table.registerPage("order/detail/:orderId", config)

        val parsedRoute = ProtocolParser.parseOrNull("iap://order/detail/123")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("orderDetail", result.config.pageId)
        assertEquals("123", result.matchResult.pathParams["orderId"])
    }

    @Test
    fun `lookup should find registered action route`() {
        val table = RouteTable()
        val handler = TestActionHandler()
        table.registerAction("showPopup", ActionRouteConfig("showPopup"), handler)

        val parsedRoute = ProtocolParser.parseOrNull("iap://action/showPopup")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.ActionRoute>(result)
        assertEquals("showPopup", result.config.actionName)
    }

    @Test
    fun `lookup should return null for unregistered route`() {
        val table = RouteTable()

        val parsedRoute = ProtocolParser.parseOrNull("iap://unknown/page")!!
        val result = table.lookup(parsedRoute)

        assertNull(result)
    }

    @Test
    fun `lookup should match best pattern when multiple patterns match`() {
        val table = RouteTable()
        table.registerPage("order/*", PageRouteConfig(pageId = "orderWildcard"))
        table.registerPage("order/detail/:id", PageRouteConfig(pageId = "orderDetailParam"))
        table.registerPage("order/detail", PageRouteConfig(pageId = "orderDetailExact"))

        val parsedRoute = ProtocolParser.parseOrNull("iap://order/detail")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("orderDetailExact", result.config.pageId)
    }

    @Test
    fun `lookup should prefer action route for action prefix`() {
        val table = RouteTable()
        table.registerPage("action/showPopup", PageRouteConfig(pageId = "actionPage"))
        table.registerAction("showPopup", ActionRouteConfig("showPopup"), TestActionHandler())

        val parsedRoute = ProtocolParser.parseOrNull("iap://action/showPopup")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.ActionRoute>(result)
    }

    // ==================== canOpen 测试 ====================

    @Test
    fun `canOpen should return true for registered route`() {
        val table = RouteTable()
        table.registerPage("order/detail", PageRouteConfig(pageId = "orderDetail"))

        val parsedRoute = ProtocolParser.parseOrNull("iap://order/detail")!!

        assertTrue(table.canOpen(parsedRoute))
    }

    @Test
    fun `canOpen should return false for unregistered route`() {
        val table = RouteTable()

        val parsedRoute = ProtocolParser.parseOrNull("iap://unknown/page")!!

        assertFalse(table.canOpen(parsedRoute))
    }

    // ==================== 删除测试 ====================

    @Test
    fun `removePage should remove registered page route`() {
        val table = RouteTable()
        table.registerPage("order/detail", PageRouteConfig(pageId = "orderDetail"))

        val removed = table.removePage("order/detail")

        assertTrue(removed)
        assertFalse(table.contains("order/detail"))
    }

    @Test
    fun `removePage should return false for unregistered route`() {
        val table = RouteTable()

        val removed = table.removePage("unknown/page")

        assertFalse(removed)
    }

    @Test
    fun `removeAction should remove registered action route`() {
        val table = RouteTable()
        table.registerAction("showPopup", ActionRouteConfig("showPopup"), TestActionHandler())

        val removed = table.removeAction("showPopup")

        assertTrue(removed)
        assertFalse(table.contains("action/showPopup"))
    }

    // ==================== 清空测试 ====================

    @Test
    fun `clear should remove all routes`() {
        val table = RouteTable()
        table.registerPage("order/detail", PageRouteConfig(pageId = "orderDetail"))
        table.registerPage("account/settings", PageRouteConfig(pageId = "accountSettings"))
        table.registerAction("showPopup", ActionRouteConfig("showPopup"), TestActionHandler())

        table.clear()

        assertEquals(0, table.size())
        assertTrue(table.getPagePatterns().isEmpty())
        assertTrue(table.getActionPatterns().isEmpty())
    }

    // ==================== 获取配置测试 ====================

    @Test
    fun `getPageConfig should return config for registered route`() {
        val table = RouteTable()
        val config = PageRouteConfig(
            pageId = "orderDetail",
            requiredParams = listOf("orderId"),
            metadata = mapOf("requireLogin" to true)
        )
        table.registerPage("order/detail/:orderId", config)

        val retrieved = table.getPageConfig("order/detail/:orderId")

        assertNotNull(retrieved)
        assertEquals("orderDetail", retrieved.pageId)
        assertEquals(listOf("orderId"), retrieved.requiredParams)
        assertEquals(true, retrieved.metadata["requireLogin"])
    }

    @Test
    fun `getPageConfig should return null for unregistered route`() {
        val table = RouteTable()

        val retrieved = table.getPageConfig("unknown/page")

        assertNull(retrieved)
    }

    // ==================== 模式列表测试 ====================

    @Test
    fun `getPagePatterns should return all registered page patterns`() {
        val table = RouteTable()
        table.registerPage("order/detail", PageRouteConfig(pageId = "orderDetail"))
        table.registerPage("account/settings", PageRouteConfig(pageId = "accountSettings"))

        val patterns = table.getPagePatterns()

        assertEquals(2, patterns.size)
        assertTrue(patterns.contains("order/detail"))
        assertTrue(patterns.contains("account/settings"))
    }

    @Test
    fun `getActionPatterns should return all registered action patterns`() {
        val table = RouteTable()
        table.registerAction("showPopup", ActionRouteConfig("showPopup"), TestActionHandler())
        table.registerAction("copyText", ActionRouteConfig("copyText"), TestActionHandler())

        val patterns = table.getActionPatterns()

        assertEquals(2, patterns.size)
        assertTrue(patterns.contains("action/showPopup"))
        assertTrue(patterns.contains("action/copyText"))
    }

    // ==================== 复杂场景测试 ====================

    @Test
    fun `lookup should extract path params for matched route`() {
        val table = RouteTable()
        table.registerPage("fx/:pairId/chart", PageRouteConfig(pageId = "fxChart"))

        val parsedRoute = ProtocolParser.parseOrNull("iap://fx/USDCNY/chart")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("USDCNY", result.matchResult.pathParams["pairId"])
    }

    @Test
    fun `lookup should handle wildcard routes`() {
        val table = RouteTable()
        table.registerPage("payment/*", PageRouteConfig(pageId = "paymentWildcard"))

        val parsedRoute = ProtocolParser.parseOrNull("iap://payment/card/bind/new")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("paymentWildcard", result.config.pageId)
        assertEquals("card/bind/new", result.matchResult.pathParams["*"])
    }

    // ==================== 测试辅助类 ====================

    private class TestActionHandler : ActionHandler {
        override fun execute(params: Map<String, Any?>, callback: ActionCallback?) {
            callback?.onSuccess(null)
        }
    }
}
