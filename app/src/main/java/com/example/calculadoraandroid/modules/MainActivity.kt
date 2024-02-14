package com.example.calculadoraandroid.modules

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import com.example.calculadoraandroid.R
import com.example.calculadoraandroid.databinding.ActivityMainBinding
import com.notkamui.keval.Keval
import com.notkamui.keval.KevalInvalidExpressionException
import com.notkamui.keval.KevalZeroDivisionException
import kotlin.math.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //flags to allow or not some action
    private var isTheLastDigitANumber: Boolean = false
    private var isTheLastDigitAOperator: Boolean = false
    private var doTheNumberAlreadyHasADecimalPoint: Boolean = false

    //flag to verify if the user had finished the calc, to perform visual changes
    private var didUserFinishedTheCalc: Boolean = false
    private var lastCalculationDone: String = ""

    //list's of the group of buttons with very similar action's
    private var listOfNumberBtn = mutableListOf<Button>()
    private var listOfOperatorsBtn = mutableListOf<Button>()

    private var result: String = ""

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

            //if the user had finished the calc, a new number will replace the current result
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

            /*
            -> if has no count/number been displayed, do not add a operator
            -> if the last digit in the count is a operator, and the user click in any other operator
            again, instead of add 2 operators like "9+-", will just replace the oldest: "9+" -> "9-"
            */
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

            /*
            -> if has no text and the user select the decimal btn ".", will automatically place "0."
            as a default value
            -> if has text, and no decimal point, verify if has a number or not to put the default
            "0." value or not
            -> if already has a decimal point, do not allow to add another one in the same number
            */
            decimalBtn.setOnClickListener {
                if (spaceForCalculation.text.isNotEmpty()) {
                    if (!doTheNumberAlreadyHasADecimalPoint) {
                        spaceForCalculation.text =
                            if (isTheLastDigitANumber) spaceForCalculation.text.toString() + "."
                            else spaceForCalculation.text.toString() + "0."
                        doTheNumberAlreadyHasADecimalPoint = true
                    }
                } else {
                    spaceForCalculation.text = "0."
                    isTheLastDigitANumber = true
                    doTheNumberAlreadyHasADecimalPoint = true
                }
            }

            //finish the current calculation and reset some flags
            equalBtn.setOnClickListener {
                if (result == getString(R.string.error_default)) {
                    Toast.makeText(
                        it.context,
                        getString(R.string.error_division_by_zero),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                //if the user tap again in the equal btn, do the last calc again
                if (didUserFinishedTheCalc) {
                    val regexPattern =
                        Regex("""[+\-*/%]-?\d+(\.\d+)?(?![+\-*/%]\s*-?\d+(\.\d+)?)${'$'}""")
                    val regexResult = regexPattern.find(lastCalculationDone) //its getting 'null'
                    regexResult?.let {
                        Log.i("test", "${regexResult.value}")
                        result = Keval.eval(regexResult.value).toString()
                    }
                }

                spaceForCalculation.text = String.format("%.5s", result)
//                spaceForCalculation.text = result
//                spaceForCalculation.text = try {
//                    String.format("%.5s", String.format("%.3f", result.toDouble()))
//                } catch (e: NumberFormatException) {
//                    getString(R.string.error_in_code)
//                }

                isTheLastDigitANumber = false
                isTheLastDigitAOperator = false
                didUserFinishedTheCalc = true
                doTheNumberAlreadyHasADecimalPoint = true
            }

            //clean the current calculation and reset the flags
            clearBtn.setOnClickListener {
                spaceForCalculation.text = ""
                result = ""
                isTheLastDigitANumber = false
                isTheLastDigitAOperator = false
                doTheNumberAlreadyHasADecimalPoint = false
            }

            //delete the last digit and verify possible flag reset's
            deleteBtn.setOnClickListener {
                if (spaceForCalculation.text.isNotEmpty()) {
                    if (isTheLastDigitAOperator) {
                        isTheLastDigitAOperator = false
                    }
                    if (spaceForCalculation.text.last() == '.') {
                        doTheNumberAlreadyHasADecimalPoint = false
                        isTheLastDigitANumber = true
                    }
                    didUserFinishedTheCalc = false
                    spaceForCalculation.text = spaceForCalculation.text.toString().dropLast(1)
                }
            }

        }
    }

    private fun setupCalculationListener() {
        binding.spaceForCalculation.doOnTextChanged { text, _, _, _ ->
            text?.let {
                if (text.length < binding.spaceForCalculation.maxLines) {
                    if (text.isNotEmpty()) {
                        val expression = validateExpression(text.toString())
                        Log.i("success", "expression1: $expression")
                        try {
                            result = Keval.eval(expression).toString()
                            lastCalculationDone = expression
                            Log.i("success", "result after calc: $result")
                        } catch (e: KevalInvalidExpressionException) {
                            Log.e("error", "$e")
                        } catch (e: KevalZeroDivisionException) {
                            Log.e("error", "$e")
                            result = getString(R.string.error_default)
                        }
                    } else {
                        Log.i("test", "calculation was reset")
                        isTheLastDigitANumber = false
                        isTheLastDigitAOperator = false
                        doTheNumberAlreadyHasADecimalPoint = false
                        result = ""
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

//    private fun validateResult(numericResult: Double): String {
//        return try {
//            if (numericResult.toInt() % 1 == 0) numericResult.toInt().toString()
//            else numericResult.toString()
//        } catch (e: NumberFormatException) {
//            getString(R.string.error_default)
//        }
//    }

}