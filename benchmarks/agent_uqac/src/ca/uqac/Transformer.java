package ca.uqac;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.ClassLoader;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javassist.bytecode.AttributeInfo;
import javassist.bytecode.MethodInfo;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtBehavior;
import javassist.Modifier;
import javassist.NotFoundException;

public class Transformer implements ClassFileTransformer {
    private static String TAG = "Transformer";
    private static String OUTFILE_CLASSES = "./output-class-list.txt";
    private static String INFILE_CLASSES = "./input-class-list.txt";
    
    private BufferedWriter writerClasses;
    private boolean canWriteFileSystem = false;
    private static HashSet<String> includedClassesHash = new HashSet<String>();

    public Transformer() {
        try {
            writerClasses = new BufferedWriter(new FileWriter(OUTFILE_CLASSES));
            canWriteFileSystem = true;
        } catch (IOException e) {
            Logger.log(Logger.ERROR, TAG, "Could not create output file. " + e.getMessage());
        }

        // read classes from input file
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(INFILE_CLASSES));
            String line = bufferedReader.readLine();
            while (null != line) {
                includedClassesHash.add(line);
                line = bufferedReader.readLine();
            }
            bufferedReader.close();
        }
        catch (IOException e) {
            Logger.log(Logger.ERROR, TAG, "Could not load input file. " + e.getMessage());
        }
    }

	private void writeOutput(BufferedWriter writer, String message){
        if(canWriteFileSystem){
            try{
                writer.write( message + "\n" );
            }
            catch(IOException e){
                Logger.log(Logger.ERROR, TAG, "Could not write on output file. " + e.getMessage());
            }
        }
	}

    @Override
    public byte[] transform( 
            ClassLoader loader, 
            String className, 
            Class<?> classBeingRedefined, 
            ProtectionDomain protectionDomain, 
            byte[] classfileBuffer ){

        // Logger.log( TAG, Logger.DEBUG, className );
        writeOutput(writerClasses, className);

        if( !includedClassesHash.contains( className ) ){
            return classfileBuffer;
        }
        Logger.log( TAG, Logger.DEBUG, "transforming class: " + className );        
        CtClass cl = null;
        try{
            ClassPool pool = ClassPool.getDefault();
            cl = pool.makeClass( new java.io.ByteArrayInputStream( classfileBuffer ) );
            if(cl.isEnum()){
                Logger.log( TAG, Logger.ERROR, "skipping class: " + className + ". reason: isEnum");
            }
            else{
                CtBehavior[] methods = cl.getDeclaredBehaviors();
                for( CtBehavior method : methods ){
                    transformMethod( method, className );
                }
                classfileBuffer = cl.toBytecode();
            }
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
        // Logger.log( TAG, Logger.DEBUG, "transforming method: " + className + "::" + method.getName() );
        if( method.getName().equals("<clinit>")){
            Logger.log( TAG, Logger.DEBUG, "skipping method: " + className + "::" + method.getName() );
            return;
        }
        int modifiers = method.getModifiers();

        String methodName = method.getName();

        // if( 
        //     // methodName.equals("TeeOutputStream") ||
        //     // methodName.equals("enableOutput") ||
        //     // methodName.equals("openLog") ||
        //     // methodName.equals("closeLog") ||
        //     // methodName.equals("close") ||
        //     // methodName.equals("flush") ||
        //     methodName.equals("write") 
        //     // methodName.equals("version") ||
        //     // methodName.equals("TeeOutputStream")
        //     ){
        
        

        if(    !method.isEmpty()
    	    && !Modifier.isNative( modifiers ) 
    	    && !Modifier.isEnum( modifiers ) 
    	    && !Modifier.isInterface( modifiers ) 
        //     &&  Modifier.isPublic( modifiers )
        ){
            Logger.log( TAG, Logger.DEBUG, "transforming method: " + className + "::" + method.getName() );
            // log the method is being called
            String message = Logger.format( "call", Logger.INFO, className + "." + method.getName() + "()" );
            String insertString = "System.out.println( \"[\" + System.currentTimeMillis() + \"]" + message + "\");";
            method.insertBefore( insertString );
        }
    // }
    }
}