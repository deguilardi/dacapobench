package ca.uqac;

public abstract class Logger{

	public static String INFO = "info";
	public static String DEBUG = "debug";
	public static String ERROR = "error";

	private static String currentLevel = INFO;

	public static void log( String tag, String level, String message ){
		if( checkLevel( level ) ){
			String output = format( tag, level, message);
			System.out.println( "[" + System.currentTimeMillis() + "]" +  output );
		}
	}

	public static String format( String tag, String level, String message ){
		StringBuffer sb = new StringBuffer();
		sb.append( "[" );
		sb.append( tag );
		sb.append( "][" );
		sb.append( level );
		sb.append( "] " );
		sb.append( message );
		return sb.toString();
	}

	private static boolean checkLevel( String level ){
		if( currentLevel.equals( INFO ) ){
			return true;
		}
		else if( currentLevel.equals( DEBUG ) && !level.equals( INFO ) ){
			return true;
		}
		else if( currentLevel.equals( level ) ){
			return true;
		}
		return false;
	}
}