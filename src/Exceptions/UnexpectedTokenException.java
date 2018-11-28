
package Exceptions;

import Model.Token;

public class UnexpectedTokenException extends Exception{
    private String expectedToken;
    private Token receivedToken;
    private int line;
    public UnexpectedTokenException(Token received, String expected, int line){
        super();
        this.expectedToken = expected;
        this.receivedToken = received;
        this.line = line;
    }
    
}
