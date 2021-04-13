package com.sergiocruz.matematica.model

import android.text.*
import android.text.style.*
import androidx.cardview.widget.CardView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.*
import java.lang.reflect.Type
import java.math.BigInteger
import java.util.*

/**
 * View input tags
 * **/
data class InputTags(
        val input: String?,
        val operation: String?,
)

data class OperationStatusTags(
        var isCalculating: Boolean = false,
        var isCompleted: Boolean = false,
        var vtoIsCompleted: Boolean = false,
        var isExpanded: Boolean = false,
)

class MyTags(
        internal var cardView: CardView,
        internal var bigNumbers: ArrayList<BigInteger>,
        internal var resultMDC: BigInteger?,
        internal var hasExplanation: Boolean?,
        internal var hasBGOperation: Boolean?,
        internal var texto: String,
        internal var bGfatores: ArrayList<ArrayList<BigInteger>>?,
        internal var taskNumber: Int
)


data class FactorizationData(
        val numberToFatorize: BigInteger,
        val rawResult: ArrayList<ArrayList<BigInteger>>,
        val strResults: String,
        val ssbStrDivisores: SpannableStringBuilder,
        val ssbFatores: SpannableStringBuilder,
        val strFactExp: SpannableStringBuilder,
        val hasExpoentes: Boolean,
        val isPrime: Boolean,
)

data class PrimalityData(
        val isPrime: Boolean,
        val factorizationData: FactorizationData? = null,
)


data class MultiplesData(
        val stringMultiplos: String,
        val lastIteration: Long,
)

/** MMC e MDC **/
data class MDData(
        val inputNumbers: List<BigInteger>,
        var resultString: String? = null,
        var result: BigInteger? = null,
        var ssbFactorization: SpannableStringBuilder? = null,
        var ssbExpanded: SpannableStringBuilder? = null,
)


class SpannableSerializer : JsonSerializer<SpannableStringBuilder?>, JsonDeserializer<SpannableStringBuilder?> {

    private val gson: Gson
        get() {
            val rtaf = RuntimeTypeAdapterFactory
                    .of(ParcelableSpan::class.java, ParcelableSpan::class.java.simpleName)
                    .registerSubtype(ForegroundColorSpan::class.java, ForegroundColorSpan::class.java.simpleName)
                    .registerSubtype(StyleSpan::class.java, StyleSpan::class.java.simpleName)
                    .registerSubtype(RelativeSizeSpan::class.java, RelativeSizeSpan::class.java.simpleName)
                    .registerSubtype(SuperscriptSpan::class.java, SuperscriptSpan::class.java.simpleName)
                    .registerSubtype(UnderlineSpan::class.java, UnderlineSpan::class.java.simpleName)
            return GsonBuilder()
                    .registerTypeAdapterFactory(rtaf)
                    .create()
        }

    override fun serialize(spannableStringBuilder: SpannableStringBuilder?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
        val spanTypes = spannableStringBuilder?.getSpans(0, spannableStringBuilder.length, ParcelableSpan::class.java)
        val spanStart = IntArray(spanTypes?.size ?: 0)
        val spanEnd = IntArray(spanTypes?.size ?: 0)
        val spanFlags = IntArray(spanTypes?.size ?: 0)
        val spanInfo = DoubleArray(spanTypes?.size ?: 0)
        spanTypes?.forEachIndexed { i, span ->
            when (span) {
                is ForegroundColorSpan -> spanInfo[i] = span.foregroundColor.toDouble()
                is StyleSpan -> spanInfo[i] = span.style.toDouble()
                is RelativeSizeSpan -> spanInfo[i] = span.sizeChange.toDouble()
            }
            spanStart[i] = spannableStringBuilder.getSpanStart(span)
            spanEnd[i] = spannableStringBuilder.getSpanEnd(span)
            spanFlags[i] = spannableStringBuilder.getSpanFlags(span)
        }

        val jsonSpannable = JsonObject()
        jsonSpannable.addProperty(INPUT_STRING, spannableStringBuilder.toString())
        jsonSpannable.addProperty(SPAN_TYPES, gson.toJson(spanTypes))
        jsonSpannable.addProperty(SPAN_START, gson.toJson(spanStart))
        jsonSpannable.addProperty(SPAN_END, gson.toJson(spanEnd))
        jsonSpannable.addProperty(SPAN_FLAGS, gson.toJson(spanFlags))
        jsonSpannable.addProperty(SPAN_INFO, gson.toJson(spanInfo))
        return jsonSpannable
    }

    override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext): SpannableStringBuilder {
        try {
            val jsonSpannable = jsonElement.asJsonObject
            val spannableString = jsonSpannable[INPUT_STRING].asString
            val spannableStringBuilder = SpannableStringBuilder(spannableString)
            val spanObjectJson = jsonSpannable[SPAN_TYPES].asString
            val spanTypes: Array<ParcelableSpan> = gson.fromJson(spanObjectJson, Array<ParcelableSpan>::class.java)
            val spanStartJson = jsonSpannable[SPAN_START].asString
            val spanStart: IntArray = gson.fromJson(spanStartJson, IntArray::class.java)
            val spanEndJson = jsonSpannable[SPAN_END].asString
            val spanEnd: IntArray = gson.fromJson(spanEndJson, IntArray::class.java)
            val spanFlagsJson = jsonSpannable[SPAN_FLAGS].asString
            val spanFlags: IntArray = gson.fromJson(spanFlagsJson, IntArray::class.java)
            val spanInfoJson = jsonSpannable[SPAN_INFO].asString
            val spanInfo: DoubleArray = gson.fromJson(spanInfoJson, DoubleArray::class.java)
            for (i in spanTypes.indices) {
                when (spanTypes[i]) {
                    is ForegroundColorSpan -> spannableStringBuilder.setSpan(ForegroundColorSpan(spanInfo[i].toInt()), spanStart[i], spanEnd[i], spanFlags[i])
                    is StyleSpan -> spannableStringBuilder.setSpan(StyleSpan(spanInfo[i].toInt()), spanStart[i], spanEnd[i], spanFlags[i])
                    is RelativeSizeSpan -> spannableStringBuilder.setSpan(RelativeSizeSpan(spanInfo[i].toFloat()), spanStart[i], spanEnd[i], spanFlags[i])
                    else -> spannableStringBuilder.setSpan(spanTypes[i], spanStart[i], spanEnd[i], spanFlags[i])
                }
            }
            return spannableStringBuilder
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            return SpannableStringBuilder("")
        }
    }

    companion object {
        private const val PREFIX = "SSB:"
        private const val INPUT_STRING = PREFIX + "string"
        private const val SPAN_TYPES = PREFIX + "spanTypes"
        private const val SPAN_START = PREFIX + "spanStart"
        private const val SPAN_END = PREFIX + "spanEnd"
        private const val SPAN_FLAGS = PREFIX + "spanFlags"
        private const val SPAN_INFO = PREFIX + "spanInfo"
    }
}


