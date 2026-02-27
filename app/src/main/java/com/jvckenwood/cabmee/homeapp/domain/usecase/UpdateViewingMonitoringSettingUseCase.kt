package com.jvckenwood.cabmee.homeapp.domain.usecase

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.jvckenwood.cabmee.homeapp.domain.entity.StateManager
import com.jvckenwood.cabmee.homeapp.domain.interfaces.MainRepositoryInterface
import com.jvckenwood.cabmee.homeapp.domain.state.MainState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateViewingMonitoringSettingUseCase @Inject constructor(
    private val repository: MainRepositoryInterface,
    private val stateMgr: StateManager
) {
    suspend operator fun invoke(
        viewingRestrictionsList: List<String>,
        viewingMonitoringMode: Boolean
    ): Result<Unit, String> {
        return repository.saveViewingMonitoringSettings(
            viewingRestrictionsList = viewingRestrictionsList,
            viewingMonitoringMode = viewingMonitoringMode
        ).onSuccess {
            val current = stateMgr.mainState.value
            val base = when (current) {
                is MainState.Success -> current.mainData
                is MainState.Error -> current.mainData
                is MainState.Init -> current.mainData
            }
            val updated = base.copy(
                viewingRestrictionsList = viewingRestrictionsList,
                viewingMonitoringMode = viewingMonitoringMode
            )
            stateMgr.updateMainState(MainState.Success(updated))
            return Ok(Unit)
        }.onFailure {
            return Err(it)
        }
    }
}
