package com.palmar.kurirapp.ui.detailtask

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.adapter.DetailTaskAdapter
import com.palmar.kurirapp.data.*
import com.palmar.kurirapp.data.retrofit.ApiConfig
import com.palmar.kurirapp.databinding.ActivityDetailTaskBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaskBinding
    private lateinit var destinationListAdapter: DetailTaskAdapter
    private lateinit var task: Task
    private var isTaskStarted = false

    private val accessToken: String
        get() {
            val sp = getSharedPreferences("userPrefs", MODE_PRIVATE)
            return sp.getString("access_token", "") ?: ""
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val taskId = intent.getIntExtra("taskId", -1)
        if (taskId == -1) {
            Toast.makeText(this, "Task ID tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        destinationListAdapter = DetailTaskAdapter(emptyList())
        binding.rvDetailTask.layoutManager = LinearLayoutManager(this)
        binding.rvDetailTask.adapter = destinationListAdapter

        fetchTaskDetail(taskId)

        binding.doTaskButton.setOnClickListener {
            if (!isTaskStarted) {
                acceptTask()
            } else {
                completeTask()
            }
        }

        binding.endTaskButton.setOnClickListener {
            completeTask()
        }
    }

    private fun mapDestinationResponseToDestination(destResp: DestinationResponse): Destination {
        val location = Location(
            latitude = destResp.latitude ?: 0.0,
            longitude = destResp.longitude ?: 0.0,
            name = destResp.destination_name
        )
        return Destination(
            id = destResp.id,
            location = location,
            sequence_order = destResp.sequence_order,
            latitude = destResp.latitude ?: 0.0,
            longitude = destResp.longitude ?: 0.0
        )
    }

    private fun fetchTaskDetail(taskId: Int) {
        if (accessToken.isBlank()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ApiConfig.getApiService(this).getTaskDetail(taskId)
            .enqueue(object : Callback<TaskResponse> {
                override fun onResponse(call: Call<TaskResponse>, response: Response<TaskResponse>) {
                    if (response.isSuccessful) {
                        val taskResp = response.body()
                        if (taskResp != null) {
                            val destinationsMapped = taskResp.destinations.map { mapDestinationResponseToDestination(it) }
                            task = Task(
                                id = taskResp.id,
                                status = taskResp.status,
                                driver_id = taskResp.driver_id,
                                destinations = destinationsMapped
                            )
                            destinationListAdapter.updateData(task.destinations)
                            isTaskStarted = task.status != "waiting"
                            updateButtonUI()
                        } else {
                            Toast.makeText(this@DetailTaskActivity, "Task tidak ditemukan", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    } else {
                        Toast.makeText(this@DetailTaskActivity, "Gagal mengambil detail task", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }

                override fun onFailure(call: Call<TaskResponse>, t: Throwable) {
                    Toast.makeText(this@DetailTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    finish()
                }
            })
    }

    private fun updateButtonUI() {
        if (task.status == "waiting") {
            binding.doTaskButton.text = getString(com.palmar.kurirapp.R.string.do_task)
            binding.endTaskButton.visibility = View.GONE
            binding.doTaskButton.visibility = View.VISIBLE
        } else {
            binding.doTaskButton.text = getString(com.palmar.kurirapp.R.string.end_task)
            binding.endTaskButton.visibility = View.VISIBLE
            binding.doTaskButton.visibility = View.GONE
        }
    }

    private fun acceptTask() {
        if (accessToken.isBlank()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        binding.doTaskButton.isEnabled = false

        ApiConfig.getApiService(this).acceptTask(task.id)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    binding.doTaskButton.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@DetailTaskActivity, response.body()?.message ?: "Task accepted", Toast.LENGTH_SHORT).show()
                        task.status = "delivery"
                        updateButtonUI()
                    } else {
                        Toast.makeText(this@DetailTaskActivity, "Gagal menerima task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    binding.doTaskButton.isEnabled = true
                    Toast.makeText(this@DetailTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun completeTask() {
        if (accessToken.isBlank()) {
            Toast.makeText(this, "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        binding.doTaskButton.isEnabled = false
        binding.endTaskButton.isEnabled = false

        ApiConfig.getApiService(this).completeTask(task.id)
            .enqueue(object : Callback<MessageResponse> {
                override fun onResponse(call: Call<MessageResponse>, response: Response<MessageResponse>) {
                    binding.doTaskButton.isEnabled = true
                    binding.endTaskButton.isEnabled = true
                    if (response.isSuccessful) {
                        Toast.makeText(this@DetailTaskActivity, response.body()?.message ?: "Task completed", Toast.LENGTH_SHORT).show()
                        task.status = "completed"
                        updateButtonUI()
                    } else {
                        Toast.makeText(this@DetailTaskActivity, "Gagal menyelesaikan task", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                    binding.doTaskButton.isEnabled = true
                    binding.endTaskButton.isEnabled = true
                    Toast.makeText(this@DetailTaskActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
