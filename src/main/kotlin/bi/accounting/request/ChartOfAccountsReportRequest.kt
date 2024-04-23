package bi.accounting.request

import com.fasterxml.jackson.annotation.JsonTypeName
import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable
import java.time.LocalDate

@Serdeable
@Introspected
data class ChartOfAccountsReportRequest(
    var id: String? = null,
    var provider: String? = null,
    var account_id: String? = null,
    var account_name: String? = null,
    var account_code: String? = null,
    var account_type: String? = null,
    var parent_account_id: String? = null,
    var parent_account_name: String? = null
){

    var orgid: String? = null
//    operator fun get(property: String): Any? {
//        return when(property) {
//            "id" -> id
//            "chunk" -> chunk
//            "isLastChunk" -> isLastChunk
//            "date" -> date
//            "timeframe" -> timeframe
//            "periods" -> periods
//            "standardLayout" -> standardLayout
//            "trackingOptionID1" -> trackingOptionID1
//            "trackingOptionID2" -> trackingOptionID2
//            "orgName" -> orgName
//            else -> throw IllegalArgumentException("Property $property not found")
//        }
//    }

//    operator fun set(property: String, value: Any?) {
//        when(property) {
//            "id" -> id = value as? String
//            "chunk" -> chunk = value as? Int
//            "isLastChunk" -> isLastChunk = value as? Boolean
//            "date" -> date = (value as? LocalDate)!!
//            "timeframe" -> timeframe = (value as? String).toString()
//            "periods" -> periods = (value as? Int)!!
//            "standardLayout" -> standardLayout = (value as? Boolean)!!
//            "trackingOptionID1" -> trackingOptionID1 = value as? List<String>
//            "trackingOptionID2" -> trackingOptionID2 = value as? List<String>
//            "orgName" -> orgName = value as? String
//            else -> throw IllegalArgumentException("Property $property not found")
//        }
//    }
}