package ch.kleis.lcaplugin.language.psi.reference

import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.language.psi.stub.techno_product_exchange.TechnoProductExchangeKeyIndex
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.psi.stubs.StubIndex
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.unmockkStatic
import junit.framework.TestCase
import org.junit.Test


class ProductReferenceTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String {
        return "testdata/language/psi/reference/prod"
    }

    override fun setUp() {
        super.setUp()
        myFixture.copyDirectoryToProject("", "")
    }

    @Test
    fun test_resolve() {
        // given
        val pkgName = "language.psi.reference.prod.test_resolve"
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .getProductRef()

        // when
        val actual = ref.reference.resolve()

        // then
        val expected = TechnoProductExchangeKeyIndex.findTechnoProductExchanges(project, "$pkgName.water.water").first()
        TestCase.assertEquals(expected, actual)
    }

    @Test
    fun test_getVariants() {
        // given
        val pkgName = "language.psi.reference.prod.test_resolve"
        val ref = ProcessStubKeyIndex.findProcesses(project, "$pkgName.p").first()
            .getInputs().first()
            .getProductRef()

        // when
        val actual = ref.reference.variants
            .map { (it as LookupElementBuilder).lookupString }
            .sorted()

        // then
        val expected = listOf("carrot", "water")
        TestCase.assertEquals(expected, actual)

        // clean
        unmockkStatic(StubIndex::class)
    }
}
