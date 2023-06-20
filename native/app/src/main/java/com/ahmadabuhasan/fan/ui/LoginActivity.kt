package com.ahmadabuhasan.fan.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.ahmadabuhasan.fan.R
import com.ahmadabuhasan.fan.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.tvSignup.setOnClickListener { toSignup() }
        binding.btnLogin.setOnClickListener { login() }
    }

    private fun toSignup() {
        val i = Intent(this, RegisterActivity::class.java)
        startActivity(i)
        overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out)
    }

    private fun login() {
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if (validateInputs(email, password)) {
            loginUser(email, password)
        }
    }

    private fun validateInputs(
        email: String,
        password: String
    ): Boolean {

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email")
            return false
        }

        if (password.isEmpty() || password.length < 8) {
            showToast("Passwords must be at least 8 characters")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null && user.isEmailVerified) {
                        showToast("Login successful.")
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        showToast("Please verify your email.")
                    }
                } else {
                    showToast("Login failed. Please try again.")
                }
            }
    }
}