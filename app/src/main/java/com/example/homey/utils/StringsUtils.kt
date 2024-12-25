package com.example.homey.utils

import java.util.Locale

object StringUtils {

    fun removeVietnameseAccents(str: String): String {
        val vietnameseChars = arrayOf(
            "aáàảãạâấầẩẫậăắằẳẵặ",
            "eéèẻẽẹêếềểễệ",
            "iíìỉĩị",
            "oóòỏõọôốồổỗộơớờởỡợ",
            "uúùủũụưứừửữự",
            "yýỳỷỹỵ",
            "dđ"
        )

        val correspondingChars = arrayOf(
            'a', 'e', 'i', 'o', 'u', 'y', 'd'
        )

        val normalizedStr = StringBuilder(str.lowercase(Locale.getDefault()))
        vietnameseChars.forEachIndexed { index, chars ->
            chars.forEach { char ->
                val normalizedChar = correspondingChars[index]
                for (i in normalizedStr.indices) {
                    if (normalizedStr[i] == char) {
                        normalizedStr.setCharAt(i, normalizedChar)
                    }
                }
            }
        }
        return normalizedStr.toString()
    }
}
