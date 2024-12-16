package utils;

/**
 * @author Huang_ruijie
 * @version 1.0
 */
public class MyException extends Exception{
    private String message;//错误信息
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\u001B[31m";

    public MyException(String message) {
        this.message = message;
    }
    @Override
    public void printStackTrace(){
        System.out.println(RED + message + RESET);
    }
}
