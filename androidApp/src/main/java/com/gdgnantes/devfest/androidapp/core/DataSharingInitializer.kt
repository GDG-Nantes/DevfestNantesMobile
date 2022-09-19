package com.gdgnantes.devfest.androidapp.core

import com.gdgnantes.devfest.androidapp.services.DataSharingSettingsService
import javax.inject.Inject

class DataSharingInitializer @Inject constructor(
    private val dataSharingSettingsService: DataSharingSettingsService
) : ApplicationInitializer {

    override suspend operator fun invoke(params: Unit?): Result<Unit> {
        dataSharingSettingsService.updatesDataServicesActivationStatus()
        return Result.success(Unit)
    }
}
