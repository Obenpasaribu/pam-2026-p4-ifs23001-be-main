package org.delcom.tables

import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp

object PlantTable : UUIDTable("plants") {
    val nama = varchar("nama", 100)
    val pathGambar = varchar("path_gambar", 255)
    val deskripsi = text("deskripsi")
    val manfaat = text("harga")
    val efekSamping = text("pengaruh")
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
}