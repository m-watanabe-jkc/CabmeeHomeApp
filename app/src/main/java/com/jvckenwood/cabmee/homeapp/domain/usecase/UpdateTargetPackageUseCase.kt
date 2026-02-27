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
class UpdateTargetPackageUseCase @Inject constructor(
    private val repository: MainRepositoryInterface,
    private val stateMgr: StateManager
) {
    suspend operator fun invoke(targetPackageList: List<String>): Result<Unit, String> {
        return repository.saveTargetPackageList(targetPackageList)
            .onSuccess {
                val current = stateMgr.mainState.value
                val base = when (current) {
                    is MainState.Success -> current.mainData
                    is MainState.Error -> current.mainData
                    is MainState.Init -> current.mainData
                }

                val currentAutoStartIndex = base.autoStartApplicationIndex
                val isCurrentAutoStartStillValid =
                    currentAutoStartIndex in targetPackageList.indices &&
                        targetPackageList[currentAutoStartIndex].isNotBlank()

                val nextAutoStartIndex = if (isCurrentAutoStartStillValid) {
                    currentAutoStartIndex
                } else {
                    -1
                }

                if (!isCurrentAutoStartStillValid && currentAutoStartIndex >= 0) {
                    repository.saveAutoStartSettings(
                        autoStartApplicationIndex = -1,
                        autoStartApplicationInterval = base.autoStartApplicationInterval
                    )
                }

                val updated = base.copy(targetPackageList = targetPackageList)
                    .copy(autoStartApplicationIndex = nextAutoStartIndex)

                stateMgr.updateMainState(MainState.Success(updated))
                return Ok(Unit)
            }
            .onFailure {
                return Err(it)
            }
    }
}
