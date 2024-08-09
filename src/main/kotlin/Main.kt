import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import io.github.cdimascio.dotenv.Dotenv

// IDK the correct code style for typing
val dotenv: Dotenv = Dotenv.load()
val apiKey: String = dotenv["WEATHER_KEY"]
val client = OkHttpClient()

@Composable
@Preview
fun App() {
    var city by remember { mutableStateOf("") }
    var weather by remember { mutableStateOf("Enter a city and click the button to get the weather.") }
    val scope = rememberCoroutineScope()

    MaterialTheme {
        Column {
            TextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") }
            )
            Button(onClick = {
                scope.launch {
                    weather = getWeather(city)
                }
            }) {
                Text("Get Weather")
            }
            Text(weather)
        }
    }
}

suspend fun getWeather(city: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://api.weatherapi.com/v1/current.json?q=$city&key=$apiKey"
            //
            val request : Request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                response.body?.string() ?: "No response body"
            }
        } catch (e: Exception) {
            "Error fetching weather: ${e.message}"
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}