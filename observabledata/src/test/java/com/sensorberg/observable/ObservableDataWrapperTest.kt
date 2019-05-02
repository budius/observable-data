package com.sensorberg.observable

import com.sensorberg.observable.ObservableDataTest.Companion.TIMEOUT
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ObservableDataWrapperTest {

	@Test fun data_wrapper_wraps_sources() {
		var wait = CountDownLatch(1)
		val tested = ObservableDataWrapper<String>()
		val received = AtomicReference<String>()

		val source = MutableObservableData<String>().apply { value = "true" }
		tested.wrap(source)
		tested.observe {
			received.set(it)
			wait.countDown()
		}
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", received.get())

		wait = CountDownLatch(1)
		source.value = "false"

		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("false", received.get())
	}

	@Test fun data_wrapper_drop_old_sources() {
		var wait = CountDownLatch(1)
		val tested = object : ObservableDataWrapper<String>() {
			fun testOnlySetValue(t: String?) {
				value = t
			}
		}
		val received = AtomicReference<String>()

		val source = MutableObservableData<String>().apply { value = "true" }
		tested.wrap(source)
		tested.observe {
			received.set(it)
			wait.countDown()
		}
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", received.get())

		wait = CountDownLatch(1)
		tested.drop()
		tested.testOnlySetValue("other")
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("other", received.get())

		assertFalse(source.hasObservers())

	}

	@Test fun data_wrapper_mirror_new_sources() {
		var wait = CountDownLatch(1)
		val tested = ObservableDataWrapper<String>()
		val received = AtomicReference<String>()

		val firstSource = MutableObservableData<String>().apply { value = "true" }
		tested.wrap(firstSource)
		tested.observe {
			received.set(it)
			wait.countDown()
		}

		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", received.get())
		wait = CountDownLatch(1)

		tested.wrap(MutableObservableData<String>().apply { value = "false" })

		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("false", received.get())
		assertFalse(firstSource.hasObservers())
	}

}