package com.jvckenwood.cabmee.homeapp.data.localdata

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import com.jvckenwood.cabmee.homeapp.MainData
import java.io.InputStream
import java.io.OutputStream

object MainDataSerializer : Serializer<MainData> {
    override val defaultValue: MainData = MainData.getDefaultInstance()

    // データ読み取り
    override suspend fun readFrom(input: InputStream): MainData {
        try {
            return MainData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    // データ書き込み
    override suspend fun writeTo(t: MainData, output: OutputStream) = t.writeTo(output)
}

val Context.mainDataStore: DataStore<MainData> by dataStore(
    fileName = "main_data.pb",
    serializer = MainDataSerializer
)
