package com.leekleak.trafficlight.integrations

interface ShizukuServicesProvider {
    fun updateSimData()
    fun updateSimDataBasic()
    fun shizukuRunning(): Boolean
    fun shizukuPermission(): Int
    fun shizukuRequestPermission()
    fun enable()
    fun disable()
}