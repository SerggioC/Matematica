package com.sergiocruz.MatematicaPro.model

import androidx.cardview.widget.CardView
import java.math.BigInteger
import java.util.*

data class InputTags(
        val input: String?,
        val operation: String?
)

class MyTags(
        internal var cardView: CardView,
        internal var longNumbers: ArrayList<Long>,
        internal var resultMDC: BigInteger?,
        internal var hasExplanation: Boolean?,
        internal var hasBGOperation: Boolean?,
        internal var texto: String,
        internal var bGfatores: ArrayList<ArrayList<Long>>?,
        internal var taskNumber: Int
)