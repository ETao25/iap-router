package com.iap.router.interceptor

import com.iap.router.core.ProtocolParser
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult
import com.iap.router.model.RouteSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InterceptorManagerTest {

    // ==================== 全局拦截器测试 ====================

    @Test
    fun `addGlobalInterceptor should add interceptor`() {
        val manager = InterceptorManager()
        val interceptor = TestInterceptor("global")

        manager.addGlobalInterceptor(interceptor)

        assertEquals(1, manager.globalInterceptorCount())
    }

    @Test
    fun `removeGlobalInterceptor should remove interceptor`() {
        val manager = InterceptorManager()
        val interceptor = TestInterceptor("global")
        manager.addGlobalInterceptor(interceptor)

        val removed = manager.removeGlobalInterceptor(interceptor)

        assertTrue(removed)
        assertEquals(0, manager.globalInterceptorCount())
    }

    @Test
    fun `removeGlobalInterceptor should return false for non-existent interceptor`() {
        val manager = InterceptorManager()
        val interceptor = TestInterceptor("global")

        val removed = manager.removeGlobalInterceptor(interceptor)

        assertFalse(removed)
    }

    // ==================== 局部拦截器测试 ====================

    @Test
    fun `addInterceptor should add local interceptor`() {
        val manager = InterceptorManager()
        val interceptor = TestInterceptor("local")

        manager.addInterceptor("payment/*", interceptor)

        assertEquals(1, manager.localInterceptorCount())
        assertTrue(manager.getLocalPatterns().contains("payment/*"))
    }

    @Test
    fun `addInterceptor should normalize pattern by removing leading slash`() {
        val manager = InterceptorManager()
        val interceptor = TestInterceptor("local")

        manager.addInterceptor("/payment/*", interceptor)

        assertTrue(manager.getLocalPatterns().contains("payment/*"))
    }

    @Test
    fun `removeInterceptor should remove specific interceptor from pattern`() {
        val manager = InterceptorManager()
        val interceptor = TestInterceptor("local")
        manager.addInterceptor("payment/*", interceptor)

        val removed = manager.removeInterceptor("payment/*", interceptor)

        assertTrue(removed)
        assertEquals(0, manager.localInterceptorCount())
    }

    @Test
    fun `removeAllInterceptors should remove all interceptors for pattern`() {
        val manager = InterceptorManager()
        manager.addInterceptor("payment/*", TestInterceptor("a"))
        manager.addInterceptor("payment/*", TestInterceptor("b"))
        manager.addInterceptor("order/*", TestInterceptor("c"))

        val removedCount = manager.removeAllInterceptors("payment/*")

        assertEquals(2, removedCount)
        assertEquals(1, manager.localInterceptorCount())
    }

    // ==================== 拦截器匹配测试 ====================

    @Test
    fun `getApplicableInterceptors should return global interceptors for any path`() {
        val manager = InterceptorManager()
        val globalInterceptor = TestInterceptor("global")
        manager.addGlobalInterceptor(globalInterceptor)

        val interceptors = manager.getApplicableInterceptors("any/path")

        assertEquals(1, interceptors.size)
        assertEquals(globalInterceptor, interceptors[0])
    }

    @Test
    fun `getApplicableInterceptors should return matching local interceptors`() {
        val manager = InterceptorManager()
        val paymentInterceptor = TestInterceptor("payment")
        val orderInterceptor = TestInterceptor("order")

        manager.addInterceptor("payment/*", paymentInterceptor)
        manager.addInterceptor("order/*", orderInterceptor)

        val interceptors = manager.getApplicableInterceptors("payment/checkout")

        assertEquals(1, interceptors.size)
        assertEquals(paymentInterceptor, interceptors[0])
    }

    @Test
    fun `getApplicableInterceptors should combine global and local interceptors`() {
        val manager = InterceptorManager()
        val globalInterceptor = TestInterceptor("global", priority = 50)
        val localInterceptor = TestInterceptor("local", priority = 10)

        manager.addGlobalInterceptor(globalInterceptor)
        manager.addInterceptor("payment/*", localInterceptor)

        val interceptors = manager.getApplicableInterceptors("payment/checkout")

        assertEquals(2, interceptors.size)
        // Should be sorted by priority
        assertEquals(localInterceptor, interceptors[0]) // priority 10
        assertEquals(globalInterceptor, interceptors[1]) // priority 50
    }

    @Test
    fun `getApplicableInterceptors should match path param patterns`() {
        val manager = InterceptorManager()
        val interceptor = TestInterceptor("order-detail")

        manager.addInterceptor("order/detail/:id", interceptor)

        val interceptors = manager.getApplicableInterceptors("order/detail/123")

        assertEquals(1, interceptors.size)
    }

    @Test
    fun `getApplicableInterceptors should match wildcard patterns`() {
        val manager = InterceptorManager()
        val interceptor = TestInterceptor("payment-all")

        manager.addInterceptor("payment/*", interceptor)

        val interceptors = manager.getApplicableInterceptors("payment/card/bind/new")

        assertEquals(1, interceptors.size)
    }

    @Test
    fun `getApplicableInterceptors should not match non-matching patterns`() {
        val manager = InterceptorManager()
        val interceptor = TestInterceptor("payment")

        manager.addInterceptor("payment/*", interceptor)

        val interceptors = manager.getApplicableInterceptors("order/detail")

        assertEquals(0, interceptors.size)
    }

    // ==================== executeChain 测试 ====================

    @Test
    fun `executeChain should execute applicable interceptors`() = runTest {
        val manager = InterceptorManager()
        val executionLog = mutableListOf<String>()

        val globalInterceptor = object : RouteInterceptor {
            override val priority = 10
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionLog.add("global")
                return chain.proceed(context)
            }
        }

        val localInterceptor = object : RouteInterceptor {
            override val priority = 20
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionLog.add("local")
                return chain.proceed(context)
            }
        }

        manager.addGlobalInterceptor(globalInterceptor)
        manager.addInterceptor("payment/*", localInterceptor)

        val context = createTestContext("iap://payment/checkout")
        val result = manager.executeChain(context) {
            executionLog.add("terminal")
            RouteResult.Success(it)
        }

        assertEquals(listOf("global", "local", "terminal"), executionLog)
        assertIs<RouteResult.Success>(result)
    }

    @Test
    fun `executeChain should only execute matching local interceptors`() = runTest {
        val manager = InterceptorManager()
        val executionLog = mutableListOf<String>()

        val paymentInterceptor = object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionLog.add("payment")
                return chain.proceed(context)
            }
        }

        val orderInterceptor = object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionLog.add("order")
                return chain.proceed(context)
            }
        }

        manager.addInterceptor("payment/*", paymentInterceptor)
        manager.addInterceptor("order/*", orderInterceptor)

        val context = createTestContext("iap://payment/checkout")
        manager.executeChain(context) {
            executionLog.add("terminal")
            RouteResult.Success(it)
        }

        assertEquals(listOf("payment", "terminal"), executionLog)
    }

    // ==================== clear 测试 ====================

    @Test
    fun `clear should remove all interceptors`() {
        val manager = InterceptorManager()
        manager.addGlobalInterceptor(TestInterceptor("global"))
        manager.addInterceptor("payment/*", TestInterceptor("local"))

        manager.clear()

        assertEquals(0, manager.globalInterceptorCount())
        assertEquals(0, manager.localInterceptorCount())
    }

    // ==================== 辅助类和方法 ====================

    private class TestInterceptor(
        override val name: String,
        override val priority: Int = 100
    ) : RouteInterceptor {
        override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
            return chain.proceed(context)
        }
    }

    private fun createTestContext(url: String): RouteContext {
        val parsedRoute = ProtocolParser.parseOrNull(url)
            ?: error("Invalid test url: $url")
        return RouteContext(
            url = url,
            parsedRoute = parsedRoute,
            params = emptyMap(),
            source = RouteSource.INTERNAL,
            timestamp = 0L
        )
    }
}
