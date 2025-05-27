package com.palmar.kurirapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.palmar.kurirapp.MainActivity
import com.palmar.kurirapp.data.LoginRequest
import com.palmar.kurirapp.data.LoginResponse
import com.palmar.kurirapp.data.retrofit.ApiConfig
import com.palmar.kurirapp.databinding.ActivityLoginBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.loginButton.setOnClickListener {
            val username = binding.username.text.toString().trim()
            val password = binding.password.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username dan password harus diisi", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val loginRequest = LoginRequest(username, password)
            val client = ApiConfig.getApiService(this).login(loginRequest)

            client.enqueue(object : Callback<LoginResponse> {
                override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        if (loginResponse != null) {

                            val sharedPreferences = getSharedPreferences("userPrefs", MODE_PRIVATE)
                            val editor = sharedPreferences.edit()
                            editor.putString("username", loginResponse.user.name)
                            editor.putString("access_token", loginResponse.access_token)
                            editor.putInt("user_id", loginResponse.user.id)
                            editor.apply()

                            Toast.makeText(this@LoginActivity, "Login berhasil", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Login gagal: Response kosong", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Username atau password salah", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error koneksi: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}