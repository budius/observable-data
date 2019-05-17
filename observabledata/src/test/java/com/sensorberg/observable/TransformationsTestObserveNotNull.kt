package com.sensorberg.observable

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class TransformationsTestObserveNotNull {
	@Test fun observe_not_null_returns_first_not_null_and_nothing_more() {
		val wait = CountDownLatch(1)
		val tested = MutableObservableData<String>()
		val received = AtomicReference<String>()

		Transformations.observeNotNull(tested) {
			received.set(it)
			wait.countDown()
		}

		// set value and grab result
		tested.value = "true"
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", received.get())

		// check observer been unregistered
		assertFalse(tested.hasObservers())

	}

	@Test fun cancellation_is_properly_handled() {
		val tested = MutableObservableData<String>()
		val cancel = Cancellation()

		val received = AtomicReference<String>()
		val wait = CountDownLatch(1)
		Transformations.observeNotNull(tested, cancel) {
			received.set(it)
			wait.countDown()
		}

		cancel.cancel()
		tested.value = "false"

		assertFalse(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertNull(received.get())
	}
}