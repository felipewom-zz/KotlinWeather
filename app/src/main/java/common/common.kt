package common

import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by felipewom on 02/09/17.
 */
object Common{
    val API_KEY = "76f44f5193a2923c42d66c2b7bf93195"
    val API_LINK = "http://api.openweathermap.org/data/2.5/"

    fun apiRequestWeather(lat:String, lon:String):String{
        var sb = StringBuilder(API_LINK)
        sb.append("weather?lat=${lat}&lon=${lon}&APPID=${API_KEY}&units=metric")
        return sb.toString()
    }

    fun unixTimeStampToDateTime(unixTimeStamp:Double):String{
        val dateFormat = SimpleDateFormat("HH:mm")
        val date = Date()
        date.time = unixTimeStamp.toLong() * 1000
        return dateFormat.format(date)
    }

    fun getImage(icon:String):String{
        return "http://openweathermap.org/img/w/${icon}.png"
    }

    val dateNow:String
        get(){
            val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")
            val date = Date()
            return dateFormat.format(date)
        }

}