package eu.hats_project.build.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mtvl.parser.Main;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

/**
 * @author pwong
 */
abstract class MTVLParser {
    
    private static final Predicate<String> LEGAL_ABS = 
        new Predicate<String>() {
        public boolean apply(String input) {
            return input != null && ! input.endsWith(".mtvl");
        }
    };

    protected List<String> parseMTVL( File mTVL,
            List<String> absArguments, 
            String productName,
            boolean verbose,
            boolean checkProductSelection,
            Log log) throws MojoExecutionException {
        
        try {
            if (productName != null && checkProductSelection) {
                parseMTVL(mTVL, absArguments, productName, verbose, false, true, false, log);
            } else {
                parseMTVL(mTVL, absArguments, null, verbose, false, false, true, log);
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Could not parse mTVL model", e);
        }
        
        return new ArrayList<String>(Collections2.filter(absArguments,LEGAL_ABS));
    }
    
    private void parseMTVL(
            File mTVL,
            List<String> absArguments, 
            String productName,
            boolean verbose,
            boolean solve,
            boolean satifiability,
            boolean solutions,
            Log log) throws Exception {
        
        if (productName == null && satifiability) {
            throw new MojoExecutionException("Cannot check satifiability " +
            		"without specifying a product name");
        }
        
        List<String> args = new ArrayList<String>();
        String prop = System.getProperty("java.class.path");
        if (prop == null)
            System.setProperty("java.class.path",mTVL.getAbsolutePath());
        else 
            System.setProperty("java.class.path",prop+":"+mTVL.getAbsolutePath());

        if (verbose) {
            args.add("-v");
        }
        
        if (solve) {
            args.add("-s");
        }
        
        if (satifiability) {
            args.add("-c");
        }
        
        if (solutions) {
            args.add("-n");
            args.add("-a"); // not sure what happens if attributes have infinite domain
        }
        
        if (productName != null) {
            args.add(productName);
        }
        
        args.addAll(absArguments);
        String[] argArray = args.toArray(new String[args.size()]);
        new DebugArgOutput().debug("Parsing MTVL File", argArray, log);
        
        Main.main(argArray);
        
    }

}