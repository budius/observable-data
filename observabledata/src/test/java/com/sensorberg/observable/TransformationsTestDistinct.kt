package com.sensorberg.observable

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class TransformationsTestDistinct {

	@Test fun distinct_only_receives_when_value_is_distinct() {
		val tested = MutableObservableData<String>()
		val distinct = Transformations.distinct(tested)
		val waitForDistinct = CountDownLatch(2)
		distinct.observe {
			waitForDistinct.countDown()
		}

		val ref = AtomicReference<CountDownLatch>()
		var waitForTested = CountDownLatch(1)
		ref.set(waitForTested)
		tested.observe {
			ref.get().countDown()
		}

		tested.value = "foo"
		waitForTested.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS)
		waitForTested = CountDownLatch(1)
		ref.set(waitForTested)

		tested.value = "foo"
		waitForTested.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS)
		waitForTested = CountDownLatch(1)
		ref.set(waitForTested)

		tested.value = "bar"
		waitForTested.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS)
		waitForTested = CountDownLatch(1)
		ref.set(waitForTested)

		assertTrue(waitForDistinct.await(ObservableDataTest.TIMEOUT, TimeUnit.MILLISECONDS))
	}
}