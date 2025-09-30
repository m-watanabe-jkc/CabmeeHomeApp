package com.jvckenwood.cabmee.homeapp.domain.entity

import com.jvckenwood.cabmee.homeapp.domain.state.MainState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StateManager @Inject constructor() {
    val mainState: StateFlow<MainState>
        get() = _mainState
    suspend fun updateMainState(newState: MainState) = _mainState.emit(newState)
    private val _mainState = MutableStateFlow(MainState.Init() as MainState)
}
