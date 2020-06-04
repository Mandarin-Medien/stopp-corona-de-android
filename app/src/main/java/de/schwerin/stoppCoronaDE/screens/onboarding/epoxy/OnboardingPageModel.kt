package de.schwerin.stoppCoronaDE.screens.onboarding.epoxy

import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import de.schwerin.stoppCoronaDE.R
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyHolder
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.view.BaseEpoxyModel
import de.schwerin.stoppCoronaDE.skeleton.core.utils.visible
import com.airbnb.epoxy.EpoxyAttribute
import com.airbnb.epoxy.EpoxyModelClass
import com.airbnb.epoxy.VisibilityState

/**
 * Model for a generic onboarding page.
 */
@EpoxyModelClass(layout = R.layout.onboarding_page_epoxy_model)
abstract class OnboardingPageModel(
    private val onPageEnter: (pageNumber: Int) -> Unit = {}
) : BaseEpoxyModel<OnboardingPageModel.Holder>() {

    @EpoxyAttribute
    var pageNumber: Int = 0

    @EpoxyAttribute
    var title: String? = null

    @EpoxyAttribute
    var description: SpannableStringBuilder? = null

    @DrawableRes
    @EpoxyAttribute
    var heroImageRes: Int = R.drawable.onboarding_hero_1

    @EpoxyAttribute
    var heroImageVisible: Boolean = true

    override fun Holder.onBind() {
        txtTitle.text = title
        txtDescription.text = description
        imgHero.setImageResource(heroImageRes)
        imgHero.visible = heroImageVisible

        txtDescription.movementMethod = LinkMovementMethod()
    }

    class Holder : BaseEpoxyHolder() {
        val txtTitle by bind<TextView>(R.id.txtTitle)
        val txtDescription by bind<TextView>(R.id.txtDescription)
        val imgHero by bind<ImageView>(R.id.imgHero)
    }

    override fun onVisibilityStateChanged(visibilityState: Int, view: Holder) {
        if (visibilityState == VisibilityState.FOCUSED_VISIBLE) {
            onPageEnter(pageNumber)
        }
    }
}