# Thread-safe observable pattern and transformations
(it's really a copy of LiveData,
 but using a background thread,
 and add a few extra transformations,
 and there's a Cancellation)

[ ![Download](https://api.bintray.com/packages/sensorberg/maven/observable-data/images/download.svg) ](https://bintray.com/sensorberg/maven/observable-data/_latestVersion)

This is great to use in the repository layer,
where there's loads of data parsing and intermediate results.

### Adding to your project

```
implementation 'com.sensorberg.libs:observable-data:<latest>'
```

### Base API

It's a copy of LiveData so here we have:
- ObservableData<T>, MutableObservableData<T>, MediatorObservableData<T>, Transformations

Notes:
- All the callbacks are called on the same background thread
- There's a `Cancellation` class that can be used on several of the Transformations

### Example usages

```Kotlin
private val myListData = MutableObservableData<List<String>>()
private val myFirstData: ObservableData<String> = createData()
private val myFirstCharData: ObservableData<Char> = createChar()

// returns the first element of the list
fun createData(): ObservableData<String> {
    return Transformations.map(myListData) { it?.firstOrNull() }
}

// returns a distinct first char of the first element of the list
fun createChar(): ObservableData<Char> {
    val char = Transformations.map(myFirstData) { it?.firstOrNull() }
    return Transformations.distinct(char)
}

fun otherTransformations() {
    val cancellation = Cancellation()

    // this will only receive 1 byte and then never change the value again
    val getOneChart: ObservableData<Byte> = Transformations.mapNotNull(myFirstCharData, cancellation) { it.toByte() }

    // any data change on any of the source data will call the lambda
    Transformations.multiObserve(listOf(myListData, myFirstData)) {
        myListData.value
        myFirstData.value
    }

    // this data value will change to `null` if cancel is called
    val nullOnCancel: ObservableData<String> = Transformations.nullOnCancelled(myFirstData, cancellation)

    cancellation.cancel()

    // and loads more, check Transformations.kt
}
```

### Testing

ObservableData <3 Testing!!!

Internally ObservableData uses this for threading https://github.com/sensorberg-dev/executioner.

So add the executioner-testing dependency

```
implementation 'com.sensorberg.libs:executioner-testing:<latest>'

```

And the execution test rule
```Kotlin
@get:Rule val executionerTestRule = ExecutionerTestRule()
```

So now all your data will be propagated on the test thread,
similar to `@get:Rule val instantExecutorRule = InstantTaskExecutorRule()`