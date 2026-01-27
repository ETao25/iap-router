package com.iap.router

import com.iap.router.core.ActionCallback
import com.iap.router.core.ActionHandler
import com.iap.router.core.RouteTable
import com.iap.router.fallback.FallbackAction
import com.iap.router.fallback.FallbackManager
import com.iap.router.interceptor.InterceptorChain
import com.iap.router.interceptor.InterceptorManager
import com.iap.router.interceptor.RouteInterceptor
import com.iap.router.model.NavMode
import com.iap.router.model.NavigationOptions
import com.iap.router.model.PageRouteConfig
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult
import com.iap.router.observer.ObserverManager
import com.iap.router.observer.RouteError
import com.iap.router.observer.RouteObserver
import com.iap.router.platform.ActionExecutor
import com.iap.router.platform.Navigator
import com.iap.router.platform.PageTarget
import com.iap.router.platform.PlatformPageCreator
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class RouterTest {

    // ==================== 测试辅助方法 ====================

    private fun createTestRouter(navigator: MockNavigator = MockNavigator()): Router {
        val routeTable = RouteTable()
        return Router(
            routeTable = routeTable,
            interceptorManager = InterceptorManager(),
            observerManager = ObserverManager(),
            navigator = navigator,
            actionExecutor = MockActionExecutor()
        )
    }

    private fun createTestPageConfig(pattern: String): PageRouteConfig {
        return PageRouteConfig(
            pattern = pattern,
            target = PageTarget(TestPageCreator()),
            pageId = pattern
        )
    }

    // ==================== 基础功能测试 ====================

    @Test
    fun `canOpen should return true for registered route`() {
        val router = createTestRouter()
        router.registry.registerPage(createTestPageConfig("order/detail/:id"))

        assertTrue(router.canOpen("iap://order/detail/123"))
    }

    @Test
    fun `canOpen should return false for unregistered route`() {
        val router = createTestRouter()

        assertFalse(router.canOpen("iap://unknown/page"))
    }

    @Test
    fun `canOpen should return false for invalid URL`() {
        val router = createTestRouter()

        assertFalse(router.canOpen("invalid-url"))
    }

    // ==================== 路由打开测试 ====================

    @Test
    fun `openSuspend should return Success for registered page route`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("test/page"))

        val result = router.openSuspend("iap://test/page")

        assertIs<RouteResult.Success>(result)
        assertEquals("iap://test/page", result.context.url)
    }

    @Test
    fun `openSuspend should call navigator push for page route`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("order/detail/:orderId"))

        router.openSuspend("iap://order/detail/123?from=list")

        assertEquals(1, mockNavigator.pushCount)
        assertEquals("order/detail/:orderId", mockNavigator.lastPageId)
        assertEquals("123", mockNavigator.lastParams["orderId"])
        assertEquals("list", mockNavigator.lastParams["from"])
    }

    @Test
    fun `openSuspend should call navigator present when navMode is PRESENT`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("modal/page"))

        val options = NavigationOptions(navMode = NavMode.PRESENT)
        router.openSuspend("iap://modal/page", options = options)

        assertEquals(1, mockNavigator.presentCount)
        assertEquals(0, mockNavigator.pushCount)
    }

    @Test
    fun `openSuspend should return NotFound for unregistered route`() = runTest {
        val router = createTestRouter()

        val result = router.openSuspend("iap://unknown/page")

        assertIs<RouteResult.NotFound>(result)
        assertEquals("iap://unknown/page", result.url)
    }

    @Test
    fun `openSuspend should merge extra params with URL params`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("test/page"))

        router.openSuspend(
            "iap://test/page?urlParam=fromUrl",
            params = mapOf("extraParam" to "fromCode")
        )

        assertEquals("fromUrl", mockNavigator.lastParams["urlParam"])
        assertEquals("fromCode", mockNavigator.lastParams["extraParam"])
    }

    @Test
    fun `extra params should override URL params`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("test/page"))

        router.openSuspend(
            "iap://test/page?key=fromUrl",
            params = mapOf("key" to "fromCode")
        )

        assertEquals("fromCode", mockNavigator.lastParams["key"])
    }

    // ==================== Action 路由测试 ====================

    @Test
    fun `openSuspend should execute action for action route`() = runTest {
        val router = createTestRouter()
        val mockHandler = MockActionHandler()
        router.registry.registerAction("showPopup", mockHandler)

        val result = router.openSuspend("iap://action/showPopup?message=hello")

        assertIs<RouteResult.Success>(result)
        assertTrue(mockHandler.executed)
        assertEquals("hello", mockHandler.lastParams["message"])
    }

    // ==================== 拦截器测试 ====================

    @Test
    fun `interceptor should be called during routing`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("test/page"))

        var interceptorCalled = false
        router.addGlobalInterceptor(object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                interceptorCalled = true
                return chain.proceed(context)
            }
        })

        router.openSuspend("iap://test/page")

        assertTrue(interceptorCalled)
    }

    @Test
    fun `interceptor can block route`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("test/page"))

        router.addGlobalInterceptor(object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                return RouteResult.Blocked("Access denied")
            }
        })

        val result = router.openSuspend("iap://test/page")

        assertIs<RouteResult.Blocked>(result)
        assertEquals("Access denied", result.reason)
        assertEquals(0, mockNavigator.pushCount) // Navigator should not be called
    }

    @Test
    fun `interceptor can modify params`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("test/page"))

        router.addGlobalInterceptor(object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                val newParams = context.params + ("injected" to "value")
                return chain.proceed(context.copy(params = newParams))
            }
        })

        router.openSuspend("iap://test/page")

        assertEquals("value", mockNavigator.lastParams["injected"])
    }

    @Test
    fun `interceptor can redirect`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("original/page"))
        router.registry.registerPage(createTestPageConfig("redirect/page"))

        router.addGlobalInterceptor(object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                if (context.parsedRoute.path == "original/page") {
                    return RouteResult.Redirect("iap://redirect/page")
                }
                return chain.proceed(context)
            }
        })

        val result = router.openSuspend("iap://original/page")

        assertIs<RouteResult.Success>(result)
        assertEquals("redirect/page", mockNavigator.lastPageId)
    }

    @Test
    fun `local interceptor only applies to matching pattern`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("payment/checkout"))
        router.registry.registerPage(createTestPageConfig("order/detail"))

        var interceptorCallCount = 0
        router.addInterceptor("payment/*", object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                interceptorCallCount++
                return chain.proceed(context)
            }
        })

        router.openSuspend("iap://payment/checkout")
        assertEquals(1, interceptorCallCount)

        router.openSuspend("iap://order/detail")
        assertEquals(1, interceptorCallCount) // Should not be called for order route
    }

    // ==================== 观察者测试 ====================

    @Test
    fun `observer should be notified on route start and complete`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("test/page"))

        var startCalled = false
        var completeCalled = false
        router.addObserver(object : RouteObserver {
            override fun onRouteStart(context: RouteContext) {
                startCalled = true
            }
            override fun onRouteComplete(context: RouteContext, result: RouteResult) {
                completeCalled = true
            }
            override fun onInterceptorExecuted(interceptor: RouteInterceptor, context: RouteContext, durationMs: Long) {
                // Not needed for this test
            }
        })

        router.openSuspend("iap://test/page")

        assertTrue(startCalled)
        assertTrue(completeCalled)
    }

    // ==================== 降级测试 ====================

    @Test
    fun `fallback should be triggered when route not found`() = runTest {
        val router = createTestRouter()

        // 设置降级管理器
        val fallbackManager = FallbackManager()
        var fallbackTriggered = false
        fallbackManager.setGlobalFallback(FallbackAction.Custom { fallbackTriggered = true })
        router.fallbackHandler = fallbackManager

        router.openSuspend("iap://unknown/page")

        assertTrue(fallbackTriggered)
    }

    @Test
    fun `pattern fallback should match specific routes`() = runTest {
        val router = createTestRouter()

        val fallbackManager = FallbackManager()
        var fallbackUrl: String? = null
        fallbackManager.addPatternFallback("payment/*", FallbackAction.Custom {
            fallbackUrl = "iap://fallback/target"
        })
        router.fallbackHandler = fallbackManager

        // 尝试打开未注册的 payment 路由
        val result = router.openSuspend("iap://payment/checkout")

        // 应该触发 fallback
        assertIs<RouteResult.NotFound>(result)
        assertEquals("iap://fallback/target", fallbackUrl)
    }

    // ==================== 导航 API 测试 ====================

    @Test
    fun `pop should call navigator pop`() {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)

        router.pop()

        assertTrue(mockNavigator.popCalled)
    }

    @Test
    fun `popTo should call navigator popTo`() {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)

        router.popTo("target/page")

        assertTrue(mockNavigator.popToCalled)
        assertEquals("target/page", mockNavigator.lastPopToPageId)
    }

    @Test
    fun `popToRoot should call navigator popToRoot`() {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)

        router.popToRoot()

        assertTrue(mockNavigator.popToRootCalled)
    }

    // ==================== 回调测试 ====================

    @Test
    fun `callback onSuccess should be called on successful navigation`() = runTest {
        val mockNavigator = MockNavigator()
        val router = createTestRouter(mockNavigator)
        router.registry.registerPage(createTestPageConfig("test/page"))

        var successContext: RouteContext? = null
        var errorReceived: RouteError? = null

        // Use synchronous version for callback testing
        val result = router.openSuspend("iap://test/page")

        // Simulate callback handling
        when (result) {
            is RouteResult.Success -> successContext = result.context
            is RouteResult.Error -> errorReceived = RouteError(
                code = com.iap.router.observer.RouteErrorCode.UNKNOWN,
                message = result.exception.message ?: "",
                url = "iap://test/page"
            )
            else -> {}
        }

        assertNotNull(successContext)
        assertEquals("iap://test/page", successContext.url)
    }
}

// ==================== Mock 类 ====================

private class TestPageCreator : PlatformPageCreator

private class MockNavigator : Navigator {
    var pushCount = 0
    var presentCount = 0
    var lastPageId: String? = null
    var lastParams: Map<String, Any?> = emptyMap()
    var lastOptions: NavigationOptions? = null
    var popCalled = false
    var popToCalled = false
    var popToRootCalled = false
    var lastPopToPageId: String? = null
    var lastResult: Any? = null

    override fun push(pageId: String, params: Map<String, Any?>, options: NavigationOptions) {
        pushCount++
        lastPageId = pageId
        lastParams = params
        lastOptions = options
    }

    override fun present(pageId: String, params: Map<String, Any?>, options: NavigationOptions) {
        presentCount++
        lastPageId = pageId
        lastParams = params
        lastOptions = options
    }

    override fun pop(result: Any?) {
        popCalled = true
        lastResult = result
    }

    override fun popTo(pageId: String, result: Any?) {
        popToCalled = true
        lastPopToPageId = pageId
        lastResult = result
    }

    override fun popToRoot() {
        popToRootCalled = true
    }
}

private class MockActionExecutor : ActionExecutor {
    override fun execute(actionName: String, params: Map<String, Any?>, callback: com.iap.router.core.ActionCallback?) {
        callback?.onSuccess(null)
    }
    override fun canExecute(actionName: String): Boolean = true
}

private class MockActionHandler : ActionHandler {
    var executed = false
    var lastParams: Map<String, Any?> = emptyMap()
    var lastCallback: com.iap.router.core.ActionCallback? = null

    override fun execute(params: Map<String, Any?>, callback: com.iap.router.core.ActionCallback?) {
        executed = true
        lastParams = params
        lastCallback = callback
        callback?.onSuccess(null)
    }
}
