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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateAutoStartAppSettingUseCase @Inject constructor(
    private val repository: MainRepositoryInterface,
    private val stateMgr: StateManager
) {
    suspend operator fun invoke(
        autoStartApplicationIndex: Int,
        autoStartApplicationInterval: Int
    ): Result<Unit, String> {
        return repository.saveAutoStartSettings(
            autoStartApplicationIndex = autoStartApplicationIndex,
            autoStartApplicationInterval = autoStartApplicationInterval
        ).onSuccess {
            val current = stateMgr.mainState.value
            val base = when (current) {
                is MainState.Success -> current.mainData
                is MainState.Error -> current.mainData
                is MainState.Init -> current.mainData
            }
            val updated = base.copy(
                autoStartApplicationIndex = autoStartApplicationIndex,
                autoStartApplicationInterval = autoStartApplicationInterval
            )
            stateMgr.updateMainState(MainState.Success(updated))
            return Ok(Unit)
        }.onFailure {
            return Err(it)
        }
    }
}
