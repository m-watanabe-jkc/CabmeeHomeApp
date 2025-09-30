package com.jvckenwood.cabmee.simpleoperator.fakes.data.repository

import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.jvckenwood.cabmee.simpleoperator.domain.interfaces.MainRepositoryInterface

class FakeMainRepository : MainRepositoryInterface {
    var savedCounter: Int? = null
    var counterToLoad: Int = 0

    override suspend fun initialCloudService(appName: String, appVersion: String, deviceId: String?): Result<Unit, String> {
        return Ok(Unit)
    }

    override suspend fun createCloudServices(): Result<Unit, String> {
        return Ok(Unit)
    }

    override suspend fun login(operatorId: String, password: String): Result<Unit, String> {
        return Ok(Unit)
    }

    override suspend fun logout(operatorId: String): Result<Unit, String> {
        return Ok(Unit)
    }

    override suspend fun order(carId: String?, lat: Double, lng: Double): Result<Unit, String> {
        return Ok(Unit)
    }

    override suspend fun sendMessage(operatorId: String, driverId: String, title: String, message: String): Result<Unit, String> {
        return Ok(Unit)
    }

    override suspend fun saveAccountInfo(operatorId: String, password: String) {}

    override suspend fun loadAccountInfo(): Result<Pair<String, String>, String> {
        return Ok(Pair("", ""))
    }

    override suspend fun saveOrderInfo(carId: String, lat: Double, lng: Double) {}

    override suspend fun loadOrderInfo(): Result<Triple<String, Double, Double>, String> {
        return Ok(Triple("", 0.0, 0.0))
    }

    override suspend fun saveMessageInfo(driverId: String, title: String, message: String) {}

    override suspend fun loadMessageInfo(): Result<Triple<String, String, String>, String> {
        return Ok(Triple("", "", ""))
    }
}
