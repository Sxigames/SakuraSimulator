package net.mrgaabriel.bomdiagenerator.generator

import com.github.kevinsawicki.http.HttpRequest
import mu.KotlinLogging
import net.mrgaabriel.bomdiagenerator.utils.drawCenteredStringWrapOutline
import net.mrgaabriel.bomdiagenerator.utils.drawStringOutline
import net.mrgaabriel.bomdiagenerator.utils.graphics
import org.jsoup.Jsoup
import java.awt.Color
import java.awt.Font
import java.awt.image.BufferedImage
import java.io.File
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.imageio.ImageIO
import kotlin.random.Random


class ImageGenerator {
    val colors = listOf(
        Color(220,64,72), // vermelho
        Color(255, 255, 255), // branco
        Color(246,130,31), // laranja
        Color(254,185,19), // amarelo
        Color(122,205,241), // azul claro
        Color(255,79,121), // rosa
        Color(42,201,133) // verde
    )

    val logger = KotlinLogging.logger {}

    fun generateImage(): BufferedImage {
        val date = OffsetDateTime.now()
        val dateFormatted = date.format(DateTimeFormatter.ofPattern("dd/MM/YYYY"))

        val quote = fetchDayQuote()

        val image = fetchRandomImage(700, 700, "rain")
        val graphics = image.graphics()

        val charlotte = Font.createFont(Font.PLAIN, File("assets", "charlotte.ttf"))

        graphics.font = charlotte.deriveFont(96f)
        graphics.color = colors.random()

        graphics.drawCenteredStringWrapOutline("Sdd da Mãe do Brentt", 700, 160, 6)

        val ubuntuItalic = Font.createFont(Font.PLAIN, File("assets", "Ubuntu-Italic.ttf"))
        val ubuntuRegular = Font.createFont(Font.PLAIN, File("assets", "Ubuntu-Regular.ttf"))

        graphics.color = colors.random()
        graphics.font = ubuntuItalic.deriveFont(40f)
        val endY = graphics.drawCenteredStringWrapOutline(quote.phrase, 700, 400, 4)

        graphics.font = ubuntuRegular.deriveFont(27f)
        val quoteAuthor = "- ${quote.author}"
        graphics.drawStringOutline(quoteAuthor, 700 - graphics.fontMetrics.stringWidth(quoteAuthor) - 10, endY, 2)

        graphics.font = ubuntuRegular.deriveFont(21f)

        graphics.color = colors.random()
        graphics.drawStringOutline("${date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("pt-br"))} • $dateFormatted", 10, 690, 2)

        val handle = "Bom Dia Generator"
        val url = "sim"

        graphics.drawStringOutline(handle, 700 - graphics.fontMetrics.stringWidth(handle) - 10, 660, 2)
        graphics.drawStringOutline(url, 700 - graphics.fontMetrics.stringWidth(url) - 10, 690, 2)

        logger.info { "Image generated successfully!" }

        return image
    }

    fun fetchRandomImage(width: Int, height: Int, vararg queries: String): BufferedImage {
        val url = "https://source.unsplash.com/${width}x$height/?${queries.joinToString(",") { it.split(" ").joinToString("%20") }}"

        val request = HttpRequest.get(url)
            .followRedirects(true)
            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")

        if (!request.ok()) {
            throw RuntimeException("Request for \"$url\" is not OK! Code: ${request.code()}")
        }

        logger.debug { "Random image fetched successfully! ${request.url()}" }

        return ImageIO.read(request.stream())
    }

    // Scrapping
    fun fetchDayQuote(): Quote {
        val url = "https://dailyverses.net/get/random?language=arc&isdirect=1&position=" + Random.nextInt(0, 1341)
        val request = HttpRequest.get(url)
            .followRedirects(true)
            .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.103 Safari/537.36")

        if (!request.ok()) {
            throw RuntimeException("Request to \"$url\" is not OK! Code: ${request.code()}")
        }

        val jsoup = Jsoup.parse(request.body())

        val author = jsoup.selectFirst(".bibleVerse > a").text()
        val phrase = jsoup.selectFirst(".bibleText").text()

        val quote = Quote(phrase, author)
        logger.debug { "Quote fetched successfully! $quote" }

        return quote
    }

    data class Quote(val phrase: String, val author: String)
}
