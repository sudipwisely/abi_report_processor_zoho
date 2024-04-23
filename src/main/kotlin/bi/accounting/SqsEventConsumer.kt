package bi.accounting

import bi.accounting.service.ReportService
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import io.micronaut.context.annotation.Value
import io.micronaut.function.FunctionBean
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Inject
import org.slf4j.LoggerFactory
import java.util.function.Consumer

@FunctionBean("abi-report-processor-zoho")
class SqsEventConsumer: Consumer<SQSEvent> {

    @Inject
    lateinit var reportServiceMap: Map<String, ReportService>

    @Inject
    lateinit var objectMapper: ObjectMapper

    @Inject
    lateinit var gzipUtil: GzipUtil

    @Value("\${source.arn}")
    var sourceArn: String = ""

    override fun accept(event: SQSEvent) {
        event.records.forEach { record ->
            LOG.info("Request entered to abi_report_processor_zoho")
            val body = record.body
            if(record.eventSourceArn != sourceArn) {
                LOG.error("Event source ARN does not match expected ARN {}", sourceArn)
                return
            }
            val reportByteData = gzipUtil.decompress(body)
            val reportRawData = reportByteData.decodeToString()
           // val reportRawData = body
            LOG.info("SQS event received: {} from {}", reportRawData, record.eventSourceArn)
            val request = objectMapper.readValue(reportRawData, HashMap::class.java)

            val service = reportServiceMap[request["Report-Type"]] ?: throw IllegalArgumentException("Invalid report type: ${request["Report-Type"]}")
            service.processReport(request)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(FunctionRequestHandler::class.java)
    }
}