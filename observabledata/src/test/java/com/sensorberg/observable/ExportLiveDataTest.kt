package com.sensorberg.observable

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.sensorberg.observable.ObservableDataTest.Companion.TIMEOUT
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ExportLiveDataTest {

	@get:Rule val instantExecutorRule = InstantTaskExecutorRule()

	@Test fun data_is_properly_mirrored_to_live_data_on_init() {
		val data = MutableObservableData<String>().apply { value = "true" }
		val tested = data.toLiveData()
		val wait = CountDownLatch(1)
		val received = AtomicReference<String>()

		tested.observeForever {
			received.set(it)
			wait.countDown()
		}

		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", received.get())
	}

	@Test fun data_is_properly_mirrored_to_live_data_after_init() {
		val data = MutableObservableData<String>()
		val tested = data.toLiveData()
		val wait = CountDownLatch(1)
		val received = AtomicReference<String>()

		tested.observeForever {
			received.set(it)
			wait.countDown()
		}

		data.value = "true"
		assertTrue(wait.await(TIMEOUT, TimeUnit.MILLISECONDS))
		assertEquals("true", received.get())
	}

	@Test fun data_is_active_inactive_matching_live_data() {
		val data = MutableObservableData<String>()
		val tested = data.toLiveData()
		val observer = androidx.lifecycle.Observer<String> { }

		tested.observeForever(observer)
		Thread.sleep(TIMEOUT)
		assertTrue(data.hasObservers())

		tested.removeObserver(observer)
		Thread.sleep(TIMEOUT)
		assertFalse(data.hasObservers())

	}
}