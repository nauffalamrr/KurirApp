package com.palmar.kurirapp.ui.detailtask

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.R
import com.palmar.kurirapp.adapter.DetailTaskAdapter
import com.palmar.kurirapp.data.Task
import com.palmar.kurirapp.databinding.ActivityDetailTaskBinding

class DetailTaskActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailTaskBinding
    private lateinit var destinationListAdapter: DetailTaskAdapter
    private lateinit var task: Task
    private var isTaskStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        task = intent.getParcelableExtra("task")!!

        destinationListAdapter = DetailTaskAdapter(task.destinations)
        binding.rvDetailTask.layoutManager = LinearLayoutManager(this)
        binding.rvDetailTask.adapter = destinationListAdapter

        setupTaskDetails()

        binding.doTaskButton.setOnClickListener {
            if (!isTaskStarted) {
                startTask()
            } else {
                endTask()
            }
        }

        binding.endTaskButton.setOnClickListener {
            endTask()
        }
    }

    private fun setupTaskDetails() {
        binding.titleDetailTask.text = "Task #${task.id}"

        if (task.status == "waiting") {
            binding.doTaskButton.text = getString(R.string.do_task)
            binding.endTaskButton.visibility = View.GONE
            binding.doTaskButton.visibility = View.VISIBLE
        } else {
            binding.doTaskButton.text = getString(R.string.end_task)
            binding.endTaskButton.visibility = View.VISIBLE
            binding.doTaskButton.visibility = View.GONE
        }
    }

    private fun startTask() {
        // Update the task to "delivery"
        task.status = "delivery"
        binding.doTaskButton.text = getString(R.string.end_task)
        binding.endTaskButton.visibility = View.VISIBLE
        binding.doTaskButton.visibility = View.GONE
        isTaskStarted = true
    }

    private fun endTask() {
        // Update the task to "completed"
        task.status = "completed"
        binding.doTaskButton.text = getString(R.string.do_task)
        binding.endTaskButton.visibility = View.GONE
        binding.doTaskButton.visibility = View.VISIBLE
        isTaskStarted = false
    }
}
