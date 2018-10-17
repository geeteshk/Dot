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
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModelProviders
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import io.geeteshk.dot.R
import io.geeteshk.dot.databinding.ActivityMainBinding
import io.geeteshk.dot.utils.*
import io.geeteshk.dot.utils.Constants.Companion.DOT_DELAY
import io.geeteshk.dot.utils.Constants.Companion.RC_SETTINGS
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_main.*

/** Activity where everything goes down */
class MainActivity : AppCompatActivity() {

    // We have a ViewModel so our data can survive configuration changed
    private lateinit var viewModel: MainViewModel

    // Thread to perform morse code flashing
    private lateinit var flashThread: Thread

    // Utility class for managing our spans
    private lateinit var spanner: Spanner

    // Utility class for enabling and disabling the flashlight
    private lateinit var flashlight: Flashlight

    // Our current state
    private var isFlashing = false

    /** Called when activity is created, we can request permissions here */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize our utilities
        spanner = Spanner(this)
        flashlight = Flashlight(this)

        requestPermissions()
    }

    /**
     * Simple function to request user runtime permissions.
     * The RuntimePermission library lets us use Kotlin DSL to make
     * requesting permissions look cleaner
     */
    private fun requestPermissions() {
        askPermission {
            // Our permissions are granted
            setupUI()
        }.onDeclined {
            // Permissions are denied, re-prompt the user
            showRationale(it, this, mainLayout)
        }
    }

    /** Abstracts all the different setups into one function */
    private fun setupUI() {
        setupArchitecture()
        setupMorseInput()
    }

    /**
     * Sets up our ViewModel, MainActivityBinding and adds
     * the observer to our Lifecycle
     */
    private fun setupArchitecture() {
        // Initialize our ViewModel and our Spannable so that it is not null
        viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.initSpannable(this)

        // Setup our data binding and set the correct lifecycle owner
        // so we can observe LiveData properly
        val binding: ActivityMainBinding = DataBindingUtil.setContentView(
                this, R.layout.activity_main)
        binding.setLifecycleOwner(this)
        binding.viewModel = viewModel

        // We attach the observer this way because we only
        // need this once permissions are given
        lifecycle.addObserver(StateObserver())

        // Make our layout visible so the user can begin
        subLayout.visibility = View.VISIBLE

        // Make sure we use vector drawables
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        // Ensure FloatingActionButton is hidden on starting
        Handler().postDelayed({
            flashControl.hide()
        }, 100)

        // Setup OnLongClickListener to show licenses
        flashControl.setOnLongClickListener {
            startActivity(Intent(this, OssLicensesMenuActivity::class.java))
            true
        }
    }

    /**
     * Starts listening for changes in the input and sets
     * the value in the ViewModel on a change
     */
    private fun setupMorseInput() {
        // Thank you Kotlin DSL
        morseInput.onTextChanged {
            // This check is here so we don't alter the output while we
            // are flashing due to all the spans being thrown around
            if (morseInput.isEnabled) {
                // Hide the FloatingActionButton when there's no text
                flashControl.display(!it.isBlank())

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
    }

    /**
     * onClick function for flashControl that changes the state
     * and kicks off the thread
     */
    fun flashControlOnClick(@Suppress("unused_parameter") view: View) {
        flashControl.setImageResource(if (isFlashing) R.drawable.ic_flashlight else R.drawable.ic_stop)
        if (isFlashing) stopFlashing() else startFlashing()
    }

    /** Used to inject our custom font into the activity */
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(base!!))
    }

    /**
     * Necessary for when the user denies permissions forever
     * and we return from the app's Settings page
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SETTINGS) requestPermissions()
    }

    /** Sets up and begins flashing the input string as morse code */
    private fun startFlashing() {
        // Update state
        isFlashing = true

        // Make all spaces in input single spaced and prepare
        // LiveData causes output to update automatically
        morseInput.removeExtraWhitespace()
        morseInput.disable()

        // Kick off our flashing thread and sit back
        flashThread = Thread(MessageRunnable(morseInput.text!!.toMorse()))
        flashThread.start()
    }

    /** Cleans up and stops flashing */
    private fun stopFlashing() {
        // Update state
        isFlashing = false

        // Allow user to enter input again
        morseInput.enable()

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
            flashControl.setImageResource(R.drawable.ic_flashlight)
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
                runOnUiThread {
                    spanner.addCharSpans(morseInput.text, numSpaces)
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
                            runOnUiThread {
                                spanner.removeSpans(morseInput.text)
                                spanner.addCharSpans(morseInput.text, numSpaces)
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
                    flashlight.flashAndWait(false, DOT_DELAY)
                }

                // We have finished looping so we can cleanup
                runOnUiThread {
                    // Restore the FloatingActionButton
                    flashControl.setImageResource(R.drawable.ic_flashlight)

                    // Ensure we stop flashing and update our state
                    stopFlashing()

                    // Clear away our spans
                    spanner.removeSpans(viewModel.currentSpannable)
                    spanner.removeSpans(morseInput.text)
                }
            } catch (ie: InterruptedException) {
                // This is supposed to happen when the FloatingActionButton
                // is clicked but the Thread may be interrupted due to other reasons?
                runOnUiThread {
                    // Ensure the flashlight is off
                    flashlight.flash(false)

                    // Clear away our spans
                    spanner.removeSpans(viewModel.currentSpannable)
                    spanner.removeSpans(morseInput.text)
                }
            }
        }

        /** Utility to get how long to wait for a given character */
        private fun getDelay(char: Char): Int {
            return when (char) {
                '.' -> DOT_DELAY
                '-' -> DOT_DELAY * 3
                ' ' -> {
                    // Since we return on space we need to delay here
                    delay(DOT_DELAY)

                    -1
                }

                else -> -1
            }
        }
    }
}
