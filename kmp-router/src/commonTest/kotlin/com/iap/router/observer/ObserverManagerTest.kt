package com.iap.router.observer

import com.iap.router.interceptor.RouteInterceptor
import com.iap.router.interceptor.InterceptorChain
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult
import com.iap.router.model.RouteSource
import com.iap.router.model.ParsedRoute
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ObserverManagerTest {

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

    // ==================== 观察者管理测试 ====================

    @Test
    fun `addObserver should add observer to list`() {
        val manager = ObserverManager()
        val observer = SimpleRouteObserver()

        manager.addObserver(observer)

        assertEquals(1, manager.observerCount())
    }

    @Test
    fun `addObserver should not add duplicate observer`() {
        val manager = ObserverManager()
        val observer = SimpleRouteObserver()

        manager.addObserver(observer)
        manager.addObserver(observer)

        assertEquals(1, manager.observerCount())
    }

    @Test
    fun `removeObserver should remove observer from list`() {
        val manager = ObserverManager()
        val observer = SimpleRouteObserver()

        manager.addObserver(observer)
        val removed = manager.removeObserver(observer)

        assertTrue(removed)
        assertEquals(0, manager.observerCount())
    }

    @Test
    fun `removeObserver should return false when observer not found`() {
        val manager = ObserverManager()
        val observer = SimpleRouteObserver()

        val removed = manager.removeObserver(observer)

        assertFalse(removed)
    }

    @Test
    fun `clearObservers should remove all observers`() {
        val manager = ObserverManager()
        manager.addObserver(SimpleRouteObserver())
        manager.addObserver(SimpleRouteObserver())
        manager.addObserver(SimpleRouteObserver())

        manager.clearObservers()

        assertEquals(0, manager.observerCount())
    }

    // ==================== 通知测试 ====================

    @Test
    fun `notifyRouteStart should call onRouteStart on all observers`() {
        val manager = ObserverManager()
        val startedUrls = mutableListOf<String>()

        manager.addObserver(SimpleRouteObserver(onStart = { context ->
            startedUrls.add(context.url)
        }))
        manager.addObserver(SimpleRouteObserver(onStart = { context ->
            startedUrls.add(context.url + "_2")
        }))

        val context = createTestContext("iap://test")
        manager.notifyRouteStart(context)

        assertEquals(2, startedUrls.size)
        assertTrue(startedUrls.contains("iap://test"))
        assertTrue(startedUrls.contains("iap://test_2"))
    }

    @Test
    fun `notifyRouteComplete should call onRouteComplete on all observers`() {
        val manager = ObserverManager()
        val completedResults = mutableListOf<RouteResult>()

        manager.addObserver(SimpleRouteObserver(onComplete = { _, result ->
            completedResults.add(result)
        }))

        val context = createTestContext()
        val result = RouteResult.Success(context)
        manager.notifyRouteComplete(context, result)

        assertEquals(1, completedResults.size)
        assertTrue(completedResults[0] is RouteResult.Success)
    }

    @Test
    fun `notifyInterceptorExecuted should call onInterceptorExecuted on all observers`() {
        val manager = ObserverManager()
        val executedInterceptors = mutableListOf<Pair<String, Long>>()

        manager.addObserver(SimpleRouteObserver(onInterceptor = { interceptor, _, duration ->
            executedInterceptors.add(interceptor::class.simpleName.orEmpty() to duration)
        }))

        val context = createTestContext()
        val interceptor = TestInterceptor("Test")
        manager.notifyInterceptorExecuted(interceptor, context, 100L)

        assertEquals(1, executedInterceptors.size)
        assertEquals("TestInterceptor", executedInterceptors[0].first)
        assertEquals(100L, executedInterceptors[0].second)
    }

    @Test
    fun `observer exception should not affect other observers`() {
        val manager = ObserverManager()
        val successfulCalls = mutableListOf<String>()

        // 第一个观察者会抛出异常
        manager.addObserver(SimpleRouteObserver(onStart = {
            throw RuntimeException("Test exception")
        }))

        // 第二个观察者应该正常执行
        manager.addObserver(SimpleRouteObserver(onStart = { context ->
            successfulCalls.add(context.url)
        }))

        val context = createTestContext("iap://test")
        manager.notifyRouteStart(context)

        // 第二个观察者应该被调用
        assertEquals(1, successfulCalls.size)
        assertEquals("iap://test", successfulCalls[0])
    }

    // ==================== 辅助类 ====================

    private class TestInterceptor(override val name: String) : RouteInterceptor {
        override val priority: Int = 100

        override suspend fun intercept(
            context: RouteContext,
            chain: InterceptorChain
        ): RouteResult {
            return chain.proceed(context)
        }
    }
}
