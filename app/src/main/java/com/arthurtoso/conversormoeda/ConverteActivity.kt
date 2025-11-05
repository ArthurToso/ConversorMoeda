package com.arthurtoso.conversormoeda

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arthurtoso.conversormoeda.api.Cotacao
import com.arthurtoso.conversormoeda.api.CotacaoApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat

class ConverteActivity : AppCompatActivity() {

    companion object {

        private const val BASE_URL = "https://economia.awesomeapi.com.br/"
        private const val CURRENCY_PAIRS = "USD-BRL,BTC-BRL,BTC-USD"
    }

    private lateinit var spinnerFrom: Spinner
    private lateinit var spinnerTo: Spinner
    private lateinit var etAmount: EditText
    private lateinit var btnConvert: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvResult: TextView
    private lateinit var cotacaoApi: CotacaoApi
    private lateinit var btnVoltar: Button

    private val currencies = arrayOf("BRL", "USD", "BTC" )
    private val brlUsdFormat = DecimalFormat("#,##0.00")
    private val btcFormat = DecimalFormat("#,##0.0000")

    private lateinit var currentUser: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_converte)

        // 2. Recebe o usuário do Intent
        intent.getParcelableExtra<User>("USER_DATA")?.let {
            currentUser = it
        } ?: run {
            showError("Erro: Usuário não encontrado.")
            finish() // Fecha a activity
            return
        }
        spinnerFrom = findViewById(R.id.spinnerFrom)
        spinnerTo = findViewById(R.id.spinnerTo)
        etAmount = findViewById(R.id.etAmount)
        btnConvert = findViewById(R.id.btnConvert)
        progressBar = findViewById(R.id.progressBar)
        tvResult = findViewById(R.id.tvResult)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerFrom.adapter = adapter
        spinnerTo.adapter = adapter

        btnConvert.setOnClickListener {
            converteMoeda()
        }

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL) // Usa a variável dinâmica
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        cotacaoApi = retrofit.create(CotacaoApi::class.java)

        btnVoltar = findViewById<Button>(R.id.btnVoltar)
        btnVoltar.setOnClickListener {
            finish()
        }
    }

    private fun converteMoeda(){
        val fromCurrency = spinnerFrom.selectedItem.toString()
        val toCurrency = spinnerTo.selectedItem.toString()
        val amountStr = etAmount.text.toString()

        if (fromCurrency == toCurrency){
            Toast.makeText(this, "Moedas iguais", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0){
            Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show()
            return
        }

        if (!hasSufficientFunds(amount, fromCurrency)) {
            showError("Saldo insuficiente na carteira de $fromCurrency.")
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                withContext(Dispatchers.Main) { showLoading(true) }

                val moedas = CURRENCY_PAIRS
                val response = cotacaoApi.getRates(moedas)

                if (response.isSuccessful && response.body() != null) {
                    val rates = response.body()!!
                    val convertedAmount = calculaConversao(amount, fromCurrency, toCurrency, rates)

                    if (convertedAmount > 0) {
                        performTransaction(amount, fromCurrency, convertedAmount, toCurrency)


                        val resultText = "Convertido: ${formatCurrency(convertedAmount, toCurrency)}"

                        withContext(Dispatchers.Main) {
                            showLoading(false)
                            tvResult.text = resultText
                            tvResult.visibility = View.VISIBLE
                            // 5. PREPARA O INTENT DE RESULTADO
                            val resultIntent = Intent()

                            resultIntent.putExtra("USER_DATA_RESULT", currentUser)

                            setResult(Activity.RESULT_OK, resultIntent)

                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            showError("Não foi possível obter a cotação para este par.")
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        showError("Erro ao buscar cotações da API.")
                    }
                }
            } catch (e: Exception) {
                // Trata exceções (ex: sem internet)
                withContext(Dispatchers.Main) {
                    showError("Erro de conexão: ${e.message}")
                }
            } finally {
                // Garante que o loading seja desativado
                withContext(Dispatchers.Main) { showLoading(false) }
            }
        }
    }
    private fun calculaConversao(amount: Double, from: String, to: String, rates: Map<String, Cotacao>): Double {
        val usdBrLRate = rates["USDBRL"]?.bid?.toDoubleOrNull() ?: 0.0
        val btcBrlRate = rates["BTCBRL"]?.bid?.toDoubleOrNull() ?: 0.0
        val btcUsdRate = rates["BTCUSD"]?.bid?.toDoubleOrNull() ?: 0.0

        if (usdBrLRate == 0.0 || btcBrlRate == 0.0 || btcUsdRate == 0.0) return 0.0

        return when (from) {
            "BRL" -> when (to) {
                "USD" -> amount / usdBrLRate // Vende BRL, Compra USD
                "BTC" -> amount / btcBrlRate // Vende BRL, Compra BTC
                else -> 0.0
            }
            "USD" -> when (to) {
                "BRL" -> amount * usdBrLRate // Vende USD, Compra BRL
                "BTC" -> amount / btcUsdRate // Vende USD, Compra BTC
                else -> 0.0
            }
            "BTC" -> when (to) {
                "BRL" -> amount * btcBrlRate // Vende BTC, Compra BRL
                "USD" -> amount * btcUsdRate // Vende BTC, Compra USD
                else -> 0.0
            }
            else -> 0.0
        }
    }

    private fun showLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnConvert.isEnabled = !isLoading
        // Esconde resultado anterior ao carregar
        if (isLoading) {
            tvResult.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        showLoading(false)
    }

    private fun formatCurrency(amount: Double, currency: String): String {
        return when (currency) {
            "BRL" -> "R$ ${brlUsdFormat.format(amount)}"
            "USD" -> "$ ${brlUsdFormat.format(amount)}"
            "BTC" -> "BTC ${btcFormat.format(amount)}"
            else -> amount.toString()
        }
    }

    private fun hasSufficientFunds(amount: Double, currency: String): Boolean {

        return when (currency) {
            "BRL" -> currentUser.brl >= amount
            "USD" -> currentUser.usd >= amount
            "BTC" -> currentUser.btc >= amount
            else -> false
        }
    }

    private fun performTransaction(fromAmount: Double, fromCurrency: String, toAmount: Double, toCurrency: String) {

        when (fromCurrency) {
            "BRL" -> currentUser.brl -= fromAmount
            "USD" -> currentUser.usd -= fromAmount
            "BTC" -> currentUser.btc -= fromAmount
        }

        when (toCurrency) {
            "BRL" -> currentUser.brl += toAmount
            "USD" -> currentUser.usd += toAmount
            "BTC" -> currentUser.btc += toAmount
        }
    }
}
