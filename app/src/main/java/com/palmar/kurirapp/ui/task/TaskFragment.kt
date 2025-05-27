package com.palmar.kurirapp.ui.task

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.palmar.kurirapp.adapter.TaskAdapter
import com.palmar.kurirapp.data.Task
import com.palmar.kurirapp.data.retrofit.ApiConfig
import com.palmar.kurirapp.databinding.FragmentTaskBinding
import com.palmar.kurirapp.ui.detailtask.DetailTaskActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TaskFragment : Fragment() {

    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TaskAdapter
    private val tasks = mutableListOf<Task>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TaskAdapter(tasks) { task ->
            val intent = Intent(requireContext(), DetailTaskActivity::class.java)
            intent.putExtra("taskId", task.id)
            startActivity(intent)
        }

        binding.rvTaskList.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTaskList.adapter = adapter

        loadTasks()
    }

    private fun loadTasks() {
        val sharedPref = requireContext().getSharedPreferences("userPrefs", android.content.Context.MODE_PRIVATE)
        val token = sharedPref.getString("access_token", null)

        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Token tidak ditemukan, silakan login ulang", Toast.LENGTH_SHORT).show()
            return
        }

        ApiConfig.getApiService(requireContext()).getAllTasks().enqueue(object : Callback<List<Task>> {
            override fun onResponse(call: Call<List<Task>>, response: Response<List<Task>>) {
                if (response.isSuccessful) {
                    tasks.clear()
                    response.body()?.let {
                        val sortedTasks = it.sortedByDescending { task -> task.id }
                        tasks.addAll(sortedTasks)
                    }
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(requireContext(), "Gagal memuat task", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Task>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
