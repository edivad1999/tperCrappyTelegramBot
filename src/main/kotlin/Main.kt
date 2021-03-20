import com.github.kittinunf.fuel.httpGet
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.location
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Location
import com.google.gson.Gson
import com.google.gson.stream.JsonReader
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.math.*


fun main() {


    val gson = Gson()
    var reader: JsonReader = JsonReader(File("fermate.json").bufferedReader())
    val data: List<Fermata> = gson.fromJson<Array<Fermata>>(reader, Array<Fermata>::class.java).toList()
    reader.close()
    val bot = bot {

        token = File("settings.txt").bufferedReader().readLine()

        dispatch {
            command("start") {
                val result = bot.sendMessage(
                    chatId = ChatId.fromId(message.chat.id),
                    text = "Invia la posizione e provvederò a inviarti le fermate che si trovano a 100m da te"
                )
            }

            text {
                if (text != "start") {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id), text = "Inviami la tua posizione!")

                }
            }
            location {
                var res = searchForStopsInRadius(data, location)
                if (res.size == 0) {
                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id),
                        text = "Non ci sono fermate nelle vicinanze")

                }
                for (fermata in res) {


                    bot.sendMessage(chatId = ChatId.fromId(message.chat.id),
                        text = prettyFermata(fermata) + "\n ${helloBus(fermata.codice)}")

                }


            }
        }
    }

    bot.startPolling()
}

fun helloBus(codice: Int): String {

    val url = "http://hellobuswsweb.tper.it/web-services/hello-bus.asmx/QueryHellobus?fermata=$codice&linea=&oraHHMM="

    val (_, response, result) = url.httpGet().responseString()


    var outputString = result.component1().toString()
    val newDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
    val parse: Document = newDocumentBuilder.parse(ByteArrayInputStream(outputString.toByteArray()))
    var final = parse.getFirstChild().getTextContent()
    final = final.removePrefix("TperHellobus: ")
    return final


}


fun prettyFermata(fermata: Fermata): String {
    return "Fermata: ${fermata.denominazione}, codice fermata: ${fermata.codice}\nUbicazione: ${fermata.ubicazione}\n "
}

fun searchForStopsInRadius(data: List<Fermata>, location: Location): MutableList<Fermata> {
    var result = mutableListOf<Fermata>()

    for (fermata in data) {

        if (inRadius(
                100.0,
                location.latitude.toDouble(),
                location.longitude.toDouble(),
                fermata.latitudine.replace(',', '.').toDouble(),
                fermata.longitudine.replace(',', '.').toDouble()
            )
        ) {
//            print(fermata)
            result.add(fermata)
        }
    }
    return result
}


fun inRadius(
    radius: Double,
    latitude1: Double,
    longitudine1: Double,
    latitude2: Double,
    longitudine2: Double,
): Boolean {

    return (radius > distance(latitude1, latitude2, longitudine1, longitudine2))

}

fun distance(lat1: Double, lat2: Double, lon1: Double, lon2: Double): Double {

    // The math module contains a function
    // named toRadians which converts from
    // degrees to radians.
    var lat1 = lat1
    var lat2 = lat2
    var lon1 = lon1
    var lon2 = lon2
    lon1 = Math.toRadians(lon1)
    lon2 = Math.toRadians(lon2)
    lat1 = Math.toRadians(lat1)
    lat2 = Math.toRadians(lat2)

    // Haversine formula
    val dlon = lon2 - lon1
    val dlat = lat2 - lat1
    val a = (sin(dlat / 2).pow(2.0)
            + (cos(lat1) * cos(lat2)
            * sin(dlon / 2).pow(2.0)))
    val c = 2 * asin(sqrt(a))

    // Radius of earth in kilometers. Use 3956
    // for miles
    val r = 6371.0

    // calculate the result
    return c * r * 1000
}


data class Fermata(
    val codice: Int,
    val denominazione: String,
    val ubicazione: String,
    val comune: String,
    val coordinata_x: Long,
    val coordinata_y: Long,
    val latitudine: String,
    val longitudine: String,
    val codice_zona: Int,

    )

