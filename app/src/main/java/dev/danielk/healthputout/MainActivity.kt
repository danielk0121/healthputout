package dev.danielk.healthputout

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataTypes
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var healthDataStore: HealthDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        healthDataStore = HealthDataService.getStore(this)

        // 1. 앱 실행 시 권한 체크 및 요청
        lifecycleScope.launch {
            checkAndRequestAllPermissions()
        }

        // TabLayout 및 ViewPager2 설정
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 2
            override fun createFragment(position: Int) = if (position == 0) ReadFragment() else WriteFragment()
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "조회" else "기록(CSV)"
        }.attach()
    }

    private suspend fun checkAndRequestAllPermissions() {
        // 읽기와 쓰기 권한을 모두 포함하는 세트 구성
        val permSet = setOf(
            Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ),
            Permission.of(DataTypes.BODY_COMPOSITION, AccessType.WRITE)
        )

        try {
            val grantedPermissions = healthDataStore.getGrantedPermissions(permSet)
            if (!grantedPermissions.containsAll(permSet)) {
                // 권한이 부족한 경우 요청 팝업을 띄움
                healthDataStore.requestPermissions(permSet, this)
            }
        } catch (e: Exception) {
            Log.e("HealthSDK", "권한 요청 중 오류 발생: ${e.message}")
        }
    }
}