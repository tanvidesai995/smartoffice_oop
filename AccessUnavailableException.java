package smartoffice.v1;

public class AccessUnavailableException extends Exception {
  public AccessUnavailableException(){
    super("Access unavailable.");
  }
  public ParsingException(String role) {
        super("Access denied to "+ role);
    }
}
