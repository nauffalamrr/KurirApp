package com.palmar.kurirapp.ui.destination

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.palmar.kurirapp.databinding.ActivityDestinationBinding

class DestinationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDestinationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
    }
}