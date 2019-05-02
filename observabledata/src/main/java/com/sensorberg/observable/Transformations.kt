package com.sensorberg.observable

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

typealias Mapper<X, Y> = ((X?) -> Y?)

object Transformations {

	/**
	 * Maps values from this ObservableData to a new ObservableData.
	 * Same from LiveData Transformations.map
	 */
	fun <I, O> map(source: ObservableData<I>, mapper: Mapper<I, O>): ObservableData<O> {
		val result = MediatorObservableData<O>()
		result.addSource(source) {
			result.value = mapper.invoke(it)
		}
		return result
	}

	/**
	 * Switches values from this ObservableData to a new ObservableData that is sourced from the result.
	 * Same from LiveData Transformations.switchMap
	 */
	fun <I, O> switchMap(source: ObservableData<I>, mapper: Mapper<I, ObservableData<O>?>): ObservableData<O> {
		val result = MediatorObservableData<O>()
		var currentSource: ObservableData<O>? = null
		result.addSource(source) { sourceValue ->
			val newSource = mapper.invoke(sourceValue)

			// if it's the same, ignore
			if (currentSource == newSource) {
				return@addSource
			}

			// remove old source
			currentSource?.let { result.removeSource(it) }

			// update current source
			currentSource = newSource

			// observe new source
			if (newSource != null) {
				// update result value
				result.addSource(newSource) { resultValue ->
					result.value = resultValue
				}
			}
		}
		return result
	}

	/**
	 * Observes the source and passes the first non-null value to the callback.
	 * When the (optional) [Cancellation.cancel] is invoked, this will remove any pending observers and the [callback] won't be called anymore.
	 * If the cancellation value is already true, nothing will be executed
	 */
	fun <T> observeNotNull(source: ObservableData<T>, cancellation: Cancellation? = null, callback: (T) -> Unit) {
		if (cancellation?.isCancelled == true) return
		val observer = object : Observer<T> {
			override fun invoke(t: T?) {
				t?.let { nonNull ->
					source.removeObserver(this)
					if (cancellation?.isCancelled == true) return
					callback.invoke(nonNull)
				}
			}
		}
		source.observe(observer)
		cancellation?.onCancelled { source.removeObserver(observer) }
	}

	/**
	 *
	 * Maps the first non-null value from the source to a new Observable.
	 * Similar to [Transformations.map], but only calls the mapper for the first non-null value.
	 * When the (optional) [Cancellation.cancel] is invoked, this will remove any pending observers and the [mapper] won't be called.
	 * If the cancellation value is already true, nothing will be executed and the returned result value won't change
	 */
	fun <I, O> mapNotNull(source: ObservableData<I>, cancellation: Cancellation? = null, mapper: (I) -> O?): ObservableData<O> {
		val result = MediatorObservableData<O>()
		if (cancellation?.isCancelled == true) return result
		val observer = object : Observer<I> {
			override fun invoke(t: I?) {
				t?.let { nonNull ->
					result.removeSource(source)
					if (cancellation?.isCancelled == true) return
					result.value = mapper.invoke(nonNull)
				}
			}
		}
		result.addSource(source, observer)
		cancellation?.onCancelled { result.removeSource(source) }
		return result
	}

	/**
	 * Switches the first non-null value from this ObservableData to a new ObservableData that is sourced from the result.
	 * Similar to [Transformations.switchMap], but only calls the mapper for the first non-null value.
	 * When the (optional) [Cancellation.cancel] is invoked, this will remove any pending observers and the [mapper] won't be called
	 * If the cancellation value is already true, nothing will be executed and the returned result value won't change
	 */
	fun <I, O> switchNotNull(source: ObservableData<I>, cancellation: Cancellation? = null, mapper: (I) -> ObservableData<O>): ObservableData<O> {
		val result = MediatorObservableData<O>()
		if (cancellation?.isCancelled == true) return result
		val observer = object : Observer<I> {
			override fun invoke(t: I?) {
				t?.let { nonNull ->
					result.removeSource(source)
					if (cancellation?.isCancelled == true) return
					val newSource = mapper.invoke(nonNull)
					result.addSource(newSource) { result.value = it }
				}
			}
		}
		result.addSource(source, observer)
		cancellation?.onCancelled { result.removeSource(source) }
		return result
	}

	/**
	 * Synchronously awaits for the source data to produces a non-null value up to the specified timeout.
	 */
	fun <T> observeNotNullSync(source: ObservableData<T>, timeout: Long, unit: TimeUnit): T? {
		if (ObservableData.isExecutorThread()) {
			throw IllegalAccessException("observeNotNullSync() must be called from a different thread from the one used by ObservableData")
		}
		val result = AtomicReference<T>()
		val wait = CountDownLatch(1)
		observeNotNull(source) {
			result.set(it)
			wait.countDown()
		}
		wait.await(timeout, unit)
		return result.get()
	}

}