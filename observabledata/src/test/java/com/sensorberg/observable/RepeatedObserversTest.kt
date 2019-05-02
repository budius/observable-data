package com.sensorberg.observable

import com.sensorberg.observable.ObservableDataTest.Companion.TIMEOUT
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class RepeatedObserversTest {

	@Test fun observable_data_observer_doesnt_repeat_on_init() {
		val tested = object : ObservableData<String>() {
			init {
				value = "true"
			}
		}
		val count = AtomicInteger(0)

		tested.observe {
			count.addAndGet(1)
		}

		Thread.sleep(TIMEOUT)
		assertEquals(1, count.get())
	}

	@Test fun observable_data_observer_doesnt_repeat_when_changed_on_active() {
		val tested = object : ObservableData<String>() {
			override fun onActive() {
				value = "on-active"
			}
		}
		val count = AtomicInteger(0)
		tested.observe {
			count.addAndGet(1)
		}


		Thread.sleep(TIMEOUT)
		assertEquals(1, count.get())
	}

	@Test fun mutable_observable_data_observer_doesnt_repeat() {
		val tested = MutableObservableData<String>()
		val count = AtomicInteger(0)

		tested.value = "true"
		tested.observe {
			count.addAndGet(1)
		}

		Thread.sleep(TIMEOUT)
		assertEquals(1, count.get())
	}

	@Test fun mediator_observable_data_observer_doesnt_repeat() {
		val tested = MediatorObservableData<String>()
		tested.addSource(MutableObservableData<String>().apply { value = "true" }) { tested.value = it }
		val count = AtomicInteger(0)

		tested.observe {
			count.addAndGet(1)
		}

		Thread.sleep(TIMEOUT)
		assertEquals(1, count.get())
	}

	@Test fun wrap_observable_data_observer_doesnt_repeat() {
		val tested = ObservableDataWrapper<String>()
		tested.wrap(MutableObservableData<String>().apply { value = "true" })
		val count = AtomicInteger(0)

		tested.observe {
			count.addAndGet(1)
		}

		Thread.sleep(TIMEOUT)
		assertEquals(1, count.get())
	}
}