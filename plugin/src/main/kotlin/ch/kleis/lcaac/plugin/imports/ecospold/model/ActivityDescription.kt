package ch.kleis.lcaac.plugin.imports.ecospold.model

data class ActivityDescription(
    val activity: Activity,
    val classifications: List<Classification> = emptyList(),
    val geography: Geography? = null
)
