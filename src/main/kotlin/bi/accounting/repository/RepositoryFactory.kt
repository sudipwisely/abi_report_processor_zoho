package bi.accounting.repository

import io.micronaut.context.annotation.Factory
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Factory
class RepositoryFactory {

    @Singleton
    fun reportRepositoryMap(reportRepositoryList: List<ReportRepository>): Map<String, ReportRepository> {
        return reportRepositoryList
            .filterNot { it::class.java.getAnnotation(Named::class.java) == null }
            .associateBy {
//                LOG.info("ReportRepository {}", it::class.java.simpleName)
                it::class.java.getAnnotation(Named::class.java).value
            }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RepositoryFactory::class.java)
    }
}