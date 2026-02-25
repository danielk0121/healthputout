package dev.danielk.healthputout

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.error.HealthDataException
import com.samsung.android.sdk.health.data.error.ResolvablePlatformException
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.Ordering
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {

    private lateinit var healthDataStore: HealthDataStore
    private lateinit var recyclerView: RecyclerView
    private val weightLogList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.rvWeightList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // 버튼 클릭 리스너 추가
        val btnFetch = findViewById<android.widget.Button>(R.id.btnFetch)
        btnFetch.setOnClickListener {
            lifecycleScope.launch {
                // 권한 체크 후 데이터 읽기 실행
                if (checkForPermissions(this@MainActivity)) {
                    readWeightData()
                }
            }
        }

        healthDataStore = HealthDataService.getStore(this)

        // 초기 앱 실행 시에도 한 번 불러오고 싶다면 유지, 버튼으로만 동작하게 하려면 주석 처리 가능합니다.
//        lifecycleScope.launch {
//            if (checkForPermissions(this@MainActivity)) {
//                readWeightData()
//            }
//        }
    }

    private suspend fun checkForPermissions(activity: Activity): Boolean {
        val permSet = setOf(
            Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ)
        )

        return try {
            val grantedPermissions = healthDataStore.getGrantedPermissions(permSet)
            if (grantedPermissions.containsAll(permSet)) {
                true
            } else {
                healthDataStore.requestPermissions(permSet, activity)
                // 팝업 이후 권한을 다시 체크하거나 재시작 로직이 필요할 수 있습니다.
                false
            }
        } catch (error: HealthDataException) {
            if (error is ResolvablePlatformException && error.hasResolution) {
                error.resolve(activity)
            }
            false
        }
    }

    // 가이드 문서 예제와 동일한 구조로 수정
    private suspend fun readWeightData() {
        try {
            val readRequest = DataTypes.BODY_COMPOSITION.readDataRequestBuilder
                .setOrdering(Ordering.DESC)
                .build()

            val response = healthDataStore.readData(readRequest)
            val dataList = response.dataList

            weightLogList.clear()

            // 1. UTC 기준 포맷터 (데이터 뷰어와 동일하게 'Z'를 붙이기 위해 ZoneId를 UTC로 설정)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .withZone(ZoneId.of("UTC")) // 시스템 시간을 따르지 않고 UTC 그대로 출력

            for (dataPoint in dataList) {
                val weight: Float? = dataPoint.getValue(DataType.BodyCompositionType.WEIGHT)

                // 2. UTC 시간 문자열 생성 (예: 2026-02-25T05:34:44.223Z)
                val utcTimeStr = formatter.format(dataPoint.startTime)

                // 3. 별도의 zoneOffset 정보 (예: +09:00)
                val offsetStr = dataPoint.zoneOffset.toString()

                val weightText = weight?.toString() ?: "0.0"

                // 데이터 뷰어의 한 줄 표현 방식 모방
                // [UTC시간] (+09:00) | 75.7 kg
                weightLogList.add("$utcTimeStr ($offsetStr) | $weightText kg")
            }

            runOnUiThread {
                updateRecyclerView()
            }

        } catch (e: Exception) {
            Log.e("HealthSDK", "데이터 읽기 실패: ${e.message}")
        }
    }

    private fun updateRecyclerView() {
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val tv = TextView(parent.context).apply {
                    textSize = 18f
                    setPadding(32, 16, 32, 16)
                }
                return object : RecyclerView.ViewHolder(tv) {}
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as TextView).text = weightLogList[position]
            }
            override fun getItemCount() = weightLogList.size
        }
    }
}