package net.prominic.groovyls.config;

import java.io.File;
import java.io.Reader;
import java.util.List;

import org.apache.groovy.parser.antlr4.Antlr4ParserPlugin;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.PackageNode;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.ParserException;
import org.codehaus.groovy.syntax.Reduction;

public class PackageInjectingParserPlugin implements ParserPlugin {
    private final ParserPlugin antlrParser = new Antlr4ParserPlugin();

    @Override
    public Reduction parseCST(SourceUnit sourceUnit, Reader reader) {
        return antlrParser.parseCST(sourceUnit, reader);
    }

    @Override
    public ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) {
        // Build the AST first
        ModuleNode moduleNode = null;
        try {
            moduleNode = antlrParser.buildAST(sourceUnit, classLoader, cst);

            if (moduleNode != null) {
            
            String packageName = moduleNode.getContext().getName();
            packageName = packageName.replaceAll(Character.toString(File.separatorChar), ".");
            packageName = packageName.replaceAll("\\.groovy$", "");
            packageName = packageName.replaceAll("[^A-Za-z0-9_.]+", "_");
            packageName = packageName.replaceAll("^[^A-Za-z0-9_]+", "");
            
            // Set the package
            if (moduleNode.getPackage() == null) {
                PackageNode packageNode = new PackageNode(packageName);
                moduleNode.setPackage(packageNode);
            }
            
            // Additionally, rename all classes in the module to ensure uniqueness
            List<ClassNode> classes = moduleNode.getClasses();
            for (ClassNode classNode : classes) {
                String originalName = classNode.getName();
                // Keep only the class name part (without package)
                String simpleName = originalName.contains(".") ? 
                    originalName.substring(originalName.lastIndexOf('.') + 1) : originalName;
                
                // Create a new unique name with our random package and the original class name with a suffix
                String newName = packageName + "." + simpleName;
                classNode.setName(newName);
            }
        }
        } catch (ParserException ex) {
        }
        
        
        
        return moduleNode;
    }
}