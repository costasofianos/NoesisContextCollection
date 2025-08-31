package org.noesis.codeanalysis.dataobjects.semanticpsi;

import org.jetbrains.kotlin.com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.kotlin.psi.*;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeContextEncapsulatorNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.file.KotlinSemanticFileImportsPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPsiTypeReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;
import org.noesis.codeanalysis.util.constants.KotlinContextCollectorConstants;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementReferenceUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KotlinSemanticPsiTreeFile extends KotlinSemanticPsiTreeChildNode<KtFile> implements KotlinSemanticPsiTreeContextEncapsulatorNode {

    private final KtFile ktFile;
    private final List<KotlinSemanticPsiTreeClassOrObject> kotlinSemanticPsiTreeClassOrObjects = new ArrayList<>();
    private final List<KotlinSemanticPsiTreeTopLevelFunction> kotlinSemanticPsiTreeTopLevelFunctions = new ArrayList<>();
    private final List<KotlinSemanticPsiTreeVariable> kotlinSemanticPsiTreeVariables = new ArrayList<>();

    private KotlinSemanticFileImportsPart kotlinSemanticFileImportsPart;
    private KotlinSemanticFileImportsPart kotlinSemanticFileAllPart;

//    private static final String FILE_SEP_SYMBOL = "<|file_sep|>";
//    private static final String FILE_COMPOSE_FORMAT = "%s%s\n%s";

    public KotlinSemanticPsiTreeFile(KtFile ktFile, KotlinSemanticPsiTreePackage parent) {
        super(ktFile, parent);
        this.ktFile = ktFile;


        // Create KotlinSemanticPsiTreeClassOrObject instances for all top-level classes and objects
        ktFile.getDeclarations().forEach(declaration -> {
            if (declaration instanceof KtClassOrObject classOrObject) {
                KotlinSemanticPsiTreeClassOrObject kotlinSemanticPsiTreeClassOrObject =
                        new KotlinSemanticPsiTreeClassOrObject(classOrObject, this);
                addClass(kotlinSemanticPsiTreeClassOrObject);
            } else if (declaration instanceof KtNamedFunction function) {
                KotlinSemanticPsiTreeTopLevelFunction kotlinSemanticPsiTreeTopLevelFunction =
                        new KotlinSemanticPsiTreeTopLevelFunction(function, this);
                addFunction(kotlinSemanticPsiTreeTopLevelFunction);
            } else if (declaration instanceof KtVariableDeclaration variable) {
                KotlinSemanticPsiTreeVariable kotlinSemanticPsiTreeVariable =
                        new KotlinSemanticPsiTreeVariable(variable, this);
                addVariable(kotlinSemanticPsiTreeVariable);
            }
        });
        initializeParts();
    }

    protected void initializeParts() {
        kotlinSemanticFileImportsPart = new KotlinSemanticFileImportsPart(this);
        kotlinSemanticFileAllPart = new KotlinSemanticFileImportsPart(this);

        for (KtImportDirective importDirective : getImportDirectives()) {
            //System.out.println("Checking import directive has a value");
            if (importDirective.getImportPath() == null) {
                continue;
            }
            String importPath = importDirective.getImportPath().getPathStr();

            // Skip star imports
            if (importPath.endsWith(".*")) {
                continue;
            }

            // Get the last segment after the last dot
            String importedName = importPath.substring(importPath.lastIndexOf('.') + 1);

            kotlinSemanticFileImportsPart.getSemanticReferences().add(
                    new KotlinSemanticPsiTypeReference(
                            importedName,
                            importDirective,
                            kotlinSemanticFileImportsPart
                    ) {}
            );
            KotlinPsiElementReferenceUtil.collectReferencesFromPsiElement(ktFile, kotlinSemanticFileAllPart);

        }
    }


    public void addClass(KotlinSemanticPsiTreeClassOrObject kotlinSemanticPsiTreeClassOrObject) {
        kotlinSemanticPsiTreeClassOrObjects.add(kotlinSemanticPsiTreeClassOrObject);
    }

    public void addFunction(KotlinSemanticPsiTreeTopLevelFunction kotlinSemanticPsiTreeTopLevelFunction) {
        kotlinSemanticPsiTreeTopLevelFunctions.add(kotlinSemanticPsiTreeTopLevelFunction);
    }

    public void addVariable(KotlinSemanticPsiTreeVariable kotlinSemanticPsiTreeVariable) {
        kotlinSemanticPsiTreeVariables.add(kotlinSemanticPsiTreeVariable);
    }

//    public List<KotlinSemanticPsiTreeClassOrObject> getKotlinSemanticPsiTreeClassOrObjects() {
//        return kotlinSemanticPsiTreeClassOrObjects;
//    }

    @Override
    public List<KotlinSemanticPsiTreeChildNode<?>> getChildren() {
        List<KotlinSemanticPsiTreeChildNode<?>> children = new ArrayList<>();
        children.addAll(kotlinSemanticPsiTreeClassOrObjects);
        children.addAll(kotlinSemanticPsiTreeTopLevelFunctions);
        children.addAll(kotlinSemanticPsiTreeVariables);
        return children;
    }

    public String getFullyQualifiedName() {
        return KotlinPsiTreeUtil.getFullyQualifiedFileName(ktFile);
    }

    public KtPackageDirective getPackageDirective() {
        KtPackageDirective packageDirective = PsiTreeUtil.findChildOfType(getPsiElement(), KtPackageDirective.class);
        return packageDirective;
    }

    public List<KtImportDirective> getImportDirectives() {
        List<KtImportDirective> importDirectives = new ArrayList<KtImportDirective>(PsiTreeUtil.findChildrenOfType(getPsiElement(), KtImportDirective.class));
        return importDirectives;
    }

    @Override
    public ElementVisibility getElementVisibility() {
        return ElementVisibility.PUBLIC;
    }

    @Override
    public String getTypeName() {
        return getSimpleFileName();
    }

    @Override
    public List<KtTypeReference> getSignatureReferencedTypes() {
        return List.of();
    }

    @Override
    public List<KtTypeReference> getBodyReferencedTypes() {
        return List.of();
    }

    @Override
    public List<String> getReferencedNames() {
        return List.of();
    }

    @Override
    public String getPublicContext() {
        return encapsulateContextWithParentNodes(getPublicContextForParent(), false);
    }

    @Override
    public String getFullTextOfElement() {
        //System.out.println("Printing full text of file without imports: " + getTypeName());
        String text = getPsiElement().getText().trim();
        return encapsulateContextWithParentNodes(
                text.lines()
                        .filter(line -> !line.trim().startsWith("import "))
                        .filter(line -> !line.trim().startsWith("package "))
                        .collect(Collectors.joining("\n")),
                true
        );
    }


    @Override
    public String getPublicContextForParent() {



        StringBuilder context = new StringBuilder();


//        // Add package directive if present
//        KtPackageDirective packageDirective = getPackageDirective();
//        if (packageDirective != null && !packageDirective.getQualifiedName().isEmpty()) {
//            context.append("package ").append(packageDirective.getQualifiedName()).append("\n\n");
//        }

//        // Add imports
//        List<KtImportDirective> imports = getImportDirectives();
//        if (!imports.isEmpty()) {
//            imports.forEach(importDirective ->
//                    context.append("import ").append(importDirective.getImportPath().getPathStr()).append("\n")
//            );
//            context.append("\n");
//        }

        // Add classes and objects
        if (!kotlinSemanticPsiTreeClassOrObjects.isEmpty()) {
            kotlinSemanticPsiTreeClassOrObjects.stream()
                    .filter(classObj -> classObj.getElementVisibility() == ElementVisibility.PUBLIC)
                    .map(classObj -> classObj.getPublicContextForParent())
                    .forEach(s -> context.append(s).append("\n\n"));
        }

        // Add top-level functions
        if (!kotlinSemanticPsiTreeTopLevelFunctions.isEmpty()) {
            kotlinSemanticPsiTreeTopLevelFunctions.stream()
                    .filter(func -> func.getElementVisibility() == ElementVisibility.PUBLIC)
                    .map(func -> func.getPublicContextForParent())
                    .forEach(s -> context.append(s).append("\n\n"));
        }

        // Add top-level variables
        if (!kotlinSemanticPsiTreeVariables.isEmpty()) {
            kotlinSemanticPsiTreeVariables.stream()
                    .filter(var -> var.getElementVisibility() == ElementVisibility.PUBLIC)
                    .map(var -> var.getPublicContextForParent())
                    .forEach(s -> context.append(s).append("\n"));
        }

        return context.toString();
    }




    @Override
    public String encapsulateChildContext(String context) {

        if (!context.contains(KotlinContextCollectorConstants.FILE_SEP_SYMBOL)) {
            context = String.format(KotlinContextCollectorConstants.FILE_COMPOSE_FORMAT, KotlinContextCollectorConstants.FILE_SEP_SYMBOL, getRelativePathOfContainingFile(), context);
        }
        return context;
    }

    @Override
    public List<KotlinSemanticPart<?>> getSemanticParts() {
        return List.of(kotlinSemanticFileImportsPart, kotlinSemanticFileAllPart);
    }

    @Override
    public KotlinSemanticReference getSemanticReference() {
        return null;
    }

    public String getSimpleFileName() {
        return KotlinPsiElementUtil.getSimpleNameOfContainingFile(getPsiElement());
    }
}