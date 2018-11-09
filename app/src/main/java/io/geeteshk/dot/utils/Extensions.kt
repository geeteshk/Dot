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

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import io.geeteshk.dot.utils.Constants.Companion.SPACE_PATTERN
import io.geeteshk.dot.utils.Constants.Companion.englishToMorseCode

/** Removes any spaces with length > 1 in a TextInputEditText */
fun TextInputEditText.removeExtraWhitespace() {
    setText(text.toString().trim().replace(SPACE_PATTERN, " "))
}

/** Converts a String to morse code by referencing map */
fun String.toMorse(): String {
    val builder = StringBuilder()
    forEach {
        builder.append(englishToMorseCode[it.toUpperCase()])
        builder.append(" ")
    }

    return builder.toString().trim()
}

/** Converts an Editable to morse code by referencing map */
fun Editable.toMorse(): String {
    val builder = StringBuilder()
    forEach {
        builder.append(englishToMorseCode[it.toUpperCase()])
        builder.append(" ")
    }

    return builder.toString().trim()
}

/** Extension to make enabling views look cleaner */
fun View.enable() {
    isEnabled = true
}

/** Extension to make disabling views look cleaner */
fun View.disable() {
    isEnabled = false
}

/** Extension to show/hide FloatingActionButton based on a condition */
fun FloatingActionButton.display(visible: Boolean) {
    if (visible) show() else hide()
}

/** Kotlin DSL implementation of TextWatcher.onTextChanged on EditText*/
fun EditText.onTextChanged(onTextChanged: (String) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(p0: Editable?) {}
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            onTextChanged.invoke(p0.toString())
        }
    })
}