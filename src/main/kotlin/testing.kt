import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException


fun getLocation(): JSONObject {
    return runBlocking {
        try {
            val url = "http://ip-api.com/json/?fields=city,query,regionName,country"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBody = response.body?.string() ?: "No response body"
                val json = JSONObject(responseBody)
                json
            }
        } catch (e: Exception) {
            JSONObject("""{"error": e.message}"""")
        }
    }
}
