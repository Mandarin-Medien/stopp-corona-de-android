package de.schwerin.stoppCoronaDE.constants

import android.os.Build
import de.schwerin.stoppCoronaDE.BuildConfig

/**
 * This file contains extensions depending on the build.
 */

/**
 * Global property indicating if application is debuggable.
 */
val isDebug = BuildConfig.DEBUG

/**
 * Global property indicating if application is running in emulator device.
 */
val isEmulator = Build.FINGERPRINT?.contains("generic") ?: false

/**
 * Global property indicating if is a beta application.
 */
val isBeta = BuildConfig.BUILD_TYPE == "beta"