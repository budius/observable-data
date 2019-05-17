package com.sensorberg.observable

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class TransformationsTestMapNotNull {

	@Test fun map_not_null_properly_maps_the_data() {
		val tested = MutableObservableData<String>()
		val mapped = Transformations.mapNotNull(tested) { "true" }

		val wait = CountDownLatch(1)
		val received = AtomicReference<String>()
		mapped.observe {
			received.set(it)
			wait.countDown()
		}

		tested.value = "false"

		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", mapped.value)
	}

	@Test fun cancellation_is_properly_handled() {
		val tested = MutableObservableData<String>()
		val cancel = Cancellation()
		val mapped = Transformations.mapNotNull(tested, cancel) { "true" }

		val wait = CountDownLatch(1)
		val received = AtomicReference<String>()
		mapped.observe {
			received.set(it)
			wait.countDown()
		}

		cancel.cancel()
		tested.value = "false"

		assertFalse(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertNull(received.get())
	}
}