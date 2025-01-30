
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.key_project.smartml.Services;
import org.key_project.smartml.ast.Converter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Test {
    public static void main(String[] args) {
        try {
            var example = Files.readString(Paths.get("/Users/adele/Università/Darmstadt/key/keyext.smartml/examples/basics.sl"),
                    Charset.defaultCharset());
            var lexer = new SmartMLLexer(CharStreams.fromString(example));
            var ts = new CommonTokenStream(lexer);
            var parser = new SmartMLParser(ts);
            var program = parser.program();
            System.out.println(program.getText());
            var converter = new Converter(new Services());
            //var converted = converter.convertCrate(program);
            //System.out.println(converted);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}