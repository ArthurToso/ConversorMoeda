package com.arthurtoso.conversormoeda

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val user = User(100000.00, 50000.00, 0.5)

        val btn = findViewById<Button>(R.id.buttonTeste)
        btn.setOnClickListener {
            val intent = Intent(this, ConverteActivity::class.java)
            intent.putExtra("USER_DATA", user)
            startActivity(intent)
        }
    }
    fun onConvertButtonClick(view: View) {}
}