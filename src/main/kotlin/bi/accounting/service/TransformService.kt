package bi.accounting.service

import com.bazaarvoice.jolt.Chainr
import com.bazaarvoice.jolt.JsonUtils
import com.bazaarvoice.jolt.Sortr
import com.bazaarvoice.jolt.exception.JoltException
import jakarta.inject.Singleton
import java.io.IOException

@Singleton
class TransformService {

    @Throws(IOException::class)
    fun doTransform(input: Any, spec: Any, doSort: Boolean): String {
        return try {
            val inputJson = if(input is String) {
                JsonUtils.jsonToObject(input)
            }else{
                input
            }

            val specJson = if(spec is String){
                val specStream = javaClass.classLoader.getResourceAsStream("spec/report.json")
                JsonUtils.jsonToObject(specStream?.let { specStream.bufferedReader().use { it.readText() } })
            }else{
                spec
            }
            val chainr = Chainr.fromSpec(specJson)
            var output = chainr.transform(inputJson)
            if (doSort) {
                output = Sortr.sortJson(output)
            }
            if(output == "null" || output == null){
                output = input
            }
            JsonUtils.toPrettyJsonString(output)
        } catch (e: Exception) {
            throw JoltException("Error transforming input", e)
        }
    }
}