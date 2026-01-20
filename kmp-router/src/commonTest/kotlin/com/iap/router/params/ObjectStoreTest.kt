package com.iap.router.params

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ObjectStoreTest {

    @Test
    fun `put and get should store and retrieve object`() {
        val store = ObjectStore()
        val testObject = "Test Value"

        store.put("key", testObject)
        val retrieved = store.get("key")

        assertEquals(testObject, retrieved)
    }

    @Test
    fun `get should return null for non-existent key`() {
        val store = ObjectStore()

        val retrieved = store.get("nonExistent")

        assertNull(retrieved)
    }

    @Test
    fun `put should store null value`() {
        val store = ObjectStore()

        store.put("key", null)

        assertTrue(store.contains("key"))
        assertNull(store.get("key"))
    }

    @Test
    fun `put should overwrite existing value`() {
        val store = ObjectStore()

        store.put("key", "first")
        store.put("key", "second")

        assertEquals("second", store.get("key"))
    }

    @Test
    fun `remove should return and remove value`() {
        val store = ObjectStore()
        store.put("key", "value")

        val removed = store.remove("key")

        assertEquals("value", removed)
        assertFalse(store.contains("key"))
    }

    @Test
    fun `remove should return null for non-existent key`() {
        val store = ObjectStore()

        val removed = store.remove("nonExistent")

        assertNull(removed)
    }

    @Test
    fun `clear should remove all values`() {
        val store = ObjectStore()
        store.put("key1", "value1")
        store.put("key2", "value2")
        store.put("key3", "value3")

        store.clear()

        assertFalse(store.contains("key1"))
        assertFalse(store.contains("key2"))
        assertFalse(store.contains("key3"))
        assertTrue(store.keys().isEmpty())
    }

    @Test
    fun `contains should return true for existing key`() {
        val store = ObjectStore()
        store.put("key", "value")

        assertTrue(store.contains("key"))
    }

    @Test
    fun `contains should return false for non-existent key`() {
        val store = ObjectStore()

        assertFalse(store.contains("nonExistent"))
    }

    @Test
    fun `keys should return all stored keys`() {
        val store = ObjectStore()
        store.put("key1", "value1")
        store.put("key2", "value2")

        val keys = store.keys()

        assertEquals(2, keys.size)
        assertTrue(keys.contains("key1"))
        assertTrue(keys.contains("key2"))
    }

    @Test
    fun `keys should return empty set for empty store`() {
        val store = ObjectStore()

        assertTrue(store.keys().isEmpty())
    }

    @Test
    fun `should store different types of objects`() {
        val store = ObjectStore()
        val stringValue = "test"
        val intValue = 42
        val listValue = listOf(1, 2, 3)
        val mapValue = mapOf("a" to 1, "b" to 2)

        store.put("string", stringValue)
        store.put("int", intValue)
        store.put("list", listValue)
        store.put("map", mapValue)

        assertEquals(stringValue, store.get("string"))
        assertEquals(intValue, store.get("int"))
        assertEquals(listValue, store.get("list"))
        assertEquals(mapValue, store.get("map"))
    }

    @Test
    fun `keys should return independent copy`() {
        val store = ObjectStore()
        store.put("key1", "value1")

        val keys = store.keys()
        store.put("key2", "value2")

        // Original keys set should not be affected
        assertEquals(1, keys.size)
    }
}
