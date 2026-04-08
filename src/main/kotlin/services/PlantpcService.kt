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

    // Ambil data request dengan penanganan streaming yang efisien dan error handling file
    private suspend fun getPlantRequestpc(call: ApplicationCall): PlantpcRequest {
        val plantReq = PlantpcRequest()
        val multipartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 10) // Naikkan limit ke 10MB jika perlu

        try {
            multipartData.forEachPart { part ->
                when (part) {
                    is PartData.FormItem -> {
                        when (part.name) {
                            "nama" -> plantReq.nama = part.value.trim()
                            "deskripsi" -> plantReq.deskripsi = part.value
                            "harga" -> plantReq.harga = part.value
                            "pengaruh" -> plantReq.pengaruh = part.value
                        }
                    }

                    is PartData.FileItem -> {
                        // Hanya proses jika file ada namanya (tidak kosong)
                        if (part.originalFileName?.isNotEmpty() == true) {
                            val ext = part.originalFileName
                                ?.substringAfterLast('.', "")
                                ?.let { if (it.isNotEmpty()) ".$it" else "" }
                                ?: ""

                            val fileName = UUID.randomUUID().toString() + ext
                            val filePath = "uploads/plants/$fileName"

                            val file = File(filePath)
                            file.parentFile.mkdirs()

                            try {
                                // Point 1: Streaming Efisien
                                part.provider().copyAndClose(file.writeChannel())
                                plantReq.pathGambar = filePath
                            } catch (e: Exception) {
                                // Point 4: Hapus file jika gagal di tengah jalan (koneksi terputus)
                                if (file.exists()) file.delete()
                                throw e
                            }
                        }
                    }
                    else -> part.dispose()
                }
                part.dispose()
            }
        } catch (e: Exception) {
            // Hapus file yang mungkin baru saja terbuat jika terjadi error selama proses multipart
            if (plantReq.pathGambar.isNotEmpty()) {
                val file = File(plantReq.pathGambar)
                if (file.exists()) file.delete()
            }
            throw e
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

        // Validasi fisik file
        val file = File(plantReq.pathGambar)
        if (!file.exists() || file.length() == 0L) {
            throw AppException(400, "Gambar komponen gagal diupload atau kosong!")
        }
    }

    // Menambahkan data komponen
    suspend fun createPlantpc(call: ApplicationCall) {
        val plantpcReq = getPlantRequestpc(call)

        validatePlantRequestpc(plantpcReq)

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

        // Point 3: Response Cepat
        call.respond(DataResponse(
            "success",
            "Berhasil menambahkan data komponen",
            mapOf(Pair("plantId", plantId))
        ))
    }

    // Mengubah data komponen
    suspend fun updatePlantpc(call: ApplicationCall) {
        val id = call.parameters["id"]
            ?: throw AppException(400, "ID komponen tidak boleh kosong!")

        val oldPlant = plantpcRepository.getPlantByIdpc(id) ?: throw AppException(404, "Data komponen tidak tersedia!")

        val plantpcReq = getPlantRequestpc(call)

        // Point 2: Pertahankan path lama jika tidak ada upload baru
        val isNewImageUploaded = plantpcReq.pathGambar.isNotEmpty()
        if(!isNewImageUploaded){
            plantpcReq.pathGambar = oldPlant.pathGambar
        }

        validatePlantRequestpc(plantpcReq)

        if(plantpcReq.nama != oldPlant.nama){
            val existPlant = plantpcRepository.getPlantByNamepc(plantpcReq.nama)
            if(existPlant != null){
                // Hapus file baru jika validasi nama gagal
                if(isNewImageUploaded){
                    val tmpFile = File(plantpcReq.pathGambar)
                    if(tmpFile.exists()) tmpFile.delete()
                }
                throw AppException(409, "komponen dengan nama ini sudah terdaftar!")
            }
        }

        // Hapus gambar lama HANYA jika ada gambar baru yang masuk
        if(isNewImageUploaded && plantpcReq.pathGambar != oldPlant.pathGambar){
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

        // Point 3: Response Cepat
        call.respond(DataResponse(
            "success",
            "Berhasil mengubah data komponen",
            null
        ))
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

        if (oldFile.exists()) {
            oldFile.delete()
        }

        call.respond(DataResponse(
            "success",
            "Berhasil menghapus data komponen",
            null
        ))
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
