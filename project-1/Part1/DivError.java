public class DivError extends Exception {

    public String getMessage() {
		return "Cannot divide by zero.";
    }
}