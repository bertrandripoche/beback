package com.depuisletemps.beback.utils

import android.content.Context
import android.graphics.drawable.Drawable
import com.depuisletemps.beback.R
import com.depuisletemps.beback.model.Loan
import com.depuisletemps.beback.model.LoanAward
import com.depuisletemps.beback.model.LoanType
import com.google.firebase.Timestamp
import org.joda.time.Days
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object Utils {

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

    /**
     * This method returns the number of days between both dates,
     * being positive if the 1st date argument is before the 2nd date argument
     * @param today representing the current day
     * @param dueDate representing the due date of the loan
     * @return an Int representing the number of days between today and dueDate (positive if today is before dueDate)
     */
    fun getDifferenceDays(today: LocalDate, dueDate: LocalDate): Int {
        return Days.daysBetween(today,dueDate).days
    }

    /**
     * This method returns true if a date is already passed
     * @param date is a String representing a date (dd/MM/yyyy format)
     * @return a Boolean
     */
    fun isStringDatePassed(date: String): Boolean {
        return Days.daysBetween(LocalDate.now(),getLocalDateFromString(date)).days < 0
    }

    /**
     * This method returns a LocalDate object
     * @param date is a String representing a date (dd/MM/yyyy format)
     * @return a LocalDate object corresponding to the String date
     */
    fun getLocalDateFromString(date: String): LocalDate {
        val formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy")
        return LocalDate.parse(date, formatter)
    }

    /**
     * This method returns a String representing the LocalDate date input
     * @param date is a LocalDate object
     * @return a String object corresponding to the LocalDate date
     */
    fun getStringFromLocalDate(localDate: LocalDate): String {
        return localDate.toString("dd/MM/yyyy")
    }

    /**
     * Allows to get a String from a Date
     * @param date provides a date (as a Date)
     * @return a String which represents the date
     */
    fun getStringFromDate(date: Date?): String {
        val df: DateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
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
     * This method returns the opposite field, eg : Borrowing -> Ended_borrowing
     * @param type is the type of loan of the Loan object
     * @return a String which is the "opposite" status of the loan type
     */
    fun reverseTypeField(type: String): String {
        return when (type) {
            LoanType.LENDING.type -> LoanType.ENDED_LENDING.type
            LoanType.BORROWING.type -> LoanType.ENDED_BORROWING.type
            else -> LoanType.ENDED_DELIVERY.type
        }
    }

    /**
     * This method returns the opposite field, eg : Borrowing -> Ended_borrowing
     * @param type is the type of loan of the Loan object
     * @return a String which is the "opposite" status of the loan type
     */
    fun awardsByType(type: String): String {
        return when (type) {
            LoanType.BORROWING.type -> LoanAward.MINE.type
            else -> LoanAward.THEIR.type
        }
    }

    /**
     * This method returns the number of points given to user (for borrowing) or recipient (for lending and delivery)
     * @param daysDiff is the difference of days between returned date and due date
     * @return a Int which is the number of points to attribute
     */
    fun getPoints(daysDiff: Int): Int {
        return when {
            daysDiff > 30 -> 4
            daysDiff > 7 -> 3
            daysDiff >= 1 -> 2
            else -> 1
        }
    }

    /**
     * This method retrieves the number of points which had been allocated to corresponding loan
     * @param loan being the loan which want to retrieve the points attributed for
     * @return a Long which is the number of points distributed for this loan
     */
    fun retrievePointsFromLoan(loan: Loan): Long {
        var points: Long = 1
        if (getStringFromDate(loan.due_date?.toDate()) != Constant.FAR_AWAY_DATE && loan.returned_date != null) {
            val dueDateLocalDate = getLocalDateFromString(getStringFromDate(loan.due_date?.toDate())) //getLocalDateFromString(loan.due_date.toString())
            val returnedLocalDate = getLocalDateFromString(getStringFromDate(loan.returned_date?.toDate())) //getLocalDateFromString(loan.returned_date!!.toString())
            val daysDiff: Int = getDifferenceDays(dueDateLocalDate, returnedLocalDate)
            points = getPoints(daysDiff).toLong()
        }
        return points
    }
}
