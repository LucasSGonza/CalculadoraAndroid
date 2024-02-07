package com.example.calculadoraandroid.modules

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.calculadoraandroid.R
import com.example.calculadoraandroid.databinding.ActivityMainBinding
import com.google.android.material.textview.MaterialTextView
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalZeroDivisionException
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var isTheLastDigitANumber: Boolean = false
    private var isTheLastDigitAOperator: Boolean = false
    private var doTheNumberAlreadyHasADecimalPoint: Boolean = false
    private var didUserFinishedTheCalc: Boolean = false

    private var listOfNumberBtn = mutableListOf<Button>()
    private var listOfOperatorsBtn = mutableListOf<Button>()
    private var result: String = ""
    private var maxLengthForANumber: Int = 13

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupBtnLists()
        setupListeners()
    }

    private fun setupListeners() {
        setupClickListeners()
        setupCalculationListener()
    }

    private fun setupBtnLists() {
        listOfNumberBtn.addAll(
            with(binding) {
                listOf(
                    zeroNumberBtn,
                    oneNumberBtn,
                    twoNumberBtn,
                    threeNumberBtn,
                    fourNumberBtn,
                    fiveNumberBtn,
                    sixNumberBtn,
                    sevenNumberBtn,
                    eightNumberBtn,
                    nineNumberBtn
                )
            }
        )

        listOfOperatorsBtn.addAll(
            with(binding) {
                listOf(
                    plusBtn,
                    minusBtn,
                    multiplicationBtn,
                    divisionBtn,
                    percentageBtn
                )
            }
        )
    }

    private fun setupClickListeners() {
        with(binding) {
            listOfNumberBtn.forEach { btn ->
                btn.setOnClickListener {
                    spaceForCalculation.text =
                        if (didUserFinishedTheCalc) "${btn.text}"
                        else spaceForCalculation.text.toString() + "${btn.text}"
                    isTheLastDigitANumber = true
                    isTheLastDigitAOperator = false
                    didUserFinishedTheCalc = false
                }
            }

            listOfOperatorsBtn.forEach { btn ->
                btn.setOnClickListener {
                    if (spaceForCalculation.text.isNotEmpty()) {
                        spaceForCalculation.text = when {
                            isTheLastDigitAOperator -> spaceForCalculation.text.dropLast(1)
                                .toString() + "${btn.text}"

                            else -> spaceForCalculation.text.toString() + "${btn.text}"
                        }
                        isTheLastDigitANumber = false
                        doTheNumberAlreadyHasADecimalPoint = false
                        isTheLastDigitAOperator = true
                        didUserFinishedTheCalc = false
                    }
                }
            }

            decimalBtn.setOnClickListener {
                if (spaceForCalculation.text.isNotEmpty()) {
                    if (spaceForCalculation.text.length < maxLengthForANumber) {
                        if (!doTheNumberAlreadyHasADecimalPoint) {
                            spaceForCalculation.text =
                                if (isTheLastDigitANumber) spaceForCalculation.text.toString() + "."
                                else spaceForCalculation.text.toString() + "0."
                            isTheLastDigitANumber = false
                            isTheLastDigitAOperator = false
                            doTheNumberAlreadyHasADecimalPoint = true
                        }
                    }
                } else {
                    spaceForCalculation.text = "0."
                    doTheNumberAlreadyHasADecimalPoint = true
                }
            }

            equalBtn.setOnClickListener {
                if (result == getString(R.string.error_default)) {
                    Toast.makeText(
                        it.context,
                        getString(R.string.error_division_by_zero),
                        Toast.LENGTH_SHORT
                    ).show()
                }

//                spaceForCalculation.text = String.format("%.5s", result)
                spaceForCalculation.text = result
                isTheLastDigitANumber = false
                isTheLastDigitAOperator = false
                didUserFinishedTheCalc = true
            }

            clearBtn.setOnClickListener {
                spaceForCalculation.text = ""
                result = ""
                isTheLastDigitANumber = false
                isTheLastDigitAOperator = false
                doTheNumberAlreadyHasADecimalPoint = false
            }

            deleteBtn.setOnClickListener {
                if (spaceForCalculation.text.isNotEmpty()) {

                    if (isTheLastDigitAOperator) {
                        isTheLastDigitAOperator = false
                    }

                    if (spaceForCalculation.text.last() == '.') {
                        doTheNumberAlreadyHasADecimalPoint = false
                    }

                    spaceForCalculation.text = spaceForCalculation.text.toString().dropLast(1)
                } else {
                    result = ""
                    isTheLastDigitANumber = false
                    isTheLastDigitAOperator = false
                    doTheNumberAlreadyHasADecimalPoint = false
                }
            }

        }
    }

    private fun setupCalculationListener() {
        binding.spaceForCalculation.doOnTextChanged { text, _, _, _ ->
            text?.let {
                if (text.isNotEmpty()) {
                    val expression = validateExpression(text.toString())
                    Log.i("success", "expression1: $expression")
                    try {
                        result = Keval.eval(expression).toString()
                        Log.i("success", "result after calc: $result")
                    } catch (e: KevalInvalidExpressionException) {
                        Log.e("error", "$e")
                    } catch (e: KevalZeroDivisionException) {
                        Log.e("error", "$e")
                        result = getString(R.string.error_default)
                    }
                }
            }
        }
    }

    private fun validateExpression(text: String): String {
        var expression = text
            .replace("x", "*")
            .replace("%", "/100*")

        if (text.first() == '-') {
            expression = expression.replace("-", "0-")
        }

        if (expression.last() == '*') {
            expression = expression.dropLast(1)
        }

        return expression
    }

}