package com.jvckenwood.cabmee.homeapp.domain.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
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
    suspend operator fun invoke(): Result<Unit, String> {
        Timber.d("InitializeUseCase invoked")
        repository.loadMainData()
            .onSuccess { mainEntity ->
                stateMgr.updateMainState(MainState.Success(mainEntity))
                return Ok(Unit)
            }
            .onFailure {
                return Err(it)
            }
        return Err("Unknown Error!")
    }
}
