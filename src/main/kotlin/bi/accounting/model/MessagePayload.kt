package bi.accounting.model

import io.micronaut.serde.annotation.Serdeable
import java.util.Date

@Serdeable.Serializable
/*data class MessagePayload(
    val userId: String,
    val orgId: String,
    val requestId: String,
    val chunk: Int?,
    val isLast: Boolean?,
    val isCompleted: Boolean?,
    val downloadUrl: String?,
    val type: String,
    val provider: String,
    val time: Date
)*/
data class MessagePayload(
    val userId: String,
    val orgId: String,
    val requestId: String,
    val type: String,
    val provider: String,
    val time: Date
)
