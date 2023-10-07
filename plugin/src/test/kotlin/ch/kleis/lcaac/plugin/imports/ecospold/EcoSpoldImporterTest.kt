package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.plugin.ide.imports.ecospold.settings.LCIASettings
import ch.kleis.lcaac.plugin.imports.SummaryInSuccess
import ch.kleis.lcaac.plugin.imports.util.AsyncTaskController
import ch.kleis.lcaac.plugin.imports.util.AsynchronousWatcher
import ch.kleis.lcaac.plugin.language.psi.LcaFile
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.RefreshQueue
import com.intellij.psi.PsiManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import io.mockk.*
import junit.framework.TestCase
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test


@RunWith(JUnit4::class)
class EcoSpoldImporterTest : BasePlatformTestCase() {
    private val file = {}.javaClass.classLoader.getResource("lcia_sample.7z")!!.file
    private val rootFolder = Files.createTempDirectory("lca_test_").toString()
    private var settings = mockk<LCIASettings>()
    private var watcher = mockk<AsynchronousWatcher>()
    private var controller = mockk<AsyncTaskController>()

    private val outputUnitFile = "$rootFolder${File.separatorChar}unit.lca"
    private val processFile = "$rootFolder${File.separatorChar}processes${File.separatorChar}electricity_high_voltage_import_from_uz_tj.lca"

    override fun setUp() {
        super.setUp()
        settings = mockk<LCIASettings>()
        every { settings.libraryFile } returns file
        every { settings.rootPackage } returns "ei391"
        every { settings.methodName } returns "EF v3.1"
        every { settings.rootFolder } returns rootFolder

        every { watcher.notifyCurrentWork(any()) } returns Unit
        every { watcher.notifyProgress(any()) } returns Unit
        every { controller.isActive() } returns true

        mockkStatic(LocalFileSystem::class)
        val fileSys = mockk<LocalFileSystem>()
        every { LocalFileSystem.getInstance() } returns fileSys
        val vFile = mockk<VirtualFile>()
        every { fileSys.findFileByPath(any()) } returns vFile
        mockkStatic(ModalityState::class)
        every { ModalityState.current() } returns mockk()
        mockkStatic(RefreshQueue::class)
        val refresh = mockk<RefreshQueue>()
        every { RefreshQueue.getInstance() } returns refresh
        justRun { refresh.refresh(any(), any(), any(), any<ModalityState>(), any<VirtualFile>()) }
    }

    override fun tearDown() {
        unmockkAll()
        super.tearDown()
    }

    @Test
    fun test_import_thenCorrectResults() {
        // given
        val importer = EcoSpoldImporter(settings)

        // when
        val result = importer.import(controller, watcher)

        // then
        TestCase.assertTrue(result is SummaryInSuccess)
        assertEquals("729 units, 1 processes", result.getResourcesAsString())
        TestCase.assertTrue(result.durationInSec >= 0)
    }

    @Test
    fun test_import_thenValidUnitFile() {
        // given
        val importer = EcoSpoldImporter(settings)

        // when
        importer.import(controller, watcher)
        val vf = VirtualFileManager.getInstance().findFileByNioPath(Path.of(outputUnitFile))!!
        val lcaFile = PsiManager.getInstance(project).findFile(vf) as LcaFile

        // then
        /*
            The number of unit definitions is different from the number of "imported units"
            because we avoid (as much as possible) redefining units that are already known.
         */
        assertEquals(708, lcaFile.getUnitDefinitions().size)
    }

    @Test
    fun test_import_thenValidProcessFile() {
        // given
        val importer = EcoSpoldImporter(settings)

        // when
        importer.import(controller, watcher)
        val vf = VirtualFileManager.getInstance().findFileByNioPath(Path.of(processFile))!!
        val lcaFile = PsiManager.getInstance(project).findFile(vf) as LcaFile

        // then
        assertEquals(1, lcaFile.getProcesses().size)
    }

    @Test
    fun test_import_thenNoDuplicateDimensions() {
        // given
        val importer = EcoSpoldImporter(settings)

        // when
        importer.import(controller, watcher)
        val vf = VirtualFileManager.getInstance().findFileByNioPath(Path.of(outputUnitFile))!!
        val lcaFile = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val unitDefinitions = lcaFile.getUnitDefinitions()

        // then
        val grouped=  unitDefinitions
            .filter { it.dimField != null }
            .groupBy { it.dimField!!.text }
            .filter { it.value.size > 1 }
        assertEmpty(grouped.entries)
    }

    @Test
    fun test_import_shouldDealWithProblematicUnitCases() {
        // given
        val importer = EcoSpoldImporter(settings)
        val cases = listOf(
            "mile_m_year" to "1609.344 m*year",
            "ounce_avoirdupois_per_gallon_Imperial" to "0.00623602328594462 kg/l",
            "pound_per_cubic_yard" to "5.93276421257783E-4 kg/l",
            "cm2_m_year" to "1.0E-4 m2*year",
            "km2_m_year" to "1000000.0 m2*year",
            "square_feet_m_degree_F_m_hour_sl_British_thermal_unit_ISO_IT" to "0.176110184 m2*K/W",
            "clo" to "0.155 m2*K/W",
            "pound_per_cubic_foot" to "0.0160184633739601 kg/l",
            "short_ton_m_mile" to "1.45997231821056 ton*km",
            "km_m_year" to "1000.0 m*year",
            "person_m_mile" to "1.609344 person*km",
            "pound_per_cubic_inch" to "27.6799047102031 kg/l",
            "ounce_avoirdupois_per_cubic_inch" to "1.7299940428621 kg/l",
            "kgkm" to "0.001 ton*km",
            "short_ton_per_cubic_yard" to "1.18655284251557 kg/l",
            "ha_m_year" to "10000.0 m2*year",
            "mm2_m_year" to "1.0E-6 m2*year",
            "pound_per_gallon_Imperial" to "0.0997763726631017 kg/l",
            "tog" to "0.1 m2*K/W",
            "long_ton_per_cubic_yard" to "1.32893918518697 kg/l",
        )

        // when
        importer.import(controller, watcher)
        val vf = VirtualFileManager.getInstance().findFileByNioPath(Path.of(outputUnitFile))!!
        val lcaFile = PsiManager.getInstance(project).findFile(vf) as LcaFile
        val actual = lcaFile.getUnitDefinitions().associateBy { it.name }

        // then
        cases.forEach { case ->
            assertEquals("case ${case.first}", case.second, actual[case.first]?.aliasForField?.dataExpression?.text)
        }
    }

    override fun getTestDataPath(): String {
        return "testdata"
    }
}
