package com.project.kantongin.create

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.project.kantongin.R
import com.project.kantongin.adapter.CategoryAdapter
import com.project.kantongin.base.BaseActivity
import com.project.kantongin.databinding.ActivityCreateBinding
import com.project.kantongin.login.LoginActivity
import com.project.kantongin.model.Category
import com.project.kantongin.model.Transaction
import com.project.kantongin.model.User
import com.project.kantongin.prefrences.PreferenceManager
import com.project.kantongin.util.PrefUtil

class CreateActivity : BaseActivity() {
    final val TAG: String = "CreateActivity"

    private val binding by lazy { ActivityCreateBinding.inflate(layoutInflater) }
    private lateinit var categoryAdapter: CategoryAdapter
    private var type: String = "";
    private var category: String = "";

    private val db by lazy { Firebase.firestore }
    private val pref by lazy { PreferenceManager(this) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupList()
        setupListener()

    }

    override fun onStart() {
        super.onStart()
        getCategory()
    }

    private fun setupList(){
        categoryAdapter = CategoryAdapter(this, arrayListOf(), object : CategoryAdapter.AdapterListener{
            override fun onClick(category: Category) {
                this@CreateActivity.category = category.name!!
                Log.e(TAG, this@CreateActivity.category )

            }

        })
        binding.listCategory.adapter = categoryAdapter
    }

    private fun setupListener(){
        binding.buttonIn.setOnClickListener {
            type = "IN"
            setButton(it as MaterialButton)
        }
        binding.buttonOut.setOnClickListener {
            type = "OUT"
            setButton(it as MaterialButton)
        }
        binding.buttonSave.setOnClickListener {
            progress(true)
            val transaction = Transaction(
                id = null,
                username = pref.getString(PrefUtil.pref_username)!!,
                category = category,
                type = type,
                amount = binding.editAmount.text.toString().toInt(),
                note = binding.editNote.text.toString(),
                created = Timestamp.now()
            )

            db.collection("transaction")
                .add(transaction)
                .addOnSuccessListener {
                    progress(false)
                    Toast.makeText(applicationContext, "Transaksi Ditambahkan", Toast.LENGTH_SHORT).show()
                    finish()
                }
        }
    }

    private fun setButton(buttonSelected: MaterialButton){
        Log.e(TAG, type )
        listOf<MaterialButton>(binding.buttonIn, binding.buttonOut).forEach {
            it.setBackgroundColor(ContextCompat.getColor(this,R.color.blue_baby))
        }
        buttonSelected.setBackgroundColor(ContextCompat.getColor(this,R.color.blue_navy))
    }

    private fun getCategory(){
        val categories : ArrayList<Category> = arrayListOf()
        db.collection("category")
            .get()
            .addOnSuccessListener { result ->
                result.forEach { document ->
                    categories.add(Category(document.data["name"].toString()))
                }
                Log.e("HomeActivity", "categories $categories")
                categoryAdapter.setData(categories)

            }

    }
    private fun progress(progress: Boolean){
        when (progress) {
            true -> {
                binding.buttonSave.text = "Loading..."
                binding.buttonSave.isEnabled = false
            }
            false -> {
                binding.buttonSave.text = "Simpan"
                binding.buttonSave.isEnabled = true
            }
        }
    }
}