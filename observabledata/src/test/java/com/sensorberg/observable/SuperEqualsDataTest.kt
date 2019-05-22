package com.sensorberg.observable

import org.junit.Assert.assertTrue
import org.junit.Test
import com.sensorberg.observable.Transformations.superEquals
import org.junit.Assert.assertFalse

class SuperEqualsDataTest {

	@Test fun test_tested_equals() {
		assertTrue(Tested(1, "value", listOf("foo", "bar")).superEquals(
				Tested(1, "value", listOf("foo", "bar"))))
	}

	@Test fun test_tested_not_equals() {
		assertFalse(Tested(1, "value", listOf("foo", "bar")).superEquals(
				Tested(2, "value", listOf("foo", "bar"))))

		assertFalse(Tested(1, "value", listOf("foo", "bar")).superEquals(
				Tested(1, "eulav", listOf("foo", "bar"))))

		assertFalse(Tested(1, "value", listOf("foo", "bar")).superEquals(
				Tested(1, "value", listOf("bar", "bar"))))

		assertFalse(Tested(1, "value", listOf("foo", "bar")).superEquals(
				Tested(1, "value", listOf("bar"))))

		assertFalse(Tested(1, "value", listOf()).superEquals(
				Tested(1, "value", listOf("foo", "bar"))))
	}

	@Test fun test_nested_equals() {
		assertTrue(NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
												   Tested(2, "eulav", listOf("hello", "world")))).superEquals(
				NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
												Tested(2, "eulav", listOf("hello", "world"))))))
	}

	@Test fun test_nested_not_equals() {
		assertFalse(NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
													Tested(2, "eulav", listOf("hello", "world")))).superEquals(
				NestedTested(2, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
												Tested(2, "eulav", listOf("hello", "world"))))))

		assertFalse(NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
													Tested(2, "eulav", listOf("hello", "world")))).superEquals(
				NestedTested(1, "eulav", listOf(Tested(1, "value", listOf("foo", "bar")),
												Tested(2, "eulav", listOf("hello", "world"))))))

		assertFalse(NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
													Tested(2, "eulav", listOf("hello", "world")))).superEquals(
				NestedTested(1, "value", listOf(Tested(2, "value", listOf("foo", "bar")),
												Tested(2, "eulav", listOf("hello", "world"))))))

		assertFalse(NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
													Tested(2, "eulav", listOf("hello", "world")))).superEquals(
				NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
												Tested(3, "eulav", listOf("hello", "world"))))))

		assertFalse(NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
													Tested(2, "eulav", listOf("hello", "world")))).superEquals(
				NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
												Tested(2, "value", listOf("hello", "world"))))))

		assertFalse(NestedTested(1, "value", listOf(Tested(1, "value", listOf("foo", "bar")),
													Tested(2, "eulav", listOf("hello", "world")))).superEquals(
				NestedTested(1, "value", listOf(Tested(1, "value", listOf("hello", "hello")),
												Tested(2, "eulav", listOf("foo", "bar"))))))
	}

	data class Tested(val id: Int, val value: String, val list: List<String>)
	data class NestedTested(val id: Int, val value: String, val list: List<Tested>)
}