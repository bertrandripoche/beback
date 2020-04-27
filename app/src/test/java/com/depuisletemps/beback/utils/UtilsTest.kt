package com.depuisletemps.beback.utils

import com.depuisletemps.beback.model.Loan
import com.google.common.truth.Truth
import com.google.firebase.Timestamp
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.joda.time.DateTimeZone
import org.joda.time.IllegalFieldValueException
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.powermock.api.mockito.PowerMockito
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@PrepareForTest(org.joda.time.LocalDate::class)
@RunWith(PowerMockRunner::class)
class UtilsTest {

    lateinit var past: LocalDate
    lateinit var pastString: String
    lateinit var pastTimestamp: Timestamp
    lateinit var today: LocalDate
    lateinit var todayString: String
    lateinit var todayTimestamp: Timestamp
    lateinit var future: LocalDate
    lateinit var futureString: String
    lateinit var futureTimestamp: Timestamp
    lateinit var nearFutureTimestamp: Timestamp
    lateinit var farFutureTimestamp: Timestamp

    @Before
    fun setup() {
        DateTimeZone.setDefault(DateTimeZone.UTC)

        past = LocalDate(2020,1,25)
        pastString = "25/01/2020"
        pastTimestamp = Timestamp(1579939200,0)

        today = LocalDate(2020,2,5)
        todayString = "05/02/2020"
        todayTimestamp = Timestamp(1580889600,0)

        future = LocalDate(2020,2,15)
        futureString = "15/02/2020"
        futureTimestamp = Timestamp(1581750000,0)
        nearFutureTimestamp = Timestamp(1580976000,0)
        farFutureTimestamp = Timestamp(1586160000,0)
    }

    @Test
    fun havingADifferenceOf10daysBetweenTodayAndAFutureDayShouldReturn10() {
        PowerMockito.mockStatic(LocalDate::class.java)
        `when`(LocalDate.now()).doReturn(today)

        Truth.assertThat(Utils.getDifferenceDays(today, future)).isEqualTo(10)
    }

    @Test
    fun havingADifferenceOf10daysBetweenAFutureDayAndTodayShouldReturnminus10() {
        PowerMockito.mockStatic(LocalDate::class.java)
        `when`(LocalDate.now()).doReturn(today)

        Truth.assertThat(Utils.getDifferenceDays(today, past)).isEqualTo(-11)
    }

    @Test
    fun havingTheSameDayShouldReturn0() {
        PowerMockito.mockStatic(LocalDate::class.java)
        `when`(LocalDate.now()).doReturn(today)

        Truth.assertThat(Utils.getDifferenceDays(today, today)).isEqualTo(0)
    }

    @Test
    fun havingAReturnedDateBeforeDueDateShouldReturn1Point() {
        var loan = mock<Loan>()
        whenever(loan.due_date).doReturn(todayTimestamp)
        whenever(loan.returned_date).doReturn(pastTimestamp)

        Truth.assertThat(Utils.retrievePointsFromLoan(loan)).isEqualTo(1)
    }

    @Test
    fun havingAReturnedDateEqualToDueDateShouldReturn1Point() {
        var loan = mock<Loan>()
        whenever(loan.due_date).doReturn(todayTimestamp)
        whenever(loan.returned_date).doReturn(todayTimestamp)

        Truth.assertThat(Utils.retrievePointsFromLoan(loan)).isEqualTo(1)
    }

    @Test
    fun havingAReturnedDateJustAfterDueDateShouldReturn2Points() {
        var loan = mock<Loan>()
        whenever(loan.due_date).doReturn(todayTimestamp)
        whenever(loan.returned_date).doReturn(nearFutureTimestamp)

        Truth.assertThat(Utils.retrievePointsFromLoan(loan)).isEqualTo(2)
    }

    @Test
    fun havingAReturnedDateAbitAfterDueDateShouldReturn3Points() {
        var loan = mock<Loan>()
        whenever(loan.due_date).doReturn(todayTimestamp)
        whenever(loan.returned_date).doReturn(futureTimestamp)

        Truth.assertThat(Utils.retrievePointsFromLoan(loan)).isEqualTo(3)
    }

    @Test
    fun havingAReturnedDateLongAfterDueDateShouldReturn4Points() {
        var loan = mock<Loan>()
        whenever(loan.due_date).doReturn(todayTimestamp)
        whenever(loan.returned_date).doReturn(farFutureTimestamp)

        Truth.assertThat(Utils.retrievePointsFromLoan(loan)).isEqualTo(4)
    }

    @Test
    fun passingACorrectlyFormattedDateShouldReturnALocalDate() {
        Truth.assertThat(Utils.getLocalDateFromString(todayString)).isEqualTo(today)
    }

    @Test(expected = IllegalArgumentException::class)
    fun passingACorrectlyFormattedDateShouldReturnAnException() {
        Utils.getLocalDateFromString("abc123")
    }

    @Test(expected = IllegalFieldValueException::class)
    fun passingAWrongDateShouldReturnAnException() {
        Utils.getLocalDateFromString("30/02/2020")
    }

    @Test
    fun passingADayAfterTodayShouldReturnFalse() {
        PowerMockito.mockStatic(LocalDate::class.java)
        `when`(LocalDate.now()).doReturn(today)
        `when`(LocalDate.parse(futureString, DateTimeFormat.forPattern("dd/MM/yyyy"))).doReturn(future)

        assertFalse(Utils.isStringDatePassed(futureString))
    }

    @Test
    fun passingTodayShouldReturnFalse() {
        PowerMockito.mockStatic(LocalDate::class.java)
        `when`(LocalDate.now()).doReturn(today)
        `when`(LocalDate.parse(todayString, DateTimeFormat.forPattern("dd/MM/yyyy"))).doReturn(today)

        assertFalse(Utils.isStringDatePassed(todayString))
    }

    @Test
    fun passingADayBeforeTodayShouldReturnTrue() {
        PowerMockito.mockStatic(LocalDate::class.java)
        `when`(LocalDate.now()).doReturn(today)
        `when`(LocalDate.parse(pastString, DateTimeFormat.forPattern("dd/MM/yyyy"))).doReturn(past)

        assertTrue(Utils.isStringDatePassed(pastString))
    }
}