package com.indwealth.core.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.indwealth.core.R
import com.indwealth.core.util.getColorById
import com.indwealth.core.util.onNext
import com.indwealth.core.util.rupeeString
import com.indwealth.core.util.shortRupeeString
import kotlinx.android.synthetic.main.view_rupee_input.view.*
import kotlinx.coroutines.*

class RupeeInput : FrameLayout {

    private var allowZero = false
    private var showWordBelow = false
    private var showWordRight = false
    private var hideThousands = false
    private var listener: ((Long) -> Unit)? = null

    private var debounceListener: ((Long) -> Unit)? = null
    private var debounceTime = 500L

    var lastInput = 0L
    var debounceJob: Job? = null
    val uiScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    val ui: View
    var min = 0
    var max = 1_00_00_000L
    var minError = ""
    var maxError = ""

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) : super(context, attrs, defStyle) {
        this.ui = LayoutInflater.from(context).inflate(R.layout.view_rupee_input, this, true)
        val a = context.obtainStyledAttributes(attrs, R.styleable.RupeeInput)

        val hint = a.getString(R.styleable.RupeeInput_android_hint)
        val label = a.getString(R.styleable.RupeeInput_android_label)
        val textSize = a.getDimensionPixelSize(R.styleable.RupeeInput_android_textSize,
                resources.getDimensionPixelSize(R.dimen.rupee_input_default_text_size))
        val textSizeWordsBelow = a.getDimensionPixelSize(R.styleable.RupeeInput_textSizeWordsBelow,
                resources.getDimensionPixelSize(R.dimen.rupee_input_default_text_size))
        showWordBelow = a.getBoolean(R.styleable.RupeeInput_showWordBelow, false)
        showWordRight = a.getBoolean(R.styleable.RupeeInput_showWordRight, true)
        hideThousands = a.getBoolean(R.styleable.RupeeInput_hideThousands, true)
        max = a.getInteger(R.styleable.RupeeInput_max_amount, 1000000000).toLong()
        a.recycle()

        if (hint != null)
            ui.customRupeeInput.hint = hint
        if (label != null)
            ui.customRupeeInput.floatingLabelText = label

        ui.customRupeeInput.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize.toFloat())
        ui.customRupeeInputFormattedValueBelow.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSizeWordsBelow.toFloat())
        ui.customRupeeInputFormattedValue.isVisible = showWordRight
        ui.customRupeeInputFormattedValueBelow.isVisible = showWordBelow
        hideCurrency(true)
        ui.customRupeeInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                ui.customRupeeInputError.isInvisible = true
                hideCurrency(s.isNullOrBlank())
                val amount = getAmount()
                listener?.invoke(amount)
                debounceLogic(amount)
                if (isValid(true)) showAmountWords()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        ui.customRupeeInput.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                ui.bottomLine.setBackgroundColor(context.getColorById(R.color.colorPrimary))
            } else {
                ui.bottomLine.setBackgroundColor(context.getColorById(R.color.textColorLabels))
            }
        }
    }

    fun getAmount(): Long {
        return if (ui.customRupeeInput.text.isNullOrBlank()) 0
        else ui.customRupeeInput.text!!.toString().toLong()
    }

    fun setOnAmountChangeListener(listener: (Long) -> Unit) {
        this.listener = listener
    }

    fun allowZero() {
        this.allowZero = true
    }

    fun isValid(showError: Boolean = true): Boolean {
        val input = ui.customRupeeInput.text.toString()
        var error: String? = null
        if (input.isBlank()) {
            return false
        } else try {
            val amount = input.toLong()
            if (!allowZero && amount == 0L) error = "Cannot be zero"
            if (amount < min) error = if (minError.isBlank()) "Min: ${min.rupeeString()}" else minError
            if (amount > max) error = if (maxError.isBlank()) "Max: ${max.rupeeString()}" else maxError
        } catch (e: Exception) {
            return false
        }

        return if (error == null) {
            ui.customRupeeInputError.isInvisible = true
            true
        } else {
            if (showError) showErrorMessage(error)
            false
        }
    }

    private fun showAmountWords() {
        val amount = getAmount()
        if (amount > 9999) {
            ui.customRupeeInputFormattedValueBelow.text = context.getString(R.string.rupee_input_below_words_prefix,
                    amount.shortRupeeString(hideThousands))

            ui.customRupeeInputFormattedValue.text = amount.shortRupeeString(hideThousands)

            ui.customRupeeInputFormattedValue.isVisible = showWordRight
            ui.customRupeeInputFormattedValueBelow.isVisible = showWordBelow
        } else {
            ui.customRupeeInputFormattedValueBelow.isGone = true
            ui.customRupeeInputFormattedValue.isGone = true
        }
    }

    fun hideCurrency(hide: Boolean) {
        ui.customRupeeCurrency.isGone = hide
    }

    fun onNext(onNext: () -> Unit) {
        ui.customRupeeInput.onNext(onNext)
    }

    fun setAmount(amount: Long) {
        ui.customRupeeInput.setText(amount.toString())
        ui.customRupeeInput.invalidate()
        ui.customRupeeInput.setSelection(ui.customRupeeInput.text!!.length)
    }

    fun setHint(hint: String) {
        ui.customRupeeInput.hint = hint
    }

    fun setLabel(label: String) {
        ui.customRupeeInput.floatingLabelText = label
    }

    fun setFloatingLabelAlwaysShown(alwaysShown: Boolean) {
        ui.customRupeeInput.isFloatingLabelAlwaysShown = alwaysShown
    }

    fun enableInput(isEnabled: Boolean) {

        ui.customRupeeInput.isEnabled = isEnabled
        ui.customRupeeInput.isEnabled = isEnabled
        ui.customRupeeInput.isFocusable = isEnabled
        ui.customRupeeInput.isFocusableInTouchMode = isEnabled

        hideCurrency(!isEnabled)

        ui.customRupeeInputFormattedValue.isVisible = showWordRight && isEnabled
        ui.customRupeeInputFormattedValueBelow.isVisible = showWordBelow && isEnabled
    }

    fun showErrorMessage(errorMessg: String?) {
        if (errorMessg.isNullOrBlank()) {
            ui.customRupeeInputError.text = ""
            ui.customRupeeInputError.isInvisible = true
        } else {
            ui.customRupeeInputError.text = errorMessg
            ui.customRupeeInputError.isVisible = true
            ui.customRupeeInputFormattedValueBelow.isVisible = false
            ui.customRupeeInputFormattedValue.isVisible = false
        }
    }

    /**
     * get amount change updates with debounce
     */
    fun setDebounceOnAmountChangeListener(time: Long = debounceTime, debounceListener: (Long) -> Unit) {
        this.debounceListener = debounceListener
        this.debounceTime = time
    }

    fun isEmpty() = customRupeeInput.text.toString().isBlank()

    private fun debounceLogic(newAmount: Long) {
        if (debounceListener == null) return

        debounceJob?.cancel()
        if (lastInput != newAmount) {
            lastInput = newAmount
            debounceJob = uiScope.launch {
                delay(debounceTime)
                if (lastInput == newAmount) {
                    debounceListener?.invoke(newAmount)
                }
            }
        }
    }
}