package com.iap.router.interceptor

import com.iap.router.core.ProtocolParser
import com.iap.router.model.RouteContext
import com.iap.router.model.RouteResult
import com.iap.router.model.RouteSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class InterceptorChainTest {

    // ==================== 基础链执行测试 ====================

    @Test
    fun `chain should execute terminal handler when no interceptors`() = runTest {
        val context = createTestContext("iap://test/page")
        var terminalCalled = false

        val result = executeInterceptorChain(
            interceptors = emptyList(),
            context = context,
            terminalHandler = {
                terminalCalled = true
                RouteResult.Success(it)
            }
        )

        assertTrue(terminalCalled)
        assertIs<RouteResult.Success>(result)
    }

    @Test
    fun `chain should execute single interceptor then terminal`() = runTest {
        val context = createTestContext("iap://test/page")
        val executionOrder = mutableListOf<String>()

        val interceptor = object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionOrder.add("interceptor")
                return chain.proceed(context)
            }
        }

        val result = executeInterceptorChain(
            interceptors = listOf(interceptor),
            context = context,
            terminalHandler = {
                executionOrder.add("terminal")
                RouteResult.Success(it)
            }
        )

        assertEquals(listOf("interceptor", "terminal"), executionOrder)
        assertIs<RouteResult.Success>(result)
    }

    @Test
    fun `chain should execute interceptors in priority order`() = runTest {
        val context = createTestContext("iap://test/page")
        val executionOrder = mutableListOf<String>()

        val lowPriority = object : RouteInterceptor {
            override val priority = 100
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionOrder.add("low")
                return chain.proceed(context)
            }
        }

        val highPriority = object : RouteInterceptor {
            override val priority = 10
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionOrder.add("high")
                return chain.proceed(context)
            }
        }

        val mediumPriority = object : RouteInterceptor {
            override val priority = 50
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionOrder.add("medium")
                return chain.proceed(context)
            }
        }

        executeInterceptorChain(
            interceptors = listOf(lowPriority, highPriority, mediumPriority),
            context = context,
            terminalHandler = {
                executionOrder.add("terminal")
                RouteResult.Success(it)
            }
        )

        assertEquals(listOf("high", "medium", "low", "terminal"), executionOrder)
    }

    // ==================== 拦截阻断测试 ====================

    @Test
    fun `interceptor can block route`() = runTest {
        val context = createTestContext("iap://test/page")
        var terminalCalled = false

        val blockingInterceptor = object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                return RouteResult.Blocked("Access denied")
            }
        }

        val result = executeInterceptorChain(
            interceptors = listOf(blockingInterceptor),
            context = context,
            terminalHandler = {
                terminalCalled = true
                RouteResult.Success(it)
            }
        )

        assertIs<RouteResult.Blocked>(result)
        assertEquals("Access denied", result.reason)
        assertEquals(false, terminalCalled)
    }

    @Test
    fun `early interceptor blocking should skip later interceptors`() = runTest {
        val context = createTestContext("iap://test/page")
        val executionOrder = mutableListOf<String>()

        val firstInterceptor = object : RouteInterceptor {
            override val priority = 10
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionOrder.add("first")
                return RouteResult.Blocked("Blocked by first")
            }
        }

        val secondInterceptor = object : RouteInterceptor {
            override val priority = 20
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                executionOrder.add("second")
                return chain.proceed(context)
            }
        }

        val result = executeInterceptorChain(
            interceptors = listOf(secondInterceptor, firstInterceptor),
            context = context,
            terminalHandler = {
                executionOrder.add("terminal")
                RouteResult.Success(it)
            }
        )

        assertEquals(listOf("first"), executionOrder)
        assertIs<RouteResult.Blocked>(result)
    }

    // ==================== 参数修改测试 ====================

    @Test
    fun `interceptor can modify context params`() = runTest {
        val context = createTestContext("iap://test/page", params = mapOf("original" to "value"))
        var receivedParams: Map<String, Any?>? = null

        val modifyingInterceptor = object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                val newContext = context.withParams(mapOf("added" to "newValue"))
                return chain.proceed(newContext)
            }
        }

        executeInterceptorChain(
            interceptors = listOf(modifyingInterceptor),
            context = context,
            terminalHandler = {
                receivedParams = it.params
                RouteResult.Success(it)
            }
        )

        assertEquals("value", receivedParams?.get("original"))
        assertEquals("newValue", receivedParams?.get("added"))
    }

    @Test
    fun `multiple interceptors can chain param modifications`() = runTest {
        val context = createTestContext("iap://test/page", params = emptyMap())
        var receivedParams: Map<String, Any?>? = null

        val firstModifier = object : RouteInterceptor {
            override val priority = 10
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                val newContext = context.withParams(mapOf("first" to "1"))
                return chain.proceed(newContext)
            }
        }

        val secondModifier = object : RouteInterceptor {
            override val priority = 20
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                val newContext = context.withParams(mapOf("second" to "2"))
                return chain.proceed(newContext)
            }
        }

        executeInterceptorChain(
            interceptors = listOf(secondModifier, firstModifier),
            context = context,
            terminalHandler = {
                receivedParams = it.params
                RouteResult.Success(it)
            }
        )

        assertEquals("1", receivedParams?.get("first"))
        assertEquals("2", receivedParams?.get("second"))
    }

    // ==================== 重定向测试 ====================

    @Test
    fun `interceptor can return redirect result`() = runTest {
        val context = createTestContext("iap://payment/checkout")

        val redirectInterceptor = object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                return RouteResult.Redirect(
                    newUrl = "iap://auth/login",
                    newParams = mapOf("returnUrl" to context.url)
                )
            }
        }

        val result = executeInterceptorChain(
            interceptors = listOf(redirectInterceptor),
            context = context,
            terminalHandler = { RouteResult.Success(it) }
        )

        assertIs<RouteResult.Redirect>(result)
        assertEquals("iap://auth/login", result.newUrl)
        assertEquals("iap://payment/checkout", result.newParams["returnUrl"])
    }

    // ==================== 错误处理测试 ====================

    @Test
    fun `interceptor can return error result`() = runTest {
        val context = createTestContext("iap://test/page")

        val errorInterceptor = object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                return RouteResult.Error(IllegalStateException("Something went wrong"))
            }
        }

        val result = executeInterceptorChain(
            interceptors = listOf(errorInterceptor),
            context = context,
            terminalHandler = { RouteResult.Success(it) }
        )

        assertIs<RouteResult.Error>(result)
        assertIs<IllegalStateException>(result.exception)
    }

    // ==================== 拦截器属性测试 ====================

    @Test
    fun `interceptor default priority is 100`() {
        val interceptor = object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                return chain.proceed(context)
            }
        }

        assertEquals(100, interceptor.priority)
    }

    @Test
    fun `interceptor default name is class simple name`() {
        val interceptor = object : RouteInterceptor {
            override suspend fun intercept(context: RouteContext, chain: InterceptorChain): RouteResult {
                return chain.proceed(context)
            }
        }

        // Anonymous class name varies by platform, just check it's not empty
        assertTrue(interceptor.name.isNotEmpty())
    }

    // ==================== 辅助方法 ====================

    private fun createTestContext(
        url: String,
        params: Map<String, Any?> = emptyMap()
    ): RouteContext {
        val parsedRoute = ProtocolParser.parseOrNull(url)
            ?: error("Invalid test url: $url")
        return RouteContext(
            url = url,
            parsedRoute = parsedRoute,
            params = params,
            source = RouteSource.INTERNAL,
            timestamp = 0L
        )
    }
}
