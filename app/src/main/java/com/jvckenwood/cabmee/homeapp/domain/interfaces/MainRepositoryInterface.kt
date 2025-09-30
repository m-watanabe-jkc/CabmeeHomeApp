package com.jvckenwood.cabmee.homeapp.domain.interfaces

import com.github.michaelbull.result.Result

interface MainRepositoryInterface {
    suspend fun loadCounter(): Result<Long, String>
    suspend fun saveCounter(counter: Long)
}
