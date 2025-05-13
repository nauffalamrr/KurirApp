package com.palmar.kurirapp.ui.destination

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.palmar.kurirapp.databinding.ActivityDestinationBinding
import com.palmar.kurirapp.ui.result.ResultActivity

class DestinationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDestinationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDestinationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.buttonOptimize.setOnClickListener {
            navigateToResultActivity()
        }
    }

    private fun navigateToResultActivity() {
        val intent = Intent(this, ResultActivity::class.java)
        startActivity(intent)
    }
}