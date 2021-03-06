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

package io.geeteshk.dot

import android.app.Application
import com.squareup.leakcanary.LeakCanary
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

/**
 * A custom application class used to inject LeakCanary for debug
 * builds and our rounded font throughout the app using Calligraphy
 */
class App : Application() {

    /**
     * Called when the Application is created
     *
     * This is where we do our installation of LeakCanary
     * and Calligraphy fonts
     */
    override fun onCreate() {
        super.onCreate()

        // LeakCanary for detecting memory leaks
        if (LeakCanary.isInAnalyzerProcess(this)) return
        LeakCanary.install(this)

        // Calligraphy to set a custom font
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/VarelaRound-Regular.otf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()
                )).build())
    }
}