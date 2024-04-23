package bi.accounting.service.impl

import bi.accounting.ReportGlueQueueProducer
import bi.accounting.RetryQueueProducer
import bi.accounting.model.MessagePayload
import bi.accounting.model.Report
import bi.accounting.repository.BatchRepository
import bi.accounting.repository.ReportRepository
import bi.accounting.repository.RequestRepository
import bi.accounting.service.ReportService
import bi.accounting.service.S3Service
import bi.accounting.service.SnsService
import bi.accounting.service.TransformService
import io.micronaut.context.annotation.Value
import io.micronaut.core.annotation.Nullable
import io.micronaut.serde.ObjectMapper
import jakarta.inject.Inject
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.HashMap


open class BaseService(
    private val objectMapper: ObjectMapper,
    private val s3Service: S3Service,
    private val transformService: TransformService,
    private val snsService: SnsService,
    private val glueQueueProducer: ReportGlueQueueProducer,
    private val batchRepository: BatchRepository,
    private val requestRepository: RequestRepository,
    private val retryQueueProducer: RetryQueueProducer
): ReportService {
    @Inject
    lateinit var reportRepositoryMap: Map<String, ReportRepository>

    @Inject
    lateinit var reportsMap: Map<String, Report>

    private var reportItemsCount: Int = 0

    override fun processReport(reportData: HashMap<*, *>) {
        LOG.info("Processing report")
        val reportType = reportData["Report-Type"] as String
        val reportRequestMap = objectMapper.readValue(reportData["Request-Body"] as String, HashMap::class.java) as HashMap<String,Any>
        val reportResponseMap = objectMapper.readValue(reportData["Response-Body"] as String, HashMap::class.java)
        val orgId = reportData["Org-ID"].toString()
        val userId = reportData["User-ID"].toString()
        val provider = reportRequestMap["provider"].toString()
        val requestId = reportRequestMap["id"].toString()
        val key = createKey(reportData, reportRequestMap)
        val itemsCount = getItemsCountFromResponse(reportResponseMap)
        reportRequestMap["itemsCount"] = itemsCount ?: 0

        LOG.info("Uploading report data to s3 {}", key)
        val rawData = transformData(reportData)
        val reportData = objectMapper.readValue(rawData, HashMap::class.java)
        s3Service.uploadReportData(rawData, "$key.json")
        saveMetadata(null.toString(), userId, orgId, reportType, key, requestId)
        LOG.info("S3 save complete")
        sendNotification(userId, orgId, requestId, reportType, provider)
        sendToGlueQueue(userId, reportType, requestId)
    }

    open fun createKey(reportData: HashMap<*, *>, reportRequestMap: HashMap<*, *>): String{
        val requestId = reportRequestMap["id"].toString()
        val orgId = reportData["Org-ID"].toString()
        val userId = reportData["User-ID"].toString()
        val reportType = reportData["Report-Type"].toString()
        return "$userId/$reportType/$orgId/$requestId"
    }

    open fun transformData(reportData: HashMap<*, *>): String{
        return reportData["Response-Body"].toString()
    }

    open fun saveMetadata(id: String, userId: String, orgId: String,  reportType: String, key: String, requestId: String){
        LOG.info("Saving metadata for report {}", requestId)
        val repository = reportRepositoryMap[reportType] ?: throw IllegalArgumentException("Invalid report repository type: ${reportType}")
        repository.saveMetadata(null.toString(), userId, orgId, reportType, key,requestId)
    }

    private fun saveBatchData(report: Report, orgId: String, requestId: String, batchId: String, pageOrOffset: Int, batchSize: Int, reportType: String, itemsCount: Int?, isLastChunk: Boolean){
        LOG.info("Saving batch data for report {} {}", requestId, batchId)
        val pk = "${report::class.java.simpleName}#${orgId}"
        val sk = let {
            if(isLastChunk){
                "${requestId}#LAST#${batchId}${pageOrOffset}"
            }else{
                "${requestId}#${batchId}#${pageOrOffset}"
            }
        }
        batchRepository.saveBatchData(pk, sk, pageOrOffset, itemsCount ?: 0, batchSize)
    }

    private fun checkReportCompletionAndPostProcess(userId: String, requestId: String, reportType: String, reportData: HashMap<*, *>, reportRequestMap: HashMap<*, *>){
        LOG.info("Checking if all chunks of report {} are present", requestId)
        val requests = requestRepository.findRequestData(userId, requestId)
        val orgIds = (requests[0]?.get("orgIds") as String).split(",")
        val report = reportsMap[reportType] ?: throw IllegalArgumentException("Invalid report type: ${reportType}")
        val allCompleted = orgIds.all {
            val pk = "${report::class.java.simpleName}#${it}"
            val sk = "${requestId}#LAST#" // check if last chunk is present
            val batches = batchRepository.findBatchData(pk, sk)
            batches.size > 0
        }
        if(allCompleted){
            LOG.info("All chunks of report {} are present, starting post processing", requestId)
            sendToGlueQueue(userId, reportType, requestId)
        }else{
            LOG.info("All chunks of report {} are not present", requestId)
        }
    }

    protected open fun getItemsCountFromResponse(reportResponseMap: HashMap<*,*>): Int?{
        return null
    }

    /*protected fun sendNotification(
        userId: String, orgId: String, requestId: String, type: String, provider: String,
        chunkNo: Int?, isLastChunk: Boolean?, isCompleted: Boolean, url: String?){
        val localDateTime = LocalDateTime.now()
        val utc = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
        LOG.info("Publishing message chunk to SNS")
        snsService.publishMessage(
            MessagePayload(
                userId,
                orgId,
                requestId,
                chunkNo,
                isLastChunk,
                isCompleted,
                url,
                type,
                provider,
                utc
            )
        )
    }
*/

    protected fun sendNotification(userId: String, orgId: String, requestId: String, type: String, provider: String){
        val localDateTime = LocalDateTime.now()
        val utc = Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
        LOG.info("Publishing message chunk to SNS")
        snsService.publishMessage(MessagePayload(userId, orgId, requestId, type, provider, utc))
    }

    private fun startNewBatch(report: Report, reportType: String, userId: String, provider: String, orgId: String, requestId: String, batchId: String, batchSize: Int, reportRequestMap: HashMap<*, *>){
        LOG.info("Checking if new batch needs to be started for report {} {}", requestId, batchId)
        val pk = "${report::class.java.simpleName}#${orgId}"
        val sk = "${requestId}#$batchId#"
        val batches = batchRepository.findBatchData(pk, sk)
        if (batches.size == batchSize){
            val request = reportRequestMap.toMutableMap()
            batches.sortBy {
                it?.get("pageOrOffset") as Int
            }
            val lastPage = batches[batches.size-1]?.get("pageOrOffset") as Int
            LOG.info("Starting new batch from page {}", lastPage+1)
            if(request["page"] !== null) {
                request["page"] = lastPage + 1
            }else if(request["offset"] !== null) {
                request["offset"] = lastPage + 1
            }
            retryQueueProducer.send(objectMapper.writeValueAsString(HashMap<String, Any>().apply {
                this["Report-Type"] = reportType
                this["User-ID"] = userId.toLong()
                this["Org-ID"] = orgId
                this["Request-Body"] = request
                this["id"] = requestId
                this["provider"] = provider
            }))
        }
    }

    fun sendToGlueQueue(userId: String, reportType: String, requestId: String){
        LOG.info("Sending report to glue queue")
        val reportGlueMap = HashMap<String, Any>()
        reportGlueMap["User-ID"] = userId
        reportGlueMap["Report-Type"] = reportType
        reportGlueMap["id"] = requestId
        glueQueueProducer.send(objectMapper.writeValueAsString(reportGlueMap))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BaseService::class.java)
    }
}