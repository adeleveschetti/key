parser grammar KeYSmartMLParser;

import KeYParser;

options { tokenVocab = KeYSmartMLLexer; }

@header {
package org.key_project.smartml.parser;
}

varexpId
 : APPLY_UPDATE_ON_RIGID
 | DROP_EFFECTLESS_ELEMENTARIES
 | SIMPLIFY_IF_THEN_ELSE_UPDATE
 | EQUAL_UNIQUE
 | NEW_TYPE_OF
 | NEW_DEPENDING_ON
 | NEW
 ;