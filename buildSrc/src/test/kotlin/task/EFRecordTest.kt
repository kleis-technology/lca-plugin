package task

import io.mockk.every
import io.mockk.mockk
import org.apache.commons.csv.CSVRecord
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class EFRecordTest {
    @Test
    fun `unit sanitization should preserve case in EF30Record`() {
        // given
        val record = mockk<CSVRecord>()
        every { record.isMapped("FLOW_propertyUnit") } returns true
        every { record["FLOW_propertyUnit"] } returns "kBq"
        val sut30 = EF30Record(record)

        // when
        val result = sut30.unit()

        // then
        assertEquals("kBq", result)
    }

    @Test
    fun `unit sanitization should preserve case in EF31Record`() {
        // given
        val record = mockk<CSVRecord>()
        every { record.isMapped("FLOW_propertyUnit") } returns true
        every { record["FLOW_propertyUnit"] } returns "kBq"
        val sut31 = EF31Record(record)

        // when
        val result = sut31.unit()

        // then
        assertEquals("kBq", result)
    }

    @ParameterizedTest
    @CsvSource(
        "Item(s), piece, item",
        "kg_m_a, kga, kg year",
        "m2_m_a, m2a, square meter year",
        "m3_m_a, m3a, cubic meter year",
    )
    fun `unit sanitization should preserve usual units for EF30`(source: String, expected: String, desc: String) {
        // given
        val record = mockk<CSVRecord>()
        every { record.isMapped("FLOW_propertyUnit") } returns true
        every { record["FLOW_propertyUnit"] } returns source
        val sut30 = EF30Record(record)

        // when
        val result = sut30.unit()

        // then
        assertEquals(expected, result, desc)
    }

    @ParameterizedTest
    @CsvSource(
        "Item(s), piece, item",
        "kg_m_a, kga, kg year",
        "m2_m_a, m2a, square meter year",
        "m3_m_a, m3a, cubic meter year",
    )
    fun `unit sanitization should preserve usual units for EF31`(source: String, expected: String, desc: String) {
        // given
        val record = mockk<CSVRecord>()
        every { record.isMapped("FLOW_propertyUnit") } returns true
        every { record["FLOW_propertyUnit"] } returns source
        val sut31 = EF31Record(record)

        // when
        val result = sut31.unit()

        // then
        assertEquals(expected, result, desc)
    }
}