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

package io.geeteshk.dot.ui.fragment

import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProviders
import io.geeteshk.dot.R
import io.geeteshk.dot.databinding.FragmentSignalBinding
import io.geeteshk.dot.ui.fragment.view.RestoreStateFragment
import io.geeteshk.dot.utils.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_signal.*
import kotlinx.android.synthetic.main.fragment_signal.view.*

class SignalFragment : RestoreStateFragment() {

    // We have a ViewModel so our data can survive configuration changes
    private lateinit var viewModel: SignalViewModel

    // Thread to perform morse code flashing
    private lateinit var flashThread: Thread

    // Utility class for managing our spans
    private lateinit var spanner: Spanner

    // Utility class for enabling and disabling the flashlight
    private lateinit var flashlight: Flashlight

    // Our current state
    private var isFlashing = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Initialize our utilities
        spanner = Spanner(activity!!)
        flashlight = Flashlight(activity!!)

        // Initialize our ViewModel and our Spannable so that it is not null
        viewModel = ViewModelProviders.of(this).get(SignalViewModel::class.java)
        viewModel.initSpannable(activity!!)

        // Setup our data binding and set the correct lifecycle owner
        // so we can observe LiveData properly
        val binding = FragmentSignalBinding.inflate(
                inflater, container, false)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        val rootView = binding.root

        // We attach the observer this way because we only
        // need this once permissions are given
        lifecycle.addObserver(StateObserver())

        // Make our layout visible so the user can begin
        rootView.mainLayout.visibility = View.VISIBLE

        // Ensure FloatingActionButton is hidden on starting
        Handler().postDelayed({
            activity?.fab?.hide()
        }, 100)

        // Set a listener for our FloatingActionButton to update the state
        activity?.fab?.setOnClickListener {
            if (isFlashing) {
                stopFlashing()
            } else {
                activity!!.fab.setImageResource(R.drawable.ic_stop)

                // Update state
                isFlashing = true

                // Make all spaces in input single spaced and prepare
                // LiveData causes output to update automatically
                rootView.morseInput.removeExtraWhitespace()
                rootView.morseInput.disable()

                // Kick off our flashing thread and sit back
                flashThread = Thread(MessageRunnable(rootView.morseInput.text!!.toMorse()))
                flashThread.start()
            }
        }

        // When we scroll down hide the fab so the user can see
        // what is behind it, scrolling up shows the fab again
        rootView.morseOutputContainer.setOnScrollChangeListener { _: View, _: Int, scrollY: Int, _: Int, oldScrollY: Int ->
            activity!!.fab.display(scrollY <= oldScrollY)
        }

        // Workaround for actionDone to work correctly
        rootView.morseInput.setRawInputType(rootView.morseInput.inputType
                and(EditorInfo.TYPE_TEXT_FLAG_MULTI_LINE.inv()))

        // Thank you Kotlin DSL
        rootView.morseInput.onTextChanged { it: String ->
            // This check is here so we don't alter the output while we
            // are flashing due to all the spans being thrown around
            if (rootView.morseInput.isEnabled) {
                // Hide the FloatingActionButton when there's no text
                activity!!.fab.display(!it.isBlank())

                // Set the value in our ViewModel
                // An empty string will display our default text
                viewModel.currentSpannable.value = SpannableString(
                        if (it.isBlank()) {
                            getString(R.string.text_prompt)
                        } else {
                            it.toMorse()
                        }
                )
            }
        }

        return rootView
    }

    override fun restore() {
        if (isFlashing) stopFlashing()
        view?.morseInput?.requestFocus()
    }

    private fun stopFlashing() {
        activity?.fab?.setImageResource(R.drawable.ic_flashlight)

        // Update state
        isFlashing = false

        // Allow user to enter input again
        view?.morseInput?.enable()

        // Kill our flashing thread if it's still running
        if (flashThread.isAlive)
            flashThread.interrupt()

        // Ensure our flashlight has been disabled
        flashlight.flash(false)
    }

    /**
     * A LifeCycleObserver that ensures our program looks and behaves
     * correctly with lifecycle changes i.e. ensure the right
     * FloatingActionButton icon is set and ensure we don't flash when
     * the user leaves the app
     */
    @Suppress("unused")
    inner class StateObserver : LifecycleObserver {

        /** On resuming we reset the drawable image to its correct icon */
        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume() {
            activity?.fab?.setImageResource(R.drawable.ic_flashlight)
        }

        /** On pausing make sure we stop flashing */
        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause() {
            if (isFlashing) stopFlashing()
        }
    }

    /**
     * A Runnable that is run by our flashing Thread that takes care of
     * flashing the morse code with the correct timings and delays. There
     * is probably a more optimal way of doing this
     */
    inner class MessageRunnable(private var currentString: String) : Runnable {

        // Number of spaces that we have passed
        // Used to track the position of the input span
        private var numSpaces = 0

        // Tracks the last character seen
        // Used to ensure that we update input span correctly
        private var prevChar = '.'

        /** Begin flashing in here, dirty stuff here, look away */
        override fun run() {
            try {
                // Begin by setting up our input span on first char
                activity!!.runOnUiThread {
                    spanner.addCharSpans(view!!.morseInput.text, numSpaces)
                }

                // Loops through each character in our string
                // while keeping track of the index
                currentString.forEachIndexed { index, element ->
                    // Some logic to track the input span in pace with the output span
                    if (prevChar == ' ') {
                        // We just passed a space so we go up
                        numSpaces++

                        // We have reached a new morse letter so we can push the
                        // input span forward while skipping any extra spaces in the
                        // original string
                        if (element != ' ') {
                            activity!!.runOnUiThread {
                                spanner.removeSpans(view!!.rootView.morseInput.text)
                                spanner.addCharSpans(view!!.rootView.morseInput.text, numSpaces)
                            }
                        }
                    }

                    // Update our last character
                    prevChar = element

                    // Get the delay we'll be sleeping for
                    val delay = getDelay(element)

                    // If it's a space or unknown character we ignore it
                    if (delay == -1) return@forEachIndexed

                    // Update the spans and enable the flash for the respective delay
                    spanner.addCharSpans(viewModel.currentSpannable, index)
                    flashlight.flashAndWait(true, delay)

                    // Clear the spans and wait a 'dot length' before
                    // moving onto the next character
                    spanner.removeSpans(viewModel.currentSpannable)
                    flashlight.flashAndWait(false, Constants.DOT_DELAY)
                }

                // We have finished looping so we can cleanup
                activity!!.runOnUiThread {
                    // Restore the FloatingActionButton
                    activity!!.fab.setImageResource(R.drawable.ic_flashlight)

                    // Ensure we stop flashing and update our state
                    stopFlashing()

                    // Clear away our spans
                    spanner.removeSpans(viewModel.currentSpannable)
                    spanner.removeSpans(view!!.morseInput.text)
                }
            } catch (ie: InterruptedException) {
                // This is supposed to happen when the FloatingActionButton
                // is clicked but the Thread may be interrupted due to other reasons?
                activity!!.runOnUiThread {
                    // Ensure the flashlight is off
                    flashlight.flash(false)

                    // Clear away our spans
                    spanner.removeSpans(viewModel.currentSpannable)
                    spanner.removeSpans(view!!.morseInput.text)
                }
            }
        }

        /** Utility to get how long to wait for a given character */
        private fun getDelay(char: Char): Int {
            return when (char) {
                '.' -> Constants.DOT_DELAY
                '-' -> Constants.DOT_DELAY * 3
                ' ' -> {
                    // Since we return on space we need to delay here
                    delay(Constants.DOT_DELAY)

                    -1
                }

                else -> -1
            }
        }
    }
}