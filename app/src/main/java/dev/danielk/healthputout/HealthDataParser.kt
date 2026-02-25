package dev.danielk.healthputout

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object HealthDataParser {
    fun parseCsvData(csvData: String): List<Pair<LocalDateTime, Float>> {
        // android:text="형식: yyyy-MM-dd,체중\n예시: 2026-02-26,75.5"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        return csvData.trim()
            .lineSequence()
            .filter { it.isNotBlank() }
            .map { line ->
                val parts = line.split(",")
//                val dateTime = LocalDateTime.parse(parts[0].trim(), formatter)
                val dateTime = LocalDate.parse(parts[0].trim(), formatter).atStartOfDay()
                val weight = parts[1].trim().toFloat() // Weight는 보통 Double/Float
                Pair(dateTime, weight)
            }
            .toList()
    }
}