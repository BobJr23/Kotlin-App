import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import io.github.cdimascio.dotenv.Dotenv
import org.json.JSONObject

val dotenv: Dotenv = Dotenv.load()
val apiKey: String = dotenv["WEATHER_KEY"]
val client = OkHttpClient()
val ipApiKey: String = dotenv["IP_KEY"]

@Composable
@Preview
fun App() {
    var location by remember { mutableStateOf(getLocation()) }
    var city by remember { mutableStateOf(location.getString("city") + ", " + location.getString("country")) }
    var weather by remember { mutableStateOf("Enter a city and click the button to get the weather.") }
    var forecast by remember { mutableStateOf("") }
    var condition by remember { mutableStateOf("clear") }
    var isCelsius by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    var isDarkTheme by remember { mutableStateOf(false) }


    val colors: Colors = if (isDarkTheme) darkColors() else lightColors()
    val backgroundColor: Color = if (isDarkTheme) Color.DarkGray else Color.White

    val backgroundImage: ImageBitmap = useResource(
        when {
            condition.contains("rain") -> "rain.jpg"
            condition.contains("cloudy") -> "cloud.jpg"
            else -> "sun.jpg"
        }
    ) { loadImageBitmap(it) }

    MaterialTheme(colors = colors) {
        Box {
            Image(bitmap = backgroundImage, contentDescription = null, modifier = Modifier.fillMaxSize())
            Column {
                Row {
                    Text("Weather App", style = MaterialTheme.typography.h4)
                    Button(onClick = {
                        isDarkTheme = !isDarkTheme
                    }) {
                        Text(if (isDarkTheme) "☀" else "🌙")
                    }
                }
                TextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text("City") }
                )
                unitToggle(isCelsius = isCelsius, onToggle = { isCelsius = it })
                Button(onClick = {
                    scope.launch {
                        val result = getWeather(city, isCelsius, location)
                        weather = result.first
                        condition = result.second
                    }
                }) {
                    Text("Get Weather")
                }
                Button(onClick = {
                    scope.launch {
                        forecast = getWeatherForecast(city, isCelsius, location)
                    }
                }) {
                    Text("Get 3-Day Forecast")
                }
                Text(weather)
                Text(forecast)
            }
        }
    }
}

@Composable
fun unitToggle(isCelsius: Boolean, onToggle: (Boolean) -> Unit) {
    Row {
        Text("Fahrenheit")
        Switch(checked = isCelsius, onCheckedChange = onToggle)
        Text("Celsius")
    }
}

suspend fun getWeather(city: String, isCelsius: Boolean, locat: JSONObject): Pair<String, String> {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://api.weatherapi.com/v1/current.json?q=${locat.getString("query")}&key=$apiKey"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBody = response.body?.string() ?: return@withContext Pair("No response body", "clear")
                val json = JSONObject(responseBody)
                val location = json.getJSONObject("location").getString("name")
                val temp = if (isCelsius) {
                    json.getJSONObject("current").getDouble("temp_c").toString() + "°C"
                } else {
                    json.getJSONObject("current").getDouble("temp_f").toString() + "°F"
                }
                val condition: String = json.getJSONObject("current").getJSONObject("condition").getString("text")
                val time = json.getJSONObject("location").getString("localtime")
                Pair(
                    "Location: $location\nTemperature: $temp\nCondition: $condition\nJSON: $json",
                    condition
                )
            }
        } catch (e: Exception) {
            Pair("Error fetching weather: ${e.message}", "clear")
        }
    }
}

suspend fun getWeatherForecast(city: String, isCelsius: Boolean, locat: JSONObject): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = "https://api.weatherapi.com/v1/forecast.json?q=${locat.getString("query")}&key=$apiKey&days=3"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val responseBody = response.body?.string() ?: return@withContext "No response body"
                val json = JSONObject(responseBody)
                val forecast = json.getJSONObject("forecast").getJSONArray("forecastday")
                val forecastString = StringBuilder()
                for (i in 0 until forecast.length()) {
                    val day = forecast.getJSONObject(i)
                    val date = day.getString("date")
                    val condition = day.getJSONObject("day").getJSONObject("condition").getString("text")
                    val temp = if (isCelsius) {
                        day.getJSONObject("day").getDouble("avgtemp_c").toString() + "°C"
                    } else {
                        day.getJSONObject("day").getDouble("avgtemp_f").toString() + "°F"
                    }
                    forecastString.append("Date: $date\nCondition: $condition\nTemperature: $temp\n\n")
                }
                forecastString.toString()
            }
        } catch (e: Exception) {
            "Error fetching weather forecast: ${e.message}"
        }
    }
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}