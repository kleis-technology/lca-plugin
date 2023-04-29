grammar LcaLang;

lcaFile
	:	pkg? pkgImport* ( process | unitDefinition | substance | globalVariables )* EOF
	;

pkg
    : PACKAGE_KEYWORD urn
    ;

pkgImport
    : IMPORT_KEYWORD urn
    ;

/*
    Global variables
*/

globalVariables
    : VARIABLES_KEYWORD LBRACE globalAssignment* RBRACE
    ;
globalAssignment
    : quantityRef EQUAL quantity
    ;

/*
    Substance
*/

substance
    : SUBSTANCE_KEYWORD substanceRef LBRACE
        nameField
        typeField
        compartmentField
        subCompartmentField?
        referenceUnitField
        ( block_impacts | block_meta )*
      RBRACE
    ;

/*
    Fields
*/

nameField
    : NAME_KEYWORD EQUAL STRING_LITERAL
    ;

typeField
    : TYPE_KEYWORD EQUAL ( TYPE_EMISSION_KEYWORD | TYPE_RESOURCE_KEYWORD | TYPE_LAND_USE_KEYWORD)
    ;

compartmentField
    : COMPARTMENT_KEYWORD EQUAL STRING_LITERAL
    ;

subCompartmentField
    : SUB_COMPARTMENT_KEYWORD EQUAL STRING_LITERAL
    ;

dimField
    : DIMENSION_KEYWORD EQUAL STRING_LITERAL
    ;

referenceUnitField
    : REFERENCE_UNIT_KEYWORD EQUAL unit
    ;

symbolField
    : SYMBOL_KEYWORD EQUAL STRING_LITERAL
    ;

aliasForField
    : ALIAS_FOR_KEYWORD EQUAL quantity
    ;

/*
    Meta
*/

block_meta
    : META_KEYWORD LBRACE meta_assignment* RBRACE
    ;
meta_assignment
    : STRING_LITERAL EQUAL STRING_LITERAL
    ;

/*
    Process
*/

process
    : PROCESS_KEYWORD processTemplateRef LBRACE
        (
            params
            | variables
            | block_products
            | block_inputs
            | block_emissions
            | block_land_use
            | block_resources
            | block_meta
        )* RBRACE

    ;

params
    : PARAMETERS_KEYWORD LBRACE assignment* RBRACE
    ;

variables
    : VARIABLES_KEYWORD LBRACE assignment* RBRACE
    ;

assignment
    : quantityRef EQUAL quantity
    ;

/*
    Blocks
*/

block_products
    : PRODUCTS_KEYWORD LBRACE (
            technoProductExchangeWithAllocateField+
            | technoProductExchange?
        ) RBRACE
    ;

block_inputs
    : INPUTS_KEYWORD LBRACE technoInputExchange* RBRACE
    ;

block_emissions
    : EMISSIONS_KEYWORD LBRACE bioExchange* RBRACE
    ;

block_land_use
    : LAND_USE_KEYWORD LBRACE bioExchange* RBRACE
    ;

block_resources
    : RESOURCES_KEYWORD LBRACE bioExchange* RBRACE
    ;

block_impacts
    : IMPACTS_KEYWORD LBRACE impactExchange* RBRACE
    ;

/*
    Exchanges
*/

technoInputExchange
    : quantity productRef fromProcessConstraint?
    ;

fromProcessConstraint
    : FROM_KEYWORD processTemplateRef LPAREN comma_sep_arguments? RPAREN
    ;

comma_sep_arguments
    : argument (COMMA argument)* COMMA?
    ;
argument
    : parameterRef EQUAL quantity
    ;

allocateField
    : ALLOCATE_KEYWORD quantity
    ;

technoProductExchange
    : quantity productRef
    ;

technoProductExchangeWithAllocateField
    : technoProductExchange allocateField
    ;

bioExchange
    : quantity substanceRef
    ;

impactExchange
    : quantity indicatorRef
    ;

/*
    Quantity
*/

quantity
    : quantityTerm ((PLUS | MINUS) quantity)?
    ;
quantityTerm
    : quantityFactor ((STAR | SLASH) quantityTerm)?
    ;
quantityFactor
    : quantityPrimitive (HAT NUMBER)?
    ;
quantityPrimitive
    : NUMBER quantityRef | LPAREN quantity RPAREN | quantityRef
    ;

/*
    Unit
*/

unit
    : unitFactor ((STAR | SLASH) unit)?
    ;
unitFactor
    : unitPrimitive (HAT NUMBER)?
    ;
unitPrimitive
    : LPAREN unit RPAREN | unitRef
    ;

unitDefinition
    : UNIT_KEYWORD unitRef LBRACE
        symbolField (dimField | aliasForField)
      RBRACE
    ;

/*
    Reference
*/

productRef : uid ;
quantityRef : uid ;
substanceRef : uid ;
indicatorRef : uid ;
unitRef : uid ;
processTemplateRef : uid ;
parameterRef : uid ;

/*
    Identifier
*/

urn : uid DOT urn | uid ;
uid : ID ;


/*
    Lexems
*/

PACKAGE_KEYWORD : 'package' ;
IMPORT_KEYWORD : 'import' ;
VARIABLES_KEYWORD : 'variables' ;
PROCESS_KEYWORD : 'process' ;
SUBSTANCE_KEYWORD : 'substance' ;
TYPE_KEYWORD : 'type' ;
TYPE_EMISSION_KEYWORD : 'Emission' ;
TYPE_RESOURCE_KEYWORD : 'Resource' ;
TYPE_LAND_USE_KEYWORD : 'Land_use' ;
COMPARTMENT_KEYWORD : 'compartment' ;
SUB_COMPARTMENT_KEYWORD : 'sub_compartment' ;
IMPACTS_KEYWORD : 'impacts' ;
META_KEYWORD : 'meta' ;
FROM_KEYWORD : 'from' ;
NAME_KEYWORD : 'name' ;
UNIT_KEYWORD : 'unit' ;
REFERENCE_UNIT_KEYWORD : 'reference_unit' ;
SYMBOL_KEYWORD : 'symbol' ;
ALIAS_FOR_KEYWORD : 'alias_for' ;
ALLOCATE_KEYWORD : 'allocate' ;
DIMENSION_KEYWORD : 'dimension' ;
PARAMETERS_KEYWORD : 'params' ;
PRODUCTS_KEYWORD : 'products' ;
INPUTS_KEYWORD  : 'inputs' ;
EMISSIONS_KEYWORD : 'emissions' ;
LAND_USE_KEYWORD : 'land_use' ;
RESOURCES_KEYWORD : 'resources' ;

EQUAL : '=' ;
LBRACK : '[' ;
RBRACK : ']' ;
LBRACE : '{' ;
RBRACE : '}' ;
LPAREN : '(' ;
RPAREN : ')' ;
COMMA : ',' ;
DOT : ' . ' ;
PLUS : '+' ;
MINUS : '-' ;
STAR : '*' ;
SLASH : '/' ;
HAT : '^' ;
DOUBLE_QUOTE : '"' ;

LINE_COMMENT : '//' .*? ('\n'|EOF)	-> channel(HIDDEN) ;
COMMENT      : '/*' .*? '*/'    	-> channel(HIDDEN) ;

ID  : [a-zA-Z_] [a-zA-Z0-9_]* ;
INT : [0-9]+ ;
NUMBER
	:   '-'? INT '.' INT EXP?   // 1.35, 1.35E-9, 0.3, -4.5
	|   '-'? INT EXP?            // 1e10 -3e4
	;
fragment EXP :   [Ee] [+\-]? INT ;

STRING_LITERAL :  '"' (ESC | ~["\\])* '"' ;
fragment ESC :   '\\' ["\bfnrt] ;

WS : [ \t\n\r]+ -> channel(HIDDEN) ;

/** "catch all" rule for any char not matche in a token rule of your
 *  grammar. Lexers in Intellij must return all tokens good and bad.
 *  There must be a token to cover all characters, which makes sense, for
 *  an IDE. The parser however should not see these bad tokens because
 *  it just confuses the issue. Hence, the hidden channel.
 */
ERRCHAR
	:	.	-> channel(HIDDEN)
	;

