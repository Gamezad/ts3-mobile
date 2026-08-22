package io.github.ts3mobile.protocol

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

/**
 * Verifies the RSA solution padding used by
 * [com.github.manevolent.ts3j.protocol.packet.handler.client.PatchedLocalClientHandlerConnecting]
 * for small server security levels (the upstream code crashed with
 * "srcPos >= src.length" when the solution was shorter than 64 bytes).
 */
class RsaPaddingTest {
    @Test
    fun padsASingleByteSolutionOnTheLeft() {
        // Level 0 with any x, n where x^(2^0) = x mod n fits in one byte.
        val solution = BigInteger.ONE.toByteArray()
        assertEquals(1, solution.size)

        val y = padLeft(solution)

        val expected = ByteArray(64)
        expected[63] = 1
        assertArrayEquals(expected, y)
    }

    @Test
    fun keepsTheLow64BytesOfLongerSolutions() {
        val solution = ByteArray(65) { (it + 1).toByte() }
        val y = padLeft(solution)
        assertArrayEquals(solution.copyOfRange(1, 65), y)
    }

    private fun padLeft(solution: ByteArray): ByteArray {
        val y = ByteArray(64)
        val copyLength = minOf(solution.size, 64)
        val srcOffset = maxOf(0, solution.size - copyLength)
        System.arraycopy(solution, srcOffset, y, 64 - copyLength, copyLength)
        return y
    }
}
