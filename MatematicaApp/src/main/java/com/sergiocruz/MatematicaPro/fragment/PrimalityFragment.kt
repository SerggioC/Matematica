package com.sergiocruz.MatematicaPro.fragment


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.sergiocruz.MatematicaPro.R
import com.sergiocruz.MatematicaPro.helper.InfoLevel
import com.sergiocruz.MatematicaPro.helper.showCustomToast
import kotlinx.android.synthetic.main.fragment_primality.*
import java.math.BigInteger

class PrimalityFragment : BaseFragment() {

    override fun loadOptionsMenus() = listOf(R.menu.menu_main, R.menu.menu_sub_main)

    override fun getLayoutIdForFragment() = R.layout.fragment_primality

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calculateButton.setOnClickListener {
            val number = inputEditText.text.toString()
            val bigNumber = number.toBigIntegerOrNull(10)
            if (bigNumber != null) {
                checkIfProbablePrime(bigNumber)
            } else {
                showCustomToast(context, "Invalid Number", InfoLevel.WARNING)
            }
        }
        clearButton.setOnClickListener {
            inputEditText.setText("")
        }
    }

    private fun checkIfProbablePrime(bigNumber: BigInteger) {
        val isPrime = bigNumber.isProbablePrime(100)
        showCustomToast(context, if (isPrime) "Prime Number!" else "Not a prime Number")
    }
}
