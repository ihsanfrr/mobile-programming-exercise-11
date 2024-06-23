package com.ihsanfrr.news

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var floatingActionButton: FloatingActionButton

    private lateinit var myAdapter: AdapterList
    private lateinit var itemList: MutableList<ItemList>
    private lateinit var db: FirebaseFirestore
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        FirebaseApp.initializeApp(this)
        db = FirebaseFirestore.getInstance()

        recyclerView = findViewById(R.id.rcvNews)
        floatingActionButton = findViewById(R.id.floatAddNews)
        progressDialog = ProgressDialog(this).apply {
            setTitle("Loading. . .")
        }

        floatingActionButton.setOnClickListener {
            startActivity(Intent(this, NewsAddActivity::class.java))
        }

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        itemList = ArrayList()
        myAdapter = AdapterList(itemList)
        recyclerView.adapter = myAdapter

        myAdapter.setOnItemClickListener(object: AdapterList.OnItemClickListener {
            override fun onItemClick(item: ItemList) {
                val intent = Intent(this@MainActivity, NewsDetailActivity::class.java).apply {
                    putExtra("id", item.id)
                    putExtra("title", item.title)
                    putExtra("desc", item.subTitle)
                    putExtra("imageUrl", item.imageUrl)
                }
                startActivity(intent)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        getData()
    }

    private fun getData() {
        progressDialog.show()
        db.collection("news")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    itemList.clear()
                    for (document in task.result) {
                        val item = ItemList(
                            document.id,
                            document.getString("title") ?: "",
                            document.getString("desc") ?: "",
                            document.getString("imageUrl") ?: ""
                        )
                        itemList.add(item)
                        Log.d("data", "${document.id} => ${document.data}")
                    }
                    myAdapter.notifyDataSetChanged()
                } else {
                    Log.w("data", "Error getting documents.", task.exception)
                }
                progressDialog.dismiss()
            }
    }
}