package com.gdgnantes.devfest.android.core

interface SuspendUseCase<T : Any, in Params> {
    suspend operator fun invoke(params: Params? = null): Result<T>
}