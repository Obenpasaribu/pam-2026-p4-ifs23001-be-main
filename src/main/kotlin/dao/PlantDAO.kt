package org.delcom.dao

import org.delcom.tables.PlantTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID


class PlantDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, PlantDAO>(PlantTable)

    var nama by PlantTable.nama
    var pathGambar by PlantTable.pathGambar
    var deskripsi by PlantTable.deskripsi
    var manfaat by PlantTable.manfaat
    var efekSamping by PlantTable.efekSamping
    var createdAt by PlantTable.createdAt
    var updatedAt by PlantTable.updatedAt
}