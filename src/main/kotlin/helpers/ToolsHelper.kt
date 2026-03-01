package org.delcom.helpers

fun parseMessageToMap(rawMessage: String): Map<String, List<String>> {
    return rawMessage.split("|").mapNotNull { part ->
        val split = part.split(":", limit = 2)
        if (split.size == 2) {
            val key = split[0].trim()
            val value = split[1].trim()
            key to listOf(value)
        } else null
    }.toMap()
}