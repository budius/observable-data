package com.sensorberg.observable

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class TransformationsTestMap {
	@Test fun map_properly_dispatches_values() {
		val tested = MutableObservableData<String>()
		val received = AtomicReference<String>()
		val mapped = Transformations.map(tested) {
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
		assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("false", value.get())
		assertEquals("true", received.get())

	}
}