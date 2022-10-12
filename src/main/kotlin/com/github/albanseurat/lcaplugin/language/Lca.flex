package com.github.albanseurat.lcaplugin.language;

import com.github.albanseurat.lcaplugin.psi.LcaTokenType;import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.github.albanseurat.lcaplugin.psi.LcaTypes;
import com.intellij.psi.TokenType;

%%

%public
%class LcaLexer
%implements FlexLexer
%unicode
%function advance
%type IElementType
%eof{  return;
%eof}


WhiteSpace     = \s+
Identifier     = \w+
StringContent  = [^\\\"]*

Number_Exp = [eE][+-]?[0-9]+
Number_Int = [0-9][0-9]*


%state LITERAL_STRING

%%

<YYINITIAL> "dataset"                { return LcaTypes.DATASET_KEYWORD; }

<YYINITIAL> "inputs"                 { return LcaTypes.INPUTS_KEYWORD; }
<YYINITIAL> "products"               { return LcaTypes.PRODUCTS_KEYWORD; }
<YYINITIAL> "resources"              { return LcaTypes.RESOURCES_KEYWORD; }
<YYINITIAL> "emissions"              { return LcaTypes.EMISSIONS_KEYWORD; }
<YYINITIAL> "meta"                   { return LcaTypes.META_KEYWORD; }
<YYINITIAL> "substance"              { return LcaTypes.SUBSTANCE_KEYWORD; }

<YYINITIAL> ":"                      { return LcaTypes.SEPARATOR; }
<YYINITIAL> "{"                      { return LcaTypes.LBRACE; }
<YYINITIAL> "-"                      { return LcaTypes.LIST_ITEM; }
<YYINITIAL> "}"                      { return LcaTypes.RBRACE; }



<YYINITIAL> {Number_Int} ("." {Number_Int}? )? {Number_Exp}? { return LcaTypes.NUMBER; }
<YYINITIAL> {Identifier}             { return LcaTypes.IDENTIFIER; }




<YYINITIAL> \"                       { yybegin(LITERAL_STRING); }


<LITERAL_STRING> {
  \"                             { yybegin(YYINITIAL); return LcaTypes.STRING; }
  \\t                            {  }
  \\n                            {  }
  \\r                            {  }
  \\\"                           {  }
  \\                             {  }
  [^\"\\]+                       {  }
}

{WhiteSpace}                  { return TokenType.WHITE_SPACE; }
[^]                           { return TokenType.BAD_CHARACTER; }