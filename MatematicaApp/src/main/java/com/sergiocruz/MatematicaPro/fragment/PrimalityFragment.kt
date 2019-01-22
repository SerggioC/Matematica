package com.sergiocruz.MatematicaPro.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.helper.showCustomToast
import kotlinx.android.synthetic.main.fragment_primality.*
import java.math.BigInteger

/**
 * A simple [Fragment] subclass.
 * Use the [PrimalityFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class PrimalityFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_primality, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPrime.setOnClickListener {
            val number = editNum.text.toString()
            val bigNumber = number.toBigIntegerOrNull(10)
            if (bigNumber != null) {
                checkIfProbablePrime(bigNumber)
            } else {
                showCustomToast(context, "Invalid Number")
            }
        }
        btnClearText.setOnClickListener {
            editNum.setText("")
        }
    }

    private fun checkIfProbablePrime(bigNumber: BigInteger) {
        val isPrime = bigNumber.isProbablePrime(100)
        showCustomToast(context, if (isPrime) "Prime Number!" else "Not a prime")
    }
}
