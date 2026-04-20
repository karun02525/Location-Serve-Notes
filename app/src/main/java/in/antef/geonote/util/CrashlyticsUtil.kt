package `in`.antef.geonote.util
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Utility class for Firebase Crashlytics integration
 */
object CrashlyticsUtil {

    private const val TAG = "CrashlyticsUtil"

    /**
     * Records an exception in Firebase Crashlytics with an optional custom message
     *
     * @param exception The exception to record
     * @param message Optional custom message to log with the exception
     * @param tag Optional tag for logging (defaults to "CrashlyticsUtil")
     */

   /* init {
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(false)
        }
    }*/

    fun recordException(exception: Throwable, message: String? = null, tag: String = TAG) {
        try {
            // Log the exception
            if (message != null) {
                Log.e(tag, message, exception)
            } else {
                Log.e(tag, "Exception caught", exception)
            }

            // Record in Crashlytics
          //  val crashlytics = FirebaseCrashlytics.getInstance()

            // Add custom key if message is provided
            message?.let {
              //  crashlytics.setCustomKey("last_error_message", it)
            }

            // Record the exception
         //   crashlytics.recordException(exception)
        } catch (e: Exception) {
            // If Crashlytics fails, at least log the original exception
            Log.e(TAG, "Failed to record exception in Crashlytics", e)
        }
    }

    /**
     * Logs a message to Crashlytics and LogCat
     *
     * @param message The message to log
     * @param tag Optional tag for logging (defaults to "CrashlyticsUtil")
     */
    fun log(message: String, tag: String = TAG) {
        try {
            // Log to LogCat
            Log.d(tag, message)

            // Log to Crashlytics
            FirebaseCrashlytics.getInstance().log(message)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to log message to Crashlytics", e)
        }
    }

    /**
     * Sets a custom key-value pair in Crashlytics
     *
     * @param key The key for the custom value
     * @param value The value to set
     */
    fun setCustomKey(key: String, value: String) {
        try {
         //   FirebaseCrashlytics.getInstance().setCustomKey(key, value)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set custom key in Crashlytics", e)
        }
    }
}