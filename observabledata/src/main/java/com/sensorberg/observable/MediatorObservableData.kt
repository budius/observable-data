package com.sensorberg.observable

open class MediatorObservableData<T> : MutableObservableData<T>() {

	private val sources = mutableMapOf<ObservableData<*>, Source<*>>()

	fun <S> addSource(source: ObservableData<S>, observer: Observer<S>) {
		execute {
			if (sources.containsKey(source)) {
				throw IllegalArgumentException("This source was already added with the different observer")
			} else {
				val newSource = Source(source, observer)
				sources[source] = newSource
				if (hasObservers()) {
					newSource.plug()
				}
			}
		}
	}

	fun <S> removeSource(source: ObservableData<S>) {
		execute {
			sources.remove(source)?.unplug()
		}
	}

	override fun onActive() {
		sources.forEach { it.value.plug() }
	}

	override fun onInactive() {
		sources.forEach { it.value.unplug() }
	}

	private class Source<T>(val source: ObservableData<T>, val observer: Observer<T>) {
		fun plug() {
			source.observe(observer)
		}

		fun unplug() {
			source.removeObserver(observer)
		}
	}

}