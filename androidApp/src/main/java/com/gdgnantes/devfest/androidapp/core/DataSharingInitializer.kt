package com.gdgnantes.devfest.androidapp.core

import com.gdgnantes.devfest.feature.settings.services.DataCollectionSettingsService

class DataSharingInitializer(
    private val dataCollectionSettingsService: DataCollectionSettingsService
) : ApplicationInitializer {
    override suspend operator fun invoke(params: Unit?): Result<Unit> {
        dataCollectionSettingsService.updatesDataServicesActivationStatus()
        return Result.success(Unit)
    }
}
