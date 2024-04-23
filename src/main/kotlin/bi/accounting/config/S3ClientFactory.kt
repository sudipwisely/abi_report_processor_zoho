package bi.accounting.config

import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Factory
@Requires(property = "awslocal.services.s3.endpoint-override")
class S3ClientFactory {

    @Primary
    @Singleton
    fun sysClient(@Value("\${awslocal.services.s3.endpoint-override}") endpointOverride: String): S3Client {
        val endpoint = URI.create(endpointOverride)
        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
            .endpointOverride(endpoint)
            .region(Region.of("us-east-1"))
            .build()
    }
}