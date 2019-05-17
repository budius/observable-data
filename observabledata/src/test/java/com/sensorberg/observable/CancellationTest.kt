package com.sensorberg.observable

import org.junit.Test

import org.junit.Assert.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class CancellationTest {

	@Test
	fun receives_cancel_signal() {
		val cancellation = Cancellation()
		val wait = CountDownLatch(1)
		cancellation.onCancelled { wait.countDown() }
		cancellation.cancel()
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test
	fun removes_cancel_listener() {
		val cancellation = Cancellation()
		val wait = CountDownLatch(1)
		val listener = { wait.countDown() }
		cancellation.onCancelled(listener)
		cancellation.removeCallback(listener)
		cancellation.cancel()
		assertFalse(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test
	fun does_not_crash_if_tries_to_remove_listener_that_was_never_added() {
		val cancellation = Cancellation()
		val wait = CountDownLatch(1)
		val listener = { wait.countDown() }
		cancellation.removeCallback(listener)
		cancellation.cancel()
		assertFalse(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
	}
}