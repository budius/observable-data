package com.sensorberg.observable

open class ObservableDataWrapper<T> : ObservableData<T>() {

	private var source: ObservableData<T>? = null
	private val observer: Observer<T> = { onNewValueReceived(it) }

	fun wrap(source: ObservableData<T>?) {
		execute {
			if (this.source == source) {
				return@execute
			}
			drop()
			this.source = source
			if (hasObservers()) {
				this.source?.observe(observer)
			}
		}
	}

	fun drop() {
		execute {
			source?.removeObserver(observer)
			source = null
		}
	}

	override fun onActive() {
		this.source?.observe(observer)
	}

	override fun onInactive() {
		source?.removeObserver(observer)
	}

	protected open fun onNewValueReceived(t: T?) {
		value = t
	}
}