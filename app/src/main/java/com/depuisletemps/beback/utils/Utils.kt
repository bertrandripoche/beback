package com.depuisletemps.beback.utils

import android.content.Context
import com.depuisletemps.beback.R
import com.google.firebase.Timestamp
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class Utils {

    /**
     * Method which allows to get the flattype string from the saved spinner position whatever the language
     * @param category provides a category to the method
     * @return the resource id Int which matches the categories
     */
    fun getIconFromCategory(category: String): Int {
        val categories = arrayOf("Miscellaneous","Appliance","Books","Clothes","Electronic","Instruments","Games","Money","Music","Sport","Tools","Kitchen")
        val icons = arrayOf(R.drawable.ic_miscellaneous, R.drawable.ic_appliance,R.drawable.ic_books,R.drawable.ic_clothes,R.drawable.ic_electronic,R.drawable.ic_music_instrument,R.drawable.ic_games,R.drawable.ic_money,R.drawable.ic_music,R.drawable.ic_sport,R.drawable.ic_tools,R.drawable.ic_kitchen)

        val index:Int = categories.indexOf(category)
        return icons[index]
    }

    companion object {
        fun getTodayDate(): String {
            return LocalDate.now().toString()
        }

        fun getDifferenceDays(today: LocalDate, dueDate: LocalDate): Int {
            return Days.daysBetween(today,dueDate).days
        }

        fun getLocalDateFromString(date: String): LocalDate {
            val formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
            return LocalDate.parse(date, formatter)
        }

        fun getStringFromLocalDate(localDate: LocalDate): String {
            return localDate.toString("MM/dd/yyyy")
        }

        fun getStringFromDate(date: Date?): String {
            val df: DateFormat = SimpleDateFormat("dd/MM/yyyy")
            return df.format(date)
        }

        fun getTimeStampFromString(date: String): Timestamp? {
            val formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
            val localDate: LocalDate = LocalDate.parse(date, formatter)
            val dateFromLocalDate = java.sql.Date.valueOf(localDate.toString())
            if (date != null) {
                return Timestamp(dateFromLocalDate)
            } else return null
        }

    }
}
