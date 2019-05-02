package com.sensorberg.observable

class Cancellation {

	private val data = MutableObservableData<Boolean>()

	val isCancelled: Boolean
		get() = data.value == true

	internal val source: ObservableData<Boolean>
		get() = data

	fun onCancelled(callback: () -> Unit) {
		data.observe(object : Observer<Boolean> {
			override fun invoke(t: Boolean?) {
				if (t == true) {
					data.removeObserver(this)
					callback.invoke()
				}
			}
		})
	}

	fun cancel() {
		data.value = true
	}
}