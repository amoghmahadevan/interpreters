package jlox;

import java.util.List;

public class LoxFunction implements LoxCallable {
  private final Stmt.Function declaration;
  private final Environment closure;

  LoxFunction(Stmt.Function declaration, Environment closure) {
    this.closure = closure;
    this.declaration = declaration;
  }

  // Most important line of code!!
  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    // We create a new environment at each CALL, NOT at function declaration!
    // Closure is the environment that is active when function is DECLARED
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme, arguments.get(i));
    }
    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      return returnValue.value;
    }
    return null;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }
}
