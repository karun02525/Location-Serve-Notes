package `in`.antef.network.utils

object Constants {
    const val BASE_URL = "https://geonote-dev-mobile.s3.ap-south-1.amazonaws.com/"

    fun String.removeBaseUrl(): String {
        return this.removePrefix(BASE_URL)
    }

}