/*
 * Copyright 2018 Geetesh Kalakoti
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.geeteshk.dot.utils

import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.Spannable
import android.text.style.CharacterStyle
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import io.geeteshk.dot.R

/** Utility class for managing spans on displayed text */
class Spanner(context: Context) {

    // Accent color ForegroundColorSpan
    private var colorSpan: ForegroundColorSpan =
            ForegroundColorSpan(ContextCompat.getColor(context, R.color.colorAccent))

    // Makes text bold
    private val boldSpan = StyleSpan(Typeface.BOLD)

    /** Extension to add span to a single character in a Spannable */
    private fun Spannable.spanChar(index: Int, span: CharacterStyle) {
        setSpan(span, index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    /** Extension to add span to a single character in an Editable */
    private fun Editable.spanChar(index: Int, span: CharacterStyle) {
        setSpan(span, index, index + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    /**
     * Add the two spans to a given character inside a Spannable
     * and then posts to the LiveData
     */
    fun addCharSpans(span: MutableLiveData<Spannable>, index: Int) {
        val tmpVal = span.value!!
        tmpVal.spanChar(index, colorSpan)
        tmpVal.spanChar(index, boldSpan)
        span.postValue(tmpVal)
    }

    /** Removes the two spans from the Spannable and posts to the LiveData */
    fun removeSpans(span: MutableLiveData<Spannable>) {
        val tmpVal = span.value!!
        tmpVal.removeSpan(colorSpan)
        tmpVal.removeSpan(boldSpan)
        span.postValue(tmpVal)
    }

    /** Adds two spans to an Editable */
    fun addCharSpans(span: Editable?, index: Int) {
        span?.spanChar(index, colorSpan)
        span?.spanChar(index, boldSpan)
    }

    /** Removes the two spans from the Editable */
    fun removeSpans(span: Editable?) {
        span?.removeSpan(colorSpan)
        span?.removeSpan(boldSpan)
    }
}