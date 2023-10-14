package ch.kleis.lcaac.plugin.project

import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.prelude.Prelude
import ch.kleis.lcaac.plugin.project.libraries.LcaLibrary
import com.intellij.ide.plugins.IdeaPluginDescriptor
import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.project.Project
import com.intellij.openapi.roots.AdditionalLibraryRootsProvider
import com.intellij.openapi.roots.SyntheticLibrary
import com.intellij.openapi.util.io.StreamUtil
import com.intellij.openapi.vfs.JarFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.io.createDirectories
import com.intellij.util.io.isDirectory
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths.get
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream
import kotlin.io.path.notExists

data class AdditionalLib(val alias: String, val jarName: String)

class LcaRootLibraryProvider : AdditionalLibraryRootsProvider() {
    private companion object {
        private val LOG = Logger.getInstance(LcaRootLibraryProvider::class.java)
    }

    private val additionalJars: Collection<LcaLibrary>

    init {
        val pluginId = PluginId.getId("ch.kleis.lcaac.plugin")
        val plugin = PluginManagerCore.getPlugins().firstOrNull { it.pluginId == pluginId }
        additionalJars =
            listOfNotNull(
                getEmissionFactorLib(AdditionalLib("ef31", "emissions_factors3.1.jar"), plugin),
                getEmissionFactorLib(AdditionalLib("ef30", "emissions_factors3.0.jar"), plugin),
                getUnitLibrary(plugin)
            )
    }

    private fun getUnitLibrary(plugin: IdeaPluginDescriptor?): LcaLibrary {
        val version: String = plugin?.version ?: "unknown"
        val jarName = "${Prelude.PKG_NAME}-$version.jar"
        val folder = cacheFolder()
        val fullPath = Path.of(folder.toString(), jarName)
        val generator = UnitLcaFileFromPreludeGenerator<BasicNumber>()
        generator.recreate(fullPath)
        val virtualFile = VfsUtil.findFile(fullPath, false)!!
        val jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(virtualFile)!!
        return LcaLibrary(jarRoot, Prelude.PKG_NAME)
    }


    private fun getEmissionFactorLib(lib: AdditionalLib, plugin: IdeaPluginDescriptor?): LcaLibrary? {
        val jarVirtualFile = plugin?.pluginPath?.let {
            val jarFile = if (it.isDirectory()) {
                // Case of the LCA As Code run as a plugin from Intellij
                it.resolve(get("lib", lib.jarName))
            } else {
                // Case of the LCA As Code run as an IDE from Intellij as a Gradle Project
                it.parent.resolve(lib.jarName)
            }

            val virtualFile =
                VfsUtil.findFile(jarFile, false) ?: extractLibToFolder(lib)

            if (virtualFile == null) {
                LOG.error("Unable to locate LCAProvider jar files, jar File was $jarFile")
            }
            virtualFile
        }
        val jarRoot = jarVirtualFile?.let {
            JarFileSystem.getInstance().getJarRootForLocalFile(it)
        }
        return jarRoot?.let {
            LcaLibrary(it, lib.alias)
        }
    }

    // Case of the LCA As Code run as an installed IDE
    private fun extractLibToFolder(lib: AdditionalLib): VirtualFile? {
        val targetFolder = cacheFolder()
        val targetFile = Path.of(targetFolder.toString() + File.separatorChar + lib.jarName)
        if (targetFile.notExists()
            || checksumOf(targetFile) != checksumOf(lib)) {
            FileOutputStream(targetFile.toFile()).use { target ->
                this.javaClass.getResourceAsStream("/${lib.jarName}").use { src ->
                    StreamUtil.copy(src!!, target)
                }
            }
        }
        return VfsUtil.findFile(targetFile, false)
    }

    private fun checksumOf(path: Path): Long {
        return checksumOf(FileInputStream(path.toFile()))
    }

    private fun checksumOf(lib: AdditionalLib): Long {
        return checksumOf(this.javaClass.getResourceAsStream("/${lib.jarName}"))
    }

    private fun checksumOf(stream: InputStream?): Long {
        if (stream == null) return 0
        return CheckedInputStream(stream, CRC32()).use {
            it.readAllBytes()
            it.checksum.value
        }
    }

    private fun cacheFolder(): Path {
        val targetFolder =
            Path.of(PathManager.getDefaultPluginPathFor("CacheLcaAsCode1.x") + File.separatorChar + "lca-as-code")
        if (targetFolder.notExists()) targetFolder.createDirectories()
        return targetFolder
    }

    override fun getAdditionalProjectLibraries(project: Project): Collection<SyntheticLibrary> {
        return additionalJars
    }

    override fun getRootsToWatch(project: Project) = emptyList<VirtualFile>()

}
