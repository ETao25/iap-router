package com.iap.router.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.iap.router.RouteRegistryImpl
import com.iap.router.core.ProtocolParser
import com.iap.router.core.RouteTable
import com.iap.router.core.RouteLookupResult
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Android 页面注册 API 测试
 * 验证 Android 特定的注册扩展函数
 */
class AndroidPageRegistrationTest {

    // ==================== KClass 注册测试 ====================

    @Test
    fun `registerPage with KClass should create AndroidPageCreator`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        // 通过 KClass 注册
        registry.registerPage("order/detail/:orderId", TestActivity::class)

        assertTrue(table.contains("order/detail/:orderId"))

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)
        assertIs<AndroidPageCreator>(config.target.creator)
    }

    @Test
    fun `registerPage with KClass should store activityClass`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("account/settings", TestActivity::class)

        val config = table.getPageConfig("account/settings")
        assertNotNull(config)

        val creator = config.getAndroidPageCreator()
        assertNotNull(creator)
        assertEquals(TestActivity::class, creator.activityClass)
    }

    @Test
    fun `registerPage should use pattern as default pageId`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("account/settings", TestActivity::class)

        val config = table.getPageConfig("account/settings")
        assertNotNull(config)
        assertEquals("account/settings", config.pageId)
    }

    // ==================== Intent 工厂函数注册测试 ====================

    @Test
    fun `registerPage with intentFactory should create AndroidPageCreator`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("order/detail/:orderId") { context, params ->
            Intent(context, TestActivity::class.java).apply {
                putExtra("orderId", params["orderId"] as? String)
            }
        }

        assertTrue(table.contains("order/detail/:orderId"))

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)

        val creator = config.getAndroidPageCreator()
        assertNotNull(creator)
        assertNotNull(creator.intentFactory)
    }

    // ==================== 路由查找测试 ====================

    @Test
    fun `lookup should find registered Android page route`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage("order/detail/:orderId", TestActivity::class)

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

        registry.registerPage("user/:userId/order/:orderId", TestActivity::class)

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

        registry.registerPage("payment/*", TestActivity::class)

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

        registry.registerPage("order/list", TestActivity::class)
        registry.registerPage("order/detail/:id", TestActivity::class)
        registry.registerPage("account/settings", TestActivity::class)
        registry.registerPage("account/profile", TestActivity::class)

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

        registry.registerPage("order/detail", TestActivity::class)
        registry.registerPage("order/detail", AnotherTestActivity::class)

        assertEquals(1, table.size())

        val config = table.getPageConfig("order/detail")
        val creator = config?.getAndroidPageCreator()
        assertEquals(AnotherTestActivity::class, creator?.activityClass)
    }

    // ==================== reified 注册测试 ====================

    @Test
    fun `registerPage with reified type should work`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage<TestActivity>("order/detail/:orderId")

        assertTrue(table.contains("order/detail/:orderId"))

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)

        val creator = config.getAndroidPageCreator()
        assertNotNull(creator)
        assertEquals(TestActivity::class, creator.activityClass)
    }
}

// ==================== 测试辅助类 ====================

/**
 * 测试用 Activity
 */
class TestActivity : Activity()

/**
 * 另一个测试用 Activity
 */
class AnotherTestActivity : Activity()

// ==================== PageRouteInfo 测试辅助类 ====================

/**
 * 实现 PageRouteInfo 的测试 Activity
 * 演示声明式路由注册模式
 */
class OrderDetailActivity : Activity() {
    companion object : PageRouteInfo {
        override val pattern = "order/detail/:orderId"

        override fun createIntent(context: Context, params: Map<String, Any?>): Intent {
            return Intent(context, OrderDetailActivity::class.java).apply {
                putExtra("orderId", params["orderId"] as? String)
            }
        }
    }
}

/**
 * 账户设置测试 Activity
 */
class AccountSettingsActivity : Activity() {
    companion object : PageRouteInfo {
        override val pattern = "account/settings"

        override fun createIntent(context: Context, params: Map<String, Any?>): Intent {
            return Intent(context, AccountSettingsActivity::class.java)
        }
    }
}

// ==================== PageRouteInfo 声明式注册测试 ====================

class PageRouteInfoRegistrationTest {

    @Test
    fun `registerPage with PageRouteInfo should work`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        // 通过 companion object 注册（一行）
        registry.registerPage(OrderDetailActivity)

        assertTrue(table.contains("order/detail/:orderId"))

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)
        assertIs<AndroidPageCreator>(config.target.creator)
    }

    @Test
    fun `registerPage with PageRouteInfo should use pattern from interface`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage(OrderDetailActivity)

        val config = table.getPageConfig("order/detail/:orderId")
        assertNotNull(config)
        assertEquals("order/detail/:orderId", config.pageId)
    }

    @Test
    fun `registerPages should batch register multiple routes`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        // 批量注册
        registry.registerPages(
            OrderDetailActivity,
            AccountSettingsActivity
        )

        assertEquals(2, table.size())
        assertTrue(table.contains("order/detail/:orderId"))
        assertTrue(table.contains("account/settings"))
    }

    @Test
    fun `lookup should find route registered via PageRouteInfo`() {
        val table = RouteTable()
        val registry = RouteRegistryImpl(table)

        registry.registerPage(OrderDetailActivity)

        val parsedRoute = ProtocolParser.parseOrNull("iap://order/detail/123")!!
        val result = table.lookup(parsedRoute)

        assertNotNull(result)
        assertIs<RouteLookupResult.PageRoute>(result)
        assertEquals("123", result.matchResult.pathParams["orderId"])
    }
}
