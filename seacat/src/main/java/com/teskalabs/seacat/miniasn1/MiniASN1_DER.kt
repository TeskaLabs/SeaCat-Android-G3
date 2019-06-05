package com.teskalabs.seacat.miniasn1

import java.text.SimpleDateFormat
import java.util.*
import kotlin.experimental.or
import kotlin.experimental.and

private fun intToBytes(value: Int): ByteArray {
    val d:MutableList<Byte> = mutableListOf<Byte>()
    var x = value

    while (x > 0) {
        d.add(0, x.and(0xff).toByte())
        x = x.shr(8)
    }

    return ByteArray(d.size) { d[it] }
}

private fun il(tag: Byte, length: Int):ByteArray {
    if (length < 128) {
        return byteArrayOf(tag, length.toByte())
    } else {
        val d = intToBytes(length)
        return byteArrayOf(tag, d.size.toByte().or(0x80.toByte())) + d
    }
}

class MiniASN1DER {

    companion object {


        fun SEQUENCE(elements: Array<ByteArray?>, implicit_tagging: Boolean = true, tag: Byte = 0x30): ByteArray {

            val d: MutableList<Byte> = mutableListOf<Byte>()
            var n: Byte = 0

            for (e in elements) {
                if (e == null) {
                    n = n.inc()
                    continue
                }

                var identifier: Byte

                if (implicit_tagging) {
                    val e0 = e[0]

                    if (((e0.and(0x1F)) == 0x10.toByte()) || ((e0.and(0x1F)) == 0x11.toByte()) || (e0 == 0xA0.toByte())) {
                        // the element is constructed
                        identifier = 0xA0.toByte()
                    } else {
                        // the element is primitive
                        identifier = 0x80.toByte()
                    }
                    identifier = identifier.plus(n).toByte()
                } else {
                    identifier = e[0]
                }

                d.add(identifier)
                e.copyOfRange(1, e.size).forEach { d.add(it) }

                n = n.inc()
            }

            return il(tag, d.size) + d
        }


        fun SEQUENCE_OF(elements: Array<ByteArray?>): ByteArray {
            return SEQUENCE(elements, false)
        }


        fun SET_OF(elements: Array<ByteArray?>): ByteArray {
            return SEQUENCE(elements, false, 0x31)
        }


        fun INTEGER(value: Int): ByteArray {
            if (value == 0) {
                return il(0x02, 1) + byteArrayOf(0x00)
            }

            val b = intToBytes(value)
            if (b[0].and(-128) == 0x80.toByte()) {
                return il(0x02, b.size+1) + byteArrayOf(0x00) + b
            } else {
                return il(0x02, b.size) + b
            }
        }


        fun OCTET_STRING(value: ByteArray): ByteArray {
            return il(0x04, value.size) + value
        }

        fun NULL(): ByteArray {
            return il(0x05, 0)
        }

        fun IA5String(value: String): ByteArray {
            val b = value.toByteArray(Charsets.US_ASCII)
            return il(0x16, b.size) + b
        }

        fun BIT_STRING(value: ByteArray): ByteArray {
            return il(0x03, value.size+1) + byteArrayOf(0x00) + value
        }

        fun UTF8String(value: String): ByteArray {
            val b = value.toByteArray(Charsets.UTF_8)
            return il(12, b.size) + b
        }

        fun PrintableString(value: String): ByteArray {
            val b = value.toByteArray(Charsets.US_ASCII)
            return il(19, b.size) + b
        }

        fun UTCTime(value: Date): ByteArray {
            val formatter = SimpleDateFormat("yyMMddHHmmss")
            val s = formatter.format(value) + "Z"
            val b = s.toByteArray(Charsets.US_ASCII)
            return il(23, b.size) + b
        }


        private fun variableLengthQuantity(value: Int): MutableList<Byte> {
            // Break it up in groups of 7 bits starting from the lowest significant bit
            // For all the other groups of 7 bits than lowest one, set the MSB to 1
            var v = value
            var m: Byte = 0x00
            val output: MutableList<Byte> = mutableListOf<Byte>()

            while (v >= 0x80) {
                output.add(0, v.and(0x7f).toByte().or(m))
                v = v.shr(7)
                m = 0x80.toByte()
            }
            output.add(0, v.toByte().or(m))

            return output
        }

        fun OBJECT_IDENTIFIER(value: String): ByteArray {
            val a = value.split(".").map { it.toInt() }
            var oid = byteArrayOf((a[0]*40 + a[1]).toByte()) // First two items are coded by a1*40+a2
            // A rest is Variable-length_quantity
            for (n in a.subList(2, a.size)){
                oid = oid + variableLengthQuantity(n)
            }
            return il(0x06, oid.size) + oid
        }
    }
}
