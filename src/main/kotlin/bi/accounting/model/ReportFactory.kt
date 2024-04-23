package bi.accounting.model

import bi.accounting.repository.RepositoryFactory
import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Factory
class ReportFactory {

    @Singleton
    fun reportsMap(reportsList: List<Report>): Map<String, Report> {
        return reportsList
            .filterNot { it::class.java.getAnnotation(Named::class.java) == null }
            .associateBy {
//                LOG.info("Report {}", it::class.java.simpleName)
                it::class.java.getAnnotation(Named::class.java).value
            }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RepositoryFactory::class.java)
    }
}