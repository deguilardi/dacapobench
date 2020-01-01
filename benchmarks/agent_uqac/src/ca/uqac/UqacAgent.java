package ca.uqac;

import java.lang.ClassLoader;
import java.lang.instrument.Instrumentation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.List;

import ca.uqac.transformers.*;

class UqacAgent{

	private static String TAG = "UqacAgent";
	private static List<Class> loadedClasses = new ArrayList<Class>();

	public static void premain( String agentArgs, Instrumentation inst ){
	    Logger.log( TAG, Logger.INFO, "premain method called" );

	    // extractClasses( inst );
	    registerTransformer( inst );
	    // transformLoadedClasses();

	    // String className = "Harness";
	    // transformClass( className, inst );
	}

	// private static void extractClasses( Instrumentation inst ){
	// 	Class[] classes = inst.getAllLoadedClasses();
	//     for( Class c : classes ){
	//         if( inst.isModifiableClass(c) && inst.isRetransformClassesSupported() ){
	//             loadedClasses.add(c);
	//         }
	//     }

	//     Logger.log( TAG, Logger.INFO, "Found " + loadedClasses.size() + " classes to be transformed" );
	// }

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