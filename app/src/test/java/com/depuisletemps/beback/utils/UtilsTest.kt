package com.depuisletemps.beback.utils

import com.depuisletemps.beback.model.Loan
import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.doReturn
import org.joda.time.LocalDate
import org.junit.Before
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito
import org.powermock.api.mockito.PowerMockito.mock
import org.powermock.core.classloader.annotations.PrepareForTest

@PrepareForTest(Utils::class)
class UtilsTest {

    lateinit var today: LocalDate
    lateinit var futureDate: LocalDate

    @Before
    fun setup() {

        PowerMockito.mockStatic(org.joda.time.LocalDate::class.java)
        PowerMockito.`when`(LocalDate.now()).doReturn(today)

    }

    @Test
    fun getDifferenceDays() {
        today = LocalDate(2020,2,5)
        futureDate = LocalDate(2020,2,20)

        PowerMockito.mockStatic(org.joda.time.LocalDate::class.java)
        PowerMockito.`when`(LocalDate.now()).doReturn(today)

        Truth.assertThat(Utils.getDifferenceDays(today, futureDate)).isEqualTo(15)
    }

    @Test
    fun test() {

    }
}