package org.delcom.services

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.cio.*
import io.ktor.utils.io.*
import org.delcom.data.AppException
import org.delcom.data.DataResponse
import org.delcom.data.PlantpcRequest
import org.delcom.helpers.ValidatorHelper
import org.delcom.repositories.IPlantpcRepository
import java.io.File
import java.util.*

class PlantpcService(private val plantpcRepository: IPlantpcRepository) {
    // Mengambil semua data komponen
    suspend fun getAllPlantspc(call: ApplicationCall) {
        val search = call.request.queryParameters["search"] ?: ""

        val plants = plantpcRepository.getPlantspc(search)

        val response = DataResponse(
            "success",
            "Berhasil mengambil daftar komponen",
            mapOf(Pair("plants", plants))
        )
        call.respond(response)
    }

    // Mengambil data komponen berdasarkan id
    suspend fun getPlantByIdpc(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID komponen tidak boleh kosong!")

        val plant = plantpcRepository.getPlantByIdpc(id) ?: throw AppException(404, "Data komponen tidak tersedia!")

        val response = DataResponse(
            "success",
            "Berhasil mengambil data komponen",
            mapOf(Pair("plant", plant))
        )
        call.respond(response)
    }

    // Ambil data request
    private suspend fun getPlantRequestpc(call: ApplicationCall): PlantpcRequest {
        // Buat object penampung
        val plantReq = PlantpcRequest()

        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 5)
        multipartData.forEachPart { part ->
            when (part) {
                // Ambil request berupa teks
                is PartData.FormItem -> {
                    when (part.name) {
                        "nama" -> plantReq.nama = part.value.trim()
                        "deskripsi" -> plantReq.deskripsi = part.value
                        "harga" -> plantReq.harga = part.value
                        "pengaruh" -> plantReq.pengaruh = part.value
                    }
                }

                // Upload file
                is PartData.FileItem -> {
                    val ext = part.originalFileName
                        ?.substringAfterLast('.', "")
                        ?.let { if (it.isNotEmpty()) ".$it" else "" }
                        ?: ""

                    val fileName = UUID.randomUUID().toString() + ext
                    val filePath = "uploads/plants/$fileName"

                    val file = File(filePath)
                    file.parentFile.mkdirs() // pastikan folder ada

                    part.provider().copyAndClose(file.writeChannel())
                    plantReq.pathGambar = filePath
                }

                else -> {}
            }

            part.dispose()
        }

        return plantReq
    }

    // Validasi request data dari pengguna
    private fun validatePlantRequestpc(plantReq: PlantpcRequest){
        val validatorHelper = ValidatorHelper(plantReq.toMap())
        validatorHelper.required("nama", "Nama tidak boleh kosong")
        validatorHelper.required("deskripsi", "Deskripsi tidak boleh kosong")
        validatorHelper.required("harga", "harga tidak boleh kosong")
        validatorHelper.required("pengaruh", "pengaruh tidak boleh kosong")
        validatorHelper.required("pathGambar", "Gambar tidak boleh kosong")
        validatorHelper.validate()

        val file = File(plantReq.pathGambar)
        if (!file.exists()) {
            throw AppException(400, "Gambar komponen gagal diupload!")
        }

    }

    // Menambahkan data komponen
    suspend fun createPlantpc(call: ApplicationCall) {
        // Ambil data request
        val plantpcReq = getPlantRequestpc(call)

        // Validasi request
        validatePlantRequestpc(plantpcReq)

        // periksa plant dengan nama yang sama
        val existPlant = plantpcRepository.getPlantByNamepc(plantpcReq.nama)
        if(existPlant != null){
            val tmpFile = File(plantpcReq.pathGambar)
            if(tmpFile.exists()){
                tmpFile.delete()
            }
            throw AppException(409, "komponen dengan nama ini sudah terdaftar!")
        }

        val plantId = plantpcRepository.addPlantpc(
            plantpcReq.toEntitypc()
        )

        val response = DataResponse(
            "success",
            "Berhasil menambahkan data komponen",
            mapOf(Pair("plantId", plantId))
        )
        call.respond(response)
    }

    // Mengubah data komponen
    suspend fun updatePlantpc(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID komponen tidak boleh kosong!")

        val oldPlant = plantpcRepository.getPlantByIdpc(id) ?: throw AppException(404, "Data komponen tidak tersedia!")

        // Ambil data request
        val plantpcReq = getPlantRequestpc(call)

        if(plantpcReq.pathGambar.isEmpty()){
            plantpcReq.pathGambar = oldPlant.pathGambar
        }

        // Validasi request
        validatePlantRequestpc(plantpcReq)

        // periksa plant dengan nama yang sama jika nama diubah
        if(plantpcReq.nama != oldPlant.nama){
            val existPlant = plantpcRepository.getPlantByNamepc(plantpcReq.nama)
            if(existPlant != null){
                val tmpFile = File(plantpcReq.pathGambar)
                if(tmpFile.exists()){
                    tmpFile.delete()
                }
                throw AppException(409, "komponen dengan nama ini sudah terdaftar!")
            }
        }

        // Hapus gambar lama jika mengupload file baru
        if(plantpcReq.pathGambar != oldPlant.pathGambar){
            val oldFile = File(oldPlant.pathGambar)
            if(oldFile.exists()){
                oldFile.delete()
            }
        }

        val isUpdated = plantpcRepository.updatePlantpc(
            id, plantpcReq.toEntitypc()
        )
        if (!isUpdated) {
            throw AppException(400, "Gagal memperbarui data komponen!")
        }

        val response = DataResponse(
            "success",
            "Berhasil mengubah data komponen",
            null
        )
        call.respond(response)
    }

    // Menghapus data komponen
    suspend fun deletePlantpc(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID komponen tidak boleh kosong!")

        val oldPlant = plantpcRepository.getPlantByIdpc(id) ?: throw AppException(404, "Data komponen tidak tersedia!")

        val oldFile = File(oldPlant.pathGambar)

        val isDeleted = plantpcRepository.removePlantpc(id)
        if (!isDeleted) {
            throw AppException(400, "Gagal menghapus data komponen!")
        }

        // Hapus data gambar jika data komponen sudah dihapus
        if (oldFile.exists()) {
            oldFile.delete()
        }

        val response = DataResponse(
            "success",
            "Berhasil menghapus data komponen",
            null
        )
        call.respond(response)
    }

    // Mengambil gambar komponen
    suspend fun getPlantImagepc(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: return call.respond(HttpStatusCode.BadRequest)

        val plant = plantpcRepository.getPlantByIdpc(id)
            ?: return call.respond(HttpStatusCode.NotFound)

        val file = File(plant.pathGambar)

        if (!file.exists()) {
            return call.respond(HttpStatusCode.NotFound)
        }

        call.respondFile(file)
    }
}