package com.iap.router.core

import com.iap.router.model.ActionRouteConfig
import com.iap.router.model.PageRouteConfig
import com.iap.router.platform.PageBuilder
import com.iap.router.platform.PageTarget
import com.iap.router.testutil.TestPage
import com.iap.router.testutil.OrderDetailTestPage
import com.iap.router.testutil.FxChartTestPage
import com.iap.router.testutil.PaymentTestPage
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RouteTableTest {

    // ==================== 页面路由注册测试（builder 方式）====================

    @Test
    fun `registerPage with builder should add page route to table`() {
        val table = RouteTable()

        table.registerPage("order/detail/:orderId", PageBuilder { params ->
            OrderDetailTestPage(params["orderId"] as? String)
        })

        assertTrue(table.contains("order/detail/:orderId"))
        assertEquals(1, table.size())
    }

    @Test
    fun `registerPage with builder should use pattern as pageId`() {
        val table = RouteTable()

        table.registerPage("order/detail/:orderId", PageBuilder { TestPage() })

        assertEquals("order/detail/:orderId", table.getPageConfig("order/detail/:orderId")?.pageId)
    }

    // ==================== 页面路由注册测试（class 方式）====================

    @Test
    fun `registerPage with class should add page route to table`() {
        val table = RouteTable()

        table.registerPage("order/detail", TestPage::class)

        assertTrue(table.contains("order/detail"))
        assertEquals(1, table.size())
    }

    @Test
    fun `registerPage with class should use pattern as pageId`() {
        val table = RouteTable()

        table.registerPage("account/settings", TestPage::class)

        assertEquals("account/settings", table.getPageConfig("account/settings")?.pageId)
    }

    // ==================== 页面路由注册测试（config 方式）====================

    @Test
    fun `registerPage with config should store target and pageId`() {
        val table = RouteTable()
        val builder = PageBuilder { TestPage() }
        val config = PageRouteConfig(
            target = PageTarget.Builder(builder),
            pageId = "customPageId"
        )

        table.registerPage("order/detail", config)

        val retrieved = table.getPageConfig("order/detail")
        assertNotNull(retrieved)
        assertEquals("customPageId", retrieved.pageId)
        assertIs<PageTarget.Builder>(retrieved.target)
    }

    @Test
    fun `registerPage with config and null pageId should use pattern as pageId`() {
        val table = RouteTable()
        val config = PageRouteConfig(
            target = PageTarget.ClassRef(TestPage::class),
            pageId = null
        )

        table.registerPage("order/detail", config)

        assertEquals("order/detail", table.getPageConfig("order/detail")?.pageId)
    }

    @Test
    fun `registerPage should normalize pattern by removing leading slash`() {
        val table = RouteTable()

        table.registerPage("/order/detail", PageBuilder { TestPage() })

        assertTrue(table.contains("order/detail"))
    }

    @Test
    fun `registerPage should overwrite existing route`() {
        val table = RouteTable()

        table.registerPage("order/detail", PageBuilder { TestPage("first") })
        table.registerPage("order/detail", PageBuilder { TestPage("second") })

        assertEquals(1, table.size())
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
        table.registerPage("order/detail/:orderId", PageBuilder { params ->
            OrderDetailTestPage(params["orderId"] as? String)
        })

        val parsedRoute = ProtocolParser.parseOrNull("iap://order/detail/123")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("order/detail/:orderId", result.config.pageId)
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
        table.registerPage("order/*", PageBuilder { TestPage("wildcard") })
        table.registerPage("order/detail/:id", PageBuilder { TestPage("param") })
        table.registerPage("order/detail", PageBuilder { TestPage("exact") })

        val parsedRoute = ProtocolParser.parseOrNull("iap://order/detail")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("order/detail", result.config.pageId)
    }

    @Test
    fun `lookup should prefer action route for action prefix`() {
        val table = RouteTable()
        table.registerPage("action/showPopup", PageBuilder { TestPage() })
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
        table.registerPage("order/detail", PageBuilder { TestPage() })

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
        table.registerPage("order/detail", PageBuilder { TestPage() })

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
        table.registerPage("order/detail", PageBuilder { TestPage() })
        table.registerPage("account/settings", PageBuilder { TestPage() })
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
        table.registerPage("order/detail/:orderId", PageBuilder { params ->
            OrderDetailTestPage(params["orderId"] as? String)
        })

        val retrieved = table.getPageConfig("order/detail/:orderId")

        assertNotNull(retrieved)
        assertEquals("order/detail/:orderId", retrieved.pageId)
        assertIs<PageTarget.Builder>(retrieved.target)
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
        table.registerPage("order/detail", PageBuilder { TestPage() })
        table.registerPage("account/settings", PageBuilder { TestPage() })

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
        table.registerPage("fx/:pairId/chart", PageBuilder { params ->
            FxChartTestPage(params["pairId"] as? String)
        })

        val parsedRoute = ProtocolParser.parseOrNull("iap://fx/USDCNY/chart")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("USDCNY", result.matchResult.pathParams["pairId"])
    }

    @Test
    fun `lookup should handle wildcard routes`() {
        val table = RouteTable()
        table.registerPage("payment/*", PageBuilder { PaymentTestPage() })

        val parsedRoute = ProtocolParser.parseOrNull("iap://payment/card/bind/new")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("payment/*", result.config.pageId)
        assertEquals("card/bind/new", result.matchResult.pathParams["*"])
    }

    // ==================== PageTarget 测试 ====================

    @Test
    fun `PageTarget Builder should store builder correctly`() {
        val table = RouteTable()
        val testPage = TestPage("test")

        table.registerPage("test", PageBuilder { testPage })

        val config = table.getPageConfig("test")
        assertNotNull(config)
        val target = config.target
        assertIs<PageTarget.Builder>(target)

        // 验证 builder 能正确创建页面
        val createdPage = target.builder.build(emptyMap())
        assertIs<TestPage>(createdPage)
    }

    @Test
    fun `PageTarget ClassRef should store class correctly`() {
        val table = RouteTable()

        table.registerPage("test", TestPage::class)

        val config = table.getPageConfig("test")
        assertNotNull(config)
        val target = config.target
        assertIs<PageTarget.ClassRef>(target)
        assertEquals(TestPage::class, target.pageClass)
    }

    // ==================== 测试辅助类 ====================

    private class TestActionHandler : ActionHandler {
        override fun execute(params: Map<String, Any?>, callback: ActionCallback?) {
            callback?.onSuccess(null)
        }
    }
}
