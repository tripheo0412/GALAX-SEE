package com.example.tripheo2410.galaxsee.dialog_handling

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.text.InputType
import android.text.InputFilter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams

/** A DialogFragment for the Resolve Dialog Box.  */
class ResolveDialogFragment : DialogFragment() {

    private var okListener: OkListener? = null
    private var cancelListener: CancelListener? = null
    private var shortCodeField: EditText? = null

    /**
     * Creates a simple layout for the dialog. This contains a single user-editable text field whose
     * input type is retricted to numbers only, for simplicity.
     */
    private val dialogLayout: LinearLayout
        get() {
            val context = context
            val layout = LinearLayout(context)
            shortCodeField = EditText(context)
            shortCodeField!!.inputType = InputType.TYPE_CLASS_NUMBER
            shortCodeField!!.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            shortCodeField!!.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(8))
            layout.addView(shortCodeField)
            layout.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            return layout
        }

    internal interface OkListener {
        fun onOkPressed(dialogValue: String)
    }

    internal interface CancelListener {
        fun onCancelPressed()
    }

    /** Sets a listener that is invoked when the OK button on this dialog is pressed.  */
    internal fun setOkListener(okListener: OkListener) {
        this.okListener = okListener
    }

    internal fun setCancelListener(cancelListener: CancelListener) {
        this.cancelListener = cancelListener
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        builder
                .setView(dialogLayout)
                .setTitle("Resolve Anchor")
                .setPositiveButton(
                        "OK"
                ) { dialog, which ->
                    val shortCodeText = shortCodeField!!.text
                    if (okListener != null && shortCodeText != null && shortCodeText.length > 0) {
                        // Invoke the callback with the current checked item.
                        okListener!!.onOkPressed(shortCodeText.toString())
                    }
                }
                .setNegativeButton("Cancel") { dialog, which ->
                    cancelListener!!.onCancelPressed()
                }
        return builder.create()
    }
}