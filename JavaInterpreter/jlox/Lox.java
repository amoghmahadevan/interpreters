package jlox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

  private static final Interpreter interpreter = new Interpreter();

  // Good engineering practice to separate code that generates errors from code
  // that reports them
  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: jlox [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    if (hadError)
      System.exit(65);
    if (hadRuntimeError)
      System.exit(70);
  }

  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    for (;;) {
      System.out.print("> ");
      String line = reader.readLine();
      if (line == null)
        break;
      run(line);
      hadError = false;
    }
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();

    /*
     * for (Token token : tokens) {
     * System.out.println(token);
     * }
     */

    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    // Stop if there is a syntax error.
    if (hadError)
      return;

    // System.out.println(new AstPrinter().print(expression));

    Resolver resolver = new Resolver(interpreter);
    resolver.resolve(statements);

    if (hadError)
      return;

    interpreter.interpret(statements);
  }

  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println(
        "[line " + line + "] Error" + where + ": " + message);
    hadError = true; // Ensures code never gets executed
  }

  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at " + token.lexeme + "'", message);
    }
  }

  static void runtimeError(RuntimeError e) {
    System.err.println(e.getMessage() + "\n[line " + e.token.line + "]");
    hadRuntimeError = true;
  }

}
