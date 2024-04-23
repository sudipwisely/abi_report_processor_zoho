package bi.accounting.repository

import bi.accounting.model.Report
import io.micronaut.core.annotation.NonNull
import jakarta.validation.constraints.NotBlank

interface ReportRepository {
    /*fun saveMetadata(id: @NonNull @NotBlank String,
                     userId: @NonNull @NotBlank String,
                     orgId: @NonNull @NotBlank String,
                     type: @NonNull @NotBlank String?,
                     uri: @NonNull @NotBlank String?,
                     requestId: @NonNull @NotBlank String?,
                     chunk: Int?,
                     isLastChunk: Boolean?,
                     data: Map<*,*>): @NonNull String?*/

    /*fun saveBatch(accountsList: List<Report>, reportData: Map<*, *>)*/
    fun saveMetadata(id: @NonNull @NotBlank String,
                     userId: @NonNull @NotBlank String,
                     orgId: @NonNull @NotBlank String,
                     type: @NonNull @NotBlank String?,
                     uri: @NonNull @NotBlank String?,
                     requestId: @NonNull @NotBlank String?,
                     ): @NonNull String?
    fun saveBatch(accountsList: List<Report>, reportData: Map<*, *>)
}