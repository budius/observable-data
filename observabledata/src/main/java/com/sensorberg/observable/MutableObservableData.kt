package com.sensorberg.observable

open class MutableObservableData<T> : ObservableData<T>() {
	final override var value: T?
		get() = super.value
		public set(value) {
			super.value = value
		}
}