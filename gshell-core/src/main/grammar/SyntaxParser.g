/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

//
// $Rev$ $Id$
//

grammar SyntaxParser;

options {
	output=AST;
}

@header {
	package org.apache.geronimo.gshell.commandline.parser;
}
@lexer::header {
  	package org.apache.geronimo.gshell.commandline.parser;
}

compilationUnit
	:	( expression ( ';' | NewLine ) | expression EOF )+
	;

expression
	:	( ( WhiteSpace )* argument ( WhiteSpace )* )+
	;

argument
	:	literal
	;

literal
	:	OpaqueStringLiteral | StringLiteral | PlainStringLiteral
	;	

//
// Lexer
//

NewLine
	:	( '\r\n' | '\n' )
	;

WhiteSpace
	:	( ' ' | '\t' | '\r' ) // { $channel=HIDDEN; }
	;

PlainStringLiteral
	: 	( ~( ';'| '\'' | '"' | WhiteSpace | NewLine ) )+
	;

OpaqueStringLiteral
	:  	'\'' ( EscapeSequence | ~('\'') )* '\''
    ;

//
// TODO: Figure out how to parse out ${foo.bar} and "$foo bar", for variable expansion
//

StringLiteral
    :  	'"' ( EscapeSequence | ~('"') )* '"'
    ;

fragment
EscapeSequence
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    |   UnicodeEscape
    |   OctalEscape
	;

fragment
OctalEscape
    :   '\\' ('0'..'3') ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7') ('0'..'7')
    |   '\\' ('0'..'7')
    ;

fragment
HexDigit
	:	('0'..'9'|'a'..'f'|'A'..'F')
	;

fragment
UnicodeEscape
    :   '\\' 'u' HexDigit HexDigit HexDigit HexDigit
    ;
