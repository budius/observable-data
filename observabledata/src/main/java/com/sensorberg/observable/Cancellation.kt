package com.sensorberg.observable

class Cancellation {

	private val wrappers = mutableMapOf<(() -> Unit), CallbackWrapper>()
	private val data = MutableObservableData<Boolean>()

	val isCancelled: Boolean
		get() = data.value == true

	fun onCancelled(callback: () -> Unit) {
		val wrapper = CallbackWrapper(callback)
		wrappers[callback] = wrapper
		data.observe(wrapper.observer)
	}

	fun removeCallback(callback: () -> Unit) {
		val wrapper = wrappers.remove(callback)
		wrapper?.let { data.removeObserver(it.observer) }
	}

	fun cancel() {
		data.value = true
	}

	private inner class CallbackWrapper(val callback: () -> Unit) {
		val observer = object : Observer<Boolean> {
			override fun invoke(t: Boolean?) {
				if (t == true) {
					this@Cancellation.data.removeObserver(this)
					callback.invoke()
				}
			}
		}
	}
}