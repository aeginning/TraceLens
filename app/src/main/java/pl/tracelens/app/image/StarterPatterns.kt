package pl.tracelens.app.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.util.Locale

data class StarterPattern(
    val id: String,
    val file: String,
    val nameEn: String,
    val namePl: String,
    val category: String,
    val tags: List<String>,
    val author: String,
    val license: String,
    val source: String,
)

object StarterPatterns {
    private const val ROOT = "starter_patterns"

    fun load(context: Context): List<StarterPattern> = runCatching {
        val json = context.assets.open("$ROOT/manifest.json").bufferedReader().use { it.readText() }
        val patterns = JSONObject(json).getJSONArray("patterns")
        buildList {
            for (index in 0 until patterns.length()) {
                val item = patterns.getJSONObject(index)
                val names = item.getJSONObject("name")
                add(
                    StarterPattern(
                        id = item.getString("id"),
                        file = item.getString("file"),
                        nameEn = names.getString("en"),
                        namePl = names.optString("pl", names.getString("en")),
                        category = item.optString("category", "other"),
                        tags = item.optJSONArray("tags")?.let { tags ->
                            buildList {
                                for (tagIndex in 0 until tags.length()) add(tags.getString(tagIndex))
                            }
                        }.orEmpty(),
                        author = item.getString("author"),
                        license = item.getString("license"),
                        source = item.optString("source"),
                    )
                )
            }
        }
    }.getOrDefault(emptyList())

    fun bitmap(context: Context, pattern: StarterPattern): Bitmap? =
        context.assets.open("$ROOT/${pattern.file}").use(BitmapFactory::decodeStream)

    fun localizedName(pattern: StarterPattern, locale: Locale): String =
        if (locale.language == "pl") pattern.namePl else pattern.nameEn
}
