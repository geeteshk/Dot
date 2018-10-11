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

/** Constant values that are used throughout the app */
class Constants {
    companion object {
        /** The duration of a single dot in morse code */
        const val DOT_DELAY = 250

        /** Request code used when opening app Settings */
        const val RC_SETTINGS = 123

        /** Pattern to detect whitespaces in a string */
        val SPACE_PATTERN = "\\s+".toRegex()

        /** Map of english characters to Morse dots and dashes */
        val englishToMorseCode = mapOf(
                'A' to ".-",
                'B' to "-...",
                'C' to "-.-.",
                'D' to "-..",
                'E' to ".",
                'F' to "..-.",
                'G' to "--.",
                'H' to "....",
                'I' to "build/generated/res/rs",
                'J' to ".---",
                'K' to "-.-",
                'L' to ".-..",
                'M' to "--",
                'N' to "-.",
                'O' to "---",
                'P' to ".--.",
                'Q' to "--.-",
                'R' to ".-.",
                'S' to "...",
                'T' to "-",
                'U' to "..-",
                'V' to "...-",
                'W' to ".--",
                'X' to "-..-",
                'Y' to "-.--",
                'Z' to "--..",
                '0' to "-----",
                '1' to ".----",
                '2' to "..---",
                '3' to "...--",
                '4' to "....-",
                '5' to ".....",
                '6' to "-....",
                '7' to "--...",
                '8' to "---..",
                '9' to "----.",
                ' ' to ""
        )
    }
}