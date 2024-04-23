package bi.accounting.service

import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.io.BufferedReader
import java.io.InputStreamReader
import java.time.Duration
import java.util.stream.Collectors


@Singleton
class S3Service(private val s3Client: S3Client) {

    @Value("\${s3.bucket-name}")
    lateinit var bucketName: String

    fun uploadReportData(reportData: String, key: String): String {
        // Implement the logic to upload the report data to S3
        // and return the S3 location.
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key("${bucketName}/$key")
            .build()

        val response = s3Client.putObject(putObjectRequest, RequestBody.fromString(reportData))

        return response.eTag()
    }

    fun downloadReportData(key: String): String {
        // Implement the logic to download the report data from S3
        // and return the report data.
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key("${bucketName}/$key")
            .build()

        s3Client.getObject(getObjectRequest).use { s3Object ->
            BufferedReader(InputStreamReader(s3Object)).use { reader ->
                return reader.lines().collect(Collectors.joining("\n"))
            }
        }
    }

    fun generateDownloadUrl(key: String): String {
        S3Presigner.create().use { presigner ->
            val objectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key("${bucketName}/$key")
                .build()
            val presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofDays(7)) // The URL will expire in 7 days.
                .getObjectRequest(objectRequest)
                .build()

            val presignedRequest = presigner.presignGetObject(presignRequest)
            return presignedRequest.url().toString()
        }
    }
}