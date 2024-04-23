package bi.accounting.service.impl


import bi.accounting.RetryQueueProducer
import bi.accounting.ReportGlueQueueProducer
import bi.accounting.model.MessagePayload
import bi.accounting.model.Report
import bi.accounting.repository.BatchRepository
import bi.accounting.repository.ReportRepository
import bi.accounting.repository.RequestRepository
import bi.accounting.request.ChartOfAccountsReportRequest
import bi.accounting.service.ReportService
import bi.accounting.service.S3Service
import bi.accounting.service.SnsService
import bi.accounting.service.TransformService
import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Nullable
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.HashMap

@Named("chartofaccounts")
@Singleton
class ChartOfAccountsReportService(private val objectMapper: ObjectMapper,
                                   private val s3Service: S3Service,
                                   private val transformService: TransformService,
                                   private val snsService: SnsService,
                                   private val glueQueueProducer: ReportGlueQueueProducer,
                                   private val batchRepository: BatchRepository,
                                   private val requestRepository: RequestRepository,
                                   private val retryQueueProducer: RetryQueueProducer
    ): BaseService(objectMapper,s3Service,transformService,snsService,glueQueueProducer,batchRepository,requestRepository,retryQueueProducer) {
    companion object {
        private val LOG = LoggerFactory.getLogger(ChartOfAccountsReportRequest::class.java)
    }
}