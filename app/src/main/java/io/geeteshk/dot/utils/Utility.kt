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

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.github.florent37.runtimepermission.PermissionResult
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import io.geeteshk.dot.R

/** Utility function to make sleeping threads look cleaner */
fun delay(period: Int) {
    Thread.sleep(period.toLong())
}

/** Display rationales to the user if something goes wrong when asking for permissions */
fun showRationale(result: PermissionResult, activity: AppCompatActivity, layout: View) {
    // The user denied one or more permissions
    if (result.hasDenied()) {
        // Kindly ask them to reconsider
        Snackbar.make(layout, R.string.camera_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ask_again) { _ ->
                    result.askAgain()
                }
                .show()
    }

    // The user has forever denied one or more permissions
    if (result.hasForeverDenied()) {
        // Kindly nudge the user into enabling the permission by
        // opening the applications' Settings page because without
        // it most of the app functionality is gone YOU MONSTER
        Snackbar.make(layout, R.string.camera_rationale, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.action_settings) { _ ->
                    activity.startActivityForResult(Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", activity.packageName, null)
                    ), Constants.RC_SETTINGS)
                }
                .show()
    }
}