package com.jvckenwood.cabmee.homeapp.data.repository.di

import com.jvckenwood.cabmee.homeapp.data.repository.MainRepository
import com.jvckenwood.cabmee.homeapp.domain.interfaces.MainRepositoryInterface
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindRepositoryInterface(
        mainRepositoryImpl: MainRepository
    ): MainRepositoryInterface
}
