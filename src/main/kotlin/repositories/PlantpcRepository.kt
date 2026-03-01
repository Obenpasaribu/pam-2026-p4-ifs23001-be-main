package org.delcom.repositories

import org.delcom.dao.PlantpcDAO
import org.delcom.entities.Plantpc
import org.delcom.helpers.daoToModelpc
import org.delcom.helpers.suspendTransaction
import org.delcom.tables.PlantpcTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.lowerCase
import java.util.UUID

class PlantpcRepository : IPlantpcRepository {
    override suspend fun getPlantspc(search: String): List<Plantpc> = suspendTransaction {
        if (search.isBlank()) {
            PlantpcDAO.all()
                .orderBy(PlantpcTable.createdAt to SortOrder.DESC)
                .limit(20)
                .map(::daoToModelpc)
        } else {
            val keyword = "%${search.lowercase()}%"

            PlantpcDAO
                .find {
                    PlantpcTable.nama.lowerCase() like keyword
                }
                .orderBy(PlantpcTable.nama to SortOrder.ASC)
                .limit(20)
                .map(::daoToModelpc)
        }
    }

    override suspend fun getPlantByIdpc(id: String): Plantpc? = suspendTransaction {
        PlantpcDAO
            .find { (PlantpcTable.id eq UUID.fromString(id)) }
            .limit(1)
            .map(::daoToModelpc)
            .firstOrNull()
    }

    override suspend fun getPlantByNamepc(name: String): Plantpc? = suspendTransaction {
        PlantpcDAO
            .find { (PlantpcTable.nama eq name) }
            .limit(1)
            .map(::daoToModelpc)
            .firstOrNull()
    }

    override suspend fun addPlantpc(plant: Plantpc): String = suspendTransaction {
        val plantpcDAO = PlantpcDAO.new {
            nama = plant.nama
            pathGambar = plant.pathGambar
            deskripsi = plant.deskripsi
            harga = plant.harga
            pengaruh = plant.pengaruh
            createdAt = plant.createdAt
            updatedAt = plant.updatedAt
        }

        plantpcDAO.id.value.toString()
    }

    override suspend fun updatePlantpc(id: String, newPlant: Plantpc): Boolean = suspendTransaction {
        val plantpcDAO = PlantpcDAO
            .find { PlantpcTable.id eq UUID.fromString(id) }
            .limit(1)
            .firstOrNull()

        if (plantpcDAO != null) {
            plantpcDAO.nama = newPlant.nama
            plantpcDAO.pathGambar = newPlant.pathGambar
            plantpcDAO.deskripsi = newPlant.deskripsi
            plantpcDAO.harga = newPlant.harga
            plantpcDAO.pengaruh = newPlant.pengaruh
            plantpcDAO.updatedAt = newPlant.updatedAt
            true
        } else {
            false
        }
    }

    override suspend fun removePlantpc(id: String): Boolean = suspendTransaction {
        val rowsDeleted = PlantpcTable.deleteWhere {
            PlantpcTable.id eq UUID.fromString(id)
        }
        rowsDeleted == 1
    }

}