package com.teskalabs.seacat_utest


import com.teskalabs.seacat.miniasn1.MiniASN1
import org.junit.Test
import org.junit.Assert.*
import java.text.SimpleDateFormat
import java.util.*

class MiniASN1UnitTest {

    @Test
    fun miniasn1_sequence_01() {
        val asn1 = MiniASN1.DER.SEQUENCE(arrayOf(
                MiniASN1.DER.NULL(),
                MiniASN1.DER.NULL()
        ))

//        println("miniasn1_sequence_01")
//        asn1.forEach { print("%02X ".format(it)) }
//        println("")

        assertArrayEquals(asn1, byteArrayOf(0x30, 0x04, -128, 0x00, -128, 0x00))
    }


    @Test
    fun miniasn1_integer() {
        var asn1 = MiniASN1.DER.INTEGER(0)
        assertArrayEquals(byteArrayOf(0x02, 0x01, 0x00), asn1)

        asn1 = MiniASN1.DER.INTEGER(1)
        assertArrayEquals(byteArrayOf(0x02, 0x01, 0x01), asn1)

        asn1 = MiniASN1.DER.INTEGER(127)
        assertArrayEquals(byteArrayOf(0x02, 0x01, 0x7F), asn1)

        asn1 = MiniASN1.DER.INTEGER(128)
        assertArrayEquals(byteArrayOf(0x02, 0x02, 0x00, -128), asn1)

        asn1 = MiniASN1.DER.INTEGER(255)
        assertArrayEquals(byteArrayOf(0x02, 0x02, 0x00, -1), asn1)

        asn1 = MiniASN1.DER.INTEGER(256)
        assertArrayEquals(byteArrayOf(0x02, 0x02, 0x01, 0x00), asn1)

        asn1 = MiniASN1.DER.INTEGER(32767)
        assertArrayEquals(byteArrayOf(0x02, 0x02, 0x7F, -1), asn1)

        asn1 = MiniASN1.DER.INTEGER(65535)
        assertArrayEquals(byteArrayOf(0x02, 0x03, 0x00, -1, -1), asn1)
    }

    @Test
    fun miniasn1_null() {
        val asn1 = MiniASN1.DER.NULL()
        assertArrayEquals(byteArrayOf(0x05, 0x00), asn1)
    }

    @Test
    fun miniasn1_ia5string() {
        val asn1 = MiniASN1.DER.IA5String("Hello world")
        assertArrayEquals(byteArrayOf(0x16, 0x0B, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x77, 0x6F, 0x72, 0x6C, 0x64), asn1)
    }

    @Test
    fun miniasn1_printablestring() {
        val asn1 = MiniASN1.DER.PrintableString("Hello world")
        assertArrayEquals(byteArrayOf(19, 0x0B, 0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x77, 0x6F, 0x72, 0x6C, 0x64), asn1)
    }

    @Test
    fun miniasn1_octecstring() {
        val asn1 = MiniASN1.DER.OCTET_STRING(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05))
        assertArrayEquals(byteArrayOf(0x04, 0x05, 0x01, 0x02, 0x03, 0x04, 0x05), asn1)
    }

    @Test
    fun miniasn1_bitstring() {
        val asn1 = MiniASN1.DER.BIT_STRING(byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05))
        assertArrayEquals(byteArrayOf(0x03, 0x06, 0x00, 0x01, 0x02, 0x03, 0x04, 0x05), asn1)
    }

    @Test
    fun miniasn1_utctime() {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val d = simpleDateFormat.parse("2011-12-31 23:59:59")
        val asn1 = MiniASN1.DER.UTCTime(d)
        assertArrayEquals(byteArrayOf(0x17, 0x0D, 0x31, 0x31, 0x31, 0x32, 0x33, 0x31, 0x32, 0x33, 0x35, 0x39, 0x35, 0x39, 0x5A), asn1)
    }

    @Test
    fun miniasn1_object_identifier() {
        val asn1 = MiniASN1.DER.OBJECT_IDENTIFIER("1.2.840.10045.2.1")
        assertArrayEquals(byteArrayOf(0x06, 0x07, 0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 0x02, 0x01), asn1)
    }

    @Test
    fun miniasn1_sequence() {

        val arr = ByteArray(400) { i -> 2 }
        val asn1 = MiniASN1.DER.OCTET_STRING(arr)

//        println("miniasn1_sequence")
//        asn1.forEach { print("0x%02X, ".format(it)) }
//        println("")

        assertArrayEquals(asn1, byteArrayOf(
            0x04, 0x82.toByte(), 0x01, 0x90.toByte(), 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
            0x02
        ))
    }


}
