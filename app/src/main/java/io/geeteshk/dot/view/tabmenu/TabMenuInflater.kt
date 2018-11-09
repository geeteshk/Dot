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

package io.geeteshk.dot.view.tabmenu

import android.content.res.Resources
import android.util.Log
import io.geeteshk.dot.R
import org.xmlpull.v1.XmlPullParser

class TabMenuInflater {

    fun inflate(resources: Resources, menu: TabMenu) {
        val xrp = resources.getXml(R.xml.menu_tabs)
        var eventType = xrp.eventType
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    val name = xrp.name
                    if (name.equals("item", true)) {
                        menu.add(resources.getString(xrp.getAttributeResourceValue(null, "title", 0)),
                                xrp.getAttributeResourceValue(null, "icon", 0))
                    }
                }
            }

            eventType = xrp.next()
        }

        xrp.close()
    }
}