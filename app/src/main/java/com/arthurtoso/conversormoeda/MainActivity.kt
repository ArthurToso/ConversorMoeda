package com.arthurtoso.conversormoeda


import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val INITIAL_BRL = 100000.00
        private const val INITIAL_USD = 50000.00
        private const val INITIAL_BTC = 0.5
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btn = findViewById<Button>(R.id.buttonTeste)
        btn.setOnClickListener {
            val intent = Intent(this, ConverteActivity::class.java)
            val user = User(INITIAL_BRL, INITIAL_USD, INITIAL_BTC) // AGORA USA VARIÁVEIS
            intent.putExtra("USER_DATA", user)
            startActivity(intent)
        }
    }
}
