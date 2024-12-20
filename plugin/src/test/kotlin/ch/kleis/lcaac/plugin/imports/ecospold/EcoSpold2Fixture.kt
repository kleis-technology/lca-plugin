package ch.kleis.lcaac.plugin.imports.ecospold

import ch.kleis.lcaac.core.lang.expression.SubstanceType
import ch.kleis.lcaac.plugin.imports.ecospold.model.*

object EcoSpold2Fixture {
    fun buildProcessDict(): Map<String, EcoSpoldImporter.ProcessDictRecord> = mapOf(
        "iNameProcessID" to EcoSpoldImporter.ProcessDictRecord(
            processId = "iNameProcessID",
            fileName = "foo",
            processName = "iName Producing Process",
            geo = "GLO",
            productName = "iName",
        ),
        "iName2ProcessID" to EcoSpoldImporter.ProcessDictRecord(
            processId = "iName2ProcessID",
            fileName = "bar",
            processName = "iName2 Producing Process",
            geo = "CH",
            productName = "iName2",
        ),
    )

    fun buildData(outputGroup: Int = 0, inputGroup: Int = 5): ActivityDataset {
        val activity = Activity(
            id = "aId",
            name = "aName",
            type = "1",
            generalComment = listOf("ageneralComment"),
            energyValues = "123",
            includedActivitiesStart = "includedActivitiesStart",
            includedActivitiesEnd = "includedActivitiesEnd",
        )
        val c = Classification("System", "Value")
        val geo = Geography("ch", listOf("comment"))
        val description = ActivityDescription(
            activity = activity,
            geography = geo,
            classifications = listOf(c),
        )
        val prod = IntermediateExchange(
            id = "prodId",
            name = "pName",
            outputGroup = outputGroup,
            classifications = listOf(Classification("PSystem", "PValue")),
            uncertainty = Uncertainty(logNormal = LogNormal(1.2, 3.4, 2.3, 4.5)),
            amount = 1.0,
            unit = "km",
            synonyms = listOf("p1"),
        )
        val i1 = IntermediateExchange(
            id = "iNameID",
            name = "iName",
            amount = 3.0,
            unit = "kg",
            inputGroup = inputGroup,
            activityLinkId = "iNameProcessID",
        )
        val i2 = IntermediateExchange(
            id = "iName2ID",
            name = "iName2",
            amount = 25.0,
            unit = "m3",
            inputGroup = inputGroup,
            activityLinkId = "iName2ProcessID"
        )
        val impacts = sequenceOf(
            ImpactExchange(0.1188, MethodIndicator("EF v3.0 no LT", "water use", "deprivation", "m3 world eq. deprived")),
            ImpactExchange(0.0013, MethodIndicator("EF v3.1", "acidification", "accumulated exceedance (AE)", "mol H+-Eq")),
            ImpactExchange(0.6, MethodIndicator("EF v3.1", "climate change", "global warming potential (GWP100)", "kg CO2-Eq")),
        )
        val emissions = sequenceOf(
            ElementaryExchange(
                "9645e02f-855a-4b9f-8baf-f34a08fa80c4",
                "1.8326477008541038E-08".toDouble(),
                "1,2-Dichlorobenzene",
                "kg",
                "air",
                "urban air close to ground",
                SubstanceType.EMISSION,
                null
            ),
            ElementaryExchange(
                "e3f5fd63-7dcb-41f1-9b8a-a48a8d68bc65",
                "0.004413253823373581".toDouble(),
                "Nitrogen",
                "kg",
                "natural resource",
                "land",
                SubstanceType.RESOURCE,
                null
            ),
            ElementaryExchange(
                "c4a82f46-381f-474c-a362-3363064b9c33",
                "0.04997982922431679".toDouble(),
                "Occupation, annual crop, irrigated",
                "m2*year",
                "natural resource",
                "land",
                SubstanceType.LAND_USE,
                null
            ),
        )
        return ActivityDataset(
            description, FlowData(
                intermediateExchanges = sequenceOf(prod, i1, i2),
                impactExchanges = impacts,
                elementaryExchanges = emissions,
            )
        )
    }
}
