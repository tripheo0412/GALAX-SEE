package com.example.tripheo2410.galaxsee

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

class CustomArFragment : ArFragment() {
    override fun getSessionConfiguration(session: Session?): Config {
        //Disable the initial hand gesture
        planeDiscoveryController.setInstructionView(null)
        var config : Config = super.getSessionConfiguration(session)
        config.cloudAnchorMode = Config.CloudAnchorMode.ENABLED
        return config
    }

}