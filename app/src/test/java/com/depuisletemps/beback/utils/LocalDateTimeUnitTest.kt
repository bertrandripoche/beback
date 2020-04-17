package com.depuisletemps.beback.utils

import com.google.common.truth.Truth.assertThat
import com.nhaarman.mockitokotlin2.doReturn
import org.joda.time.LocalDate
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.powermock.api.mockito.PowerMockito.`when`
import org.powermock.api.mockito.PowerMockito.mockStatic
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

@RunWith(PowerMockRunner::class)
@PrepareForTest(org.joda.time.LocalDate::class)
class LocalDateUnitTest {

    @Test
    fun givenLocalDateTimeMock_whenNow_thenGetFixedLocalDateTime() {
        val expectedDate = LocalDate(2020,2,5)
        mockStatic(LocalDate::class.java)
        `when`(LocalDate.now()).doReturn(expectedDate)
        val now = LocalDate.now()
        assertThat(now).isEqualTo(expectedDate)
    }
}