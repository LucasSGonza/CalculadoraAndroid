package com.example.calculadoraandroid.modules

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.calculadoraandroid.R
import com.example.calculadoraandroid.databinding.ActivityMainBinding
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalZeroDivisionException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var calculation: TextView

    private var isTheLastDigitANumber: Boolean = false
    private var isTheLastDigitAOperator: Boolean = false
    private var doTheNumberAlreadyHasADecimalPoint: Boolean = false

    private var result: String = ""

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

            zeroNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "0"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            oneNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "1"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            twoNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "2"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            threeNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "3"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            fourNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "4"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            fiveNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "5"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            sixNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "6"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            sevenNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "7"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            eightNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "8"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            nineNumberBtn.setOnClickListener {
                calculation.text = calculation.text.toString() + "9"
                isTheLastDigitANumber = true
                isTheLastDigitAOperator = false
            }

            decimalBtn.setOnClickListener {
                if (isTheLastDigitANumber && !doTheNumberAlreadyHasADecimalPoint) {
                    calculation.text = calculation.text.toString() + "."
                    isTheLastDigitANumber = false
                    isTheLastDigitAOperator = false
                    doTheNumberAlreadyHasADecimalPoint = true
                }
            }

            multiplicationBtn.setOnClickListener {
                if (calculation.text.isNotEmpty()) {
                    calculation.text = when {
                        isTheLastDigitAOperator -> calculation.text.dropLast(1).toString() + "x"
                        else -> calculation.text.toString() + "x"
                    }
                    isTheLastDigitANumber = false
                    doTheNumberAlreadyHasADecimalPoint = false
                    isTheLastDigitAOperator = true
                }
            }

            divisionBtn.setOnClickListener {
                if (calculation.text.isNotEmpty()) {
                    calculation.text = when {
                        isTheLastDigitAOperator -> calculation.text.dropLast(1).toString() + "/"
                        else -> calculation.text.toString() + "/"
                    }
                    isTheLastDigitANumber = false
                    doTheNumberAlreadyHasADecimalPoint = false
                    isTheLastDigitAOperator = true
                }
            }

            plusBtn.setOnClickListener {
                if (calculation.text.isNotEmpty()) {
                    calculation.text = when {
                        isTheLastDigitAOperator -> calculation.text.dropLast(1).toString() + "+"
                        else -> calculation.text.toString() + "+"
                    }
                    isTheLastDigitANumber = false
                    doTheNumberAlreadyHasADecimalPoint = false
                    isTheLastDigitAOperator = true
                }
            }

            minusBtn.setOnClickListener {
                if (calculation.text.isNotEmpty()) {
                    calculation.text = when {
                        isTheLastDigitAOperator -> calculation.text.dropLast(1).toString() + "-"
                        else -> calculation.text.toString() + "-"
                    }
                    isTheLastDigitANumber = false
                    doTheNumberAlreadyHasADecimalPoint = false
                    isTheLastDigitAOperator = true
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
                calculation.text = String.format("%.5s", result)
                isTheLastDigitANumber = false
                isTheLastDigitAOperator = false
            }

            clearBtn.setOnClickListener {
                calculation.text = ""
                result = ""
                isTheLastDigitANumber = false
                isTheLastDigitAOperator = false
                doTheNumberAlreadyHasADecimalPoint = false
            }

        }

    }

    private fun setupCalculationListener() {
        binding.spaceForCalculation.doOnTextChanged { text, _, _, _ ->
            text?.let {
                var expression = text.toString().replace("x", "*")
                Log.i("success", "expression: $expression")
                try {
                    result = Keval.eval(expression).toString()
                    Log.i("success", "result: $result")
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