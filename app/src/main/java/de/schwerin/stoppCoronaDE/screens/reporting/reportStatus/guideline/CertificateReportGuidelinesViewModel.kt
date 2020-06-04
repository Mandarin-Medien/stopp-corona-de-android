package de.schwerin.stoppCoronaDE.screens.reporting.reportStatus.guideline

import de.schwerin.stoppCoronaDE.model.repositories.QuarantineRepository
import de.schwerin.stoppCoronaDE.skeleton.core.model.helpers.AppDispatchers
import de.schwerin.stoppCoronaDE.skeleton.core.screens.base.viewmodel.ScopedViewModel
import com.github.dmstocking.optional.java.util.Optional
import io.reactivex.Observable
import org.threeten.bp.ZonedDateTime

/**
 * Handles the user interaction and provides data for [CertificateReportGuidelinesFragment].
 */
class CertificateReportGuidelinesViewModel(
    appDispatchers: AppDispatchers,
    private val quarantineRepository: QuarantineRepository
) : ScopedViewModel(appDispatchers) {

    fun observeDateOfFirstMedicalConfirmation(): Observable<Optional<ZonedDateTime>> {
        return quarantineRepository.observeDateOfFirstMedicalConfirmation()
    }
}
