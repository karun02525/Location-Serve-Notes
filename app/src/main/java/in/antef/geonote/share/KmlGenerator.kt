package `in`.antef.geonote.share

import android.content.Context
import `in`.antef.geonote.domain.model.ProjectModel
import `in`.antef.geonote.utils.isAudioFile
import `in`.antef.geonote.utils.isPhotoFile
import `in`.antef.geonote.utils.isVideoFile
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility class to generate KML files from ShareProjectModel data
 */
object KmlGenerator {

    /**
     * Generates KML content for a ShareProjectModel
     * @param project The project data to convert to KML
     * @return KML format string
     */
    fun generateKml(project: ProjectModel): String {
        val kmlBuilder = StringBuilder()

        // KML header
        kmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        kmlBuilder.append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
        kmlBuilder.append("  <Document>\n")
        kmlBuilder.append("    <name>${project.title}</name>\n")
        kmlBuilder.append("    <description>Project ID: ${project.projectId}</description>\n")

        // Style for placemarks
        kmlBuilder.append("    <Style id=\"pinStyle\">\n")
        kmlBuilder.append("      <IconStyle>\n")
        kmlBuilder.append("        <Icon><href>http://maps.google.com/mapfiles/kml/pushpin/red-pushpin.png</href></Icon>\n")
        kmlBuilder.append("      </IconStyle>\n")
        kmlBuilder.append("    </Style>\n")

        // Add coordinate points
        project.coordinates.forEachIndexed { index, point ->
            kmlBuilder.append("    <Placemark>\n")
            kmlBuilder.append("      <name>Point ${point.title}</name>\n")

            // Add description with image and audio file info
            kmlBuilder.append("      <description><![CDATA[\n")
            if (point.media.isNotEmpty()) {
                kmlBuilder.append("<h3>Photos</h3>\n")
                point.media.forEach {
                    if(it.path.isPhotoFile()) {
                        kmlBuilder.append("<img src=\"${ it.path}\" width=\"200\" height=\"500\" style=\"margin: 5px;\"/><br/>\n")
                    }
                }
                kmlBuilder.append("<h3>Videos</h3>\n")
                point.media.forEach {
                    if(it.path.isVideoFile()) {
                        kmlBuilder.append("<video controls width=\"200\" height=\"500\" style=\"margin: 5px;\"><source src=\"${it.path}\" type=\"video/mp4\"></video><br/>\n")
                    }
                }
            }
            if (point.media.isNotEmpty()) {
                kmlBuilder.append("<h3>Audio Recordings</h3>\n")
                point.media.forEach {
                    if(it.path.isAudioFile()) {
                        kmlBuilder.append("<audio controls style=\"margin: 5px;\"><source src=\"${it.path}\" type=\"audio/mpeg\"></audio><br/>\n")
                    }
                }
            }

            kmlBuilder.append("      ]]></description>\n")
            kmlBuilder.append("      <styleUrl>#pinStyle</styleUrl>\n")
            kmlBuilder.append("      <Point>\n")
            kmlBuilder.append("        <coordinates>${point.longitude},${point.latitude}</coordinates>\n")
            kmlBuilder.append("      </Point>\n")
            kmlBuilder.append("    </Placemark>\n")
        }

        // KML footer
        kmlBuilder.append("  </Document>\n")
        kmlBuilder.append("</kml>")

        return kmlBuilder.toString()
    }

    /**
     * Saves a KML string to a temporary file
     * @param context Android context
     * @param kmlContent The KML content to save
     * @param fileName The name for the KML file
     * @return The created File or null if creation failed
     */
    fun saveKmlToFile(
        context: Context,
        kmlContent: String,
        fileName: String
    ): File? {
        try {
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { output ->
                output.write(kmlContent.toByteArray())
            }
            return file
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
}

