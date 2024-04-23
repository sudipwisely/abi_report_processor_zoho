package bi.accounting.config

import io.micronaut.context.annotation.ConfigurationProperties
import io.micronaut.context.annotation.Requires
import jakarta.validation.constraints.NotBlank

@Requires(property = "dynamodb.table-name")
@ConfigurationProperties("dynamodb")
interface DynamoConfiguration {
    val tableName: @NotBlank String?
}