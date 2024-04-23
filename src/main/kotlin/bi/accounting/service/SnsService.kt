package bi.accounting.service

import bi.accounting.model.MessagePayload
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import software.amazon.awssdk.services.sns.SnsClient

@Singleton
class SnsService(
    private val snsClient: SnsClient,
    private val objectMapper: ObjectMapper
) {

    @Value("\${topic.arn}")
    lateinit var topicArn: String

    fun publishMessage(message: MessagePayload) {
        // Implement the logic to publish the message to the SNS topic.
        try {
            val res = snsClient.publish {
                it.message(objectMapper.writeValueAsString(message))
                it.topicArn(topicArn)
            }

            LOG.info("Message published to SNS: {}", res.messageId())
        }catch (e: Exception){
            LOG.error("Error publishing message to SNS", e)
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SnsService::class.java)
    }
}