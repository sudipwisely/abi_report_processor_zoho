package bi.accounting

import jakarta.inject.Singleton
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.*
import java.util.zip.GZIPInputStream

@Singleton
class GzipUtil {

    fun decompress(compressedBase64: String): ByteArray {
        val compressed = Base64.getDecoder().decode(compressedBase64)
        ByteArrayInputStream(compressed).use { bis ->
            GZIPInputStream(bis).use { gis ->
                ByteArrayOutputStream().use { bos ->
                    val buffer = ByteArray(1024)
                    var len: Int
                    while (gis.read(buffer).also { len = it } != -1) {
                        bos.write(buffer, 0, len)
                    }
                    return bos.toByteArray()
                }
            }
        }
    }
}