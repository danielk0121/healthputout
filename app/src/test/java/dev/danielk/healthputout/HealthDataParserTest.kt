package dev.danielk.healthputout

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import dev.danielk.healthputout.HealthDataParser as Target

class HealthDataParserTest {

    @Test
    fun `time_없으면_파싱에러인가`() {
        // 시간값이 없으면 LocalDate.parse().atStartOfDay() 사용
        val withOutTime = "2026-02-26"
        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val dateTime1 = LocalDate.parse(withOutTime, formatter1).atStartOfDay()
        println(dateTime1)

        val isoStandard = "2026-02-26T14:30:40" // iso 표준 형식이면 포맷터 필요 없음
        val dateTime2 = LocalDateTime.parse(isoStandard)
        println(dateTime2)

        val customFormat = "2026-02-26 14:30:40"
        val formatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val dateTime3 = LocalDateTime.parse(customFormat, formatter3)
        println(dateTime3)
    }

    @Test
    fun `parseCsvData_정상적인_데이터_파싱_성공`() {
        // Given: 날짜와 체중이 포함된 CSV 형식의 문자열
        val csvData = """
            2026-02-25, 70.5
            2026-02-26, 72.0
        """.trimIndent()

        // When: 파싱 메서드 호출
        val result = Target.parseCsvData(csvData)

        // Then: 리스트 크기 및 데이터 정확성 검증
        assertEquals(2, result.size)

        // 첫 번째 데이터 확인
        assertEquals(LocalDateTime.of(2026, 2, 25, 0, 0), result[0].first)
        assertEquals(70.5f, result[0].second)

        // 두 번째 데이터 확인
        assertEquals(LocalDateTime.of(2026, 2, 26, 0, 0), result[1].first)
        assertEquals(72.0f, result[1].second)
    }

    @Test
    fun `parseCsvData_공백라인이_포함된_경우_무시`() {
        // Given: 중간에 공백 라인이 섞인 데이터
        val csvData = """
            2026-02-25, 70.5
            
            2026-02-26, 72.0
            
        """.trimIndent()

        // When
        val result = Target.parseCsvData(csvData)

        // Then
        assertEquals(2, result.size)
    }

    @Test(expected = Exception::class)
    fun `parseCsvData_잘못된_형식의_경우_예외발생`() {
        // Given: 날짜 형식이 잘못된 데이터
        val csvData = "2026/02/25, 70.5"

        // When: 호출 시 파싱 에러 발생 예상
        Target.parseCsvData(csvData)
    }
}