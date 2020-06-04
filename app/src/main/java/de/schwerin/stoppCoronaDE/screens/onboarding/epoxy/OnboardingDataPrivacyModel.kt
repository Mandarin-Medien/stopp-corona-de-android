package de.schwerin.stoppCoronaDE.screens.onboarding.epoxy

import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.widget.CheckBox
import android.widget.TextView
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.VisibilityState

/**
 * Model for the onboarding terms and conditions page
 */
@EpoxyModelClass(layout = R.layout.onboarding_data_privacy_page_epoxy_model)
abstract class OnboardingDataPrivacyModel(
    private val onPageEnter: (pageNumber: Int) -> Unit = {},
    private val onCheckBoxChanged: (checked: Boolean) -> Unit
) : BaseEpoxyModel<OnboardingDataPrivacyModel.Holder>() {

    @EpoxyAttribute
    var pageNumber: Int = 0

    @EpoxyAttribute
    var title: String? = null

    @EpoxyAttribute
    var firstDescription: SpannableString? = null

    @EpoxyAttribute
    var secondDescription: SpannableString? = null

    @EpoxyAttribute
    var checkBoxDescription: String? = null

    override fun Holder.onBind() {
        txtTitle.text = title
        txtDescription1.text = firstDescription
        txtDescription2.text = secondDescription
        checkbox.text = checkBoxDescription

        checkbox.setOnCheckedChangeListener { _, isChecked -> onCheckBoxChanged(isChecked) }

        txtDescription2.movementMethod = LinkMovementMethod()
    }

    class Holder : BaseEpoxyHolder() {
        val txtTitle by bind<TextView>(R.id.txtTitle)
        val txtDescription1 by bind<TextView>(R.id.txtDescription_1)
        val txtDescription2 by bind<TextView>(R.id.txtDescription_2)
        val checkbox by bind<CheckBox>(R.id.checkbox)
    }

    override fun onVisibilityStateChanged(visibilityState: Int, view: Holder) {
        if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
            onPageEnter(pageNumber)
        }
    }
}
