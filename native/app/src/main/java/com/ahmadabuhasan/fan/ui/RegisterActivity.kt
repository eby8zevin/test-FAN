package com.ahmadabuhasan.fan.ui

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ahmadabuhasan.fan.R
import com.ahmadabuhasan.fan.databinding.ActivityRegisterBinding
import com.ahmadabuhasan.fan.modal.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthActionCodeException
import com.google.firebase.database.FirebaseDatabase

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://fanintek-bekasi-default-rtdb.asia-southeast1.firebasedatabase.app")

        binding.tvSignIn.setOnClickListener { toSignIn() }
        binding.btnRegister.setOnClickListener { register() }
    }

    private fun toSignIn() {
        val i = Intent(this, LoginActivity::class.java)
        startActivity(i)
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out)
    }

    private fun register() {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirm = binding.etConfirm.text.toString()

        if (validateInputs(name, email, password, confirm)) {

            registerUser(name, email, password)
        }
    }

    private fun validateInputs(
        name: String,
        email: String,
        password: String,
        confirm: String
    ): Boolean {

        if (name.isEmpty() || name.length < 3 || name.length > 50) {
            showToast("The name must be between 3 and 50 characters")
            return false
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Invalid email")
            return false
        }

        if (password.isEmpty() || password.length < 8 || !isPasswordValid(password)) {
            showToast("Passwords must be at least 8 characters and contain numbers, uppercase and lowercase letters.")
            return false
        }

        if (confirm.isEmpty() || confirm != password) {
            showToast("Password confirmation does not match the password")
            return false
        }

        return true
    }

    private fun isPasswordValid(password: String): Boolean {
        val pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}".toRegex()
        return pattern.matches(password)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun registerUser(name: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = User(name, email, confirmed = false)
                    val uid = auth.currentUser?.uid

                    if (uid != null) {
                        database.reference.child("users")
                            .child(uid)
                            .setValue(user)
                            .addOnCompleteListener { saveTask ->
                                if (saveTask.isSuccessful) {
                                    sendConfirmationEmail()
                                } else {
                                    showToast("Registration failed. Please try again.")
                                }
                            }
                    }
                } else {
                    val exception = task.exception
                    if (exception is FirebaseAuthActionCodeException) {
                        showToast("User with this email already exists.")
                    } else {
                        showToast("Registration failed. Please try again.")
                    }
                }
            }
    }

    private fun sendConfirmationEmail() {
        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Confirmation email sent.", Toast.LENGTH_LONG).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                } else {
                    Toast.makeText(this, "Failed to send confirmation email.", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }
}