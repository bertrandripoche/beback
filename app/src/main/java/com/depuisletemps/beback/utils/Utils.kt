package com.depuisletemps.beback.utils

import com.depuisletemps.beback.R
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
        val categories = arrayOf<String>("Miscellaneous","Appliance","Books","Clothes","Games","Money","Sport","Tools","Utensil")
        val icons = arrayOf<Int>(R.drawable.ic_miscellaneous,R.drawable.ic_appliance,R.drawable.ic_books,R.drawable.ic_clothes,R.drawable.ic_games,R.drawable.ic_money,R.drawable.ic_sport,R.drawable.ic_tools,R.drawable.ic_kitchen)

        val index:Int = categories.indexOf(category)
        return icons[index]
    }

    companion object {
        private val RIGHT_DATE_FORMAT: DateFormat =
            SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)

        fun getTodayDate(): String {
            val thisDay = Calendar.getInstance()
            val today = thisDay.timeInMillis

            return RIGHT_DATE_FORMAT.format(today)
        }
    }
}
