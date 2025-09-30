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
    suspend operator fun invoke(): Result<Unit, String> {
        Timber.d("InitializeUseCase invoked")
        repository.loadCounter()
            .onSuccess { counter ->
                stateMgr.updateMainState(
                    MainState.Success(
                        MainEntity(
                            counter = counter
                        )
                    )
                )
                return Ok(Unit)
            }
            .onFailure { it ->
                return Err(it)
            }
        return Err("Unknown Error!")
    }
}
