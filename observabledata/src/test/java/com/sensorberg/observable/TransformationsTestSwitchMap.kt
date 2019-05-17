package com.sensorberg.observable

import org.junit.Assert
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class TransformationsTestSwitchMap {
	@Test fun switch_map_properly_switches_data() {
		val wait = CountDownLatch(1)
		val tested = MutableObservableData<String>().apply { value = "true" }
		val received = AtomicReference<String>()

		val mapped = Transformations.switchMap(tested) { MutableObservableData<String>().apply { value = "false" } }
		mapped.observe {
			received.set(it)
			wait.countDown()
		}
		Assert.assertTrue(wait.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
		Assert.assertEquals("false", received.get())
	}
}