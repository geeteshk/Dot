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
import android.hardware.camera2.CameraManager

/** Utility class to control device Flashlight */
class Flashlight(private var context: Context) {

    // Camera service
    private val camera: CameraManager
        get() = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager

    /** Enables/disables device flash */
    fun flash(enabled: Boolean) {
        // First camera is most likely to be main camera
        val cameraId = camera.cameraIdList[0]
        camera.setTorchMode(cameraId, enabled)
    }

    /** Updates device flash and sleeps thread for given time */
    fun flashAndWait(enabled: Boolean, period: Int) {
        flash(enabled)
        delay(period)
    }
}