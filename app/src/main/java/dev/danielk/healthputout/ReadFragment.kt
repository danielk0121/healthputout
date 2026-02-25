package dev.danielk.healthputout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.samsung.android.sdk.health.data.HealthDataService
import com.samsung.android.sdk.health.data.HealthDataStore
import com.samsung.android.sdk.health.data.permission.AccessType
import com.samsung.android.sdk.health.data.permission.Permission
import com.samsung.android.sdk.health.data.request.DataType
import com.samsung.android.sdk.health.data.request.DataTypes
import com.samsung.android.sdk.health.data.request.Ordering
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ReadFragment : Fragment() {
    private lateinit var healthDataStore: HealthDataStore
    private lateinit var recyclerView: RecyclerView
    private val weightLogList = mutableListOf<String>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_read, container, false)

        healthDataStore = HealthDataService.getStore(requireContext())
        recyclerView = view.findViewById(R.id.rvWeightList)
        recyclerView.layoutManager = LinearLayoutManager(context)

        view.findViewById<Button>(R.id.btnFetch).setOnClickListener {
            lifecycleScope.launch {
                readWeightData()
            }
        }
        return view
    }

    private suspend fun readWeightData() {
        try {
            val readRequest = DataTypes.BODY_COMPOSITION.readDataRequestBuilder
                .setOrdering(Ordering.DESC).build()
            val response = healthDataStore.readData(readRequest)

            weightLogList.clear()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").withZone(ZoneId.of("UTC"))

            for (dataPoint in response.dataList) {
                val weight = dataPoint.getValue(DataType.BodyCompositionType.WEIGHT) ?: 0.0f
                val utcTimeStr = formatter.format(dataPoint.startTime)
                weightLogList.add("$utcTimeStr (${dataPoint.zoneOffset}) | $weight kg")
            }
            updateUI()
        } catch (e: Exception) {
            Log.e("HealthSDK", "Read fail", e)
        }
    }

    private fun updateUI() {
        recyclerView.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val tv = TextView(parent.context).apply { textSize = 16f; setPadding(40, 20, 40, 20) }
                return object : RecyclerView.ViewHolder(tv) {}
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                (holder.itemView as TextView).text = weightLogList[position]
            }
            override fun getItemCount() = weightLogList.size
        }
    }
}