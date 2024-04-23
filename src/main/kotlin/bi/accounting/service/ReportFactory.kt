package bi.accounting.service

import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton

@Factory
class ReportFactory {

    @Singleton
    fun reportServiceMap(reportServiceList: List<ReportService>): Map<String, ReportService> {
        return reportServiceList.associateBy { it::class.java.getAnnotation(Named::class.java)?.value ?: "unknown" }
    }
}