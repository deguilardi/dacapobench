package ca.uqac;

import java.lang.instrument.Instrumentation;
import java.io.IOException;
import java.util.jar.JarFile;

class UqacAgent{

	private static String TAG = "UqacAgent";

	public static void premain( String agentArgs, Instrumentation inst ){
		Logger.log( TAG, Logger.INFO, "premain method called" );
		Logger.log( TAG, Logger.INFO, "tracing, the defined classes will be traced" );
		registerTransformer( inst );
	}

	private static void registerTransformer( Instrumentation inst ){
		try{
			inst.appendToSystemClassLoaderSearch( new JarFile( "uqacAgent.jar" ) );
		}
		catch( IOException e ){
			Logger.log( TAG, Logger.ERROR, e.getMessage() );
		}
		Transformer transformer = new Transformer();
	    inst.addTransformer( transformer, true );
	}
}