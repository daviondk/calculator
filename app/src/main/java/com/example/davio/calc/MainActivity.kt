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
    val STATE_INPUT = ""
    val STATE_DEQUE_COUNTER: Deque<Int> = LinkedList()
    val STATE_DEQUE_OPERATIONS: Deque<Int> = LinkedList()
    var NUMBER_FLAG = 1
    var LEFT_FUNC_FLAG = 2
    var RIGHT_FUNC_FLAG = 3
    var SIMPLE_OPER_FLAG = 4
    var FRACTION_FLAG = 5
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
        savedInstanceState.putString(STATE_INPUT, input_field.text.toString())
        savedInstanceState.putBoolean("memFlag", memoryFlag)
        savedInstanceState.putBoolean("inv", invert)
        savedInstanceState.putBoolean("unit", unit)
        savedInstanceState.putBoolean("dot", lastNumberWithDotFlag)
        savedInstanceState.putString("memValue", memoryValue.toString())
        savedInstanceState.putIntArray("stack_operations", STATE_DEQUE_OPERATIONS.toIntArray())
        savedInstanceState.putIntArray("stack_counter", STATE_DEQUE_COUNTER.toIntArray())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        for (element in savedInstanceState?.getIntArray("stack_operations") as IntArray)
            STATE_DEQUE_OPERATIONS.addLast(element)
        for (element in savedInstanceState.getIntArray("stack_counter") as IntArray)
            STATE_DEQUE_COUNTER.addLast(element)
        input_field.text = savedInstanceState.getString(STATE_INPUT)
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
        STATE_DEQUE_COUNTER.addFirst(curText.length)
        if (!lastNumberWithDotFlag) {
            STATE_DEQUE_OPERATIONS.addFirst(NUMBER_FLAG)
        } else {
            STATE_DEQUE_OPERATIONS.addFirst(FRACTION_FLAG)
        }
        getResult()
    }

    fun onSimpleOperationClick(view: View) {
        if ((STATE_DEQUE_OPERATIONS.isEmpty() || STATE_DEQUE_OPERATIONS.peek() == LEFT_FUNC_FLAG || STATE_DEQUE_OPERATIONS.peek() == -NUMBER_FLAG) && view.id == minus.id
                || STATE_DEQUE_OPERATIONS.peek() == FRACTION_FLAG || STATE_DEQUE_OPERATIONS.peek() == RIGHT_FUNC_FLAG
                || STATE_DEQUE_OPERATIONS.peek() == NUMBER_FLAG || STATE_DEQUE_OPERATIONS.peek() == 0) {
            onCharacterClick(view)
            STATE_DEQUE_OPERATIONS.pop()
            STATE_DEQUE_OPERATIONS.addFirst(SIMPLE_OPER_FLAG)
        } else if (STATE_DEQUE_OPERATIONS.peek() == SIMPLE_OPER_FLAG) {
            onBackspaceClick(backspace)

            onCharacterClick(view)
            STATE_DEQUE_OPERATIONS.pop()
            STATE_DEQUE_OPERATIONS.addFirst(SIMPLE_OPER_FLAG)
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
        STATE_DEQUE_COUNTER.addFirst(curText.length)
        STATE_DEQUE_OPERATIONS.addFirst(LEFT_FUNC_FLAG)
        lastNumberWithDotFlag = false
        getResult()
    }

    fun onRightFunctionClick(view: View) {
        if (STATE_DEQUE_OPERATIONS.peek() == 0 || STATE_DEQUE_OPERATIONS.peek() == FRACTION_FLAG ||
                STATE_DEQUE_OPERATIONS.peek() == NUMBER_FLAG || STATE_DEQUE_OPERATIONS.peek() == RIGHT_FUNC_FLAG) {
            onLeftFunctionClick(view)
            STATE_DEQUE_OPERATIONS.pop()
            STATE_DEQUE_OPERATIONS.addFirst(RIGHT_FUNC_FLAG)
        }
        lastNumberWithDotFlag = false
    }

    fun onBackspaceClick(view: View) {
        if (!STATE_DEQUE_COUNTER.isEmpty()) {
            input_field.text = input_field.text.dropLast(STATE_DEQUE_COUNTER.pop())
            STATE_DEQUE_OPERATIONS.pop()
            getResult()
        }
    }

    fun onClearClick(view: View) {
        input_field.text = ""
        result_field.text = ""
        STATE_DEQUE_COUNTER.clear()
        STATE_DEQUE_OPERATIONS.clear()
    }

    fun onPlusMinusClick(view: View) {
        if (input_field.text.isEmpty()) {
            append(input_field, "-")
            STATE_DEQUE_COUNTER.addFirst(NUMBER_FLAG)
            STATE_DEQUE_OPERATIONS.addFirst(SIMPLE_OPER_FLAG)
        } else {
            appendFront(input_field, "-(")
            STATE_DEQUE_COUNTER.addLast(LEFT_FUNC_FLAG)
            STATE_DEQUE_OPERATIONS.addLast(SIMPLE_OPER_FLAG)
        }
        getResult()
    }

    fun onMCClick(view: View) {
        memoryFlag = false
        memoryValue = BigDecimal.ZERO
    }

    fun onMRClick(view: View) {
        if (STATE_DEQUE_OPERATIONS.peek() != NUMBER_FLAG && STATE_DEQUE_OPERATIONS.peek() != FRACTION_FLAG && memoryFlag) {
            lastNumberWithDotFlag = false
            for (digit in memoryValue.toString()) {
                if (digit == ',') {
                    lastNumberWithDotFlag = true
                }
                append(input_field, digit.toString())
                STATE_DEQUE_COUNTER.addFirst(NUMBER_FLAG)
                STATE_DEQUE_OPERATIONS.addFirst(NUMBER_FLAG)
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
            STATE_DEQUE_COUNTER.clear()
            STATE_DEQUE_OPERATIONS.clear()

            for (symbol in result_field.text) {
                onCharacterClickString(symbol.toString())
            }
        }
    }

    fun onOpenBracketClick(view: View) {
        onCharacterClick(view)
        STATE_DEQUE_OPERATIONS.pop()
        STATE_DEQUE_OPERATIONS.addFirst(-NUMBER_FLAG)

    }

    fun onCloseBracketClick(view: View) {
        onCharacterClick(view)
        STATE_DEQUE_OPERATIONS.pop()
        STATE_DEQUE_OPERATIONS.addFirst(0)
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
        if (!lastNumberWithDotFlag && (STATE_DEQUE_OPERATIONS.isEmpty() || STATE_DEQUE_OPERATIONS.peek() == NUMBER_FLAG)) {
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

}