package com.depuisletemps.beback.utils

import android.content.Context
import android.graphics.drawable.Drawable
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

    companion object {
        /**
         * Method which allows to get the flattype string from the saved spinner position whatever the language
         * @param category provides a category to the method
         * @param context provides a context to the method
         * @return the resource id Int which matches the categories
         */
        fun getIndexFromCategory(category: String, context: Context): Int {
            val categories: Array<String> = context.resources.getStringArray(R.array.product_category)
            return categories.indexOf(category)
        }

        /**
         * Method which allows to get the category icon Int from the saved spinner position whatever the language
         * @param category provides a category to the method
         * @param context provides a context to the method
         * @return the resource id Int which matches the categories
         */
        fun getIconFromCategory(category: String, context: Context): Drawable {
            val categories: Array<String> = context.resources.getStringArray(R.array.product_category)
            val categoriesIcons = context.resources.obtainTypedArray(R.array.product_category_icon)
            val index:Int = categories.indexOf(category)
            val drawable = categoriesIcons.getDrawable(index)
            categoriesIcons.recycle()
            return drawable
        }

        /**
         * This method returns the readable name of the category in the required langage
         * @param category provides a category to the method
         * @param context provides a context to the method
         * @return a String which is the name of the category in the language
         */
        fun transformCategoryNumberIntoTranslatedWord(categoryNumber: String, context: Context): String {
            return context.resources.getString(context.resources.getIdentifier(categoryNumber, Constant.STRING, Constant.PACKAGE))
        }

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

        /**
         * Allows to get a String from a Date
         * @param date provides a date (as a Date)
         * @return a String which represents the date
         */
        fun getStringFromDate(date: Date?): String {
            val df: DateFormat = SimpleDateFormat("dd/MM/yyyy")
            return df.format(date)
        }

        /**
         * Allows to get a Timestamp from a date (as a String)
         * @param date provides a date (as a String)
         * @return a Timestamp which represents the date
         */
        fun getTimeStampFromString(date: String): Timestamp? {
            val formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
            val localDate: LocalDate = LocalDate.parse(date, formatter)
            val dateFromLocalDate = java.sql.Date.valueOf(localDate.toString())
            return if (date != null) {
                Timestamp(dateFromLocalDate)
            } else null
        }

        /**
         * Allows to get data to correctly populate the spinner whatever the language
         * @param context provides a context to the method
         * @return an array of String for categories spinner which matches the smartphone language
         */
        fun createDataForCategoriesSpinners(context: Context): Array<String?>? {
            val origin = context.resources.getStringArray(R.array.product_category)
            val finalData = arrayOfNulls<String>(14)
            for (i in origin.indices) {
                val resId = context.resources.getIdentifier(origin[i], "string", "com.depuisletemps.beback")
                finalData[i] = context.getString(resId)
            }
            return finalData
        }

        /**
         * Allows to get data to correctly populate the spinner whatever the language
         * @param context provides a context to the method
         * @return an array of String for categories spinner which matches the smartphone language
         */
        fun createIconsDataForCategoriesSpinners(context: Context): Array<String?>? {
            val origin = context.resources.getStringArray(R.array.product_category_icon)
            val finalData = arrayOfNulls<String>(14)

            for (i in origin.indices) {
                val resId = context.resources.getIdentifier(origin[i], "string", "com.depuisletemps.beback")
                finalData[i] = context.getString(resId)
            }
            return finalData
        }

        /**
         * Method which allows to get the category string from the saved spinner position whatever the language
         * @param context provides a context to the method
         * @param position is the saved position (of the item in the spinner)
         * @return the category String which matches the smartphone language
         */
        fun getStringFromCategorySpinners(
            context: Context,
            position: Int
        ): String? {
            val origin =
                context.resources.getStringArray(R.array.product_category)
            val resId = context.resources.getIdentifier(origin[position],"string","com.depuisletemps.beback")
            return context.getString(resId)
        }

    }
}
