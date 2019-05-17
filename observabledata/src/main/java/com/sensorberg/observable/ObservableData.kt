package com.sensorberg.observable

import androidx.lifecycle.LiveData
import com.sensorberg.executioner.Executioner
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.atomic.AtomicReference

@SuppressWarnings("UnnecessaryAbstractClass")
abstract class ObservableData<T> {

	private var dataVersion = 0L
	private val observers = Collections.synchronizedMap(mutableMapOf<Observer<T>, ObserverHolder<T>>())
	private val reference = AtomicReference<Any>(NOT_SET)
	private val pending = AtomicReference<Any>(NOT_SET)
	private val exportLiveData: LiveData<T> by lazy { ExportLiveData(this) }

	//region Base implementation
	open var value: T?
		get() = internalGetValue()
		protected set(value) = internalSetValue(value)

	private fun internalGetValue(): T? {
		return reference.get().let {
			if (it == NOT_SET) {
				null
			} else {
				it as T
			}
		}
	}

	private fun internalSetValue(value: T?) {
		pending.set(value)
		execute {
			pending.getAndSet(NOT_SET).let {
				if (it != NOT_SET) {
					dataVersion++
					reference.set(it)
					onSetValue(value)
					val snapshot = observers.map { pair -> pair.value }
					snapshot.forEach { observer -> observer.considerDispatch(it as T, dataVersion) }
				}
			}
		}
	}

	fun observe(observer: Observer<T>) {
		execute {
			val holder = ObserverHolder<T>(observer)
			observers[observer] = holder
			if (observers.size == 1) {
				// it's very important that onActive() be called AFTER observer have been added to the list
				// that way calls to hasObservers() returns true from onActive()
				// also, if value is changed from inside onActive(), only this new value gets propagated
				onActive()
			}
			reference.get().let {
				if (it != NOT_SET) {
					holder.considerDispatch(it as T, dataVersion)
				}
			}
		}
	}

	fun removeObserver(observer: Observer<T>) {
		execute {
			val isNotEmpty = observers.isNotEmpty()
			observers.remove(observer)
			if (isNotEmpty && observers.isEmpty()) {
				onInactive()
			}
		}
	}

	protected open fun onActive() {}

	protected open fun onInactive() {}

	protected open fun onSetValue(t: T?) {}

	fun hasObservers(): Boolean {
		return observers.isNotEmpty()
	}

	fun toLiveData(): LiveData<T> {
		return exportLiveData
	}
	//endregion

	private class ObserverHolder<T>(val observer: Observer<T>) {
		var dataVersion = 0L
		fun considerDispatch(t: T?, dataVersion: Long) {
			if (dataVersion > this.dataVersion) {
				this.dataVersion = dataVersion
				observer.invoke(t)
			}
		}
	}

	companion object {
		private val executor: Executor = Executioner.SINGLE
		@Volatile private var thread: Thread? = null

		init {
			// grabs reference to the executor thread
			// this allows the `execute` method to run sequentially when already on the right thread
			executor.execute { thread = Thread.currentThread() }
		}

		internal fun isExecutorThread(): Boolean {
			return Thread.currentThread() == thread
		}

		internal fun execute(runnable: () -> Unit) {
			if (isExecutorThread()) {
				runnable.invoke()
			} else {
				executor.execute(runnable)
			}
		}

		private val NOT_SET = Any()
	}
}

typealias Observer<T> = ((T?) -> Unit)