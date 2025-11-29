#!/usr/bin/env bash
curl -O https://www.antlr.org/download/antlr-4.13.2-complete.jar
java -jar antlr-4.13.2-complete.jar  -o language/src/main/java/com/zhhz/truffle/lua/parser  -package com.zhhz.truffle.lua.parser  -visitor  language/src/main/java/com/zhhz/truffle/lua/parser/LuaLexer.g4 language/src/main/java/com/zhhz/truffle/lua/parser/LuaParser.g4