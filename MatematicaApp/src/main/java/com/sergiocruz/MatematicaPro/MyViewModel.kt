package com.sergiocruz.MatematicaPro

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.sendBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.system.measureNanoTime
import kotlin.system.measureTimeMillis


/** Kotlin coroutines tests file */
class MyViewModel : ViewModel(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    init {
        refresh()
    }

    private fun refresh() {
        launch(coroutineContext) {
            // Do stuff on UI thread
        }

        val launchScope: Job = launch(Dispatchers.Default) {
            val str1: Deferred<Unit> = async { testThis() }
            val str2: Deferred<String> = async { dothis() }
            val theStr1: Unit = str1.await()
            val theStr2: String = str2.await()

            print("The String 1 is $theStr1 and the string 2 is $theStr2")
        }

    }

    private suspend fun dothis(): String {
        lateinit var result: String
        withContext(coroutineContext) {
            result = "Some string that takes time to compute"
        }
        return result
    }

    fun getMyLastLocation(): Location? {
        lateinit var myLocation: Location
        launch(Dispatchers.Default) {
            val myDeferedLocation: Deferred<Location> = async { getLocation() }
            myLocation = myDeferedLocation.await()
        }
        return myLocation
    }

    private suspend fun getLocation(): Location {
        return suspendCoroutine { continuation ->
            FusedLocationProviderClient(getMyActivity()).lastLocation.addOnCompleteListener { task: Task<Location> ->
                if (task.isSuccessful) {
                    continuation.resume(task.result!!)
                } else {
                    continuation.resumeWithException(task.exception!!)
                }
            }
        }
    }


    private fun CoroutineScope.downloader(
        references: ReceiveChannel<String>,
        sendChanel: SendChannel<String>
    ) {
        launch {
            val requested = mutableSetOf<String>()
            for (reference: String in references) {
                if (requested.add(reference)) {
                    sendChanel.send(reference)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(): LocationRequest {
        val request: LocationRequest = LocationRequest().setSmallestDisplacement(10f) // 10m

        val channel = Channel<Location>()

        val callBack = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                channel.sendBlocking(locationResult.lastLocation)
            }
        }
        val locationClient = FusedLocationProviderClient(getMyActivity())
        locationClient.requestLocationUpdates(
            request,
            callBack,
            Looper.myLooper()
        )

        channel.invokeOnClose {
            locationClient.removeLocationUpdates(callBack)
        }
        return getLocationUpdates()
    }


    private fun getMyActivity(): Activity {
        return Activity()
    }

    private suspend fun someFun(): Unit {
        val time: Long = measureTime { }

    }


    override fun onCleared() {
        super.onCleared()
        job.cancel()
    }


    private suspend fun testThis() {
        val job: Job = Job()
        val scope: CoroutineScope = CoroutineScope(job + Dispatchers.Default)
        val deferred = scope.async {
            "Async Done!"
        }
        val result: String = deferred.await()
        Log.i("Sergio> ", "result: $result")

        lateinit var cena: Deferred<String>
        lateinit var cena2: Deferred<String>

        cena = scope.async { "Cena" }
        cena2 = scope.async { "Cena 2" }
        val str: String = cena.await()
        val str2 = cena2.await()

        measureNanoTime {  }

        measureTimeMillis {  }

        val uiScope = CoroutineScope(Dispatchers.Main)
        uiScope.launch {
            // do something on UI
        }

        val te: String = withContext(Dispatchers.IO) {
            "Async With Context Done!"
        }

        val yalor: Int = PrefetchSize.MediumSize.value
        val valor2: DeliveryStatus = DeliveryStatus.NotDelivered("fuck")
        val valor3 = AnotherEnum.Open

        val valor4: Endpoint = Endpoint.Production
        valor4.getUrl()

    }

    //receives a function that returns String and returns the String
    private suspend fun measureTime(function: () -> Unit): Long {
        val startTime = System.currentTimeMillis()
        function()
        return System.currentTimeMillis() - startTime
    }

    enum class AnotherEnum {
        Open,
        Closed
    }

    enum class PrefetchSize(val value: Int) {
        LowSize(1),
        MediumSize(2),
        HighSize(3)
    }

    sealed class DeliveryStatus {
        object Delivered : DeliveryStatus()
        object Delivering : DeliveryStatus()
        class NotDelivered(val error: String) : DeliveryStatus()
    }


    enum class Endpoint(private val path: String) {
        Production("/pinterest/v1/"),
        Debug("/pinterest/debug/"),
        Test("/pinterest/test/");

        fun getUrl(): String {
            return baseUrl + path
        }

        private companion object {
            private const val baseUrl: String = "www.myEndpoint.com"
        }
    }


}
