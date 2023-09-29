package ch.kleis.lcaac.plugin.ide.imports.ecospold.settings

sealed interface EcoSpoldImportSettings {
    var rootPackage: String
    var libraryFile: String
    var rootFolder: String
}
