package com.example.davio.calc

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.udojava.evalex.Expression
import kotlinx.android.synthetic.main.activity_main.*
import java.math.BigDecimal
import java.util.*


class MainActivity : AppCompatActivity() {
    val stateDequeCounter: Deque<Int> = LinkedList()
    val stateDequeOperations: Deque<Int> = LinkedList()
    var memoryFlag = false
    var memoryValue = BigDecimal(0)
    var invert = false
    var unit = false
    var lastNumberWithDotFlag = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putString(Companion.STATE_INPUT, input_field.text.toString())
        savedInstanceState.putBoolean("memFlag", memoryFlag)
        savedInstanceState.putBoolean("inv", invert)
        savedInstanceState.putBoolean("unit", unit)
        savedInstanceState.putBoolean("dot", lastNumberWithDotFlag)
        savedInstanceState.putString("memValue", memoryValue.toString())
        savedInstanceState.putIntArray("stack_operations", stateDequeOperations.toIntArray())
        savedInstanceState.putIntArray("stack_counter", stateDequeCounter.toIntArray())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        for (element in savedInstanceState?.getIntArray("stack_operations") as IntArray)
            stateDequeOperations.addLast(element)
        for (element in savedInstanceState.getIntArray("stack_counter") as IntArray)
            stateDequeCounter.addLast(element)
        input_field.text = savedInstanceState.getString(Companion.STATE_INPUT)
        memoryFlag = savedInstanceState.getBoolean("memFlag")
        lastNumberWithDotFlag = savedInstanceState.getBoolean("dot")
        invert = savedInstanceState.getBoolean("inv")
        if (invert && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            switchInvert()
        }
        unit = savedInstanceState.getBoolean("unit")
        if (unit && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            switchUnit()
        }
        memoryValue = BigDecimal(savedInstanceState.getString("memValue"))
        getResult()
    }

    fun append(textView: TextView, appendedText: String) {
        textView.text = textView.text.toString() + appendedText
    }

    fun appendFront(textView: TextView, appendedText: String) {
        textView.text = appendedText + textView.text.toString()
    }

    fun onCharacterClick(view: View) {
        val curText = (view as? TextView)?.contentDescription.toString()
        onCharacterClickString(curText)
    }

    fun onCharacterClickString(curText: String) {
        append(input_field, curText)
        stateDequeCounter.addFirst(curText.length)
        if (!lastNumberWithDotFlag) {
            stateDequeOperations.addFirst(Companion.NUMBER_FLAG)
        } else {
            stateDequeOperations.addFirst(Companion.FRACTION_FLAG)
        }
        getResult()
    }

    fun onSimpleOperationClick(view: View) {
        if ((stateDequeOperations.isEmpty() || stateDequeOperations.peek() == Companion.LEFT_FUNC_FLAG || stateDequeOperations.peek() == -Companion.NUMBER_FLAG) && view.id == minus.id
                || stateDequeOperations.peek() == Companion.FRACTION_FLAG || stateDequeOperations.peek() == Companion.RIGHT_FUNC_FLAG
                || stateDequeOperations.peek() == Companion.NUMBER_FLAG || stateDequeOperations.peek() == 0) {
            onCharacterClick(view)
            stateDequeOperations.pop()
            stateDequeOperations.addFirst(Companion.SIMPLE_OPER_FLAG)
        } else if (stateDequeOperations.peek() == Companion.SIMPLE_OPER_FLAG) {
            onBackspaceClick(backspace)

            onCharacterClick(view)
            stateDequeOperations.pop()
            stateDequeOperations.addFirst(Companion.SIMPLE_OPER_FLAG)
        }
        lastNumberWithDotFlag = false
        //getResult()
    }

    fun onLeftFunctionClick(view: View) {
        val curText = (view as? TextView)?.contentDescription.toString()
        /*if(input_field.text.isEmpty() || !operationFlag){
            curText.padStart(NUMBER_FLAG, '*')
        }*/
        append(input_field, curText)
        stateDequeCounter.addFirst(curText.length)
        stateDequeOperations.addFirst(Companion.LEFT_FUNC_FLAG)
        lastNumberWithDotFlag = false
        getResult()
    }

    fun onRightFunctionClick(view: View) {
        if (stateDequeOperations.peek() == 0 || stateDequeOperations.peek() == Companion.FRACTION_FLAG ||
                stateDequeOperations.peek() == Companion.NUMBER_FLAG || stateDequeOperations.peek() == Companion.RIGHT_FUNC_FLAG) {
            onLeftFunctionClick(view)
            stateDequeOperations.pop()
            stateDequeOperations.addFirst(Companion.RIGHT_FUNC_FLAG)
        }
        lastNumberWithDotFlag = false
    }

    fun onBackspaceClick(view: View) {
        if (!stateDequeCounter.isEmpty()) {
            input_field.text = input_field.text.dropLast(stateDequeCounter.pop())
            stateDequeOperations.pop()
            getResult()
        }
    }

    fun onClearClick(view: View) {
        input_field.text = ""
        result_field.text = ""
        stateDequeCounter.clear()
        stateDequeOperations.clear()
    }

    fun onPlusMinusClick(view: View) {
        if (input_field.text.isEmpty()) {
            append(input_field, "-")
            stateDequeCounter.addFirst(Companion.NUMBER_FLAG)
            stateDequeOperations.addFirst(Companion.SIMPLE_OPER_FLAG)
        } else {
            appendFront(input_field, "-(")
            stateDequeCounter.addLast(Companion.LEFT_FUNC_FLAG)
            stateDequeOperations.addLast(Companion.SIMPLE_OPER_FLAG)
        }
        getResult()
    }

    fun onMCClick(view: View) {
        memoryFlag = false
        memoryValue = BigDecimal.ZERO
    }

    fun onMRClick(view: View) {
        if (stateDequeOperations.peek() != Companion.NUMBER_FLAG && stateDequeOperations.peek() != Companion.FRACTION_FLAG && memoryFlag) {
            lastNumberWithDotFlag = false
            for (digit in memoryValue.toString()) {
                if (digit == ',') {
                    lastNumberWithDotFlag = true
                }
                append(input_field, digit.toString())
                stateDequeCounter.addFirst(Companion.NUMBER_FLAG)
                stateDequeOperations.addFirst(Companion.NUMBER_FLAG)
            }
        }
    }

    fun onMPlusClick(view: View) {
        getResult()
        if (!result_field.text.isEmpty() && result_field.text != "Error") {
            memoryFlag = true
            memoryValue = memoryValue.add(BigDecimal(result_field.text.toString()))
        } else {
            memoryFlag = false
            memoryValue = BigDecimal.ZERO
        }
    }

    fun onMMinusClick(view: View) {
        getResult()
        if (!result_field.text.isEmpty() && result_field.text != "Error") {
            memoryFlag = true
            memoryValue = memoryValue.subtract(BigDecimal(result_field.text.toString()))
        } else {
            memoryFlag = false
            memoryValue = BigDecimal.ZERO
        }
    }

    fun getResult() {
        val expr_string = input_field.text.toString()
        var balance = 0
        for (symbol in expr_string) {
            if (symbol == '(') {
                balance++
            } else if (symbol == ')') {
                balance--
            }
        }
        try {
            val expression = Expression(expr_string.padEnd(expr_string.length + balance, ')'))
            val result = expression.eval()
            result_field.text = result.toString()
        } catch (e: Exception) {
            if (expr_string.length > 0) {
                result_field.text = "Error"
            } else {
                result_field.text = ""
            }
        }
    }

    fun onEqualityClick(view: View) {
        getResult()
        if (!result_field.text.isEmpty() && result_field.text != "Error") {
            input_field.text = ""
            stateDequeCounter.clear()
            stateDequeOperations.clear()

            for (symbol in result_field.text) {
                onCharacterClickString(symbol.toString())
            }
        }
    }

    fun onOpenBracketClick(view: View) {
        onCharacterClick(view)
        stateDequeOperations.pop()
        stateDequeOperations.addFirst(-Companion.NUMBER_FLAG)

    }

    fun onCloseBracketClick(view: View) {
        onCharacterClick(view)
        stateDequeOperations.pop()
        stateDequeOperations.addFirst(0)
    }

    fun switchInvert() {
        if (!invert) {
            if (!unit) {
                sin.text = "sin"
                cos.text = "cos"
                tan.text = "tan"
            } else {
                sin.text = "sinh"
                cos.text = "cosh"
                tan.text = "tanh"
            }
        } else {
            if (!unit) {
                sin.text = "asin"
                cos.text = "acos"
                tan.text = "atan"
            } else {
                sin.text = "asinh"
                cos.text = "acosh"
                tan.text = "atanh"
            }
        }
        sin.contentDescription = sin.text.toString() + "("
        cos.contentDescription = cos.text.toString() + "("
        tan.contentDescription = tan.text.toString() + "("
    }

    fun onInvertClick(view: View) {
        invert = invert.not()
        switchInvert()
    }

    fun onPointClick(view: View) {
        if (!lastNumberWithDotFlag && (stateDequeOperations.isEmpty() || stateDequeOperations.peek() == Companion.NUMBER_FLAG)) {
            lastNumberWithDotFlag = true
            onCharacterClick(view)
        }
    }

    fun switchUnit() {
        if (unit) {
            angle_unit.text = "DEG"
        } else {
            angle_unit.text = "RAD"
        }
        switchInvert()
    }

    fun onSwitchUnitClick(view: View) {
        unit = unit.not()
        switchUnit()
    }

    companion object {
        const val NUMBER_FLAG = 1
        const val LEFT_FUNC_FLAG = 2
        const val RIGHT_FUNC_FLAG = 3
        const val FRACTION_FLAG = 5
        const val SIMPLE_OPER_FLAG = 4
        const val STATE_INPUT = ""
    }

}