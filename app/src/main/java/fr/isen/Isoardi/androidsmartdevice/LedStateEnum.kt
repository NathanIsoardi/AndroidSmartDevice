package fr.isen.Isoardi.androidsmartdevice

enum class LedStateEnum(val hex: ByteArray) {
    Led_1(byteArrayOf(0x01)),
    Led_2(byteArrayOf(0x02)),
    Led_3(byteArrayOf(0x03)),
    NONE(byteArrayOf(0x00))
}
