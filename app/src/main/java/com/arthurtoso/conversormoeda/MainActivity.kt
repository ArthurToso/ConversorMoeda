package com.arthurtoso.conversormoeda

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.arthurtoso.conversormoeda.UserHolder.currentUser
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {

    private val brlUsdFormat = DecimalFormat("#,##0.00")
    private val btcFormat = DecimalFormat("#,##0.0000")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if (UserHolder.currentUser == null) {
            UserHolder.currentUser = User(
                brl = 100000.00,
                usd = 50000.00,
                btc = 0.5000
            )
        }

        val btn = findViewById<Button>(R.id.btnConvert)
        btn.setOnClickListener {
            val intent = Intent(this, ConverteActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI(){
        currentUser?.let { user ->
            try {
                val tvBRL = findViewById<TextView>(R.id.tvBrlBalance)
                val tvUSD = findViewById<TextView>(R.id.tvUsdBalance)
                val tvBTC = findViewById<TextView>(R.id.tvBtcBalance)
                val tvBRLAmnt = findViewById<TextView>(R.id.tvBrlAmount)
                val tvUSDAmnt = findViewById<TextView>(R.id.tvUsdAmount)
                val tvBTCAmnt = findViewById<TextView>(R.id.tvBtcAmount)
                tvBRL.text = "Saldo: R$ ${brlUsdFormat.format(user.brl)}"
                tvUSD.text = "Saldo: $ ${brlUsdFormat.format(user.usd)}"
                tvBTC.text = "Saldo: BTC ${btcFormat.format(user.btc)}"
                tvBRLAmnt.text = "R$ ${brlUsdFormat.format(user.brl)}"
                tvUSDAmnt.text = "$ ${brlUsdFormat.format(user.usd)}"
                tvBTCAmnt.text = "BTC ${btcFormat.format(user.btc)}"

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Erro ao atualizar a UI", Toast.LENGTH_LONG).show()
            }
        }
    }
}