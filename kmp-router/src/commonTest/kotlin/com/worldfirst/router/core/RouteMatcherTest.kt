package com.worldfirst.router.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RouteMatcherTest {

    // ==================== 精确匹配测试 ====================

    @Test
    fun `match should succeed for exact path`() {
        val result = RouteMatcher.match("order/detail", "order/detail")

        assertNotNull(result)
        assertEquals("order/detail", result.pattern)
        assertTrue(result.pathParams.isEmpty())
    }

    @Test
    fun `match should fail for different paths`() {
        val result = RouteMatcher.match("order/detail", "order/list")

        assertNull(result)
    }

    @Test
    fun `match should fail for different path lengths`() {
        val result = RouteMatcher.match("order/detail/123", "order/detail")

        assertNull(result)
    }

    @Test
    fun `match should succeed for multi-segment exact path`() {
        val result = RouteMatcher.match("account/settings/security", "account/settings/security")

        assertNotNull(result)
        assertEquals("account/settings/security", result.pattern)
    }

    // ==================== 路径参数测试 ====================

    @Test
    fun `match should extract single path param`() {
        val result = RouteMatcher.match("order/detail/123", "order/detail/:orderId")

        assertNotNull(result)
        assertEquals("123", result.pathParams["orderId"])
    }

    @Test
    fun `match should extract multiple path params`() {
        val result = RouteMatcher.match("fx/USDCNY/chart", "fx/:pairId/chart")

        assertNotNull(result)
        assertEquals("USDCNY", result.pathParams["pairId"])
    }

    @Test
    fun `match should extract path params at different positions`() {
        val result = RouteMatcher.match("user/123/order/456", "user/:userId/order/:orderId")

        assertNotNull(result)
        assertEquals("123", result.pathParams["userId"])
        assertEquals("456", result.pathParams["orderId"])
    }

    @Test
    fun `match should handle path param at the end`() {
        val result = RouteMatcher.match("category/electronics", "category/:name")

        assertNotNull(result)
        assertEquals("electronics", result.pathParams["name"])
    }

    @Test
    fun `match should handle path param at the beginning`() {
        val result = RouteMatcher.match("products/detail", ":section/detail")

        assertNotNull(result)
        assertEquals("products", result.pathParams["section"])
    }

    // ==================== 通配符测试 ====================

    @Test
    fun `match should succeed for wildcard pattern`() {
        val result = RouteMatcher.match("payment/card/bind", "payment/*")

        assertNotNull(result)
        assertEquals("card/bind", result.pathParams["*"])
    }

    @Test
    fun `match should succeed for exact prefix with wildcard`() {
        val result = RouteMatcher.match("payment/card", "payment/*")

        assertNotNull(result)
        assertEquals("card", result.pathParams["*"])
    }

    @Test
    fun `match should succeed for deep path with wildcard`() {
        val result = RouteMatcher.match("payment/card/bind/new/step1", "payment/*")

        assertNotNull(result)
        assertEquals("card/bind/new/step1", result.pathParams["*"])
    }

    @Test
    fun `match should fail for wildcard when prefix does not match`() {
        val result = RouteMatcher.match("order/detail", "payment/*")

        assertNull(result)
    }

    @Test
    fun `match should handle wildcard with path param in prefix`() {
        val result = RouteMatcher.match("user/123/settings/security/advanced", "user/:userId/*")

        assertNotNull(result)
        assertEquals("123", result.pathParams["userId"])
        assertEquals("settings/security/advanced", result.pathParams["*"])
    }

    // ==================== 分数/优先级测试 ====================

    @Test
    fun `match should give higher score to exact match`() {
        val exactResult = RouteMatcher.match("order/detail", "order/detail")
        val paramResult = RouteMatcher.match("order/detail", ":section/detail")

        assertNotNull(exactResult)
        assertNotNull(paramResult)
        assertTrue(exactResult.score > paramResult.score)
    }

    @Test
    fun `match should give higher score to more exact segments`() {
        val moreExact = RouteMatcher.match("order/detail/view", "order/detail/:action")
        val lessExact = RouteMatcher.match("order/detail/view", "order/:type/:action")

        assertNotNull(moreExact)
        assertNotNull(lessExact)
        assertTrue(moreExact.score > lessExact.score)
    }

    // ==================== findBestMatch 测试 ====================

    @Test
    fun `findBestMatch should return best matching pattern`() {
        val patterns = listOf(
            "order/*",
            "order/detail/:id",
            "order/detail"
        )

        val result = RouteMatcher.findBestMatch("order/detail", patterns)

        assertNotNull(result)
        assertEquals("order/detail", result.pattern)
    }

    @Test
    fun `findBestMatch should prefer exact match over param match`() {
        val patterns = listOf(
            "order/:type",
            "order/detail"
        )

        val result = RouteMatcher.findBestMatch("order/detail", patterns)

        assertNotNull(result)
        assertEquals("order/detail", result.pattern)
    }

    @Test
    fun `findBestMatch should prefer param match over wildcard`() {
        val patterns = listOf(
            "order/*",
            "order/:id"
        )

        val result = RouteMatcher.findBestMatch("order/123", patterns)

        assertNotNull(result)
        assertEquals("order/:id", result.pattern)
    }

    @Test
    fun `findBestMatch should return null when no pattern matches`() {
        val patterns = listOf("order/detail", "payment/card")

        val result = RouteMatcher.findBestMatch("user/profile", patterns)

        assertNull(result)
    }

    @Test
    fun `findBestMatch should handle empty pattern list`() {
        val result = RouteMatcher.findBestMatch("order/detail", emptyList())

        assertNull(result)
    }

    // ==================== 辅助方法测试 ====================

    @Test
    fun `matches should return true for matching path`() {
        assertTrue(RouteMatcher.matches("order/detail", "order/detail"))
        assertTrue(RouteMatcher.matches("order/123", "order/:id"))
        assertTrue(RouteMatcher.matches("payment/card/bind", "payment/*"))
    }

    @Test
    fun `matches should return false for non-matching path`() {
        assertFalse(RouteMatcher.matches("order/detail", "order/list"))
        assertFalse(RouteMatcher.matches("order/detail/123", "order/detail"))
    }

    @Test
    fun `hasPathParams should detect path params`() {
        assertTrue(RouteMatcher.hasPathParams("order/:id"))
        assertTrue(RouteMatcher.hasPathParams("user/:userId/order/:orderId"))
        assertFalse(RouteMatcher.hasPathParams("order/detail"))
    }

    @Test
    fun `hasWildcard should detect wildcard`() {
        assertTrue(RouteMatcher.hasWildcard("payment/*"))
        assertTrue(RouteMatcher.hasWildcard("user/:id/*"))
        assertFalse(RouteMatcher.hasWildcard("order/detail"))
    }

    @Test
    fun `extractParamNames should return param names`() {
        val names = RouteMatcher.extractParamNames("user/:userId/order/:orderId")

        assertEquals(listOf("userId", "orderId"), names)
    }

    @Test
    fun `extractParamNames should return empty list for no params`() {
        val names = RouteMatcher.extractParamNames("order/detail")

        assertTrue(names.isEmpty())
    }

    // ==================== 边界条件测试 ====================

    @Test
    fun `match should handle empty path`() {
        val result = RouteMatcher.match("", "")

        assertNotNull(result)
        assertTrue(result.pathParams.isEmpty())
    }

    @Test
    fun `match should handle single segment`() {
        val result = RouteMatcher.match("home", "home")

        assertNotNull(result)
        assertEquals("home", result.pattern)
    }

    @Test
    fun `match should handle single param segment`() {
        val result = RouteMatcher.match("123", ":id")

        assertNotNull(result)
        assertEquals("123", result.pathParams["id"])
    }

    @Test
    fun `fillPathParams should update ParsedRoute`() {
        val parsedRoute = ProtocolParser.parseOrNull("worldfirst://order/detail/123")!!
        val matchResult = RouteMatchResult(
            pattern = "order/detail/:orderId",
            pathParams = mapOf("orderId" to "123"),
            score = 21
        )

        RouteMatcher.fillPathParams(parsedRoute, matchResult)

        assertEquals("123", parsedRoute.pathParams["orderId"])
    }
}
