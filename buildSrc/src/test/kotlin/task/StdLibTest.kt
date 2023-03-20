package task

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import org.apache.commons.csv.CSVRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.io.FileInputStream
import java.io.FileReader
import java.io.InputStream
import java.nio.charset.Charset
import kotlin.streams.asSequence


class StdLibTest {
    @Test
    fun sanitizeString_whenNormal_thenDoNothing() {
        // given
        val s = "abcd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "abcd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeString_whenSpace_shouldReplaceWithUnderscore() {
        // given
        val s = "ab   cd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "ab_cd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeString_whenSpecialSymbols_shouldRemove() {
        // given
        val s = "ab(#+cd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "ab_cd"
        assertEquals(expected, actual)
    }

    @Test
    fun sanitizeString_whenBeginsWithNumber_shouldPrependWithUnderscore() {
        // given
        val s = "123abcd"

        // when
        val actual = sanitizeString(s)

        // then
        val expected = "_123abcd"
        assertEquals(expected, actual)
    }

    @Test
    fun substance() {
        // given
        val substances = loadRecords()
            .groupingBy { it.substanceId() }
            .fold({ _: String, element: CSVRecord -> Impact() },
                { _: String, accumulator: Impact, element: CSVRecord -> accumulator.factor(element)})

        // when
        val actual = substances.values.groupingBy { it.lcaFileName }
            .fold("package ef31\n\n") { accumulator: String, element: Impact ->
                accumulator.plus(element.fileContent).plus("\n\n")
            }

        // then
        val key = "_3_sec_butyl_4_decyloxy_phenyl_methanetriyl_tribenzene"
        val expected = """
                package ef31

                substance _3_sec_butyl_4_decyloxy_phenyl_methanetriyl_tribenzene_soil_non_agricultural {

                    name = "((3-(sec-butyl)-4-(decyloxy)phenyl)methanetriyl)tribenzene"
                    compartment = "soil"
                    sub_compartment = "non-agricultural"
                    reference_unit = kg
                    
                    impacts {
                        2.0522E-08 u Human_toxicity_non_cancer
                        2.0522E-08 u Human_toxicity_non_cancer_organics    
                    }
                    
                    meta {
                        type = "emissions"
                        generator = "kleis-lca-generator"
                        casNumber = "1404190-37-9"
                        ecNumber = "801-941-7"
                    }    
                
                }
                
                
                """.trimIndent()
        assertEquals(expected, actual[key])
    }

    private fun loadRecords(): Sequence<CSVRecord> {
        val csvFormat = CSVFormat.Builder.create().setHeader().build()
        val flowInput = FileReader("src/test/resources/flows.csv")
        val flows = csvFormat.parse(flowInput).asSequence()


        val factorsInput = FileReader("src/test/resources/factors.csv")
        val factors = csvFormat.parse(factorsInput).asSequence()
        return flows.plus(factors)
    }

    private fun inputStreamOf(filename: String): InputStream? {
        return object {}.javaClass.getResourceAsStream(filename)
    }
}
