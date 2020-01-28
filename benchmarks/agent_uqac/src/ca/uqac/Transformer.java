package ca.uqac;

import java.io.IOException;
import java.lang.ClassLoader;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtBehavior;
import javassist.Modifier;
import javassist.NotFoundException;

public class Transformer implements ClassFileTransformer{
    private static String TAG = "Transformer";

    private static String[] includedPackages = { 
        // "Harness",
        // "org/dacapo",
        "java/io",
        "avrora/core",
        "avrora/Main",

        "org/apache",
    };
    private static HashSet<String> includedPackagesHash = new HashSet<String>( Arrays.asList( includedPackages ) );

    @Override
    public byte[] transform( ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer ){
        String classPackage = extractPackage( className );
        if( !includedPackagesHash.contains( classPackage ) ){
            return classfileBuffer;
        }

        Logger.log( TAG, Logger.DEBUG, "loading class: " + className );
        CtClass cl = null;
        try{
            ClassPool pool = ClassPool.getDefault();
            cl = pool.makeClass( new java.io.ByteArrayInputStream( classfileBuffer ) );
            CtBehavior[] methods = cl.getDeclaredBehaviors();
            for( CtBehavior method : methods ){
                transformMethod( method, className );
            }
            classfileBuffer = cl.toBytecode();
        }
        catch( NotFoundException | CannotCompileException | IOException e ){
        	Logger.log( TAG, Logger.ERROR, e.getMessage() );
        }
        finally{
            cl.detach();
        }
        return classfileBuffer;
    }

    private void transformMethod( CtBehavior method, String className ) throws NotFoundException, CannotCompileException{
        Logger.log( TAG, Logger.DEBUG, "loading method: " + method.getName() );
        int modifiers = method.getModifiers();
        if(    !method.isEmpty()
    	    && !Modifier.isNative( modifiers ) 
    	    &&  Modifier.isPublic( modifiers )
        ){
            String message = Logger.format( "call", Logger.INFO, className + "::" + method.getName() );
            String insertString = "System.out.println( \"[\" + System.currentTimeMillis() + \"]" + message + "\");";
            method.insertBefore( insertString );
        }
    }

    private String extractPackage( String input ){
        Pattern pattern = Pattern.compile( "([A-Za-z0-9]{1,}.[A-Za-z0-9]{1,})" );
        Matcher matcher = pattern.matcher( input );
        matcher.find();
        return matcher.group( 1 );
    }
}