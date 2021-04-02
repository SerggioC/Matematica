package com.sergiocruz.MatematicaPro.tasks

import android.os.AsyncTask
import com.sergiocruz.MatematicaPro.helper.delayedTimerAsync
import kotlinx.coroutines.Deferred
import java.math.BigInteger

// Asynctask <Params, Progress, Result>
class SimpleFactorizationTask(
        private val numbersToFactorize: List<BigInteger>,
        private val updateProgress: (percent: List<Number?>) -> Unit,
        private val onFinished: (data: List<List<BigInteger>>?) -> Unit,
) : AsyncTask<Void, Float, List<List<BigInteger>>>() {

    private var timerProgressUpdate: Deferred<Unit>? = null

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

            timerProgressUpdate = delayedTimerAsync(repeatMillis = 1000) {
                progress = try {
                    (j.toBigDecimal(scale = 2) / (number.toBigDecimal(scale = 2) / j.toBigDecimal(scale = 2))).toFloat()
                } catch (e: Exception) {
                    0f
                }
                updateProgress(listOf(progress, i))
            }
            timerProgressUpdate?.start()

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
        return fatores
    }

    override fun onPostExecute(result: List<List<BigInteger>>?) {
        timerProgressUpdate?.cancel()
        onFinished(result)
    }
}