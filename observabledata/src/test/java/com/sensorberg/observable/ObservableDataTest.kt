package com.sensorberg.observable

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class ObservableDataTest {

	companion object {
		internal const val TIMEOUT = 20L
	}

	@Test fun all_observers_receive_values() {
		val tested = MutableObservableData<String>()
		val wait = CountDownLatch(3)
		for (i in 0..3) {
			tested.observe { wait.countDown() }
		}
		tested.value = "true"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test fun all_observers_receive_values_twice() {
		val tested = MutableObservableData<String>()
		var wait = CountDownLatch(3)
		for (i in 0..3) {
			tested.observe {
				if (wait.count > 0) wait.countDown()
				else wait.countDown()
			}
		}
		tested.value = "true"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		wait = CountDownLatch(3)
		tested.value = "false"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test fun if_set_value_is_not_called_observer_doesnt_get_called() {
		val tested = MutableObservableData<String>()
		val wait = CountDownLatch(1)
		tested.observe { wait.countDown() }
		assertFalse(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test fun observer_immediately_receives_value_already_stored() {
		val tested = MutableObservableData<String>()
		var wait = CountDownLatch(1)
		tested.observe { wait.countDown() }
		tested.value = "true"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))

		val received = AtomicReference<String>()
		wait = CountDownLatch(1)
		tested.observe {
			received.set(it)
			wait.countDown()
		}
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", received.get())

	}

	@Test fun set_value_twice_observer_only_receive_last_value() {
		val tested = MutableObservableData<String>()
		val received = AtomicReference<String>()
		val wait = CountDownLatch(1)
		tested.observe {
			received.set(it)
			wait.countDown()
		}
		tested.value = "true"
		tested.value = "false"
		wait.await(TIMEOUT, TimeUnit.MILLISECONDS)
		assertEquals("false", received.get())
	}

	@Test fun set_value_twice_observer_only_receive_once() {
		val tested = MutableObservableData<String>()
		val wait = CountDownLatch(2)
		tested.value = "true"
		tested.value = "false"
		tested.observe { wait.countDown() }
		assertFalse(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
	}

	@Test fun on_active_inactive_are_properly_called() {
		val wait = CountDownLatch(2)
		val active = AtomicBoolean(false)
		val inactive = AtomicBoolean(false)
		val tested = object : MutableObservableData<String>() {
			override fun onActive() {
				active.set(true)
				wait.countDown()
			}

			override fun onInactive() {
				inactive.set(true)
				wait.countDown()
			}
		}

		val observer: Observer<String> = {}
		tested.observe(observer)
		tested.value = "true"
		tested.removeObserver(observer)
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertTrue(active.get())
		assertTrue(inactive.get())
	}

	@Test fun observers_are_called_on_separate_thread() {
		val wait = CountDownLatch(1)
		val tested = MutableObservableData<String>()
		val testThread = Thread.currentThread()
		val observerThread = AtomicReference<Thread>()
		tested.observe {
			wait.countDown()
			observerThread.set(Thread.currentThread())
		}
		tested.value = "true"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertNotEquals(testThread, observerThread.get())
	}

	@Test fun get_value_returns_value_after_execution_on_background_thread() {
		val wait = CountDownLatch(1)
		val tested = MutableObservableData<String>()
		tested.observe { wait.countDown() }
		tested.value = "true"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", tested.value)
	}

	@Test fun has_observer_values_are_correct() {
		var wait = CountDownLatch(1)
		val tested = object : MutableObservableData<String>() {
			override fun onActive() {
				wait.countDown()
			}

			override fun onInactive() {
				wait.countDown()
			}
		}
		val observer: Observer<String> = { wait.countDown() }

		assertFalse(tested.hasObservers())
		tested.observe(observer)
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertTrue(tested.hasObservers())

		wait = CountDownLatch(1)
		tested.removeObserver(observer)
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertFalse(tested.hasObservers())
	}

	@Test fun value_changes_inside_on_active_gets_dispatched_and_previous_one_doesnt() {
		val wait = CountDownLatch(2)
		val received = AtomicReference<String>()
		val tested = object : MutableObservableData<String>() {
			override fun onActive() {
				if (hasObservers()) {
					this.value = "false"
				}
				wait.countDown()
			}
		}
		tested.value = "true"
		tested.observe {
			received.set(it)
			wait.countDown()
		}
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("false", received.get())
	}

	@Test fun getValue_matches_value_received_on_observer() {
		val tested = MutableObservableData<String>()
		val first = CountDownLatch(1)

		// add some value
		tested.value = "true"
		tested.observe { first.countDown() }
		assertTrue(first.await(TIMEOUT, TimeUnit.MILLISECONDS))

		// change value
		tested.value = "false"
		val wait = CountDownLatch(1)
		val received1 = AtomicReference<String>()
		val received2 = AtomicReference<String>()
		val observer: Observer<String> = {
			received1.set(it)
			received2.set(tested.value)
			assertEquals(it, tested.value)
			wait.countDown()
		}
		tested.observe(observer)

		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("false", received1.get())
		assertEquals("false", received2.get())
		assertEquals("false", tested.value)

	}
}