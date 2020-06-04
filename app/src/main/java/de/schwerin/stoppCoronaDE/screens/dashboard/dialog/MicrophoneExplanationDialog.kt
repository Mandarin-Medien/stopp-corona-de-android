package de.schwerin.stoppCoronaDE.screens.dashboard.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.model.exceptions.SilentError
import de.schwerin.stoppCoronaDE.screens.dashboard.DashboardFragment
import de.schwerin.stoppCoronaDE.screens.handshake.startHandshakeFragment
import de.schwerin.stoppCoronaDE.utils.withCustomStyle
import kotlinx.android.synthetic.main.handshake_microphone_explanation_dialog.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

class MicrophoneExplanationDialog(nextPage: DashboardFragment) : DialogFragment() {


    private val nextPage = nextPage;

    private val viewModel: MicrophoneExplanationDialogViewModel by viewModel()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setView(R.layout.handshake_microphone_explanation_dialog)
            .show()
            .withCustomStyle()
            .apply {
                btnOk.setOnClickListener {
                    if (checkbox.isChecked) {
                        viewModel.doNotShowAgain()
                    }
                    targetFragment
                        ?.onActivityResult(targetRequestCode, Activity.RESULT_OK, Intent())
                        ?: Timber.e(SilentError("Dialog is not shown for result."))
                    dismiss()
                    nextPage.startHandshakeFragment()
                }
                constraintLayoutCheckbox.setOnClickListener {
                    checkbox.toggle()
                }
            }
    }
}