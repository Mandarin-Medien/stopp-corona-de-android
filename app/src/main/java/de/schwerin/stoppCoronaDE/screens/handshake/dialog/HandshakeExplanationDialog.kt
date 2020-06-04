package de.schwerin.stoppCoronaDE.screens.handshake.dialog

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.utils.startDefaultBrowser
import de.schwerin.stoppCoronaDE.utils.withCustomStyle
import kotlinx.android.synthetic.main.handshake_explanation_dialog.*

/**
 * Dialog to explain how handshake works.
 */
class HandshakeExplanationDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setView(R.layout.handshake_explanation_dialog)
            .show()
            .withCustomStyle()
            .apply {
                btnClose.setOnClickListener {
                    dismiss()
                }
                btnFaq.setOnClickListener {
                    startDefaultBrowser(getString(R.string.handshake_dialog_faq_link))
                }
            }
    }
}