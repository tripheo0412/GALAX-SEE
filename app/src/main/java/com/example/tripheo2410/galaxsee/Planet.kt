package com.example.tripheo2410.galaxsee

import android.view.MotionEvent
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node

class Planet {
    private var infoCard: Node? = null
    companion object {

        private val INFO_CARD_Y_POS_COEFF = 0.55f

        override fun onTap(hitTestResult: HitTestResult?, motionEvent: MotionEvent?) {
            if (infoCard == null) {
                return
            }

            infoCard!!.isEnabled = !infoCard!!.isEnabled
        }
    }
}