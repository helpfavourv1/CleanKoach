package com.zdmgold.cleankoach.core.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun `bytes returns zero for zero and negatives`() {
        assertThat(FormatUtils.bytes(0)).isEqualTo("0 B")
        assertThat(FormatUtils.bytes(-5)).isEqualTo("0 B")
    }

    @Test
    fun `bytes formats sub-kilobyte values as raw bytes`() {
        assertThat(FormatUtils.bytes(512)).isEqualTo("512 B")
    }

    @Test
    fun `bytes formats kilobytes with two decimals`() {
        assertThat(FormatUtils.bytes(1536)).isEqualTo("1.50 KB")
    }

    @Test
    fun `bytes formats megabytes with two decimals`() {
        assertThat(FormatUtils.bytes(1024L * 1024L)).isEqualTo("1.00 MB")
    }

    @Test
    fun `bytes formats gigabytes with two decimals`() {
        assertThat(FormatUtils.bytes(1024L * 1024L * 1024L)).isEqualTo("1.00 GB")
    }

    @Test
    fun `bytesShort omits decimals for values of 100 or more`() {
        val hundredMb = 100L * 1024L * 1024L
        assertThat(FormatUtils.bytesShort(hundredMb)).isEqualTo("100 MB")
    }

    @Test
    fun `bytesShort keeps one decimal for values below 100`() {
        val fiftyMb = 50L * 1024L * 1024L
        assertThat(FormatUtils.bytesShort(fiftyMb)).isEqualTo("50.0 MB")
    }

    @Test
    fun `bytesShort returns zero B for zero`() {
        assertThat(FormatUtils.bytesShort(0)).isEqualTo("0 B")
    }

    @Test
    fun `count adds thousands separators`() {
        assertThat(FormatUtils.count(0)).isEqualTo("0")
        assertThat(FormatUtils.count(999)).isEqualTo("999")
        assertThat(FormatUtils.count(1234)).isEqualTo("1,234")
        assertThat(FormatUtils.count(1234567)).isEqualTo("1,234,567")
    }

    @Test
    fun `mbps formats with two decimals and Mbps unit`() {
        assertThat(FormatUtils.mbps(0.0)).isEqualTo("0.00 Mbps")
        assertThat(FormatUtils.mbps(12.345)).isEqualTo("12.35 Mbps")
        assertThat(FormatUtils.mbps(100.0)).isEqualTo("100.00 Mbps")
    }

    @Test
    fun `isMeaningfulSize is true only above one kilobyte`() {
        assertThat(FormatUtils.isMeaningfulSize(500L)).isFalse()
        assertThat(FormatUtils.isMeaningfulSize(1025L)).isTrue()
    }
}
