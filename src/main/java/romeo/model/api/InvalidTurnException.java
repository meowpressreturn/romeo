package romeo.model.api;

/**
 * Indicates an out of bounds turn value
 */
public class InvalidTurnException extends RuntimeException {
  protected int _turn;

  public InvalidTurnException(int turn) {
    super("invalid turn:" + turn);
  }

  public int getTurn() {
    return _turn;
  }
}
