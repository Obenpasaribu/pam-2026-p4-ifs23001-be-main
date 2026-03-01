package org.delcom.repositories

import org.delcom.entities.Plantpc

interface  IPlantpcRepository {
    suspend fun getPlantspc(search: String): List<Plantpc>
    suspend fun getPlantByIdpc(id: String): Plantpc?
    suspend fun getPlantByNamepc(name: String): Plantpc?
    suspend fun addPlantpc(plant: Plantpc) : String
    suspend fun updatePlantpc(id: String, newPlant: Plantpc): Boolean
    suspend fun removePlantpc(id: String): Boolean
}