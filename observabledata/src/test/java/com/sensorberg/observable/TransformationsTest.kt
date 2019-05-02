package com.sensorberg.observable

import com.sensorberg.observable.ObservableDataTest.Companion.TIMEOUT
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import com.sensorberg.observable.Transformations.switchMap
import com.sensorberg.observable.Transformations.map
import com.sensorberg.observable.Transformations.observeNotNull
import com.sensorberg.observable.Transformations.switchNotNull

class TransformationsTest {

	@Test fun map_properly_dispatches_values() {
		val tested = MutableObservableData<String>()
		val received = AtomicReference<String>()
		val mapped = map(tested) {
			received.set(it)
			"false"
		}
		val value = AtomicReference<String>()

		tested.value = "true"

		val wait = CountDownLatch(1)
		mapped.observe {
			value.set(it)
			wait.countDown()
		}
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("false", value.get())
		assertEquals("true", received.get())

	}

	@Test fun switch_map_properly_switches_data() {
		val wait = CountDownLatch(1)
		val tested = MutableObservableData<String>().apply { value = "true" }
		val received = AtomicReference<String>()

		val mapped = switchMap(tested) { MutableObservableData<String>().apply { value = "false" } }
		mapped.observe {
			received.set(it)
			wait.countDown()
		}
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("false", received.get())
	}

	@Test fun observe_not_null_returns_first_not_null_and_nothing_more() {
		val wait = CountDownLatch(1)
		val tested = MutableObservableData<String>()
		val received = AtomicReference<String>()


		observeNotNull(tested) {
			received.set(it)
			wait.countDown()
		}

		// set value and grab result
		tested.value = "true"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", received.get())

		// check observer been unregistered
		assertFalse(tested.hasObservers())

	}

	@Test fun switch_not_null_returns_first_not_null_and_nothing_more() {
		val wait = CountDownLatch(1)
		val tested = MutableObservableData<String>()
		val received = AtomicReference<String>()

		val mapped = switchNotNull(tested) {
			MutableObservableData<String>().apply { value = "false" }
		}

		mapped.observe {
			received.set(it)
			wait.countDown()
		}

		// set value and grab result
		tested.value = "true"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("false", received.get())

		// check observer been unregistered
		assertFalse(tested.hasObservers())

	}

	@Test fun cancellation_is_properly_handled() {
		val tested = MutableObservableData<String>().apply { value = "true" }
		val cancel = Cancellation()

		val mapped = switchNotNull(tested, cancel) {
			MutableObservableData<String>().apply { value = "false" }
		}

		val wait = CountDownLatch(1)
		val received = AtomicReference<String>()

		cancel.cancel()

		mapped.observe {
			received.set(it)
			assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		}

		assertFalse(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertNull(received.get())
	}
}