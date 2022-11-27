package ch.kleis.lcaplugin.services

import ch.kleis.lcaplugin.language.parser.LcaParserDefinition
import ch.kleis.lcaplugin.lib.ModelCoreSystemVisitor
import com.intellij.testFramework.ParsingTestCase
import junit.framework.TestCase
import org.junit.Test
import org.openlca.simapro.csv.Numeric
import org.openlca.simapro.csv.enums.ProcessCategory
import org.openlca.simapro.csv.enums.ProcessType
import org.openlca.simapro.csv.process.ElementaryExchangeRow
import org.openlca.simapro.csv.process.ProcessBlock
import org.openlca.simapro.csv.process.ProductOutputRow
import org.openlca.simapro.csv.process.TechExchangeRow
import tech.units.indriya.quantity.Quantities.getQuantity
import tech.units.indriya.unit.Units.KILOGRAM
import tech.units.indriya.unit.Units.LITRE

internal class ScsvFormatterTest : ParsingTestCase("", "lca", LcaParserDefinition()){

    @Test
    fun testFormat() {
        // given
        val processBlock = ProcessBlock()
            .name("process \"a process\" name")
            .identifier("123456")
            .category(ProcessCategory.MATERIAL)
            .processType(ProcessType.UNIT_PROCESS)
        val products = processBlock.products()
        products.add(ProductOutputRow()
            .name("reference product")
            .amount(Numeric.of(1.0))
            .unit("kg"))
        val formatter = ScsvProcessBlockFormatter()

        // when
        val actual = formatter.format(processBlock)

        // then
        val expected = """
            process "process \"a process\" name" {
                products {
                    - "reference product" 1.0 kg
                }
                
                meta {
                    - identifier: "123456"
                    - category: "material"
                    - processType: "Unit process"
                }
                
            }
            
        """.trimIndent()
        assertEquals(actual, expected)
    }

    @Test
    fun testFormatAndParse() {
        // given
        val processBlock = ProcessBlock()
            .name("hello world")
            .identifier("123456")
            .category(ProcessCategory.MATERIAL)
            .processType(ProcessType.UNIT_PROCESS)
        val products = processBlock.products()
        products.add(ProductOutputRow()
            .name("reference product")
            .amount(Numeric.of(1.0))
            .unit("kg"))
        val materialAndFuels = processBlock.materialsAndFuels()
        materialAndFuels.add(TechExchangeRow()
            .name("heat")
            .amount(Numeric.of(2.0))
            .unit("kg")
        )
        val emissionsToAir = processBlock.emissionsToAir()
        emissionsToAir.add(ElementaryExchangeRow()
            .name("co2")
            .subCompartment("low pop")
            .amount(Numeric.of(3.0))
            .unit("kg")
        )
        val resources = processBlock.resources()
        resources.add(ElementaryExchangeRow()
            .name("land use")
            .subCompartment("in ground")
            .amount(Numeric.of(5.0))
            .unit("l")
        )
        val formatter = ScsvProcessBlockFormatter()
        val visitor = ModelCoreSystemVisitor()

        // when
        val content = formatter.format(processBlock)
        val psiFile = parseFile("test", content)
        psiFile.accept(visitor)
        val actual = visitor.getSystem().getProcess(".processes.hello world")

        // then
        TestCase.assertEquals(actual.getUniqueId(), ".processes.hello world")
        TestCase.assertEquals(actual.outputs.size, 1)
        TestCase.assertEquals(actual.outputs[0].flow.getUniqueId(), ".flows.reference product")
        TestCase.assertEquals(actual.outputs[0].quantity, getQuantity(1.0, KILOGRAM))
        TestCase.assertEquals(actual.inputs[0].flow.getUniqueId(), ".flows.heat")
        TestCase.assertEquals(actual.inputs[0].quantity, getQuantity(2.0, KILOGRAM))
        TestCase.assertEquals(actual.inputs[1].flow.getUniqueId(), ".flows.co2, air, low pop")
        TestCase.assertEquals(actual.inputs[1].quantity, getQuantity(3.0, KILOGRAM))
        TestCase.assertEquals(actual.inputs[2].flow.getUniqueId(), ".flows.land use, raw, in ground")
        TestCase.assertEquals(actual.inputs[2].quantity, getQuantity(5.0, LITRE))
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
