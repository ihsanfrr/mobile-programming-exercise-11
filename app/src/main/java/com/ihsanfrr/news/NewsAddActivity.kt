@file:Suppress("DEPRECATION")

package com.ihsanfrr.news

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class NewsAddActivity : AppCompatActivity() {
    private var pickImageRequest: Int = 1

    private var id: String? = null
    private var judul: String? = null
    private var deskripsi: String? = null
    private var image: String? = null

    private lateinit var title: EditText
    private lateinit var desc: EditText
    private lateinit var imageView: ImageView
    private lateinit var saveNews: Button
    private lateinit var chooseImage: Button
    private var imageUrl: Uri? = null

    private lateinit var dbNews: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_news_add)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        dbNews = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        title = findViewById(R.id.title)
        desc = findViewById(R.id.desc)
        imageView = findViewById(R.id.imageView)
        saveNews = findViewById(R.id.btnAdd)
        chooseImage = findViewById(R.id.btnChooseImage)


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading. . .")

        chooseImage.setOnClickListener {
            openFileChooser()
        }

        val updateOption = intent
        if (updateOption != null) {
            id = updateOption.getStringExtra("id")
            judul = updateOption.getStringExtra("title")
            deskripsi = updateOption.getStringExtra("desc")
            image = updateOption.getStringExtra("imageUrl")

            title.setText(judul)
            desc.setText(deskripsi)
            Glide.with(this).load(image).into(imageView)
        }

        saveNews.setOnClickListener {
            val newsTitle: String = title.text.toString().trim()
            val newsDesc: String = desc.text.toString().trim()

            if (newsTitle.isEmpty() || newsDesc.isEmpty()) {
                Toast.makeText(this, "Title & Desc cannot be empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else {
                progressDialog.show()
                if (imageUrl != null) {
                    uploadImageToStorage(newsTitle, newsDesc)
                } else {
                    saveData(newsTitle, newsDesc, image?:"")
                }

            }
        }
    }

    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, this.pickImageRequest)
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == pickImageRequest && resultCode == RESULT_OK && data != null && data.data != null) {
            this.imageUrl = data.data
            imageView.setImageURI(imageUrl)
        }
    }

    private fun uploadImageToStorage(newsTitle: String, newsDesc: String) {
        if (imageUrl != null) {
            val storageRef: StorageReference = storage.reference.child("news_images/" + System.currentTimeMillis() + ".jpg")
            storageRef.putFile(imageUrl!!)
                .addOnSuccessListener { _ ->
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveData(newsTitle, newsDesc, imageUrl)
                    }
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Failed to upload image: " + e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveData(newsTitle: String, newsDesc: String, imageUrl: String) {
        val news: MutableMap<String, Any> = mutableMapOf()
        news["title"] = newsTitle
        news["desc"] = newsDesc
        news["imageUrl"] = imageUrl

        if (id != null) {
            dbNews.collection("news").document(id?:"")
                .update(news)
                .addOnSuccessListener {
                    progressDialog.dismiss()
                    Toast.makeText(this, "News Updated Successfully", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "News updating news: "+ e.message, Toast.LENGTH_SHORT).show()
                    Log.w("NewsAdd", "Error updating document", e)
                }
        } else {
            dbNews.collection("news")
                .add(news)
                .addOnSuccessListener { _ ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "News Added Successfully", Toast.LENGTH_SHORT).show()
                    title.setText("")
                    desc.setText("")
                    imageView.setImageResource(0)
                }
                .addOnFailureListener { e ->
                    progressDialog.dismiss()
                    Toast.makeText(this, "Error adding news: " + e.message, Toast.LENGTH_SHORT).show()
                    Log.w("NewsAdd", "Error adding document", e)
                }
        }
    }
}