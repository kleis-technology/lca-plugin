package com.github.albanseurat.lcaplugin.language.parser;

import javax.measure.format.MeasurementParseException;
import javax.measure.format.UnitFormat;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import tech.units.indriya.format.SimpleUnitFormat;

import static com.github.albanseurat.lcaplugin.psi.LcaTypes.*;
import static com.intellij.psi.TokenType.ERROR_ELEMENT;
import static java.lang.String.format;

public class ExtensionParser extends GeneratedParserUtilBase
{
    static UnitFormat parser = SimpleUnitFormat.getInstance();

    public static boolean parseQuantity(PsiBuilder builder, int level)
    {
        PsiBuilder.Marker marker = builder.mark();
        String text = builder.getTokenText();
        try
        {
            parser.parse(text);
            builder.advanceLexer();
            marker.done(UNIT);
            return true;
        } catch (MeasurementParseException e)
        {
            builder.advanceLexer();
            marker.error(format("%s is not a valid unit", text));
            return true;
        }
    }

}
