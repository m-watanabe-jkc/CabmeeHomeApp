package com.jvckenwood.cabmee.homeapp.domain.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.jvckenwood.cabmee.homeapp.domain.entity.MainEntity
import com.jvckenwood.cabmee.homeapp.domain.entity.StateManager
import com.jvckenwood.cabmee.homeapp.domain.interfaces.MainRepositoryInterface
import com.jvckenwood.cabmee.homeapp.domain.state.MainState
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitializeUseCase @Inject constructor(
    private val repository: MainRepositoryInterface,
    private val stateMgr: StateManager
) {
    private val defaultTargetPackageList = listOf(
        "com.jvckenwood.taitis.taitiscarapp",
        "com.ubercab.driver",
        "com.jvckenwood.taitis.cabmeekeyboard",
        "com.jvckenwood.taitis.carappupdater",
        "com.jvckenwood.carappupdater",
        "com.android.calculator2"
    )

    suspend operator fun invoke(): Result<Unit, String> {
        Timber.d("InitializeUseCase invoked")
        repository.loadMainData()
            .onSuccess { loaded ->
                val normalizedTargets = if (loaded.targetPackageList.isEmpty()) {
                    defaultTargetPackageList
                } else {
                    loaded.targetPackageList
                }

                val mainEntity = loaded.copy(targetPackageList = normalizedTargets)
                stateMgr.updateMainState(MainState.Success(mainEntity))

                if (loaded.targetPackageList.isEmpty()) {
                    repository.saveTargetPackageList(normalizedTargets)
                }
                return Ok(Unit)
            }
            .onFailure {
                return Err(it)
            }
        return Err("Unknown Error!")
    }
}
