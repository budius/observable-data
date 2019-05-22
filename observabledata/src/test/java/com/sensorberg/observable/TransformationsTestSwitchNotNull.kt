package com.sensorberg.observable

import com.sensorberg.observable.ObservableDataTest.Companion.TIMEOUT
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

class TransformationsTestSwitchNotNull {

	@Test fun switch_not_null_returns_first_not_null_and_nothing_more() {
		val wait = CountDownLatch(1)
		val tested = MutableObservableData<String>()
		val received = AtomicReference<String>()

		val mapped = Transformations.switchNotNull(tested) {
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

	@Test fun switch_not_null_does_nothing_on_null_value() {
		val wait = CountDownLatch(1)
		val tested = MutableObservableData<String>()
		val count = AtomicInteger(0)

		val mapped = Transformations.switchNotNull(tested) {
			MutableObservableData<String>().apply { value = "false" }
		}

		mapped.observe {
			count.incrementAndGet()
			wait.countDown()
		}

		val waitForNull = CountDownLatch(1)
		tested.observe {
			waitForNull.countDown()
		}
		tested.value = null
		// check that null was received
		assertTrue(waitForNull.await(TIMEOUT, TimeUnit.MILLISECONDS))

		// set value and grab result
		tested.value = "true"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals(1, count.get())
	}

	@Test fun cancellation_is_properly_handled() {
		val tested = MutableObservableData<String>().apply { value = "true" }
		val cancel = Cancellation()

		val mapped = Transformations.switchNotNull(tested, cancel) {
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