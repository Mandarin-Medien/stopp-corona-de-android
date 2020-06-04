package de.schwerin.stoppCoronaDE.screens.dashboard.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.utils.withCustomStyle

class GooglePlayServicesNotAvailableDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(R.string.main_play_services_not_available_dialog_title)
            .setMessage(R.string.main_play_services_not_available_dialog_message)
            .setPositiveButton(R.string.general_ok) { _, _ -> dismiss() }
            .show()
            .withCustomStyle()
    }
}
