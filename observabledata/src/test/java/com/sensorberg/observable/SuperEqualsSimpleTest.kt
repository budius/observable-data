package com.sensorberg.observable

import org.junit.Assert.assertTrue
import org.junit.Test
import com.sensorberg.observable.Transformations.superEquals
import org.junit.Assert.assertFalse

class SuperEqualsSimpleTest {

	@Test fun test_nulls() {
		assertTrue(null.superEquals(null))
		assertFalse(null.superEquals("foo"))
		assertFalse("bar".superEquals(null))
	}

	@Test fun simple_scalars_equals() {
		assertTrue(100.superEquals(100))
		assertTrue(1L.superEquals(1L))
		assertTrue(0x8.superEquals(0x8))
		assertTrue("foo".superEquals("foo"))
		assertTrue((0.8f).superEquals(0.8f))
		assertTrue((3.5).superEquals(3.5))
		assertTrue((true).superEquals(true))
		assertTrue((false).superEquals(false))
	}

	@Test fun simple_scalars_not_equals() {
		assertFalse(100.superEquals(101))
		assertFalse(1L.superEquals(2L))
		assertFalse(0x8.superEquals(0xa))
		assertFalse("foo".superEquals("bar"))
		assertFalse((0.8f).superEquals(1.8f))
		assertFalse((3.5).superEquals(4.2))
		assertFalse((true).superEquals(false))
	}

	@Test fun simple_arrays_equals() {
		val v1 = arrayOf(1, 2, 3)
		val v2 = arrayOf(1, 2, 3)
		assertTrue(v1.superEquals(v2))
	}

	@Test fun simple_arrays_not_equals() {
		var v1 = arrayOf(1, 2, 3)
		var v2 = arrayOf(3, 2, 1)
		assertFalse(v1.superEquals(v2))

		v1 = arrayOf(1, 2, 3)
		v2 = arrayOf(4, 5, 6)
		assertFalse(v1.superEquals(v2))

		v1 = arrayOf(1, 2, 3)
		v2 = arrayOf(1, 2, 3, 4, 5, 6)
		assertFalse(v1.superEquals(v2))
	}

	@Test fun simple_set_equals() {
		val v1 = setOf(1, 2, 3)
		val v2 = setOf(1, 2, 3)
		assertTrue(v1.superEquals(v2))
	}

	@Test fun simple_set_not_equals() {
		var v1 = setOf(1, 2, 3)
		var v2 = setOf(3, 2, 1)
		assertFalse(v1.superEquals(v2))

		v1 = setOf(1, 2, 3)
		v2 = setOf(4, 5, 6)
		assertFalse(v1.superEquals(v2))

		v1 = setOf(1, 2, 3)
		v2 = setOf(1, 2, 3, 4, 5, 6)
		assertFalse(v1.superEquals(v2))
	}

	@Test fun simple_map_equals() {
		var v1 = mapOf(1 to 1, 2 to 2, 3 to 3)
		var v2 = mapOf(1 to 1, 2 to 2, 3 to 3)
		assertTrue(v1.superEquals(v2))

		// in a map the order is irrelevant
		v1 = mapOf(1 to 1, 2 to 2, 3 to 3)
		v2 = mapOf(3 to 3, 2 to 2, 1 to 1)
		assertTrue(v1.superEquals(v2))
	}

	@Test fun simple_map_not_equals() {
		var v1 = mapOf(1 to 1, 2 to 2, 3 to 3)
		var v2 = mapOf(1 to 3, 2 to 2, 3 to 1)
		assertFalse(v1.superEquals(v2))

		v1 = mapOf(1 to 1, 2 to 2, 3 to 3)
		v2 = mapOf(4 to 1, 2 to 2, 3 to 3)
		assertFalse(v1.superEquals(v2))

		v1 = mapOf(1 to 1, 2 to 2, 3 to 3)
		v2 = mapOf(1 to 4, 2 to 2, 3 to 3)
		assertFalse(v1.superEquals(v2))

		v1 = mapOf(1 to 1, 2 to 2, 3 to 3)
		v2 = mapOf(1 to 1, 2 to 2)
		assertFalse(v1.superEquals(v2))
	}

	@Test fun simple_list_equals() {
		val v1 = listOf(1, 2, 3)
		val v2 = listOf(1, 2, 3)
		assertTrue(v1.superEquals(v2))
	}

	@Test fun simple_list_not_equals() {
		var v1 = listOf(1, 2, 3)
		var v2 = listOf(3, 2, 1)
		assertFalse(v1.superEquals(v2))

		v1 = listOf(1, 2, 3)
		v2 = listOf(4, 5, 6)
		assertFalse(v1.superEquals(v2))

		v1 = listOf(1, 2, 3)
		v2 = listOf(1, 2, 3, 4, 5, 6)
		assertFalse(v1.superEquals(v2))
	}

	@Test fun kotlin_arrays_equals() {
		assertTrue(byteArrayOf(0x1, 0x2, 0x3).superEquals(byteArrayOf(0x1, 0x2, 0x3)))
		assertTrue(shortArrayOf(1, 2, 3).superEquals(shortArrayOf(1, 2, 3)))
		assertTrue(intArrayOf(1, 2, 3).superEquals(intArrayOf(1, 2, 3)))
		assertTrue(floatArrayOf(1f, 2f, 3f).superEquals(floatArrayOf(1f, 2f, 3f)))
		assertTrue(doubleArrayOf(1.1, 2.1, 3.0).superEquals(doubleArrayOf(1.1, 2.1, 3.0)))
		assertTrue(charArrayOf('f', 'o', 'o').superEquals(charArrayOf('f', 'o', 'o')))
		assertTrue(booleanArrayOf(true, false, false).superEquals(booleanArrayOf(true, false, false)))
	}

	@Test fun kotlin_arrays_not_equals() {
		assertFalse(byteArrayOf(0x1, 0x2, 0x3).superEquals(byteArrayOf(0x3, 0x2, 0x3)))
		assertFalse(shortArrayOf(1, 2, 3).superEquals(shortArrayOf(3, 2, 3)))
		assertFalse(intArrayOf(1, 2, 3).superEquals(intArrayOf(3, 2, 3)))
		assertFalse(floatArrayOf(1f, 2f, 3f).superEquals(floatArrayOf(3f, 2f, 3f)))
		assertFalse(doubleArrayOf(1.1, 2.1, 3.0).superEquals(doubleArrayOf(1.3, 2.1, 3.0)))
		assertFalse(charArrayOf('f', 'o', 'o').superEquals(charArrayOf('b', 'a', 'r')))
		assertFalse(booleanArrayOf(true, false, false).superEquals(booleanArrayOf(true, true, false)))
	}

}
