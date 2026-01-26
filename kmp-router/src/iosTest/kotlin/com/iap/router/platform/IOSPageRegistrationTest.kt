package com.iap.router.platform

import com.iap.router.RouteRegistryImpl
import com.iap.router.core.ProtocolParser
import com.iap.router.core.RouteTable
import com.iap.router.core.RouteLookupResult
import platform.UIKit.UIViewController
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * iOS 页面注册 API 测试
 * 验证 iOS 特定的注册扩展函数
 */
class IOSPageRegistrationTest {

    // ==================== 基础注册测试 ====================

    @Test
    fun `registerPage with factory should create IOSPageCreator`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        // 通过工厂函数注册
        registry.registerPage("order/detail/:orderId") { params ->
            TestViewController(params)
        }

        assertTrue(table.contains("order/detail/:orderId"))

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)
        assertIs<IOSPageCreator>(config.target.creator)
    }

    @Test
    fun `registerPage should use pattern as default pageId`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("account/settings") { TestViewController() }

        val config = table.getPageConfig("account/settings")
        assertNotNull(config)
        assertEquals("account/settings", config.pageId)
    }

    @Test
    fun `registerPage with custom pageId should store pageId`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage(
            pattern = "order/detail/:orderId",
            pageId = "orderDetail"
        ) { TestViewController() }

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)
        assertEquals("orderDetail", config.pageId)
    }

    // ==================== 工厂函数调用测试 ====================

    @Test
    fun `IOSPageCreator should create ViewController with params`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("fx/:pairId/chart") { params ->
            TestViewController(params)
        }

        val config = table.getPageConfig("fx/:pairId/chart")
        assertNotNull(config)

        val creator = config.getIOSPageCreator()
        assertNotNull(creator)

        val vc = creator.createViewController(mapOf("pairId" to "USDCNY"))
        assertIs<TestViewController>(vc)
        assertEquals("USDCNY", (vc as TestViewController).params["pairId"])
    }

    @Test
    fun `PageRouteConfig createViewController extension should work`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("payment/card") { params ->
            TestViewController(params)
        }

        val config = table.getPageConfig("payment/card")
        assertNotNull(config)

        val vc = config.createViewController(mapOf("source" to "checkout"))
        assertNotNull(vc)
        assertIs<TestViewController>(vc)
    }

    // ==================== 路由查找测试 ====================

    @Test
    fun `lookup should find registered iOS page route`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("order/detail/:orderId") { params ->
            TestViewController(params)
        }

        val parsedRoute = ProtocolParser.parseOrNull("iap://order/detail/123")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("order/detail/:orderId", result.config.pageId)
        assertEquals("123", result.matchResult.pathParams["orderId"])
    }

    @Test
    fun `lookup should extract multiple path params`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("user/:userId/order/:orderId") { params ->
            TestViewController(params)
        }

        val parsedRoute = ProtocolParser.parseOrNull("iap://user/u001/order/o123")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("u001", result.matchResult.pathParams["userId"])
        assertEquals("o123", result.matchResult.pathParams["orderId"])
    }

    @Test
    fun `lookup should handle wildcard routes`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("payment/*") { params ->
            TestViewController(params)
        }

        val parsedRoute = ProtocolParser.parseOrNull("iap://payment/card/bind/new")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("card/bind/new", result.matchResult.pathParams["*"])
    }

    // ==================== 多路由注册测试 ====================

    @Test
    fun `multiple page registrations should work`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("order/list") { TestViewController() }
        registry.registerPage("order/detail/:id") { TestViewController(it) }
        registry.registerPage("account/settings") { TestViewController() }
        registry.registerPage("account/profile") { TestViewController() }

        assertEquals(4, table.size())
        assertTrue(table.contains("order/list"))
        assertTrue(table.contains("order/detail/:id"))
        assertTrue(table.contains("account/settings"))
        assertTrue(table.contains("account/profile"))
    }

    @Test
    fun `overwriting route should replace existing`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("order/detail") { TestViewController(mapOf("version" to "v1")) }
        registry.registerPage("order/detail") { TestViewController(mapOf("version" to "v2")) }

        assertEquals(1, table.size())

        val config = table.getPageConfig("order/detail")
        val vc = config?.createViewController(emptyMap()) as? TestViewController
        assertEquals("v2", vc?.params?.get("version"))
    }
}

// ==================== 测试辅助类 ====================

/**
 * 测试用 UIViewController
 */
class TestViewController(
    val params: Map<String, Any?> = emptyMap()
) : UIViewController(nibName = null, bundle = null)

// ==================== PageRoutable 测试 ====================

/**
 * PageRoutable 声明式注册测试
 * 演示 iOS 上使用 PageRoutable 和 pageBuilder 进行路由注册
 */
class PageRoutableTest {

    // 模拟 ViewController 实现 PageRoutable
    private object OrderDetailRoutable : PageRoutable {
        override val pattern = "order/detail/:orderId"
        override fun createPage(params: Map<String, Any?>): UIViewController {
            return TestViewController(params)
        }
    }

    private object PaymentRoutable : PageRoutable {
        override val pattern = "payment/checkout"
        override fun createPage(params: Map<String, Any?>): UIViewController {
            return TestViewController(params)
        }
    }

    private object AccountRoutable : PageRoutable {
        override val pattern = "account/settings"
        override fun createPage(params: Map<String, Any?>): UIViewController {
            return TestViewController()
        }
    }

    @Test
    fun `registerPage with PageRoutable should work`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        // 通过 PageRoutable 注册
        registry.registerPage(OrderDetailRoutable)

        assertTrue(table.contains("order/detail/:orderId"))

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)
        assertIs<IOSPageCreator>(config.target.creator)
    }

    @Test
    fun `registerPage with pageBuilder should work`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        // 通过 pageBuilder 注册
        registry.registerPage(OrderDetailRoutable.pageBuilder)

        assertTrue(table.contains("order/detail/:orderId"))

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)
        assertIs<IOSPageCreator>(config.target.creator)
    }

    @Test
    fun `registerPage with PageRoutable should use pattern`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage(OrderDetailRoutable)

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)
        assertEquals("order/detail/:orderId", config.pageId)
    }

    @Test
    fun `registerPages should batch register multiple routables`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        // 批量注册
        registry.registerPages(listOf(
            OrderDetailRoutable,
            PaymentRoutable,
            AccountRoutable
        ))

        assertEquals(3, table.size())
        assertTrue(table.contains("order/detail/:orderId"))
        assertTrue(table.contains("payment/checkout"))
        assertTrue(table.contains("account/settings"))
    }

    @Test
    fun `lookup should find route registered via PageRoutable`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage(OrderDetailRoutable)

        val parsedRoute = ProtocolParser.parseOrNull("iap://order/detail/123")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("123", result.matchResult.pathParams["orderId"])
    }

    @Test
    fun `createPage in PageRoutable should receive params`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage(OrderDetailRoutable)

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)

        val vc = config.createViewController(mapOf("orderId" to "test123"))
        assertIs<TestViewController>(vc)
        assertEquals("test123", (vc as TestViewController).params["orderId"])
    }

    @Test
    fun `PageRoutable companion createConfig should work`() {
        val config = PageRoutable.createConfig(
            pattern = "test/pattern",
            factory = { TestViewController(it) }
        )

        assertEquals("test/pattern", config.pattern)
        assertIs<IOSPageCreator>(config.target.creator)
    }
}
