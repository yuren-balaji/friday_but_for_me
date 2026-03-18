package com.example.first_app_0_0_1.data

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

class MediaManager(private val context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    fun savePrivateFile(fileName: String, inputStream: InputStream): File {
        val file = File(context.filesDir, "private/$fileName")
        file.parentFile?.mkdirs()
        
        val encryptedFile = EncryptedFile.Builder(
            file,
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()

        encryptedFile.openFileOutput().use { output ->
            inputStream.copyTo(output)
        }
        return file
    }

    fun getPrivateFile(fileName: String): InputStream {
        val file = File(context.filesDir, "private/$fileName")
        val encryptedFile = EncryptedFile.Builder(
            file,
            context,
            masterKeyAlias,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        return encryptedFile.openFileInput()
    }

    fun savePublicFile(fileName: String, inputStream: InputStream): File {
        val file = File(context.getExternalFilesDir(null), "public/$fileName")
        file.parentFile?.mkdirs()
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        // Save compressed backup to internal
        saveCompressedBackup(file)
        return file
    }

    private fun saveCompressedBackup(publicFile: File) {
        val backupFile = File(context.filesDir, "backups/${publicFile.name}.gz")
        backupFile.parentFile?.mkdirs()
        publicFile.inputStream().use { input ->
            java.util.zip.GZIPOutputStream(backupFile.outputStream()).use { output ->
                input.copyTo(output)
            }
        }
    }
}
