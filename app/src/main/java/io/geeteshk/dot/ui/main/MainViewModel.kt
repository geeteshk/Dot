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

package io.geeteshk.dot.ui.main

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.geeteshk.dot.R

/** ViewModel for MainActivity */
class MainViewModel : ViewModel() {

    /** Spannable LiveData that is used to update the output */
    lateinit var currentSpannable: MutableLiveData<Spannable>

    /** Used to make sure we don't have a null value as default */
    fun initSpannable(context: Context) {
        currentSpannable = MutableLiveData<Spannable>().apply {
            postValue(SpannableString(context.getString(R.string.text_prompt)))
        }
    }
}