package bi.accounting.repository

import bi.accounting.config.DynamoConfiguration
import bi.accounting.model.Identified
import bi.accounting.model.Report
import bi.accounting.service.impl.BaseService
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.annotation.Nullable
import io.micronaut.core.util.CollectionUtils
import jakarta.inject.Named
import jakarta.inject.Singleton
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.util.*
import kotlin.collections.HashMap

@Requires(beans = [DynamoConfiguration::class, DynamoDbClient::class])
@Singleton
@Primary
open class DynamoRepository<T : Report?>(
    protected open val dynamoDbClient: DynamoDbClient,
    protected open val dynamoConfiguration: DynamoConfiguration
): ReportRepository {

    fun save(itemResponse: @NonNull MutableMap<String, AttributeValue?>?): String {
        try{
            val request = PutItemRequest.builder()
                .tableName(dynamoConfiguration.tableName)
                .item(itemResponse)
                .build()
            LOG.info("Saving report metadata {}", request)
            val response = dynamoDbClient.putItem(request)
            LOG.info("Saved report metadata {}", response.consumedCapacity())
            return response.toString()
        } catch (e: Exception) {
            LOG.error("Error saving report metadata", e)
            throw e
        }
    }
    override fun saveMetadata(
        id: String,
        userId: String,
        orgId: String,
        type: String?,
        uri: String?,
        requestId: String?
    ): String? {
        TODO("Not yet implemented")
    }
    override fun saveBatch(accountsList: List<Report>, reportData: Map<*, *>) {
        TODO("Not yet implemented")
    }

    open fun item(entity: @NonNull T?, keys: HashMap<String, AttributeValue>): @NonNull MutableMap<String, AttributeValue?>? {
        val item: MutableMap<String, AttributeValue?> = HashMap()
//        val pk = id(entity!!::class.java, entity.id) // Use ::class.java for entity's class reference
        item[ATTRIBUTE_PK] = keys["pk"]
        item[ATTRIBUTE_SK] = keys["sk"]
//        item[ATTRIBUTE_GSI_1_PK] = classAttributeValue(entity!!::class.java) // Use ::class.java here as well
        item[ATTRIBUTE_GSI_1_PK] = keys["gsi_pk"]
        item[ATTRIBUTE_GSI_1_SK] = keys["gsi_sk"]
        item[ATTRIBUTE_GSI_2_PK] = keys["gsi2_pk"]
        item[ATTRIBUTE_GSI_2_SK] = keys["gsi2_sk"]
        item[ATTRIBUTE_ID] =
            AttributeValue.builder().s(
                entity?.id
            ).build()
        item[ATTRIBUTE_TYPE] =
            AttributeValue.builder().s(
                entity?.type
            ).build()
        item[ATTRIBUTE_URI] =
            AttributeValue.builder().s(
                entity?.uri
            ).build()
        item[ATTRIBUTE_REQUEST_ID] =
            AttributeValue.builder().s(
                entity?.requestId
            ).build()
        return item
    }


    companion object {
        private val LOG = LoggerFactory.getLogger(DynamoRepository::class.java)
        protected const val HASH = "#"
        protected const val ATTRIBUTE_PK = "pk"
        protected const val ATTRIBUTE_SK = "sk"
        protected const val ATTRIBUTE_GSI_1_PK = "GSI1PK"
        protected const val ATTRIBUTE_GSI_1_SK = "GSI1SK"
        protected const val ATTRIBUTE_GSI_2_PK = "GSI2PK"
        protected const val ATTRIBUTE_GSI_2_SK = "GSI2SK"
        protected const val INDEX_GSI_1 = "GSI1"
        const val ATTRIBUTE_ID = "id"
        const val ATTRIBUTE_TYPE = "type"
        private const val ATTRIBUTE_URI = "uri"
        private const val ATTRIBUTE_REQUEST_ID = "requestId"
        private const val ATTRIBUTE_CHUNK = "chunk"
        private const val ATTRIBUTE_IS_LAST_CHUNK = "isLastChunk"
        private const val ATTRIBUTE_ORG_NAME = "orgName"
    }
}