package com.depuisletemps.beback.utils

import com.google.common.truth.Truth
import com.nhaarman.mockitokotlin2.doReturn
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest

@PrepareForTest(Utils::class)
class UtilsTest {

    @Test
    fun getDifferenceDays() {
        val today = LocalDate(2020,2,5)
        val futureDate = LocalDate(2020,2,20)

        PowerMockito.mockStatic(LocalDate::class.java)
        PowerMockito.`when`(LocalDate.now()).doReturn(today)

        Truth.assertThat(Utils.getDifferenceDays(today, futureDate)).isEqualTo(15)
    }
}