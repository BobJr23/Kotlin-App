import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

fun saveApiKeyToDotenv(apiKey: String) {
    val dotenvPath = Paths.get(".env")
    val content = "WEATHER_KEY=$apiKey\n"
    Files.write(dotenvPath, content.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
}