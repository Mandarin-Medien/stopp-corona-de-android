package de.schwerin.stoppCoronaDE.skeleton.core.utils

import android.app.Activity
import android.view.View
import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.skeleton.core.R
import de.schwerin.stoppCoronaDE.skeleton.core.model.exceptions.GeneralServerException
import de.schwerin.stoppCoronaDE.skeleton.core.model.exceptions.NoInternetConnectionException
import timber.log.Timber

/**
 * Extensions for handling general errors.
 */

/**
 * Basic error handling in Activity.
 */
fun Activity.handleBaseErrors(error: Throwable) {
    handleErrors(findViewById(android.R.id.content), error)
}

/**
 * Handle basic errors in Fragment.
 */
fun Fragment.handleBaseErrors(error: Throwable) {
    handleErrors(view, error)
}

/**
 * Common logic for handling of errors showing in different [view]s.
 */
private fun handleErrors(view: View?, error: Throwable) {
    view ?: Timber.e("view is null")
    when (error) {
        is NoInternetConnectionException -> view?.snackbar(R.string.general_no_connection_error)
        is GeneralServerException -> view?.snackbar(R.string.general_server_connection_error)
        else -> {
            view?.snackbar(R.string.general_unknown_exception)
            Timber.e(error)
        }
    }
}