package com.sensorberg.observable

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class TransformationsTestObserveNotNullSync {

	@Test fun test_value_is_received_synchronously() {
		val tested = MutableObservableData<String>()
		val received = AtomicReference<String>()
		val wait = CountDownLatch(1)

		Thread {
			val value = Transformations.observeNotNullSync(tested, ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS)
			received.set(value)
			wait.countDown()
		}.start()

		tested.value = "true"
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", received.get())
	}

	@Test fun test_value_is_not_received_if_takes_too_long() {
		val tested = MutableObservableData<String>()
		val received = AtomicReference<String>()
		val wait = CountDownLatch(1)

		Thread {
			val value = Transformations.observeNotNullSync(tested, ObservableDataTest.TIMEOUT / 2, TimeUnit.MILLISECONDS)
			received.set(value)
			wait.countDown()
		}.start()

		Thread.sleep(ObservableDataTest.TIMEOUT)

		tested.value = "true"
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertNull(received.get())
	}
}