package com.iap.router.params

import com.iap.router.exception.ParamValidationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParamsExtensionsTest {

    // ==================== requireString 测试 ====================

    @Test
    fun `requireString should return value when key exists`() {
        val params = mapOf("name" to "John")

        assertEquals("John", params.requireString("name"))
    }

    @Test
    fun `requireString should convert non-string to string`() {
        val params = mapOf<String, Any?>("count" to 42)

        assertEquals("42", params.requireString("count"))
    }

    @Test
    fun `requireString should throw when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertFailsWith<ParamValidationException> {
            params.requireString("name")
        }
    }

    @Test
    fun `requireString should throw when value is null`() {
        val params = mapOf<String, Any?>("name" to null)

        assertFailsWith<ParamValidationException> {
            params.requireString("name")
        }
    }

    // ==================== optString 测试 ====================

    @Test
    fun `optString should return value when key exists`() {
        val params = mapOf<String, Any?>("name" to "John")

        assertEquals("John", params.optString("name"))
    }

    @Test
    fun `optString should return null when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertNull(params.optString("name"))
    }

    @Test
    fun `optString should return default when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertEquals("default", params.optString("name", "default"))
    }

    // ==================== requireInt 测试 ====================

    @Test
    fun `requireInt should return value when key exists`() {
        val params = mapOf<String, Any?>("count" to 42)

        assertEquals(42, params.requireInt("count"))
    }

    @Test
    fun `requireInt should convert string to int`() {
        val params = mapOf<String, Any?>("count" to "42")

        assertEquals(42, params.requireInt("count"))
    }

    @Test
    fun `requireInt should throw when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertFailsWith<ParamValidationException> {
            params.requireInt("count")
        }
    }

    @Test
    fun `requireInt should throw when value cannot be converted`() {
        val params = mapOf<String, Any?>("count" to "not a number")

        assertFailsWith<ParamValidationException> {
            params.requireInt("count")
        }
    }

    // ==================== optInt 测试 ====================

    @Test
    fun `optInt should return value when key exists`() {
        val params = mapOf<String, Any?>("count" to 42)

        assertEquals(42, params.optInt("count"))
    }

    @Test
    fun `optInt should return default when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertEquals(0, params.optInt("count"))
        assertEquals(100, params.optInt("count", 100))
    }

    @Test
    fun `optInt should return default when value cannot be converted`() {
        val params = mapOf<String, Any?>("count" to "not a number")

        assertEquals(0, params.optInt("count"))
    }

    // ==================== requireLong 测试 ====================

    @Test
    fun `requireLong should return value when key exists`() {
        val params = mapOf<String, Any?>("timestamp" to 1234567890123L)

        assertEquals(1234567890123L, params.requireLong("timestamp"))
    }

    @Test
    fun `requireLong should convert string to long`() {
        val params = mapOf<String, Any?>("timestamp" to "1234567890123")

        assertEquals(1234567890123L, params.requireLong("timestamp"))
    }

    // ==================== optLong 测试 ====================

    @Test
    fun `optLong should return value when key exists`() {
        val params = mapOf<String, Any?>("timestamp" to 1234567890123L)

        assertEquals(1234567890123L, params.optLong("timestamp"))
    }

    @Test
    fun `optLong should return default when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertEquals(0L, params.optLong("timestamp"))
    }

    // ==================== requireDouble 测试 ====================

    @Test
    fun `requireDouble should return value when key exists`() {
        val params = mapOf<String, Any?>("price" to 99.99)

        assertEquals(99.99, params.requireDouble("price"))
    }

    @Test
    fun `requireDouble should convert string to double`() {
        val params = mapOf<String, Any?>("price" to "99.99")

        assertEquals(99.99, params.requireDouble("price"))
    }

    // ==================== optDouble 测试 ====================

    @Test
    fun `optDouble should return value when key exists`() {
        val params = mapOf<String, Any?>("price" to 99.99)

        assertEquals(99.99, params.optDouble("price"))
    }

    @Test
    fun `optDouble should return default when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertEquals(0.0, params.optDouble("price"))
    }

    // ==================== requireBoolean 测试 ====================

    @Test
    fun `requireBoolean should return value when key exists`() {
        val params = mapOf<String, Any?>("enabled" to true)

        assertTrue(params.requireBoolean("enabled"))
    }

    @Test
    fun `requireBoolean should convert string to boolean`() {
        val params = mapOf<String, Any?>("enabled" to "true")

        assertTrue(params.requireBoolean("enabled"))
    }

    @Test
    fun `requireBoolean should convert number to boolean`() {
        val params1 = mapOf<String, Any?>("enabled" to 1)
        val params2 = mapOf<String, Any?>("enabled" to 0)

        assertTrue(params1.requireBoolean("enabled"))
        assertFalse(params2.requireBoolean("enabled"))
    }

    // ==================== optBoolean 测试 ====================

    @Test
    fun `optBoolean should return value when key exists`() {
        val params = mapOf<String, Any?>("enabled" to true)

        assertTrue(params.optBoolean("enabled"))
    }

    @Test
    fun `optBoolean should return default when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertFalse(params.optBoolean("enabled"))
        assertTrue(params.optBoolean("enabled", true))
    }

    @Test
    fun `optBoolean should return default for invalid string`() {
        val params = mapOf<String, Any?>("enabled" to "not a boolean")

        assertFalse(params.optBoolean("enabled"))
    }

    // ==================== optList 和 optMap 测试 ====================

    @Test
    fun `optList should return list when key exists`() {
        val params = mapOf<String, Any?>("items" to listOf("a", "b", "c"))

        val list = params.optList<String>("items")
        assertEquals(listOf("a", "b", "c"), list)
    }

    @Test
    fun `optList should return null when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertNull(params.optList<String>("items"))
    }

    @Test
    fun `optMap should return map when key exists`() {
        val params = mapOf<String, Any?>("data" to mapOf("key" to "value"))

        val map = params.optMap<String, String>("data")
        assertEquals(mapOf("key" to "value"), map)
    }

    @Test
    fun `optMap should return null when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertNull(params.optMap<String, String>("data"))
    }

    // ==================== requireFloat 测试 ====================

    @Test
    fun `requireFloat should return value when key exists`() {
        val params = mapOf<String, Any?>("ratio" to 0.5f)

        assertEquals(0.5f, params.requireFloat("ratio"))
    }

    @Test
    fun `requireFloat should convert double to float`() {
        val params = mapOf<String, Any?>("ratio" to 0.5)

        assertEquals(0.5f, params.requireFloat("ratio"))
    }

    @Test
    fun `requireFloat should convert string to float`() {
        val params = mapOf<String, Any?>("ratio" to "0.5")

        assertEquals(0.5f, params.requireFloat("ratio"))
    }

    @Test
    fun `requireFloat should throw when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertFailsWith<ParamValidationException> {
            params.requireFloat("ratio")
        }
    }

    @Test
    fun `requireFloat should throw when value cannot be converted`() {
        val params = mapOf<String, Any?>("ratio" to "not a number")

        assertFailsWith<ParamValidationException> {
            params.requireFloat("ratio")
        }
    }

    // ==================== optFloat 测试 ====================

    @Test
    fun `optFloat should return value when key exists`() {
        val params = mapOf<String, Any?>("ratio" to 0.5f)

        assertEquals(0.5f, params.optFloat("ratio"))
    }

    @Test
    fun `optFloat should return default when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertEquals(0.0f, params.optFloat("ratio"))
        assertEquals(1.0f, params.optFloat("ratio", 1.0f))
    }

    @Test
    fun `optFloat should return default when value cannot be converted`() {
        val params = mapOf<String, Any?>("ratio" to "not a number")

        assertEquals(0.0f, params.optFloat("ratio"))
    }

    // ==================== requireList 测试 ====================

    @Test
    fun `requireList should return list when key exists`() {
        val params = mapOf<String, Any?>("items" to listOf("a", "b", "c"))

        val list = params.requireList<String>("items")
        assertEquals(listOf("a", "b", "c"), list)
    }

    @Test
    fun `requireList should throw when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertFailsWith<ParamValidationException> {
            params.requireList<String>("items")
        }
    }

    @Test
    fun `requireList should throw when value is not a list`() {
        val params = mapOf<String, Any?>("items" to "not a list")

        assertFailsWith<ParamValidationException> {
            params.requireList<String>("items")
        }
    }

    // ==================== requireMap 测试 ====================

    @Test
    fun `requireMap should return map when key exists`() {
        val params = mapOf<String, Any?>("data" to mapOf("key" to "value"))

        val map = params.requireMap<String, String>("data")
        assertEquals(mapOf("key" to "value"), map)
    }

    @Test
    fun `requireMap should throw when key is missing`() {
        val params = emptyMap<String, Any?>()

        assertFailsWith<ParamValidationException> {
            params.requireMap<String, String>("data")
        }
    }

    @Test
    fun `requireMap should throw when value is not a map`() {
        val params = mapOf<String, Any?>("data" to "not a map")

        assertFailsWith<ParamValidationException> {
            params.requireMap<String, String>("data")
        }
    }
}
