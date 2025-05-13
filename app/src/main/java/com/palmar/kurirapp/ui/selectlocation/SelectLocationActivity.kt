package com.palmar.kurirapp.ui.selectlocation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.palmar.kurirapp.databinding.ActivitySelectLocationBinding

class SelectLocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
    }
}