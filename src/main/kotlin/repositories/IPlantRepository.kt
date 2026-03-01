package org.delcom.repositories

import org.delcom.entities.Plant

interface  IPlantRepository {
    suspend fun getPlantspc(search: String): List<Plant>
    suspend fun getPlantByIdpc(id: String): Plant?
    suspend fun getPlantByNamepc(name: String): Plant?
    suspend fun addPlantpc(plant: Plant) : String
    suspend fun updatePlantpc(id: String, newPlant: Plant): Boolean
    suspend fun removePlantpc(id: String): Boolean
}