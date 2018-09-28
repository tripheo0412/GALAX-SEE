package com.example.tripheo2410.galaxsee

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

class CustomArFragment : ArFragment() {

    override fun getSessionConfiguration(session: Session): Config {
        planeDiscoveryController.setInstructionView(null)
        val config = super.getSessionConfiguration(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        return config
    }
}