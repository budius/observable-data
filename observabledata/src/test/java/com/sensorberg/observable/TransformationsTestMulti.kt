package com.sensorberg.observable

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

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
		val result: ObservableData<Int> = Transformations.multiMap(listOf(data1, data2)) { wait.countDown() }
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
		val result: ObservableData<Int> = Transformations.multiMap(listOf(data1, data2)) {
			it.value = value.incrementAndGet()
		}
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
		val result: ObservableData<Int> = Transformations.multiMap(listOf(data1, data2), cancel) { wait.countDown() }
		result.observe { }
		cancel.cancel()
		data1.value = "hello"
		assertFalse(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
	}
}