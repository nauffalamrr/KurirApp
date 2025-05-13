package com.palmar.kurirapp.ui.detailtask

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.palmar.kurirapp.databinding.ActivityDetailTaskBinding

class DetailTaskActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
    }
}