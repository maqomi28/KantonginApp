package com.project.kantongin.create

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.project.kantongin.model.Category
import com.project.kantongin.model.Transaction

class UpdateActivity : BaseActivity() {

    private final val TAG: String = "UpdateActivity"

    private val binding by lazy { ActivityCreateBinding.inflate(layoutInflater) }
    private lateinit var categoryAdapter: CategoryAdapter
    private val transactionId by lazy { intent.getStringExtra("id")}
    private val db by lazy { Firebase.firestore }
    private var type: String = "";
    private var category: String = "";

    private lateinit var transaction: Transaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupList()
        setupListener()
        Log.e(TAG, "transactionId: $transactionId")
    }

    override fun onStart() {
        super.onStart()
        detailTransaction()
    }

    private fun setupList(){
        categoryAdapter = CategoryAdapter(this, arrayListOf(), object : CategoryAdapter.AdapterListener{
            override fun onClick(category: Category) {
                transaction.category = category.name!!
            }

        })
        binding.listCategory.adapter = categoryAdapter
    }

    private fun setupListener(){
        binding.buttonSave.setText("Simpan Perubahan")
        binding.buttonSave.setOnClickListener {
            progress(true)
            transaction.amount = binding.editAmount.text.toString().toInt()
            transaction.note = binding.editNote.text.toString()
            db.collection("transaction")
                .add(transaction)
                .addOnSuccessListener {
                    progress(false)
                    Toast.makeText(applicationContext, "Transaksi Diperbarui", Toast.LENGTH_SHORT).show()
                    finish()
                }

        }
        binding.buttonIn.setOnClickListener {
            transaction.type = "IN"
            setButton(it as MaterialButton)
        }
        binding.buttonOut.setOnClickListener {
            transaction.type = "OUT"
            setButton(it as MaterialButton)
        }
    }

    private fun setButton(buttonSelected: MaterialButton){
        Log.e(TAG, type )
        listOf<MaterialButton>(binding.buttonIn, binding.buttonOut).forEach {
            it.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_baby))
        }
        buttonSelected.setBackgroundColor(ContextCompat.getColor(this, R.color.blue_navy))
    }

    private fun progress(progress: Boolean){
        when (progress) {
            true -> {
                binding.buttonSave.text = "Loading..."
                binding.buttonSave.isEnabled = false
            }
            false -> {
                binding.buttonSave.text = "Simpan Perubahan"
                binding.buttonSave.isEnabled = true
            }
        }
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
                Handler(Looper.myLooper()!!).postDelayed({
                    categoryAdapter.setButton(transaction.category)
                },300)

            }

    }

    private fun detailTransaction(){
        db.collection("transaction")
            .document(transactionId!!)
            .get()
            .addOnSuccessListener { result ->
                transaction = Transaction(
                    id = result.id,
                    amount = result["amount"].toString().toInt(),
                    category = result["category"].toString(),
                    type = result["type"].toString(),
                    note = result["note"].toString(),
                    username = result["username"].toString(),
                    created = result["created"] as Timestamp
                )
                binding.editAmount.setText(transaction.amount.toString())
                binding.editNote.setText(transaction.note.toString())

                when(transaction.type){
                    "IN" -> setButton(binding.buttonIn)
                    "OUT" -> setButton(binding.buttonOut)
                }
                getCategory()
            }
    }
}