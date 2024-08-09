import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import io.github.cdimascio.dotenv.Dotenv
import org.json.JSONObject


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
    var isDarkTheme by remember { mutableStateOf(false) }

    val colors = if (isDarkTheme) darkColors() else lightColors()
    val backgroundColor = if (isDarkTheme) Color.DarkGray else Color.White

    MaterialTheme(colors = colors) {
        Surface(color = backgroundColor, modifier = Modifier.fillMaxSize()) {
            Column {
                Row {
                    Text("Weather App", style = MaterialTheme.typography.h4)
                    Button(onClick = {
                        isDarkTheme = !isDarkTheme
                    }) {
                        Text(if (isDarkTheme)"☀" else "🌙")
                    }
                }
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
}

suspend fun getWeather(city: String): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://api.weatherapi.com/v1/current.json?q=$city&key=$apiKey"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBody = response.body?.string() ?: return@withContext "No response body"
                val json = JSONObject(responseBody)
                val location = json.getJSONObject("location").getString("name")
                val tempC = json.getJSONObject("current").getDouble("temp_c")
                val tempF = json.getJSONObject("current").getDouble("temp_f")
                val condition = json.getJSONObject("current").getJSONObject("condition").getString("text")
                val time = json.getJSONObject("location").getString("localtime")
                "Location: $location\nTemperature: $tempF°F ($tempC°C)\nCondition: $condition\nJSON: $json"
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