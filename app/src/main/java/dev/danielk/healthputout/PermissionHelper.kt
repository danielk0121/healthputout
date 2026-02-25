package dev.danielk.healthputout

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataTypes

class PermissionHelper {

    suspend fun checkAndRequestReadPermission(activity: Activity) {
        val healthDataStore = HealthDataService.getStore(activity)

        val permSet = setOf(
            Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ)
        )

        try {
            val grantedPermissions = healthDataStore.getGrantedPermissions(permSet)

            // 이미 보유한 권한 확인
            val grantedPermsString = grantedPermissions.map {
                "" + it.dataType.name + "-" + it.accessType.name
            }.toString()
            Toast.makeText(activity, "보유권한: $grantedPermsString", Toast.LENGTH_LONG).show()
            Log.i("HealthSDK", "보유권한: $grantedPermsString")

            if (!grantedPermissions.containsAll(permSet)) {
                healthDataStore.requestPermissions(permSet, activity)
            }
        } catch (e: Exception) {
            Log.e("HealthSDK", "권한 요청 중 오류 발생: ${e.message}")
        }
    }

    suspend fun checkAndRequestWritePermission(activity: Activity) {
        val healthDataStore = HealthDataService.getStore(activity)

        // 읽기와 쓰기 권한을 모두 포함하는 세트 구성
//        val permSet = setOf(
//            Permission.of(DataTypes.BODY_COMPOSITION, AccessType.READ),
//            Permission.of(DataTypes.BODY_COMPOSITION, AccessType.WRITE)
//        )
        val permSet = setOf(
            Permission.of(DataTypes.BODY_COMPOSITION, AccessType.WRITE)
        )

        try {
            val grantedPermissions = healthDataStore.getGrantedPermissions(permSet)

            // 이미 보유한 권한 확인
            val grantedPermsString = grantedPermissions.map {
                "" + it.dataType.name + "-" + it.accessType.name
            }.toString()
            Log.i("HealthSDK", "보유권한: $grantedPermsString")
            Toast.makeText(activity, "보유권한: $grantedPermsString", Toast.LENGTH_LONG).show()

            if (!grantedPermissions.containsAll(permSet)) {
                healthDataStore.requestPermissions(permSet, activity) // 에러 발생 ?
            }
        } catch (e: Exception) {
            Log.e("HealthSDK", "권한 요청 중 오류 발생: ${e.message}")
            Toast.makeText(activity, "권한 요청 중 오류 발생: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}