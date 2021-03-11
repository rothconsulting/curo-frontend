package ch.umb.curo.starter.util

import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipUtil {

    fun zipFiles(file: List<Pair<String, ByteArray>>): ByteArray {
        val outputStream = ByteArrayOutputStream()
        val zos = ZipOutputStream(outputStream)

        file.forEach { data ->
            val zipEntry = ZipEntry(data.first)
            zos.putNextEntry(zipEntry)
            zos.write(data.second)
            zos.closeEntry();
        }
        zos.close();

        return outputStream.toByteArray()
    }


}
