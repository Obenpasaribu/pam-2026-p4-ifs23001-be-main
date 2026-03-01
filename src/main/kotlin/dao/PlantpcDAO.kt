package org.delcom.dao

import org.delcom.tables.PlantpcTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import java.util.UUID


class PlantpcDAO(id: EntityID<UUID>) : Entity<UUID>(id) {
    companion object : EntityClass<UUID, PlantpcDAO>(PlantpcTable)

    var nama by PlantpcTable.nama
    var pathGambar by PlantpcTable.pathGambar
    var deskripsi by PlantpcTable.deskripsi
    var harga by PlantpcTable.harga
    var pengaruh by PlantpcTable.pengaruh
    var createdAt by PlantpcTable.createdAt
    var updatedAt by PlantpcTable.updatedAt
}