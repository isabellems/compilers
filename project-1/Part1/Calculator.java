import java.io.InputStream;
import java.io.IOException;

public class Calculator{

    private int lookaheadToken;
    private InputStream in;

	public Calculator(InputStream in) throws IOException, ParseError {
		this.in = in;
		lookaheadToken = in.read();
		if(lookaheadToken == '\n')
			consume(lookaheadToken);
		if(lookaheadToken == -1)
			System.exit(0);
    }	

    public int goal() throws IOException, ParseError, DivError{
    	int result = exp();
    	if(lookaheadToken == '\n' || lookaheadToken == -1){
			return result;
		}
		else{
			consumeInvalidStr();
			throw new ParseError();
		}
    }

	public int exp() throws IOException, ParseError, DivError{
		consumeSpaces();
		int eval = term();
		int result = exp2(eval);
		return result;
	}

	public int exp2(int result) throws IOException, ParseError, DivError{
		consumeSpaces();
		if(lookaheadToken == '+'){
			consume(lookaheadToken);
			int eval1 = term();
			int eval2 = exp2(eval1);
			return result + eval2;
		}
		else if(lookaheadToken == '-'){
			consume(lookaheadToken);
			int eval1 = term();
			int eval2 = exp2(eval1);
			return result - eval2;
		}
		else{
			return result;
		}
	}

	public int term() throws IOException, ParseError, DivError{
		consumeSpaces();
		int eval = factor();
		return term2(eval);
	}

	public int term2(int result) throws IOException, ParseError, DivError{
		consumeSpaces();
		if(lookaheadToken == '*'){
			consume(lookaheadToken);
			int eval = factor() * result;
			int res = term2(eval);
			return res;
		}
		else if(lookaheadToken == '/'){
			consume(lookaheadToken);
			int eval1 = factor();
			if(eval1 == 0){
				throw new DivError();
			}
			int eval2 = result / eval1;
			int res = term2(eval2);
			return res;
		}
		else{
			return result;
		}
	}

	public int factor() throws IOException, ParseError, DivError{
		consumeSpaces();
		if(lookaheadToken == '('){
			consume(lookaheadToken);
			int eval1 = exp();
			if(lookaheadToken == ')'){
				consume(lookaheadToken);
				return eval1;
			}
			else{
				consumeInvalidStr();
				throw new ParseError();
			}
		}

		return number();
	}

	public int number() throws IOException, ParseError{
		consumeSpaces();
		if(lookaheadToken >='1' && lookaheadToken <='9'){
			int digit = lookaheadToken - '0';
			String digitStr = Integer.toString(digit);
			consume(lookaheadToken);
			String wholeNumber = moreDigits(digitStr);
			int result = Integer.parseInt(wholeNumber);
			return result;
		}
		else if(lookaheadToken == '0'){
			int result = lookaheadToken - '0';
			consume(lookaheadToken);
			return result;
		}
		else{
		    consumeInvalidStr();
		    throw new ParseError();
		}
	}

	public String moreDigits(String result) throws IOException, ParseError{
		if(lookaheadToken >= '0' && lookaheadToken <='9'){
			int digit = lookaheadToken - '0';
			String digitStr = Integer.toString(digit);
			consume(lookaheadToken);
			String res = result + digitStr;
			return moreDigits(res);
		}
		else{
		    return result;
		}
	}

	private void consume(int symbol) throws IOException, ParseError {
		if (lookaheadToken != symbol){
		    throw new ParseError();
		}
		lookaheadToken = in.read();
    }

    private void consumeSpaces() throws IOException {
    	while(lookaheadToken == ' ') {
    		lookaheadToken = in.read();
    	}	
    }

    private void consumeInvalidStr() throws IOException {
    	while(lookaheadToken != '\n'){
    		lookaheadToken = in.read();
    		if (lookaheadToken == -1){
    			break;
    		}
    	}
    }


	public static void main(String[] args){
		while(true){
			System.out.print(">> ");
			try {
				Calculator calc = new Calculator(System.in);
			   	System.out.println(calc.goal());
			}
			catch (IOException e) {
			    System.err.println(e.getMessage());
			}
			catch(ParseError err){
			    System.err.println(err.getMessage());
			}
			catch(DivError div){
			    System.err.println(div.getMessage());
			}
		}
		
	}
}


