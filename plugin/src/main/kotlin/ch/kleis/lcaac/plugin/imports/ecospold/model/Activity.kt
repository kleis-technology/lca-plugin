package ch.kleis.lcaac.plugin.imports.ecospold.model

data class Activity(
    val id: String? = null,
    val type: String,
    val energyValues: String? = null,
    val name: String,
    val includedActivitiesStart: String? = null,
    val includedActivitiesEnd: String? = null,
    val generalComment: List<String>? = null,
    val tags: List<String> = emptyList()
)
