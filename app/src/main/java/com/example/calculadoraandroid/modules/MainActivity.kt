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
import kotlin.math.abs

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
        setupVisual()
        supportActionBar?.hide()
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

    private fun setupVisual() {
        listOfNumberBtn.forEach { it.setBackgroundColor(getColor(R.color.dark_blue)) }
        listOfOperatorsBtn.forEach { it.setBackgroundColor(getColor(R.color.orange)) }
        with(binding) {
            integerNumberBtn.setBackgroundColor(getColor(R.color.dark_blue))
            decimalBtn.setBackgroundColor(getColor(R.color.dark_blue))
            parenthesesBtn.setBackgroundColor(getColor(R.color.orange))
        }
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
                                if (Regex("""[\d()]${'$'}""").find(this) == null)
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
                                    "$this."
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
                            }
                        }
                        try {
                            spaceForCalculation.text =
                                result.toDouble().toString().replace(",", ".")

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

            deleteBtn.setOnClickListener {
                with(spaceForCalculation.text) {
                    if (this.isNotEmpty()) {
                        if (this.last() == '.') {
                            doTheNumberAlreadyHasADecimalPoint = false
                            isTheLastDigitANumber = true
                        }
                        spaceForCalculation.text = this.toString().dropLast(1)
                        isTheLastDigitANumber =
                            Regex("""\d${'$'}""").find(spaceForCalculation.text) != null
                        didUserFinishedTheCalc = false
                    }
                }
            }

            integerNumberBtn.setOnClickListener {
                spaceForCalculation.text = with(spaceForCalculation.text) {
                    when {
                        isEmpty() || Regex("""[+*x/%]${'$'}""").find(this) != null ->
                            "$this(-"

                        Regex("""\(-${'$'}""").find(this) != null ->
                            this.toString().removeSuffix("(-")

                        this.last() == '-' && (this.removeSuffix("(-") != this) ->
                            "$this(-"

                        else -> {
                            var newNumber = validateNumberSignalChange(this.toString())
                            var oldNumber = validateNumberSignalChange(newNumber)
                            if (this.last() == ')') {
                                oldNumber = oldNumber.plus(")")
                            }
                            val newCount = this.toString() + "f"
                            newCount.replace(oldNumber + "f", newNumber)
                        }
                    }
                }
            }

            parenthesesBtn.setOnClickListener {
                with(spaceForCalculation.text) {
                    val openingParentheses = this.filter { it == '(' }
                    val closingParentheses = this.filter { it == ')' }
                    var parenthesesToAdd =
                        if (openingParentheses.count() > closingParentheses.count()
//                            && Regex("""\((?![-+*x/%]\(?[-+]?\d+(\.\d+)?\)?)${'$'}""")
//                                .find(this) == null
                        )
                            ")" else "("
                    spaceForCalculation.text = "$this$parenthesesToAdd"
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
//                            Log.i("test", "result2: $result2")
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
                        } catch (e: NumberFormatException) {

                        }
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
        lastCalculationDone = ""
    }

    private fun validateNumericExpression(text: String): String {
        var expression = text
            .replace("x", "*")
            .replace("%", "/100*")

        if (expression.first() == '-' || expression.removePrefix("(-") != expression) {
            expression = expression.replaceFirst("-", "0-")
        }

        val listOfIntegerNumberCases = validateIntegerNumberInTheMiddleOfCount(expression)
        listOfIntegerNumberCases.forEach { value -> //*-2 , /(-2)
            val numberToAdapt = Regex("""[-+]\d+""").find(value) //-2
            numberToAdapt?.let {
                expression =
                    expression
                        .replace(
                            value,
                            value.replace(numberToAdapt.value, "(0${numberToAdapt.value})")
                        )
                        .replace(
                            "((0${numberToAdapt.value}))",
                            "(0${numberToAdapt.value})"
                        ) //validate cases like: ((0-2))
            }
        }

        if (expression.last() == '*') {
            expression = expression.dropLast(1)
        }

        return expression
    }

    private fun validateIntegerNumberInTheMiddleOfCount(expression: String): MutableList<String> {
        val regexResult = Regex("""[-+*x/%]\(?[-+]\d+\)?""").findAll(expression, 0)
        val groupValues = mutableListOf<String>()
        regexResult.forEach { matchResult -> //*-2
            groupValues.add(matchResult.value)
        }
        Log.i("test", "$groupValues")
        return groupValues
    }

    private fun validateNumberSignalChange(expression: String): String {
        Regex("""\(?[-+]?\d+(\.\d+)?\)?${'$'}""").find(expression)
            ?.let { matchResult ->  // -2 , (-5), ...
                return if (matchResult.value.contains("(-"))
                    matchResult.value
                        .removePrefix("(-")
                        .removeSuffix(")")
                        .removePrefix("+")
                        .removePrefix("-")
                else
                    "(-${
                        matchResult.value
                            .removePrefix("+")
                            .removePrefix("-")
                    }"
            } ?: return getString(R.string.error_default)
    }

    private fun validateLastCalculation(expression: String): String? {
        val regexPattern =
            Regex("""[-+*x/%]\(?([-+]?\d+(\.\d+)?)*[-+]?\d+(\.\d+)?\)?${'$'}""")
        val regexResult = regexPattern.find(expression)?.value
        regexResult?.let {
            return if (it.contains("(")) it
            else Regex("""[-+*x/%]\(?[-+]?\d+(\.\d+)?\)?${'$'}""").find(it)?.value
        } ?: return null
    }

}