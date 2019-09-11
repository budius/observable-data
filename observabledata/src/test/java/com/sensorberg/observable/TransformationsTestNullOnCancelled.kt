package com.sensorberg.observable

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class TransformationsTestNullOnCancelled {

	@Test fun receives_values_before_cancel() {
		val cancellation = Cancellation()
		val source = MutableObservableData<String>()
		val tested = Transformations.nullOnCancelled(source, cancellation)

		val wait = CountDownLatch(1)
		val received = AtomicReference<String>()
		tested.observe {
			received.set(it)
			wait.countDown()
		}

		source.value = "hello"
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("hello", received.get())
	}

	@Test fun changes_to_null_on_cancel() {
		val cancellation = Cancellation()
		val source = MutableObservableData<String>()
		val tested = Transformations.nullOnCancelled(source, cancellation)

		val wait = CountDownLatch(1)
		val cancelWait = CountDownLatch(1)
		val received = AtomicReference<String>()
		tested.observe {
			received.set(it)
			if (cancellation.isCancelled) {
				cancelWait.countDown()
			} else {
				wait.countDown()
			}
		}

		source.value = "hello"
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))

		cancellation.cancel()
		assertTrue(cancelWait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertNull(received.get())
	}

	@Test fun no_new_values_after_cancel() {
		val cancellation = Cancellation()
		val source = MutableObservableData<String>()
		val tested = Transformations.nullOnCancelled(source, cancellation)

		val helloWait = CountDownLatch(1)
		val worldWait = CountDownLatch(2)
		val cancelWait = CountDownLatch(1)

		val received = AtomicReference<String>()
		tested.observe {
			received.set(it)
			if (cancellation.isCancelled) {
				cancelWait.countDown()
			} else {
				worldWait.countDown()
				helloWait.countDown()
			}
		}

		source.value = "hello"
		assertTrue(helloWait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		cancellation.cancel()
		assertTrue(cancelWait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		source.value = "world"
		assertFalse(worldWait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertNull(received.get())
	}
}