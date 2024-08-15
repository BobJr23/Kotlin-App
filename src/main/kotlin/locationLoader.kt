import java.io.File

fun saveFavoriteLocations(locations: List<String>, filePath: String) {
    File(filePath).writeText(locations.joinToString("\n"))
}

fun loadFavoriteLocations(filePath: String): List<String> {
    return if (File(filePath).exists()) {
        File(filePath).readLines()
    } else {
        emptyList()
    }
}