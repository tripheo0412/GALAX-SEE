package com.example.tripheo2410.galaxsee.dialogHandling

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.Toast

/** Static utility methods to simplify creating multiple demo activities.  */
object DemoUtils {
     /**
     * Creates and shows a Toast containing an error message. If there was an exception passed in it
     * will be appended to the toast. The error will also be written to the Log
     */
    fun displayError(
            context: Context, errorMsg: String, problem: Throwable?) {
        val tag = context.javaClass.getSimpleName()
        val toastText: String
        toastText = when {
            problem?.message != null -> {
                Log.e(tag, errorMsg, problem)
                errorMsg + ": " + problem.message
            }
            problem != null -> {
                Log.e(tag, errorMsg, problem)
                errorMsg
            }
            else -> {
                Log.e(tag, errorMsg)
                errorMsg
            }
        }

        Handler(Looper.getMainLooper())
                .post {
                    val toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
    }
}