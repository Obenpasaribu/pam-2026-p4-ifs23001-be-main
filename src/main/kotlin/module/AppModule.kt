package org.delcom.module

import org.delcom.repositories.IPlantRepository
import org.delcom.repositories.PlantRepository
import org.delcom.services.PlantService
import org.delcom.services.ProfileService
import org.koin.dsl.module


val appModule = module {
    // Plant Repository
    single<IPlantRepository> {
        PlantRepository()
    }

    // Plant Service
    single {
        PlantService(get())
    }

    // Profile Service
    single {
        ProfileService()
    }
}