/* This file is part of KeY - https://key-project.org
 * KeY is licensed under the GNU General Public License Version 2
 * SPDX-License-Identifier: GPL-2.0-only */
package org.key_project.smartml.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.key_project.smartml.proof.io.RuleSource;
import org.key_project.smartml.settings.Configuration;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.file.Path;
import java.util.*;

public final class ParsingFacade {
    private ParsingFacade() {
    }

    private static KeYSmartMLParser createParser(CharStream stream) {
        KeYSmartMLParser p = new KeYSmartMLParser(new CommonTokenStream(createLexer(stream)));
        // p.removeErrorListeners();
        // p.addErrorListener(p.getErrorReporter());
        return p;
    }

    public static KeYSmartMLLexer createLexer(Path file) throws IOException {
        return createLexer(CharStreams.fromPath(file));
    }

    public static KeYSmartMLLexer createLexer(CharStream stream) {
        return new KeYSmartMLLexer(stream);
    }

    public static KeYAst.File parseFile(URL url) throws IOException {
        long start = System.currentTimeMillis();
        try (BufferedInputStream is = new BufferedInputStream(url.openStream());
                ReadableByteChannel channel = Channels.newChannel(is)) {
            CodePointCharStream stream = CharStreams.fromChannel(channel, Charset.defaultCharset(),
                4096, CodingErrorAction.REPLACE, url.toString(), -1);
            return parseFile(stream);
        } finally {
            long stop = System.currentTimeMillis();
        }
    }

    public static List<KeYAst.File> parseFiles(URL url) throws IOException {
        List<KeYAst.File> ctxs = new LinkedList<>();
        ArrayDeque<URL> queue = new ArrayDeque<>();
        queue.push(url);
        Set<URL> reached = new HashSet<>();

        while (!queue.isEmpty()) {
            url = queue.pop();
            reached.add(url);
            KeYAst.File ctx = parseFile(url);
            ctxs.add(ctx);
            Collection<RuleSource> includes = ctx.getIncludes(url).getRuleSets();
            for (RuleSource u : includes) {
                if (!reached.contains(u.url())) {
                    queue.push(u.url());
                }
            }
        }
        return ctxs;
    }

    public static KeYAst.File parseFile(Path file) throws IOException {
        return parseFile(CharStreams.fromPath(file));
    }

    public static KeYAst.File parseFile(File file) throws IOException {
        return parseFile(file.toPath());
    }

    public static KeYAst.File parseFile(CharStream stream) {
        KeYSmartMLParser p = createParser(stream);

        p.getInterpreter().setPredictionMode(PredictionMode.SLL);
        // p.removeErrorListeners();
        // p.setErrorHandler(new BailErrorStrategy());
        KeYSmartMLParser.FileContext ctx;
        try {
            ctx = p.file();
        } catch (ParseCancellationException ex) {
            stream.seek(0);
            p = createParser(stream);
            // p.setErrorHandler(new BailErrorStrategy());
            ctx = p.file();
            // if (p.getErrorReporter().hasErrors()) {
            // throw ex;
            // }
        }

        // p.getErrorReporter().throwException();
        return new KeYAst.File(ctx);
    }

    public static KeYAst.Term parseExpression(CharStream stream) {
        KeYSmartMLParser p = createParser(stream);
        KeYSmartMLParser.TermContext term = p.termEOF().term();
        // p.getErrorReporter().throwException();
        return new KeYAst.Term(term);
    }

    public static KeYAst.Seq parseSequent(CharStream stream) {
        KeYSmartMLParser p = createParser(stream);
        var seq = new KeYAst.Seq(p.seqEOF().seq());
        // p.getErrorReporter().throwException();
        return seq;
    }

    /**
     * Parses the configuration determined by the given {@code file}.
     * A configuration corresponds to the grammar rule {@code cfile} in the {@code KeYParser.g4}.
     *
     * @param file non-null {@link Path} object
     * @return monad that encapsluate the ParserRuleContext
     * @throws IOException if the file is not found or not readable.
     * @throws BuildingException if the file is syntactical broken.
     */
    public static KeYAst.ConfigurationFile parseConfigurationFile(Path file) throws IOException {
        return parseConfigurationFile(CharStreams.fromPath(file));
    }

    /**
     * Parses the configuration determined by the given {@code stream}.
     * A configuration corresponds to the grammar rule {@code cfile} in the {@code KeYParser.g4}.
     *
     * @param stream non-null {@link CharStream} object
     * @return monad that encapsluate the ParserRuleContext
     * @throws BuildingException if the file is syntactical broken.
     */
    public static KeYAst.ConfigurationFile parseConfigurationFile(CharStream stream) {
        KeYSmartMLParser p = createParser(stream);
        var ctx = p.cfile();
        // p.getErrorReporter().throwException();
        return new KeYAst.ConfigurationFile(ctx);
    }

    /**
     * Parses the configuration determined by the given {@code stream}.
     * A configuration corresponds to the grammar rule {@code cfile} in the {@code KeYParser.g4}.
     *
     * @param input non-null {@link CharStream} object
     * @return a configuration object with the data deserialize from the given file
     * @throws BuildingException if the file is syntactical broken.
     */
    public static Configuration readConfigurationFile(CharStream input) {
        return parseConfigurationFile(input).asConfiguration();
    }

    /**
     * @see #readConfigurationFile(CharStream)
     * @throws IOException if the file is not found or not readable.
     */
    public static Configuration readConfigurationFile(Path file) throws IOException {
        return readConfigurationFile(CharStreams.fromPath(file));
    }

    /**
     * @see #readConfigurationFile(CharStream)
     * @throws IOException if the file is not found or not readable.
     */
    public static Configuration readConfigurationFile(File file) throws IOException {
        return readConfigurationFile(file.toPath());
    }

    public static Configuration getConfiguration(KeYSmartMLParser.TableContext ctx) {
        final var cfg = new ConfigurationBuilder();
        return cfg.visitTable(ctx);
    }
}
