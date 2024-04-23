package bi.accounting.repository

import io.micronaut.context.annotation.Value
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest
import software.amazon.awssdk.services.dynamodb.model.QueryRequest
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

@Singleton
@Named("request")
class RequestRepository(
    private val dynamoDbClient: DynamoDbClient,
) {

    @Value("\${dynamodb.request-table-name}")
    private lateinit var tableName: String

    fun findRequestData(userId: String, requestId: String): MutableList<HashMap<String, Any>?> {
        val result: MutableList<HashMap<String, Any>?> = ArrayList()
        val builder = QueryRequest.builder()
            .tableName(tableName)
        val request = builder.keyConditionExpression("#pk = :pk")
            .expressionAttributeNames(Collections.singletonMap("#pk", "pk"))
            .expressionAttributeValues(
                Collections.singletonMap(
                    ":pk",
                    AttributeValue.builder().s("Request#$userId#$requestId").build()
                )
            )
            .build()
        val response = dynamoDbClient.query(request)
        if (response.hasItems()) {
            val items = response.items()
            for (item in items) {
                val batchMap = HashMap<String, Any>()
                batchMap["orgIds"] = item["orgIds"]!!.s()
                result.add(batchMap)
            }
        }

        return result
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(RequestRepository::class.java)
    }
}