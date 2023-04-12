package task

import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.FileReader
import java.io.InputStream


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
    fun substance31() {
        // given
        val substances = loadRecords("31")
            .map { EF31Record(it) }
            .groupingBy { it.substanceId() }
            .fold({ _: String, element: EFRecord -> Impact() },
                { _: String, accumulator: Impact, element: EFRecord -> accumulator.factor(element) })

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

    @Test
    fun substance30() {
        // given
        val substances = loadRecords("30")
            .map { EF30Record(it) }
            .groupingBy { it.substanceId() }
            .fold({ _: String, element: EFRecord -> Impact() },
                { _: String, accumulator: Impact, element: EFRecord -> accumulator.factor(element) })

        // when
        val actual = substances.values.groupingBy { it.lcaFileName }
            .fold("package ef30\n\n") { accumulator: String, element: Impact ->
                accumulator.plus(element.fileContent).plus("\n\n")
            }

        // then
        val key = "_3ar_5as_9as_9br_3a_6_6_9a_tetramethyldodecahydronaphtho_2_1_b_furan"
        val expected = """
                package ef30

                substance _3ar_5as_9as_9br_3a_6_6_9a_tetramethyldodecahydronaphtho_2_1_b_furan_water {

                    name = "(-)-(3ar,5as,9as,9br)-3a,6,6,9a-tetramethyldodecahydronaphtho[2,1-b]furan"
                    compartment = "water"

                    reference_unit = kg

                    impacts {
                        1199.7 u Ecotoxicity_freshwater
                        1199.7 u Ecotoxicity_freshwater_organics
                    }
                
                    meta {
                        type = "emissions"
                        generator = "kleis-lca-generator"
                        casNumber = "6790-58-5"
                        ecNumber = "229-861-2"
                    }

                }
                
                
                """.trimIndent()
        assertEquals(expected, actual[key])
    }

    private fun loadRecords(version: String): Sequence<CSVRecord> {
        val csvFormat = CSVFormat.Builder.create().setHeader().build()
        val flowInput = FileReader("src/test/resources/flows$version.csv")
        val flows = csvFormat.parse(flowInput).asSequence()


        val factorsInput = FileReader("src/test/resources/factors$version.csv")
        val factors = csvFormat.parse(factorsInput).asSequence()
        return flows.plus(factors)
    }

    private fun inputStreamOf(filename: String): InputStream? {
        return object {}.javaClass.getResourceAsStream(filename)
    }
}
