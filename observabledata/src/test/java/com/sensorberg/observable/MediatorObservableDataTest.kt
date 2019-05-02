package com.sensorberg.observable

import com.sensorberg.observable.ObservableDataTest.Companion.TIMEOUT
import com.sensorberg.executioner.Executioner
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class MediatorObservableDataTest {

	@Test fun sources_are_only_plugged_if_mediator_have_observer() {
		val source = MutableObservableData<String>()
		val tested = MediatorObservableData<String>()
		val wait = CountDownLatch(1)
		tested.addSource(source) { tested.value = "false" }
		source.value = "true"
		assertFalse(source.hasObservers())
		tested.observe { wait.countDown() }
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertTrue(source.hasObservers())
	}

	@Test fun values_from_source_are_properly_received_on_the_source() {
		val source = MutableObservableData<String>()
		val tested = MediatorObservableData<String>()
		val wait = CountDownLatch(1)
		val value = AtomicReference<String>()
		tested.addSource(source) {
			value.set(it)
			wait.countDown()
		}
		source.value = "true"
		tested.observe { }
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test(expected = IllegalArgumentException::class) fun cannot_add_same_source_twice() {
		// this test MUST be executed synchronously,
		// or else the expected crash happens in a different thread
		Executioner.setDelegate(Executor { it.run() })

		try {
			val source = MutableObservableData<String>()
			val tested = MediatorObservableData<String>()
			tested.addSource(source) { }
			tested.addSource(source) { }
		} finally {
			// clears the executioner
			Executioner.setDelegate(null)
		}
	}
}