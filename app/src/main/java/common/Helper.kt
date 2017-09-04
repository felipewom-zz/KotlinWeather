package common

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Created by felipewom on 02/09/17.
 */

class Helper {

    internal var stream: String? = null

    fun getHttpData(url: String): String? {
        try {
            val mURL = URL(url)
            val mURLConnection = mURL.openConnection() as HttpURLConnection
            if (mURLConnection.responseCode == HttpURLConnection.HTTP_OK) {
                val mStringBuilder = StringBuilder()
                BufferedReader(InputStreamReader(mURLConnection.inputStream)).use { reader ->
                    reader.forEachLine {
                        mStringBuilder.append(it)
                    }
                }
                stream = mStringBuilder.toString()
                mURLConnection.disconnect()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return stream
    }
}

