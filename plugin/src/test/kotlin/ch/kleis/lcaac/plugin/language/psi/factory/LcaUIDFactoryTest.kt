package ch.kleis.lcaac.plugin.language.psi.factory

import ch.kleis.lcaac.plugin.language.loader.LcaParserDefinition
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.testFramework.ParsingTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class LcaUIDFactoryTest : ParsingTestCase("", "lca", LcaParserDefinition()) {

    @Test
    fun test_createUid() {
        // when
        val actual = LcaUIDFactory { content ->
            parseFile("create_uid", content) as LcaFile
        }.createUid("foo")

        // then
        assertEquals("foo", actual.name)
    }

    override fun getTestDataPath(): String {
        return ""
    }
}
