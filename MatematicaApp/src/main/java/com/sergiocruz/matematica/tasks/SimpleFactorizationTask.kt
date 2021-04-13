package com.sergiocruz.matematica.tasks

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.util.Log
import com.sergiocruz.matematica.helper.delayedTimerAsync
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import java.math.BigInteger

// Asynctask <Params, Progress, Result>
open class SimpleFactorizationTask(
        private val numbersToFactorize: List<BigInteger>,
        private val updateProgress: (percent: List<Float>) -> Unit,
        private val onFinished: ((data: List<List<BigInteger>>?) -> Boolean)? = null,
) : AsyncTask<Void, Float, List<List<BigInteger>>>() {

    private var timerJob: Job = Job()

    @SuppressLint("LogNotTimber")
    override fun doInBackground(vararg voids: Void): List<List<BigInteger>> {
        val fatores = ArrayList<ArrayList<BigInteger>>()

        val numbersSize = numbersToFactorize.size
        for (i in 0 until numbersSize) {
            var progress: Float
            val fatoresIx = ArrayList<BigInteger>()

            var number = numbersToFactorize[i]
            if (number == BigInteger.ONE) {
                fatoresIx.add(BigInteger.ONE)
            }
            while (number % BigInteger.valueOf(2) == BigInteger.ZERO) {
                fatoresIx.add(BigInteger.valueOf(2))
                number /= BigInteger.valueOf(2)
            }

            var j: BigInteger = BigInteger.valueOf(3)

            delayedTimerAsync(repeatMillis = 1000, job = timerJob) {
                progress = try {
                    (j.toBigDecimal(scale = 2) / (number.toBigDecimal(scale = 2) / j.toBigDecimal(scale = 2))).toFloat()
                } catch (e: Exception) {
                    0f
                }
                updateProgress(listOf(progress, i.toFloat()))
            }
            timerJob.start()

            while (j <= number / j) {
                if (isCancelled) break
                while (number % j == BigInteger.ZERO) {
                    fatoresIx.add(j)
                    number /= j
                }
                j += BigInteger.valueOf(2)
            }
            if (number > BigInteger.ONE) {
                fatoresIx.add(number)
            }

            fatores.add(fatoresIx)
        }
        timerJob.cancel()
        return fatores
    }

    @SuppressLint("LogNotTimber")
    override fun onPostExecute(result: List<List<BigInteger>>?) {
        timerJob.cancel()
        onFinished?.invoke(result)
        Log.i("TAG", "onPostExecute SimpleFactorizationTask isActive: ${timerJob?.isActive}")
    }

    override fun onCancelled(result: List<List<BigInteger>>?) {
        timerJob.cancel()
        onFinished?.invoke(result)
    }
}