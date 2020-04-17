package com.depuisletemps.beback.utils

import com.depuisletemps.beback.model.FieldType
import com.google.common.truth.Truth
import org.junit.Test

import org.powermock.core.classloader.annotations.PrepareForTest

@PrepareForTest(StringUtilsTest::class)
class StringUtilsTest {

    @Test
    fun capitalizeWordsOnRecipientNameShouldReturnAllFirstLettersCapitalized() {
        Truth.assertThat(StringUtils.capitalizeWords("jean-claude van damme", FieldType.NAME)).isEqualTo("Jean-Claude Van Damme")
    }

    @Test
    fun capitalizeWordsOnCapitalizedRecipientNameShouldReturnAllCapitalizedName() {
        Truth.assertThat(StringUtils.capitalizeWords("J-C VD", FieldType.NAME)).isEqualTo("J-C VD")
    }

    @Test
    fun capitalizeWordsOnProductShouldReturnAllFirstLettersCapitalized() {
        Truth.assertThat(StringUtils.capitalizeWords("blue canoe-kayak", FieldType.PRODUCT)).isEqualTo("Blue canoe-kayak")
    }

    @Test
    fun capitalizeWordsOnCapitalizedProductShouldReturnAllFirstLettersCapitalized() {
        Truth.assertThat(StringUtils.capitalizeWords("BLUE CANOE-KAYAK", FieldType.PRODUCT)).isEqualTo("Blue canoe-kayak")
    }
}