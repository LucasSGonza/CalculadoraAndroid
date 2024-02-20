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
import com.notkamui.keval.KevalInvalidSymbolException
import com.notkamui.keval.KevalZeroDivisionException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //flags to allow or not some action
    private var isTheLastDigitANumber: Boolean = false
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

            listOfNumberBtn.forEach { btn ->
                btn.setOnClickListener {
                    spaceForCalculation.text =
                        if (didUserFinishedTheCalc) "${btn.text}"
                        else spaceForCalculation.text.toString() + "${btn.text}"
                    isTheLastDigitANumber = true
                    didUserFinishedTheCalc = false
                }
            }

            listOfOperatorsBtn.forEach { btn ->
                btn.setOnClickListener {
                    with(spaceForCalculation.text) {
                        if (this.isNotEmpty()) {
                            spaceForCalculation.text =
                                if (Regex("""\d${'$'}""").find(this) == null)
                                    this.dropLast(1).toString() + "${btn.text}"
                                else this.toString() + "${btn.text}"
                            isTheLastDigitANumber = false
                            doTheNumberAlreadyHasADecimalPoint = false
                            didUserFinishedTheCalc = false
                        }
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
                with(spaceForCalculation.text) {
                    if (this.isNotEmpty()) {
                        if (!doTheNumberAlreadyHasADecimalPoint) {
                            spaceForCalculation.text =
                                if (Regex("""\d${'$'}""").find(this) != null)
                                    this.toString() + "."
                                else this.toString() + "0."
                            doTheNumberAlreadyHasADecimalPoint = true
                        }
                    } else {
                        spaceForCalculation.text = "0."
                        isTheLastDigitANumber = true
                        doTheNumberAlreadyHasADecimalPoint = true
                    }
                }
            }

            //finish the current calculation and reset some flags
            equalBtn.setOnClickListener {
                if (spaceForCalculation.text.isNotEmpty()) {
                    if (result == getString(R.string.error_default)) {
                        Toast.makeText(
                            it.context,
                            getString(R.string.error_division_by_zero),
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        //if the user tap again in the equal btn, do the last calc again
                        if (didUserFinishedTheCalc) {
                            val regexResult =
                                validateLastCalculation(lastCalculationDone)
                            regexResult?.let {
                                Log.i("test", "last calc to do: $regexResult")
                                try {
                                    result =
                                        Keval.eval(validateNumericExpression("$result$regexResult"))
                                            .toString()
                                } catch (e: KevalInvalidExpressionException) {
                                    Log.e("error2", "$e")
                                    result = "0"
                                    isTheLastDigitANumber = true
                                } catch (e: KevalZeroDivisionException) {
                                    Log.e("error", "$e")
                                    result = getString(R.string.error_default)
                                }
//                                catch (e: KevalInvalidSymbolException) {
//                                    result = "0"
//                                    isTheLastDigitANumber = true
//                                }
                            }
                        }
                        try {
                            with(spaceForCalculation.text) {
                                if (this.first() == '+') {
                                    spaceForCalculation.text = this.removePrefix("+")
                                }
                                spaceForCalculation.text =
                                    result.toDouble().toString().replace(",", ".")
                            }
                            isTheLastDigitANumber = false
                            doTheNumberAlreadyHasADecimalPoint =
                                true //the calc always return double
                            didUserFinishedTheCalc = true
                        } catch (e: NumberFormatException) {
                            Log.e("error", "$e")
                        }
                    }
                }
            }

            //clean the current calculation and reset the flags
            clearBtn.setOnClickListener {
                resetCalculation()
                spaceForCalculation.text = ""
            }

            //verify possible flag reset's and then delete the last digit
            deleteBtn.setOnClickListener {
                with(spaceForCalculation.text) {
                    if (this.isNotEmpty()) {
                        spaceForCalculation.text = this.toString().dropLast(1)
                        when {
                            Regex("""\d${'$'}""").find(spaceForCalculation.text) != null ->
                                isTheLastDigitANumber = true

                            this.last() == '.' -> {
                                doTheNumberAlreadyHasADecimalPoint = false
                                isTheLastDigitANumber = true
                            }

                            else -> {
                                isTheLastDigitANumber = false
                            }
                        }
                        didUserFinishedTheCalc = false
                    }
                }
            }

            integerNumberBtn.setOnClickListener {
                spaceForCalculation.text = with(spaceForCalculation.text) {
                    when {
                        isEmpty() -> "-"
                        this == "-" -> this.toString().removePrefix("-")
                        else -> {
                            val newNumber = validateNumberSignalChange(this.toString())
                            val oldNumber = validateNumberSignalChange(newNumber)
                            val newCount = this.toString()
                                .replace(Regex("""[-+]?\d+(\.\d+)?${'$'}"""), newNumber)

                            if (newCount == this.toString()) this.toString()
                                .replace(oldNumber.removePrefix("+"), newNumber)
                            else newCount
                        }
                    }
                }
            }

        }
    }

    private fun setupCalculationListener() {
        binding.spaceForCalculation.doOnTextChanged { text, _, _, _ ->
            text?.let {
                if (text.length < binding.spaceForCalculation.maxLines) {
                    if (text.isNotEmpty()) {
                        val expression = validateNumericExpression(text.toString())
                        Log.i("success", "expression: $expression")
                        try {
                            result = Keval.eval(expression).toString()
                            Log.i("success", "user finished the calc: $didUserFinishedTheCalc")
                            if (!didUserFinishedTheCalc) {
                                validateLastCalculation(expression)?.let {
                                    lastCalculationDone =
                                        if (it != result) it else lastCalculationDone
                                    Log.i("success", "last calc done: $lastCalculationDone")
                                }
                            }
                            Log.i("success", "result: $result")
                        } catch (e: KevalInvalidExpressionException) {
                            Log.e("error2", "$e")
                            result = "0"
                            isTheLastDigitANumber = true
                        } catch (e: KevalZeroDivisionException) {
                            Log.e("error", "$e")
                            result = getString(R.string.error_default)
                        }
//                        catch (e: KevalInvalidSymbolException) {
//                            result = "0"
//                            isTheLastDigitANumber = true
//                        }
                    } else {
                        Log.i("test", "calculation was reset")
                        resetCalculation()
                    }
                }
            }
        }
    }

    private fun resetCalculation() {
        result = ""
        isTheLastDigitANumber = false
        doTheNumberAlreadyHasADecimalPoint = false
        didUserFinishedTheCalc = false
    }

    private fun validateNumericExpression(text: String): String {
        var expression = text
            .replace("x", "*")
            .replace("%", "/100*")

        if (expression.first() == '-') {
            expression = expression.replaceFirst("-", "0-")
        }

        val optionalNumberCase = validateIntegerNumberInTheMiddleOfCount(expression)
        optionalNumberCase?.let {
            expression =
                expression.replace(
                    optionalNumberCase,
                    optionalNumberCase.replace("-", "(0-value)")
                )
        }

        if (expression.last() == '*') {
            expression = expression.dropLast(1)
        }

        return expression
    }

    //retornar um Pair de String, a primeira sendo a expressão toda e a segunda somente o numero
    private fun validateIntegerNumberInTheMiddleOfCount(expression: String): String? {
        val regexPattern = Regex("""[-+*x/%][-+]\d+${'$'}""") //ex: 5*-2
        regexPattern.find(expression)?.let {
            return it?.value //*-2
        } ?: return null
    }

    private fun validateNumberSignalChange(expression: String): String {
        val regexPattern =
            Regex("""[-+]?\d+(\.\d+)?${'$'}""")
        val regexResult = regexPattern.find(expression)
        regexResult?.let {
            return when {
                regexResult.value.first() == '-' -> regexResult.value.replace("-", "+")
                regexResult.value.first() == '+' -> regexResult.value.replace("+", "-")
                else -> "-" + regexResult.value
            }
        } ?: return getString(R.string.error_default)
    }

    private fun validateLastCalculation(expression: String): String? {
        val regexPattern =
            Regex("""[+\-*x/%]-?\d+(\.\d+)?(?![+\-*/%]*-?\d+(\.\d+)?)${'$'}""")
        return regexPattern.find(expression)?.value
    }

}