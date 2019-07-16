package com.sensorberg.observable

import androidx.lifecycle.LiveData

internal class ExportLiveData<T>(private val source: ObservableData<T>) : LiveData<T>() {

	override fun getValue(): T? {
		return source.value
	}

	override fun onActive() {
		source.observe(observer)
	}

	override fun onInactive() {
		source.removeObserver(observer)
	}

	private val observer: Observer<T> = { postValue(it) }

}