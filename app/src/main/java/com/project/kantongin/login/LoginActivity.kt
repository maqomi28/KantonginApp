package com.project.kantongin.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.kantongin.R
import com.project.kantongin.base.BaseActivity
import com.project.kantongin.databinding.ActivityLoginBinding
import com.project.kantongin.home.HomeActivity
import com.project.kantongin.model.User
import com.project.kantongin.prefrences.PreferenceManager
import com.project.kantongin.register.RegisterActivity
import com.project.kantongin.util.timestampToString

class LoginActivity : BaseActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }
    private val db by lazy { Firebase.firestore }
    private val pref by lazy { PreferenceManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupListener()
    }
    private fun setupListener(){
        binding.textRegister.setOnClickListener{
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
        binding.buttonLogin.setOnClickListener{
            if (isRequired()) login()
            else Toast.makeText(applicationContext, "Isi data dengan lengkap", Toast.LENGTH_SHORT).show()
        }
    }

    private fun progress(progress: Boolean){
        binding.textAlert.visibility = View.GONE
        when (progress) {
            true -> {
                binding.buttonLogin.text = "Loading..."
                binding.buttonLogin.isEnabled = false
            }
            false -> {
                binding.buttonLogin.text = "Login"
                binding.buttonLogin.isEnabled = true
            }
        }
    }

    private fun login(){
        progress(true)
        db.collection("user")
            .whereEqualTo("username", binding.editUsername.text.toString())
            .whereEqualTo("password", binding.editPassword.text.toString())
            .get()
            .addOnSuccessListener { result ->
                progress(false)
                if (result.isEmpty) binding.textAlert.visibility = View.VISIBLE
                else {
                    result.forEach { document ->
                        saveSession(
                            User(
                                username = document.data["username"].toString(),
                                email = document.data["email"].toString(),
                                password = document.data["password"].toString(),
                                created = document.data["created"] as Timestamp
                            )
                        )
                    }
                    Toast.makeText(applicationContext, "Login success", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
            }
    }

    private fun isRequired(): Boolean{
        return (
                binding.editUsername.text.toString().isNotEmpty() &&
                binding.editPassword.text.toString().isNotEmpty()
                )
    }

    private fun saveSession (user: User){
        Log.e("LoginActivity", user.toString())
        pref.put("pref_is_login",1)
        pref.put("pref_username",user.username)
        pref.put("pref_email",user.email)
        pref.put("pref_password",user.password)
        pref.put("pref_date", timestampToString(user.created)!!)
        if (pref.getInt("pref_avatar")== 0)pref.put("pref_avatar", R.drawable.avatar7)
    }
}