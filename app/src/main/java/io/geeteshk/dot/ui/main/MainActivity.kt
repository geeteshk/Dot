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
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.florent37.runtimepermission.kotlin.askPermission
import io.geeteshk.dot.R
import io.geeteshk.dot.ui.fragment.SendFragment
import io.geeteshk.dot.ui.fragment.view.RestoreStateFragment
import io.geeteshk.dot.utils.Constants.Companion.RC_SETTINGS
import io.geeteshk.dot.utils.showRationale
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_main.*

/** Activity where everything goes down */
class MainActivity : AppCompatActivity() {

    /** Called when activity is created, we can request permissions here */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
            showRationale(it, this, rootLayout)
        }
    }

    /** Sets up the tabs and pager */
    private fun setupUI() {
        val viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)
        viewModel.currentFragment.observe(this, Observer {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, it)
                    .commit()
        })

        bottomNavigation.setOnNavigationItemSelectedListener {
            viewModel.currentFragment.value = when (it.itemId) {
                R.id.action_send -> SendFragment()
                else -> RestoreStateFragment()
            }

            true
        }

        bottomNavigation.setOnNavigationItemReselectedListener {
            viewModel.currentFragment.value?.restore()
        }

        viewModel.currentFragment.value = SendFragment()
        bottomNavigation.selectedItemId = R.id.action_send
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
}
