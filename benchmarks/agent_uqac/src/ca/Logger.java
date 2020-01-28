package ca.uqac;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public abstract class Logger{

	public static String INFO = "info";
	public static String DEBUG = "debug";
	public static String ERROR = "error";

	public static void log( String tag, String level, String message ){
		System.out.println( "[" + System.currentTimeMillis() + "]" +  format( tag, level, message) );
	}

	public static String format( String tag, String level, String message ){
		return "[" + tag + "][" + level + "] " + message;
	}
}