package com.worldfirst.router.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProtocolParserTest {

    // ==================== 正常路径测试 ====================

    @Test
    fun `parse should return ParsedRoute when url is valid`() {
        val result = ProtocolParser.parse("worldfirst://order/detail")

        assertTrue(result.isSuccess)
        val parsedRoute = result.getOrNull()
        assertNotNull(parsedRoute)
        assertEquals("worldfirst", parsedRoute.scheme)
        assertEquals("order/detail", parsedRoute.path)
    }

    @Test
    fun `parse should extract scheme correctly`() {
        val result = ProtocolParser.parseOrNull("customscheme://some/path")

        assertNotNull(result)
        assertEquals("customscheme", result.scheme)
    }

    @Test
    fun `parse should extract path correctly`() {
        val result = ProtocolParser.parseOrNull("worldfirst://account/settings/security")

        assertNotNull(result)
        assertEquals("account/settings/security", result.path)
        assertEquals(listOf("account", "settings", "security"), result.pathSegments)
    }

    @Test
    fun `parse should extract query params`() {
        val result = ProtocolParser.parseOrNull("worldfirst://order/detail?id=123&from=list")

        assertNotNull(result)
        assertEquals("123", result.queryParams["id"])
        assertEquals("list", result.queryParams["from"])
    }

    @Test
    fun `parse should handle url with only scheme and path`() {
        val result = ProtocolParser.parseOrNull("worldfirst://home")

        assertNotNull(result)
        assertEquals("worldfirst", result.scheme)
        assertEquals("home", result.path)
        assertTrue(result.queryParams.isEmpty())
    }

    @Test
    fun `parse should handle deep path`() {
        val result = ProtocolParser.parseOrNull("worldfirst://fx/trade/USDCNY/chart")

        assertNotNull(result)
        assertEquals("fx/trade/USDCNY/chart", result.path)
        assertEquals(4, result.pathSegments.size)
    }

    @Test
    fun `parse should decode url encoded params`() {
        val result = ProtocolParser.parseOrNull("worldfirst://search?query=hello%20world")

        assertNotNull(result)
        assertEquals("hello world", result.queryParams["query"])
    }

    @Test
    fun `parse should decode plus sign as space`() {
        val result = ProtocolParser.parseOrNull("worldfirst://search?query=hello+world")

        assertNotNull(result)
        assertEquals("hello world", result.queryParams["query"])
    }

    @Test
    fun `parse should handle empty query value`() {
        val result = ProtocolParser.parseOrNull("worldfirst://page?flag=&name=test")

        assertNotNull(result)
        assertEquals("", result.queryParams["flag"])
        assertEquals("test", result.queryParams["name"])
    }

    @Test
    fun `parse should handle param without value`() {
        val result = ProtocolParser.parseOrNull("worldfirst://page?flag&name=test")

        assertNotNull(result)
        assertEquals("", result.queryParams["flag"])
        assertEquals("test", result.queryParams["name"])
    }

    // ==================== 异常路径测试 ====================

    @Test
    fun `parse should return failure when url is empty`() {
        val result = ProtocolParser.parse("")

        assertTrue(result.isFailure)
    }

    @Test
    fun `parse should return failure when scheme separator is missing`() {
        val result = ProtocolParser.parse("worldfirst/order/detail")

        assertTrue(result.isFailure)
    }

    @Test
    fun `parse should return failure when scheme is empty`() {
        val result = ProtocolParser.parse("://order/detail")

        assertTrue(result.isFailure)
    }

    @Test
    fun `parseOrNull should return null for invalid url`() {
        val result = ProtocolParser.parseOrNull("invalid url")

        assertNull(result)
    }

    // ==================== 边界条件测试 ====================

    @Test
    fun `parse should handle url with whitespace`() {
        val result = ProtocolParser.parseOrNull("  worldfirst://order/detail  ")

        assertNotNull(result)
        assertEquals("order/detail", result.path)
    }

    @Test
    fun `parse should remove leading slash from path`() {
        val result = ProtocolParser.parseOrNull("worldfirst:///order/detail")

        assertNotNull(result)
        assertEquals("order/detail", result.path)
    }

    @Test
    fun `parse should handle empty path`() {
        val result = ProtocolParser.parseOrNull("worldfirst://")

        assertNotNull(result)
        assertEquals("", result.path)
        assertTrue(result.pathSegments.isEmpty())
    }

    @Test
    fun `parse should handle special characters in query`() {
        val result = ProtocolParser.parseOrNull("worldfirst://page?url=https%3A%2F%2Fexample.com")

        assertNotNull(result)
        assertEquals("https://example.com", result.queryParams["url"])
    }

    @Test
    fun `parse should handle Chinese characters in query`() {
        val result = ProtocolParser.parseOrNull("worldfirst://search?query=%E4%B8%AD%E6%96%87")

        assertNotNull(result)
        assertEquals("中文", result.queryParams["query"])
    }

    @Test
    fun `parse should handle multiple query params with same key - last wins`() {
        val result = ProtocolParser.parseOrNull("worldfirst://page?key=first&key=second")

        assertNotNull(result)
        // 由于使用 toMap()，后面的值会覆盖前面的值
        assertEquals("second", result.queryParams["key"])
    }

    // ==================== buildUrl 测试 ====================

    @Test
    fun `buildUrl should create correct url without params`() {
        val url = ProtocolParser.buildUrl(path = "order/detail")

        assertEquals("worldfirst://order/detail", url)
    }

    @Test
    fun `buildUrl should create correct url with params`() {
        val url = ProtocolParser.buildUrl(
            path = "order/detail",
            queryParams = mapOf("id" to "123", "from" to "list")
        )

        assertTrue(url.startsWith("worldfirst://order/detail?"))
        assertTrue(url.contains("id=123"))
        assertTrue(url.contains("from=list"))
    }

    @Test
    fun `buildUrl should encode special characters`() {
        val url = ProtocolParser.buildUrl(
            path = "search",
            queryParams = mapOf("query" to "hello world")
        )

        assertTrue(url.contains("query=hello+world"))
    }

    @Test
    fun `buildUrl should use custom scheme`() {
        val url = ProtocolParser.buildUrl(
            scheme = "myapp",
            path = "page"
        )

        assertEquals("myapp://page", url)
    }

    // ==================== ParsedRoute 方法测试 ====================

    @Test
    fun `matchesPattern should match exact path`() {
        val route = ProtocolParser.parseOrNull("worldfirst://order/detail")!!

        assertTrue(route.matchesPattern("order/detail"))
        assertFalse(route.matchesPattern("order/list"))
    }

    @Test
    fun `matchesPattern should match path param pattern`() {
        val route = ProtocolParser.parseOrNull("worldfirst://order/detail/123")!!

        assertTrue(route.matchesPattern("order/detail/:id"))
        assertFalse(route.matchesPattern("order/detail"))
    }

    @Test
    fun `matchesPattern should match wildcard pattern`() {
        val route = ProtocolParser.parseOrNull("worldfirst://payment/card/bind/new")!!

        assertTrue(route.matchesPattern("payment/*"))
        assertTrue(route.matchesPattern("payment/card/*"))
    }

    @Test
    fun `matchesPattern should not match different length paths`() {
        val route = ProtocolParser.parseOrNull("worldfirst://order/detail/123/extra")!!

        assertFalse(route.matchesPattern("order/detail/:id"))
    }

    @Test
    fun `getAllParams should merge path and query params`() {
        val route = ProtocolParser.parseOrNull("worldfirst://order/detail?from=list")!!
        route.pathParams["orderId"] = "123"

        val allParams = route.getAllParams()
        assertEquals("list", allParams["from"])
        assertEquals("123", allParams["orderId"])
    }
}
