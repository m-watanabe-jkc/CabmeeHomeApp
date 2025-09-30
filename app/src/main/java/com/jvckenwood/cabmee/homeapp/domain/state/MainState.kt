package com.jvckenwood.cabmee.homeapp.domain.state

import com.jvckenwood.cabmee.homeapp.domain.entity.MainEntity

sealed class MainState {
    data class Init(val mainData: MainEntity = MainEntity()) : MainState()
    data class Success(val mainData: MainEntity = MainEntity()) : MainState()
    data class Error(val mainData: MainEntity = MainEntity()) : MainState()
}
