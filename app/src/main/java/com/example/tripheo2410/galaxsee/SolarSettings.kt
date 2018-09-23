package com.example.tripheo2410.galaxsee

public class SolarSettings {
    private var orbitSpeedMultipiler : Float = 1.0f
    private var rotationSpeedMultiplier : Float = 1.0f

    public fun setOrbitSpeedMultiplier(orbitSpeedMultiplier: Float){
        this.orbitSpeedMultipiler = orbitSpeedMultiplier
    }

    public fun  getOrbitSpeedMultiplier() :Float {
        return orbitSpeedMultipiler
    }

    public fun setRotationSpeedMultiplier(rotationSpeedMultiplier: Float) {
        this.rotationSpeedMultiplier = rotationSpeedMultiplier
    }

    public fun getRotationSpeedMultiplier(): Float {
        return rotationSpeedMultiplier
    }
}