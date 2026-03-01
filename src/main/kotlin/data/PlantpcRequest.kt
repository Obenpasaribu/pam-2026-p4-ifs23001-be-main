package org.delcom.data

import kotlinx.serialization.Serializable
import org.delcom.entities.Plantpc

@Serializable
data class PlantpcRequest(
    var nama: String = "",
    var deskripsi: String = "",
    var harga: String = "",
    var pengaruh: String = "",
    var pathGambar: String = "",
){
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "nama" to nama,
            "deskripsi" to deskripsi,
            "harga" to harga,
            "pengaruh" to pengaruh,
            "pathGambar" to pathGambar
        )
    }

    fun toEntitypc(): Plantpc {
        return Plantpc(
            nama = nama,
            deskripsi = deskripsi,
            harga = harga,
            pengaruh = pengaruh,
            pathGambar =  pathGambar,
        )
    }



}