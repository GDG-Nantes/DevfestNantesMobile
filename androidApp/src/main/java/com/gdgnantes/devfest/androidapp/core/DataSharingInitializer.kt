package com.gdgnantes.devfest.androidapp.core

import com.gdgnantes.devfest.androidapp.services.DataCollectionSettingsService
import javax.inject.Inject

class DataSharingInitializer @Inject constructor(
    private val dataCollectionSettingsService: DataCollectionSettingsService
) : ApplicationInitializer {
    override suspend operator fun invoke(params: Unit?): Result<Unit> {
        dataCollectionSettingsService.updatesDataServicesActivationStatus()
        return Result.success(Unit)
    }
}
