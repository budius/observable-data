package com.sensorberg.observable

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.ArrayBlockingQueue
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

	@Test(expected = IllegalAccessException::class) fun crashes_if_on_wrong_thread() {
		val tested = MutableObservableData<String>()
		val value = ArrayBlockingQueue<Exception>(1)
		tested.observe {
			try {
				Transformations.observeNotNullSync(tested, 1, TimeUnit.MILLISECONDS) // crash
			} catch (e: Exception) {
				value.offer(e)
			}
		}
		tested.value = "foo"
		value.poll(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS)?.let {
			throw it
		}
	}
}