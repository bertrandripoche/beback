package com.depuisletemps.beback.utils

import com.depuisletemps.beback.model.FieldType
import org.apache.commons.text.WordUtils
import java.util.*

object StringUtils {

    fun capitalizeWords(s: String, fieldType: FieldType): String {

        return when {
            fieldType == FieldType.PRODUCT -> {
                WordUtils.capitalize(s.toLowerCase(Locale.getDefault()),'#')
            }
            s == s.toUpperCase(Locale.getDefault()) -> s
            fieldType == FieldType.NAME -> WordUtils.capitalizeFully(s,' ' ,'-')
            else -> WordUtils.capitalize(s)
        }
    }
}