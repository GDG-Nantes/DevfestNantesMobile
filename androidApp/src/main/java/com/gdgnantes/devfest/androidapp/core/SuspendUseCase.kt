package com.gdgnantes.devfest.androidapp.core

interface SuspendUseCase<T : Any, in Params> {
    suspend operator fun invoke(params: Params? = null): Result<T>
}