package com.sensorberg.observable

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class TransformationsTestMulti {

	@Test fun multiObserve_calls_the_mapper_for_every_interaction() {
		val data1 = MutableObservableData<String>()
		val data2 = MutableObservableData<String>()
		val wait = CountDownLatch(2)
		Transformations.multiObserve(listOf(data1, data2)) { wait.countDown() }
		data1.value = "hello"
		data2.value = "world"
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test fun multiObserve_cancellation_is_properly_handled() {
		val data1 = MutableObservableData<String>()
		val data2 = MutableObservableData<String>()
		val cancel = Cancellation()
		val wait = CountDownLatch(1)
		Transformations.multiObserve(listOf(data1, data2), cancel) { wait.countDown() }
		cancel.cancel()
		data1.value = "hello"
		assertFalse(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test fun multiMap_calls_the_mapper_for_every_interaction() {
		val data1 = MutableObservableData<String>()
		val data2 = MutableObservableData<String>()
		val wait = CountDownLatch(2)
		val result: ObservableData<Long> = Transformations.multiMap(listOf(data1, data2)) { wait.countDown(); wait.count }
		result.observe { }
		data1.value = "hello"
		data2.value = "world"
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test fun multiMap_maps_the_value() {
		val data1 = MutableObservableData<String>()
		val data2 = MutableObservableData<String>()
		val wait = CountDownLatch(2)
		val value = AtomicInteger(0)
		val result: ObservableData<Int> = Transformations.multiMap(listOf(data1, data2)) { value.incrementAndGet() }
		val received = AtomicInteger(-100)
		result.observe { nullableInt ->
			nullableInt?.let { received.set(it) }
			wait.countDown()
		}
		data1.value = "hello"
		data2.value = "world"
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals(2, received.get())
	}

	@Test fun multiMap_cancellation_is_properly_handled() {
		val data1 = MutableObservableData<String>()
		val data2 = MutableObservableData<String>()
		val cancel = Cancellation()
		val wait = CountDownLatch(1)
		val result: ObservableData<Long> = Transformations.multiMap(listOf(data1, data2), cancel) { wait.countDown(); wait.count }
		result.observe { }
		cancel.cancel()
		data1.value = "hello"
		assertFalse(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test fun multiMap_receives_the_last_value() {
		val wait = AtomicReference(CountDownLatch(1))
		val data1 = MutableObservableData<String>()
		val data2 = MutableObservableData<String>()

		val expected = mutableListOf(null, 1, 2, 3)
		val mapped = AtomicInteger(0)
		val result: ObservableData<Int> = Transformations.multiMap(listOf(data1, data2)) {
			val next = expected.removeAt(0)
			assertEquals(next, it)
			wait.get().countDown()
			mapped.incrementAndGet()
		}
		result.observe {}

		data1.value = "0"
		assertTrue(wait.get().await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		wait.set(CountDownLatch(1))
		data2.value = "1"
		assertTrue(wait.get().await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		wait.set(CountDownLatch(1))
		data1.value = "2"
		assertTrue(wait.get().await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		wait.set(CountDownLatch(1))
		data1.value = "3"
		assertTrue(wait.get().await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))

		assertTrue(expected.isEmpty())

	}
}