package bi.accounting.model

import io.micronaut.core.annotation.NonNull
import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank

@Serdeable
abstract class Report(
    override val id: String?,
    open val type: String?,
    open val uri: String?,
    open var requestId: String?): Identified {
}