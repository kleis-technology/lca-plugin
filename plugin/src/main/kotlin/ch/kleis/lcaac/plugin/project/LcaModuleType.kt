package ch.kleis.lcaac.plugin.project

import ch.kleis.lcaac.plugin.LcaIcons
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.ModuleTypeManager
import javax.swing.Icon


class LcaModuleType : ModuleType<LcaModuleBuilder>(ID) {

    object FACTORY {
        fun getInstance(): LcaModuleType {
            return ModuleTypeManager.getInstance().findByID(ID) as LcaModuleType
        }
    }

    companion object {
        const val ID = "LCA_MODULE_TYPE"

    }

    override fun createModuleBuilder(): LcaModuleBuilder {
        return LcaModuleBuilder()
    }

    override fun getName(): String {
        return "LCA Module Type"
    }

    override fun getDescription(): String {
        return "This module allow to create process using LCA methodology"
    }

    override fun getNodeIcon(isOpened: Boolean): Icon {
        return LcaIcons.PROJECT
    }

}
