package bi.accounting.config

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sns.SnsClient
import java.net.URI
import javax.inject.Singleton

@Factory
@Requires(property = "awslocal.services.sns.endpoint-override")
class SnsClientFactory {

    @Primary
    @Singleton
    fun snsClient(@Value("\${awslocal.services.sns.endpoint-override}") endpointOverride: String): SnsClient {
        val endpoint = URI.create(endpointOverride)
        return SnsClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
            .endpointOverride(endpoint)
            .region(Region.of("us-east-1"))
            .build()
    }
}