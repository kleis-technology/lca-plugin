package task

import io.mockk.every
import io.mockk.mockk
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.internal.provider.DefaultProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import java.io.InputStream
import java.util.concurrent.Callable

class SubstanceWithImpactAccumulatorTest {

    @Test
    fun substance31() {
        // given
        val inputDir = mockk<DirectoryProperty>()
        registerMockFiles(inputDir, "flows.31.csv.gz")
        registerMockFiles(inputDir, "factors.31.csv.gz")
        val outDir = mockk<DirectoryProperty>()
        val sut = GenerateEmissionFactorsTask<EF31Record>(inputDir, outDir)

        // when
        val actual = sut.createSubstancesAsString("31") { i -> EF31Record(i) }

        // then
        val key = "_3_sec_butyl_4_decyloxy_phenyl_methanetriyl_tribenzene"
        val expected = """
                package ef31

                substance _3_sec_butyl_4_decyloxy_phenyl_methanetriyl_tribenzene {

                    name = "((3-(sec-butyl)-4-(decyloxy)phenyl)methanetriyl)tribenzene"
                    type = Emission
                    compartment = "soil"
                    sub_compartment = "non-agricultural"
                    reference_unit = kg

                    impacts {
                        2.0522E-08 u Human_toxicity_non_cancer
                        2.0522E-08 u Human_toxicity_non_cancer_organics
                    }

                    meta {
                        "generator" = "kleis-lca-generator"
                        "casNumber" = "1404190-37-9"
                        "ecNumber"  = "801-941-7"
                    }

                }


                """.trimIndent()
        Assertions.assertEquals(expected, actual[key])
    }

    @Test
    fun substance30() {
        // given

        val inputDir = mockk<DirectoryProperty>()
        registerMockFiles(inputDir, "flows.30.csv.gz")
        registerMockFiles(inputDir, "factors.30.csv.gz")
        val outDir = mockk<DirectoryProperty>()
        val sut = GenerateEmissionFactorsTask<EF30Record>(inputDir, outDir)

        // when
        val actual = sut.createSubstancesAsString("30") { i -> EF30Record(i) }

        // then
        val key = "_3ar_5as_9as_9br_3a_6_6_9a_tetramethyldodecahydronaphtho_2_1_b_furan"
        val expected = """
                package ef30

                substance _3ar_5as_9as_9br_3a_6_6_9a_tetramethyldodecahydronaphtho_2_1_b_furan {

                    name = "(-)-(3ar,5as,9as,9br)-3a,6,6,9a-tetramethyldodecahydronaphtho[2,1-b]furan"
                    type = Emission
                    compartment = "water"

                    reference_unit = kg

                    impacts {
                        1199.7 u Ecotoxicity_freshwater
                        1199.7 u Ecotoxicity_freshwater_organics
                    }
                
                    meta {
                        "generator" = "kleis-lca-generator"
                        "casNumber" = "6790-58-5"
                        "ecNumber"  = "229-861-2"
                    }

                }
                
                
                """.trimIndent()
        Assertions.assertEquals(expected, actual[key])
    }

    @Test
    fun test_index_is_present() {
        // given
        val inputDir = mockk<DirectoryProperty>()
        registerMockFiles(inputDir, "flows.31.csv.gz")
        registerMockFiles(inputDir, "factors.31.csv.gz")
        val outDir = mockk<DirectoryProperty>()
        val sut = GenerateEmissionFactorsTask<EF31Record>(inputDir, outDir)

        // when
        val actual = sut.createSubstancesAsString("31") { i -> EF31Record(i) }

        // then
        val key = "META-INF/dictionary.csv"
        val expected = """
                Name;Type;Compartment;SubCompartment
                _3_sec_butyl_4_decyloxy_phenyl_methanetriyl_tribenzene;Emission;soil;non-agricultural
                """.trimIndent()
        Assertions.assertEquals(expected, actual[key])
    }


    private fun registerMockFiles(inputDir: DirectoryProperty, fileName: String) {
        val flowFile = File("src/test/resources/$fileName")
        val regularFile = mockk<RegularFile>()
        every { regularFile.asFile } returns flowFile
        val lambda: Callable<RegularFile> = Callable { -> regularFile }
        val provider = DefaultProvider(lambda)
        every { inputDir.file(fileName) } returns provider
    }

    private fun inputStreamOf(filename: String): InputStream? {
        return object {}.javaClass.getResourceAsStream(filename)
    }

}
