package ca.uqac;

import java.lang.instrument.Instrumentation;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.List;

class UqacAgent{

	private static String TAG = "UqacAgent";
	private static String ARG_EXAMINE = "examine";
	private static String ARG_TRACE = "trace";
	private static String FILE_CLASSES = "./classesList.txt";

	private static List<Class> loadedClasses = new ArrayList<Class>();

	public static void premain( String agentArgs, Instrumentation inst ){
		Logger.log( TAG, Logger.INFO, "premain method called" );
		

		if( agentArgs == null || agentArgs.equals( "" ) ){
			Logger.log( TAG, Logger.INFO, "no argument, doing nothing" );
		}
		else if( agentArgs.equals( ARG_EXAMINE ) ){
			Logger.log( TAG, Logger.INFO, "examining, a classes list will be extracted" );
			try{
				examineClasses( inst );
			}
			catch(IOException e){
				Logger.log( TAG, Logger.ERROR, e.getMessage() );
			}
		}
		else if( agentArgs.equals( ARG_TRACE ) ){
			Logger.log( TAG, Logger.INFO, "tracing, the defined classes will be traced" );
			registerTransformer( inst );
		}
		else{
			Logger.log( TAG, Logger.ERROR, "wrong argument, 'examine' and 'trace' are valid arguments" );
		}

	    // transformLoadedClasses();
	    // String className = "Harness";
	    // transformClass( className, inst );
	}

	private static void examineClasses( Instrumentation inst ) throws IOException {
		BufferedWriter writer = new BufferedWriter( new FileWriter( FILE_CLASSES ) );
		Class[] classes = inst.getAllLoadedClasses();
	    for( Class c : classes ){
	        if( inst.isModifiableClass(c) && inst.isRetransformClassesSupported() && !c.getName().startsWith( "ca.uqac" ) ){
	            loadedClasses.add(c);
				Logger.log( TAG, Logger.DEBUG, c.getName() );
				writer.write( c.getName() + "\n" );
	        }
		}
		writer.close();
	    Logger.log( TAG, Logger.INFO, "Found " + loadedClasses.size() + " classes that could be transformed" );
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


	    // try {
	    //     inst.retransformClasses( clazz );
	    // }
	    // catch( Exception e ){
	    //     throw new RuntimeException(
	    //       "Transform failed for: [" + clazz.getName() + "]", e);
	    // }
	}

	// private static void transformLoadedClasses(){
	// 	for( Class c : loadedClasses ){
	// 		String className = c.getName();
	// 	}
	// }

	// private static void transformClass( String className, Instrumentation inst ){
	// 	Class<?> targetClass = null;
 //    	ClassLoader targetClassLoader = null;
 //    	try{
	//         targetClass = Class.forName( className );
	//         targetClassLoader = targetClass.getClassLoader();
	//         transformHarness( targetClass, targetClassLoader, inst );
	//         return;
	//     }
	//     catch( Exception e ){
	//         log( "Class ["+className+"] not found with Class.forName" );
	//     }
	// }

	// private static void transformHarness( Class<?> clazz, ClassLoader classLoader, Instrumentation inst ){
	// 	log( "transform method" );

	// 	HarnessTransformer transformer = new HarnessTransformer( clazz.getName(), classLoader );
	//     inst.addTransformer( transformer, true );
	//     try {
	//         inst.retransformClasses(clazz);
	//     }
	//     catch( Exception e ){
	//         throw new RuntimeException(
	//           "Transform failed for: [" + clazz.getName() + "]", e);
	//     }
	// }
}