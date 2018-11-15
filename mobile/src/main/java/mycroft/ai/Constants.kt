/*
 *  Copyright (c) 2017. Mycroft AI, Inc.
 *
 *  This file is part of Mycroft-Android a client for Mycroft Core.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package mycroft.ai

class Constants {
    object MycroftMobileConstants {
        //how to properly call constants in kotlin?
        const val VERSION_NAME_PREFERENCE_KEY = "versionName"
        const val VERSION_CODE_PREFERENCE_KEY = "versionCode"
        const val BE_A_BEACON_PREFERENCE_KEY = "beABeaconSwitch"
        const val LOCATION_PERMISSION_PREFERENCE_KEY = "locationPermissionGranted"
        const val PERMISSION_REQUEST_COARSE_LOCATION = 1
    }
}
