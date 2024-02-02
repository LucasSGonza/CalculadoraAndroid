package com.example.calculadoraandroid.modules

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.calculadoraandroid.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var calculation: TextView
    private var flagDecimalPoint: Boolean = false
    private var flagOperator: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        calculation = binding.spaceForCalculation
        setContentView(binding.root)
        setupListeners()
    }

    private fun setupListeners() {
        setupClickListeners()
        setupCalculationListener()
    }

    private fun setupClickListeners() {
        with(binding) {
            zeroNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "0" }
            oneNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "1" }
            twoNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "2" }
            threeNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "3" }
            fourNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "4" }
            fiveNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "5" }
            sixNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "6" }
            sevenNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "7" }
            eightNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "8" }
            nineNumberBtn.setOnClickListener { calculation.text = calculation.text.toString() + "9" }

            decimalBtn.setOnClickListener { calculation.text = calculation.text.toString() + "." }

            multiplicationBtn.setOnClickListener { calculation.text = calculation.text.toString() + "x" }
            divisionBtn.setOnClickListener { calculation.text = calculation.text.toString() + "/" }
            plusBtn.setOnClickListener { calculation.text = calculation.text.toString() + "+" }
            minusBtn.setOnClickListener { calculation.text = calculation.text.toString() + "-" }

            equalBtn.setOnClickListener {  }
            clearBtn.setOnClickListener { calculation.text = "" }
        }
    }

    private fun setupCalculationListener() {
        binding.spaceForCalculation.doOnTextChanged { text, start, before, count ->
            //criar uma variavel para receber o valor do texto
            //antes de manda-la como parametro para a função abs(), que irá calcular o valor absoluto,
            //trocar os operadores em forma de texto para em forma matematica. EX: 'x' -> '*'
        }
    }

}