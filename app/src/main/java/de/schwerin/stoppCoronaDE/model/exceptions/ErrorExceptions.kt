package de.schwerin.stoppCoronaDE.model.exceptions

import androidx.fragment.app.Fragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.model.api.ApiError
import de.schwerin.stoppCoronaDE.screens.base.dialog.GeneralErrorDialog
import de.schwerin.stoppCoronaDE.screens.mandatory_update.startMandatoryUpdateFragment
import de.schwerin.stoppCoronaDE.skeleton.core.model.exceptions.GeneralServerException
import de.schwerin.stoppCoronaDE.skeleton.core.model.exceptions.NoInternetConnectionException
import de.schwerin.stoppCoronaDE.skeleton.core.model.exceptions.UnexpectedError
import timber.log.Timber

/**
 * Extension for handling general errors in Corona app domain inspired by [handleBaseErrors] from skeleton.
 * Everything is logged, when use this method, there is no need to log it again.
 */
fun Fragment.handleBaseCoronaErrors(error: Throwable) {
    when (error) {
        is NoInternetConnectionException -> {
            Timber.i(error, "No internet connection or server timeout")
            GeneralErrorDialog(R.string.error_no_internet_title, R.string.error_no_internet_message)
                .show(childFragmentManager, GeneralErrorDialog::class.java.name)
        }
        is GeneralServerException -> {
            Timber.w(error, "Unhandled server exception")
            GeneralErrorDialog(R.string.error_server_title, R.string.error_server_message)
                .show(childFragmentManager, GeneralErrorDialog::class.java.name)
        }
        is UnexpectedError -> {
            Timber.w(error, "Unhandled unknown exception")
            GeneralErrorDialog(R.string.error_unknown_title, R.string.error_unknown_message)
                .show(childFragmentManager, GeneralErrorDialog::class.java.name)
        }
        is DataFetchFailedException -> {
            Timber.w(error, "Fetch failed exception")
            GeneralErrorDialog(R.string.error_unknown_title, R.string.error_unknown_message)
                .show(childFragmentManager, GeneralErrorDialog::class.java.name)
        }
        is ApiError.Critical.ForceUpdate -> {
            Timber.w(error, "Force update exception")
            startMandatoryUpdateFragment()
        }
        else -> {
            Timber.e(error, "Unhandled else exception")
            GeneralErrorDialog(R.string.error_unknown_title, R.string.error_unknown_message)
                .show(childFragmentManager, GeneralErrorDialog::class.java.name)
        }
    }
}
