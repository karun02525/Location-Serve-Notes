package `in`.antef.geonote.share

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import `in`.antef.geonote.domain.model.ProjectModel

/**
 * Utility class for sharing project data
 */
object ShareProjectUtils {

    /**
     * Share a project as a KML file
     * @param context Android context
     * @param project The project to share
     * @param authorities FileProvider authorities string from your app
     * @return true if sharing was initiated successfully, false otherwise
     */
    fun shareProjectAsKml(
        context: Context,
        project: ProjectModel,
        authorities: String
    ): Boolean {
        try {
            // Generate KML content
            val kmlContent = KmlGenerator.generateKml(project)
            println("*****************************")
            println(" Generated KML: "+kmlContent)
            println("*****************************")

            // Create a safe file name
            val safeProjectName = project.title.replace("[^a-zA-Z0-9]".toRegex(), "_")
            val fileName = "${safeProjectName}.kml"

            // Save KML to file
            val kmlFile = KmlGenerator.saveKmlToFile(context, kmlContent, fileName)
                ?: return false

            // Get content URI via FileProvider
            val contentUri = FileProvider.getUriForFile(context, authorities, kmlFile)

            // Create sharing intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.google-earth.kml+xml"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "KML File: ${project.title}")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Sharing project: ${project.title} (${project.projectId})"
                )
            }

            // Start the sharing activity
            context.startActivity(Intent.createChooser(shareIntent, "Share KML File"))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Share all projects as a single KML file
     * @param context Android context
     * @param projects List of projects to share
     * @param authorities FileProvider authorities string from your app
     * @param combinedFileName Name for the combined KML file
     * @return true if sharing was initiated successfully, false otherwise
     */
/*    fun shareMultipleProjectsAsKml(
        context: Context,
        projects: List<ProjectModel>,
        authorities: String,
        combinedFileName: String = "combined_projects.kml"
    ): Boolean {
        try {
            // Generate KML for each project and combine
            val combinedKml = buildString {
                append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                append("<kml xmlns=\"http://www.opengis.net/kml/2.2\">\n")
                append("  <Document>\n")
                append("    <name>Combined Projects</name>\n")

                // Add each project as a folder
                projects.forEach { project ->
                    append("    <Folder>\n")
                    append("      <name>${project.projectName}</name>\n")
                    append("      <description>Project ID: ${project.projectId}</description>\n")

                    // Add coordinate points from each project
                    project.coordinatePoints.forEachIndexed { index, point ->
                        append("      <Placemark>\n")
                        append("        <name>${project.projectName} - Point ${index + 1}</name>\n")

                        // Add description with image and audio file info
                        append("        <description><![CDATA[\n")
                        if (point.images.isNotEmpty()) {
                            append("          <p>Images: ${point.images.joinToString(", ")}</p>\n")
                        }
                        if (point.audios.isNotEmpty()) {
                            append("          <p>Audio Recordings: ${point.audios.joinToString(", ")}</p>\n")
                        }
                        append("        ]]></description>\n")

                        append("        <Point>\n")
                        append("          <coordinates>${point.longitude},${point.latitude},0</coordinates>\n")
                        append("        </Point>\n")
                        append("      </Placemark>\n")
                    }

                    append("    </Folder>\n")
                }

                append("  </Document>\n")
                append("</kml>")
            }

            // Save combined KML to file
            val kmlFile = KmlGenerator.saveKmlToFile(context, combinedKml, combinedFileName)
                ?: return false

            // Get content URI via FileProvider
            val contentUri = FileProvider.getUriForFile(context, authorities, kmlFile)

            // Create sharing intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/vnd.google-earth.kml+xml"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_SUBJECT, "Combined KML File")
                putExtra(Intent.EXTRA_TEXT, "Sharing ${projects.size} project(s)")
            }

            // Start the sharing activity
            context.startActivity(Intent.createChooser(shareIntent, "Share KML File"))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }*/
}