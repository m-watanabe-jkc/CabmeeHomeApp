package com.jvckenwood.cabmee.homeapp.domain.entity

data class MainEntity(
    var counter: Long = 0,
    var autoStartApplicationIndex: Int = -1,
    var autoStartApplicationInterval: Int = 30,
    var targetPackageList: List<String> = emptyList()
)
