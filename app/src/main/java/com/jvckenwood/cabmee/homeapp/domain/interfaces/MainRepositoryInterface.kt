package com.jvckenwood.cabmee.homeapp.domain.interfaces

import com.github.michaelbull.result.Result
import com.jvckenwood.cabmee.homeapp.domain.entity.MainEntity

interface MainRepositoryInterface {
    suspend fun loadMainData(): Result<MainEntity, String>
    suspend fun saveCounter(counter: Long)
    suspend fun saveAutoStartSettings(
        autoStartApplicationIndex: Int,
        autoStartApplicationInterval: Int
    ): Result<Unit, String>

    suspend fun saveTargetPackageList(targetPackageList: List<String>): Result<Unit, String>
}
