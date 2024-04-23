package bi.accounting.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.micronaut.core.annotation.NonNull
import io.micronaut.serde.annotation.Serdeable
import jakarta.inject.Named
import jakarta.validation.constraints.NotBlank

@Serdeable
@Named("chartofaccounts")
class ChartofAccount(
    id: String?,
    type: String? = "chartofaccounts",
    uri: String?,
    requestId: String?
): Report(id, type, uri, requestId)