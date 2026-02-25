package dev.danielk.healthputout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.data.HealthDataPoint
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class WriteFragment : Fragment() {
    private lateinit var healthDataStore: HealthDataStore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_write, container, false)
        healthDataStore = HealthDataService.getStore(requireContext())

        val etInput = view.findViewById<EditText>(R.id.etCsvInput)
        val btnSave = view.findViewById<Button>(R.id.btnSave)

        btnSave.setOnClickListener {
            val csvText = etInput.text.toString()
            if (csvText.isNotBlank()) {
                lifecycleScope.launch {
                    saveCsvToHealth(csvText)
                }
            } else {
                Toast.makeText(context, "데이터를 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    private suspend fun saveCsvToHealth(csvData: String) {
        val SYSTEM_OFFSET: ZoneOffset = OffsetDateTime.now().offset

        try {
            // csv 텍스트 파싱
            val parsedList = HealthDataParser.parseCsvData(csvData)

            // 삼성 헬스 sdk 데이터로 변환
            val healthDataPoints = parsedList.map {
                val startTime = it.first.toInstant(SYSTEM_OFFSET)
                val weight = it.second
                createWeightDataPoint(startTime, SYSTEM_OFFSET, weight)
            }.toMutableList()

            // 삼성 헬스 insert 요청
            var successCount = 0
            val totalCount = healthDataPoints.size
            for(hdp in healthDataPoints) {
                if(insertWeightDataPoint(hdp)) {
                    successCount++
                }
            }

            Toast.makeText(context, "데이터 저장 종료, $successCount/$totalCount", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

    }

    private suspend fun insertWeightDataPoint(data: HealthDataPoint): Boolean {
        val insertRequest = DataTypes.BODY_COMPOSITION.insertDataRequestBuilder
            .addData(data)
            .build()

        return try {
            healthDataStore.insertData(insertRequest)
            true
        } catch (e: Exception) {
            Toast.makeText(context, "오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
            false
        }
    }

    private fun createWeightDataPoint(
        startTime: Instant,
        zoneOffset: ZoneOffset,
        weight: Float
    ): HealthDataPoint {
        return HealthDataPoint.builder()
            .setStartTime(startTime, zoneOffset)
            .addFieldData(DataType.BodyCompositionType.WEIGHT, weight)
            .build()
    }
}