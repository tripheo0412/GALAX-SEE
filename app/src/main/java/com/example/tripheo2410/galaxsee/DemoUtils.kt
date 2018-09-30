package com.example.tripheo2410.galaxsee

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.widget.Toast


/** Static utility methods to simplify creating multiple demo activities.  */
object DemoUtils {
    private val TAG = "SceneformDemoUtils"
    private val MIN_OPENGL_VERSION = 3.0

    /**
     * Creates and shows a Toast containing an error message. If there was an exception passed in it
     * will be appended to the toast. The error will also be written to the Log
     */
    fun displayError(
            context: Context, errorMsg: String, problem: Throwable?) {
        val tag = context.javaClass.getSimpleName()
        val toastText: String
        if (problem != null && problem.message != null) {
            Log.e(tag, errorMsg, problem)
            toastText = errorMsg + ": " + problem.message
        } else if (problem != null) {
            Log.e(tag, errorMsg, problem)
            toastText = errorMsg
        } else {
            Log.e(tag, errorMsg)
            toastText = errorMsg
        }

        Handler(Looper.getMainLooper())
                .post {
                    val toast = Toast.makeText(context, toastText, Toast.LENGTH_LONG)
                    toast.setGravity(Gravity.CENTER, 0, 0)
                    toast.show()
                }
    }
}