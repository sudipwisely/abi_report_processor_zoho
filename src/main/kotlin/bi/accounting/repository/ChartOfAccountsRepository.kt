package bi.accounting.repository

import bi.accounting.config.DynamoConfiguration
import bi.accounting.model.ChartofAccount
import bi.accounting.model.Report
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.util.CollectionUtils
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import software.amazon.awssdk.services.dynamodb.model.*
import java.time.Instant
import java.time.ZoneId

@Singleton
@Named("chartofaccounts")
class ChartOfAccountsRepository(
    override val dynamoDbClient: DynamoDbClient,
    override val dynamoConfiguration: DynamoConfiguration,
) : DynamoRepository<Report?>(dynamoDbClient, dynamoConfiguration) {

    override fun saveMetadata(
        id: String,
        userId: String,
        orgId: String,
        type: String?,
        uri: String?,
        requestId: String?
    ): String {
        val itemResponse = item(userId, orgId, ChartofAccount(
            id,
            type,
            uri,
            requestId
        ))
        return save(itemResponse)
    }

    private fun item(userId: String, orgId: String, entity: @NonNull ChartofAccount?): @NonNull MutableMap<String, AttributeValue?> {
        val keys = HashMap<String, AttributeValue>()
        keys["pk"] = AttributeValue.builder().s("USER#${userId}#${entity!!::class.java.simpleName}").build()
        keys["sk"] = AttributeValue.builder().s("$orgId#${entity.requestId}#${Instant.now().atZone(ZoneId.of("UTC"))}").build()
        keys["gsi_pk"] = AttributeValue.builder().s("USER#${userId}").build()
        keys["gsi_sk"] = AttributeValue.builder().s("${Instant.now().atZone(ZoneId.of("UTC"))}#${orgId}").build()
        keys["gsi2_pk"] = AttributeValue.builder().s("xero#$userId#accounts#$orgId").build()
        keys["gsi2_sk"] = AttributeValue.builder().n(Instant.now().toEpochMilli().toString()).build()
        val result = super.item(entity, keys)
        result!![ATTRIBUTE_ID] =
            AttributeValue.builder().s(
                entity!!.id
            ).build()
        result[ATTRIBUTE_TYPE] =
            AttributeValue.builder().s(
                entity.type.toString()
            ).build()
//        LOG.info("Item: {}", result)
        return result
    }
}