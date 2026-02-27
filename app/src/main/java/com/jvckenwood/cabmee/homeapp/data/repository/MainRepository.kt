package com.jvckenwood.cabmee.homeapp.data.repository

import android.content.Context
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.jvckenwood.cabmee.homeapp.data.localdata.mainDataStore
import com.jvckenwood.cabmee.homeapp.domain.entity.MainEntity
import com.jvckenwood.cabmee.homeapp.domain.interfaces.MainRepositoryInterface
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepository @Inject constructor(
    @ApplicationContext val context: Context
) : MainRepositoryInterface {
    override suspend fun saveCounter(counter: Long) {
        context.mainDataStore.updateData { currentData ->
            currentData.toBuilder()
                .setCounter(counter)
                .build()
        }
    }

    override suspend fun loadMainData(): Result<MainEntity, String> {
        val data = context.mainDataStore.data.first()
        return Ok(
            MainEntity(
                counter = data.counter,
                autoStartApplicationIndex = data.autoStartApplicationIndex,
                autoStartApplicationInterval = data.autoStartApplicationInterval
            )
        )
    }

    override suspend fun saveAutoStartSettings(
        autoStartApplicationIndex: Int,
        autoStartApplicationInterval: Int
    ): Result<Unit, String> {
        context.mainDataStore.updateData { currentData ->
            currentData.toBuilder()
                .setAutoStartApplicationIndex(autoStartApplicationIndex)
                .setAutoStartApplicationInterval(autoStartApplicationInterval)
                .build()
        }
        return Ok(Unit)
    }
}
