package bi.accounting.repository

import bi.accounting.config.DynamoConfiguration
import io.micronaut.context.annotation.Value
import io.micronaut.core.util.CollectionUtils
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.util.*
import kotlin.collections.HashMap

@Singleton
@Named("batch")
class BatchRepository(
    private val dynamoDbClient: DynamoDbClient,
) {

    @Value("\${dynamodb.batch-table-name}")
    private lateinit var tableName: String

    fun saveBatchData(pk: String, sk: String, pageOrOffset: Int, count: Int?, batchSize: Int): String {
        val item: MutableMap<String, AttributeValue?> = HashMap()
        item["pk"] = AttributeValue.builder().s(pk).build()
        item["sk"] = AttributeValue.builder().s(sk).build()
        item["pageOrOffset"] = AttributeValue.builder().n(pageOrOffset.toString()).build()
        item["count"] = AttributeValue.builder().n(count.toString()).build()
        item["batchSize"] = AttributeValue.builder().n(batchSize.toString()).build()
        try{
            val request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build()
            LOG.info("Saving report metadata {}", request)
            val response = dynamoDbClient.putItem(request)
            LOG.info("Saved report metadata {}", response.consumedCapacity())
            return response.toString()
        } catch (e: Exception) {
            LOG.error("Error saving report batch data", e)
            throw e
        }
    }

    fun findBatchData(pk: String, sk: String): MutableList<HashMap<String, Any>?> {
        LOG.info("Finding batch data for pk {} and sk {}", pk, sk)
        val result: MutableList<HashMap<String, Any>?> = ArrayList()
        val builder = QueryRequest.builder()
            .tableName(tableName)
        val request = builder.keyConditionExpression("#pk = :pk and begins_with(#sk, :sk)")
            .expressionAttributeNames(CollectionUtils.mapOf("#pk", "pk", "#sk", "sk") as MutableMap<String, String>?)
            .expressionAttributeValues(
                CollectionUtils.mapOf(
                    ":pk",
                    AttributeValue.builder().s(pk).build(),
                    ":sk",
                    AttributeValue.builder().s(sk).build()
                ) as MutableMap<String, AttributeValue>?
            )
            .build()
        val response = dynamoDbClient.query(request)
        if (response.hasItems()) {
            val items = response.items()
            for (item in items) {
                val batchMap = HashMap<String, Any>()
                batchMap["count"] = item["count"]!!.n().toInt()
                batchMap["sk"] = item["sk"]!!.s()
                batchMap["pageOrOffset"] = item["pageOrOffset"]!!.n().toInt()
                result.add(batchMap)
            }
        }

        return result
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(BatchRepository::class.java)
    }
}