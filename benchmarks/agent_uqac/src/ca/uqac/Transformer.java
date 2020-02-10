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
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javassist.bytecode.AttributeInfo;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;

public class Transformer implements ClassFileTransformer {
    private static String TAG = "Transformer";
    private static String OUTFILE_CLASSES = "./output-class-list.txt";
    private static String INFILE_CLASSES = "./input-class-list.txt";
    
    private BufferedWriter writerClasses;
    private boolean canWriteFileSystem = false;
    private static HashMap<String, ClassToTrace> includedClassesHash = new HashMap<>();

    private class MethodToTrace{
        String name;
        String[] params;

        MethodToTrace( String methodName){
            name = methodName;
        }

        MethodToTrace( String[] methodInfoSplit ){
            name = methodInfoSplit[0].substring( 0, methodInfoSplit[0].length() - 1 );
            params = Arrays.copyOfRange( methodInfoSplit , 1, methodInfoSplit.length -1 );
        }

        @Override
        public boolean equals( Object obj ){
            if (obj instanceof MethodToTrace){
                return name.equals( ((MethodToTrace)obj).name );
            }
            return false;
        }

        @Override
        public int hashCode(){
            return name.hashCode();
        }

        @Override
        public String toString(){
            return name;
        }
    }

    private class ClassToTrace{
        String name;
        HashMap<String, MethodToTrace> methods = new HashMap<>();

        ClassToTrace( String className ){
            name = className;
        }

        @Override
        public boolean equals( Object obj ){
            if (obj instanceof ClassToTrace){
                return name.equals( ((ClassToTrace)obj).name );
            }
            return false;
        }

        @Override
        public int hashCode(){
            return name.hashCode();
        }

        @Override
        public String toString(){
            return name;
        }
    }

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
            ClassToTrace classToTrace = null;
            while( line != null ){
                if( !line.startsWith( "    " )){
                    String className = line.substring( 0, line.length() - 1 );
                    classToTrace = new ClassToTrace( className );
                    Logger.log( TAG, Logger.INFO, "loading class " + classToTrace );
                    includedClassesHash.put( className, classToTrace );
                }
                else if( line.length() > 5 ){
                    String methodInfo = line.trim();
                    if( methodInfo.endsWith( "()" ) ){
                        String methodName = methodInfo.substring( 0, methodInfo.length() - 2 );
                        MethodToTrace methodToTrace = new MethodToTrace( methodName );
                        classToTrace.methods.put( methodToTrace.name, methodToTrace );
                    }
                    else{
                        String[] methodInfoSplit = methodInfo.split( " " );
                        MethodToTrace methodToTrace = new MethodToTrace( methodInfoSplit );
                        classToTrace.methods.put( methodToTrace.name, methodToTrace );
                    }
                }
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
        writeOutput(writerClasses, className + "{");

        CtClass cl = null;
        CtMethod[] methods = null;
        try{
            ClassPool pool = ClassPool.getDefault();
            cl = pool.makeClass( new java.io.ByteArrayInputStream( classfileBuffer ) );
            methods = cl.getDeclaredMethods();
        }
        catch( IOException e ){
            Logger.log( TAG, Logger.ERROR, e.getMessage() );
        }
        
        ClassToTrace classToTrace = includedClassesHash.get( className );
        if( classToTrace == null ){
            Logger.log( TAG, Logger.INFO, "skipping class: " + className + ". reason: not in input file" );
            justWriteMethodsOnOutput( className, methods );
            writeOutput( writerClasses, "    }" );
            return classfileBuffer;
        }
        else{
            if(cl.isEnum()){
                Logger.log( TAG, Logger.INFO, "skipping class: " + className + ". reason: isEnum");
            }
            else{
                Logger.log( TAG, Logger.DEBUG, "transforming class: " + className );
                transformMethods( classToTrace, className, methods );
                try{
                    classfileBuffer = cl.toBytecode();
                    cl.detach();
                }
                catch( IOException | CannotCompileException e ){
                    Logger.log( TAG, Logger.ERROR, e.getMessage() );
                }
            }
        }
        writeOutput( writerClasses, "    }" );
        return classfileBuffer;
    }

    private void justWriteMethodsOnOutput( String className, CtMethod[] methods ){
        for( CtMethod method : methods ){
            if( method.getName().equals("<clinit>")){
                // Logger.log( TAG, Logger.INFO, "skipping method: " + className + "::" + method.getName() );
                continue;
            }
            writeMethodOnOutput( method );
        }
    }

    private void transformMethods( ClassToTrace classToTrace, String className, CtMethod[] methods ){
        for( CtMethod method : methods ){
            if( method.getName().equals("<clinit>")){
                // Logger.log( TAG, Logger.INFO, "skipping method: " + className + "::" + method.getName() );
                continue;
            }
            try{
                writeMethodOnOutput( method );

                // check if method is in the input list
                MethodToTrace methodToTrace = classToTrace.methods.get( method.getName() );
                if( methodToTrace == null ){
                    Logger.log( TAG, Logger.INFO, "skipping method: " + className + "." + method.getName() + ". reason: not in input file" );
                }
                else{
                    transformMethod( className, method, methodToTrace );
                }
            }
            catch( NotFoundException | CannotCompileException e ){
                Logger.log( TAG, Logger.ERROR, e.getMessage() );
            }
        }
    }

    private void transformMethod( String className, CtMethod method, MethodToTrace methodToTrace ) throws NotFoundException, CannotCompileException{
        int modifiers = method.getModifiers();
        String methodName = method.getName();
        
        if(    !method.isEmpty()
    	    && !Modifier.isNative( modifiers ) 
    	    && !Modifier.isEnum( modifiers ) 
    	    && !Modifier.isInterface( modifiers ) 
        ){
            Logger.log( TAG, Logger.DEBUG, "transforming method: " + className + "::" + methodName );
            StringBuffer sb = new StringBuffer();

            // log the method is being called
            sb.append( "System.out.println( \"[\" + System.currentTimeMillis() + \"]" );
            sb.append( Logger.format( "call", Logger.INFO, className + "." + methodName + "()" ) );
            sb.append( "\");" );

            // log params
            String[] params = methodToTrace.params;
            if( params.length > 0 ){
                for( String param: params ){
                    sb.append( "System.out.println(\"               [call][info][param] name:" );
                    sb.append( param );
                    sb.append( " value:\" + " );
                    sb.append( param );
                    sb.append( ");" );
                }
            }

            method.insertBefore( sb.toString() );
        }
    }

    private String[] getParamNamesForMethod( CtMethod method ){
        MethodInfo methodInfo = method.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        if( codeAttribute == null ){
            Logger.log( TAG, Logger.INFO, "Could not read method params: " + method.getName() );
            return null;
        }
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute( LocalVariableAttribute.tag );
        if( attr == null ){
            Logger.log( TAG, Logger.INFO, "Could not read method params: " + method.getName() );
            return null;
        }
        try{
            String[] paramNames = new String[ method.getParameterTypes().length ];
            int pos = Modifier.isStatic( method.getModifiers() ) ? 0 : 1;
            for( int i = 0; i < paramNames.length; i++ ){
                paramNames[ i ] = attr.variableName( i + pos );
            }
            return paramNames;
        }
        catch( NotFoundException e ){
            Logger.log( TAG, Logger.ERROR, "Could not read method params: " + method.getName() );
            return null;
        }
    }

    private String concatParamNames( StringBuffer sb, String[] paramNames ){
        if( paramNames == null || paramNames.length == 0 ){
            sb.append( "()" );
        }
        else{
            sb.append( "( " );
            for( String paramName: paramNames ){
                sb.append( paramName );
                sb.append( " " );
            }
            sb.append( ")" );
        }
        return sb.toString();
    }

    private void writeMethodOnOutput( CtMethod method ){
        StringBuffer sb = new StringBuffer();
        sb.append( "    " );
        sb.append( method.getName() );
        String[] paramNames = getParamNamesForMethod( method );
        concatParamNames( sb, paramNames );
        writeOutput( writerClasses, sb.toString() );
    }
}