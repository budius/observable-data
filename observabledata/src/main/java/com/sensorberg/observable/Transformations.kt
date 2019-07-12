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
	 * When the (optional) [Cancellation.cancel] is invoked, this will remove any pending observers and the [callback] won't be called.
	 * If the cancellation value is already true when invoked, nothing will be executed.
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
	 * If the cancellation value is already true when invoked, nothing will be executed and the returned result value won't change.
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
	 * When the (optional) [Cancellation.cancel] is invoked, this will remove any pending observers and the [mapper] won't be called.
	 * If the cancellation value is already true when invoked, nothing will be executed and the returned result value won't change.
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
	 * Throws [IllegalAccessException] if called from the ObservableData thread.
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

	/**
	 * Observe several sources and invokes [onDataChanged] every time any of them changes.
	 * When the (optional) [Cancellation.cancel] is invoked, this will remove any pending observers and [onDataChanged] won't be called.
	 * If the cancellation value is already true when invoked, nothing will be executed.
	 */
	fun multiObserve(sources: Collection<ObservableData<out Any>>,
					 cancellation: Cancellation? = null,
					 onDataChanged: () -> Unit) {
		if (cancellation?.isCancelled == true) return
		val sourcesObserver: Observer<Any> = { onDataChanged.invoke() }
		sources.forEach { it.observe(sourcesObserver) }
		cancellation?.onCancelled { sources.forEach { it.removeObserver(sourcesObserver) } }
	}

	/**
	 * Maps from a collection of ObservableData to a new ObservableData.
	 * The lambda will receive the data current value, this can be used to help processing complex data.
	 * When the (optional) [Cancellation.cancel] is invoked, this will remove any pending observers and the [mapper] won't be called.
	 * If the cancellation value is already true when invoked, nothing will be executed and the returned result value won't change.
	 */
	fun <T> multiMap(sources: Collection<ObservableData<out Any>>,
					 cancellation: Cancellation? = null,
					 mapper: (T?) -> T?): ObservableData<T> {
		val result = MediatorObservableData<T>()
		if (cancellation?.isCancelled == true) return result
		val observer: Observer<Any> = {
			result.value = mapper.invoke(result.value)
		}
		sources.forEach {
			result.addSource(it, observer)
		}
		cancellation?.onCancelled {
			sources.forEach {
				result.removeSource(it)
			}
		}
		return result
	}

	fun <T> distinct(source: ObservableData<T>): ObservableData<T> {
		val result = MediatorObservableData<T>()
		result.addSource(source) { newValue ->
			if (!newValue.superEquals(result.value)) {
				result.value = newValue
			}
		}
		return result
	}

	private fun <T> Collection<T>.superEqualsCollection(other: Collection<T>): Boolean {
		if (this === other) return true
		if (this.size != other.size) return false

		if (!this.containsAll(other)) return false
		if (!other.containsAll(this)) return false

		val iteratorThis = this.iterator()
		val iteratorOther = other.iterator()

		while (iteratorThis.hasNext()) {
			val v1 = iteratorThis.next()
			val v2 = iteratorOther.next()
			if (!v1.superEquals(v2)) return false
		}
		return true
	}

	private fun <T> List<T>.superEqualsList(other: List<T>): Boolean {
		if (this === other) return true // I guess exact same instance is the fastest
		if (size != other.size) return false

		for (i in indices) {
			val v1 = this[i]
			val v2 = other[i]
			if (!v1.superEquals(v2)) return false
		}
		return true
	}

	internal fun <T> T.superEquals(other: T): Boolean {
		if (this === other) return true
		if (this == null || other == null) {
			return false
		}

		return when {
			this is Array<*> && other is Array<*> -> (this.contentDeepEquals(other))
			this is List<*> && other is List<*> -> (this.superEqualsList(other))
			this is Collection<*> && other is Collection<*> -> (this.superEqualsCollection(other))
			this is ByteArray && other is ByteArray -> (this.contentEquals(other))
			this is ShortArray && other is ShortArray -> (this.contentEquals(other))
			this is IntArray && other is IntArray -> (this.contentEquals(other))
			this is LongArray && other is LongArray -> (this.contentEquals(other))
			this is FloatArray && other is FloatArray -> (this.contentEquals(other))
			this is DoubleArray && other is DoubleArray -> (this.contentEquals(other))
			this is CharArray && other is CharArray -> (this.contentEquals(other))
			this is BooleanArray && other is BooleanArray -> (this.contentEquals(other))
			else -> (this == other)
		}
	}

}