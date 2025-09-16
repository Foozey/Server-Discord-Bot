package com.fooze.serverdiscordbot.util

object Placeholder {
    // Replaces placeholders in a string template with values from a map
    fun replace(template: String, values: Map<String, String>): String {
        var result = template

        values.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }

        return result
    }
}