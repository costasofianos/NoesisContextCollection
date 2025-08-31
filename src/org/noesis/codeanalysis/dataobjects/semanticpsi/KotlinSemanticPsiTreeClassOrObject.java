package org.noesis.codeanalysis.dataobjects.semanticpsi;

import org.jetbrains.kotlin.lexer.KtTokens;
import org.jetbrains.kotlin.psi.*;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeContextEncapsulatorNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPsiTypeReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeUtil;
import org.noesis.codeanalysis.util.kotlin.psi.formatters.KotlinPsiTreeFormatUtil;

import java.util.*;
import java.util.stream.Collectors;

public class KotlinSemanticPsiTreeClassOrObject extends KotlinSemanticPsiTreeChildNode<KtClassOrObject> implements KotlinSemanticPsiTreeContextEncapsulatorNode {

    private final KtClassOrObject ktClassOrObject;
    private List<KotlinSemanticPsiTreeClassOrObject> nestedSemanticClassesOrObjects;
    private List<KotlinSemanticPsiTreeVariable> variables;
    private List<KotlinSemanticPsiTreeConstructor> constructors = new ArrayList<>();
    private List<KotlinSemanticPsiTreeMemberFunction> kotlinSemanticPsiTreeMemberFunctions = new ArrayList<>();
    private KotlinSemanticPsiTreeClassOrObject parentSemanticClassOrObject;

    public KotlinSemanticPsiTreeClassOrObject(KtClassOrObject ktClassOrObject, KotlinSemanticPsiTreeNode parent) {
        super(ktClassOrObject, parent);

        this.ktClassOrObject = ktClassOrObject;
        initialise();

        // Create child nodes for properties
        ktClassOrObject.getDeclarations().stream()
                .filter(declaration -> declaration instanceof KtProperty)
                .map(declaration -> (KtProperty) declaration)
                .forEach(property -> {
                    KotlinSemanticPsiTreeVariable variable = new KotlinSemanticPsiTreeVariable(property, this);
                    addVariable(variable);
                });

        // Create child nodes for constructors
        ktClassOrObject.getDeclarations().stream()
                .filter(declaration -> declaration instanceof KtConstructor<?>)
                .map(declaration -> (KtConstructor<?>) declaration)
                .forEach(constructor -> {
                    KotlinSemanticPsiTreeConstructor kotlinConstructor = new KotlinSemanticPsiTreeConstructor(constructor, this);
                    addConstructor(kotlinConstructor);
                });


        // Create child nodes for functions
        ktClassOrObject.getDeclarations().stream()
                .filter(declaration -> declaration instanceof KtNamedFunction)
                .map(declaration -> (KtNamedFunction) declaration)
                .forEach(function -> {
                    KotlinSemanticPsiTreeMemberFunction kotlinSemanticPsiTreeMemberFunction = new KotlinSemanticPsiTreeMemberFunction(function, this);
                    addFunction(kotlinSemanticPsiTreeMemberFunction);
                });

        // Create child nodes for nested classes and objects
        ktClassOrObject.getDeclarations().stream()
                .filter(declaration -> declaration instanceof KtClassOrObject && !(declaration instanceof KtEnumEntry))
                .map(declaration -> (KtClassOrObject) declaration)
                .forEach(classOrObject -> {
                    KotlinSemanticPsiTreeClassOrObject nestedClass = new KotlinSemanticPsiTreeClassOrObject(classOrObject, this);
                    addNestedClassOrObject(nestedClass);
                });
    }



    @Override
    public KotlinSemanticReference getSemanticReference() {
        return new KotlinSemanticPsiTypeReference(getTypeName(), getPsiElement(), getSelfPart());
    }

    @Override
    public List<KotlinSemanticPart<?>> getSemanticParts() {
        return List.of();
    }

    @Override
    public Set<String> getBodyReferences() {
        return Set.of();
    }


    @Override
    public String getTypeName() {
        return ktClassOrObject.getName() != null ? ktClassOrObject.getName() : "<Anonymous>";
    }

    @Override
    public List<KtTypeReference> getSignatureReferencedTypes() {
        List<KtTypeReference> types = new ArrayList<>();

        // Add supertype references (both parent class and interfaces)
        ktClassOrObject.getSuperTypeListEntries().forEach(entry -> {
            KtTypeReference typeReference = entry.getTypeReference();
            if (typeReference != null) {
                types.add(typeReference);
            }
        });

        // Add type parameters if the class is generic
        ktClassOrObject.getTypeParameters().forEach(ktTypeParameter ->
                types.addAll(KotlinPsiTreeUtil.getEligibleReferencedTypes(ktTypeParameter))
        );

        return types;
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
    public ElementVisibility getElementVisibility() {
        return KotlinPsiElementUtil.getElementVisibility(ktClassOrObject);
    }

    public String getHeader() {
        return KotlinPsiTreeFormatUtil.getKtClassOrObjectHeader(ktClassOrObject);
    }

    public void addFunction(KotlinSemanticPsiTreeMemberFunction kotlinSemanticPsiTreeMemberFunction) {
        kotlinSemanticPsiTreeMemberFunctions.add(kotlinSemanticPsiTreeMemberFunction);
    }

    public List<KotlinSemanticPsiTreeMemberFunction> getKotlinSemanticPsiTreeMemberFunctions() {
        return kotlinSemanticPsiTreeMemberFunctions;
    }

    // For nested classes/objects
    public void addNestedClassOrObject(KotlinSemanticPsiTreeClassOrObject nestedClass) {
        if (nestedSemanticClassesOrObjects == null) {
            nestedSemanticClassesOrObjects = new ArrayList<>();
        }
        nestedSemanticClassesOrObjects.add(nestedClass);
    }

    public List<KotlinSemanticPsiTreeClassOrObject> getNestedSemanticClassesOrObjects() {
        return nestedSemanticClassesOrObjects != null ? nestedSemanticClassesOrObjects : new ArrayList<>();
    }

    // For variables
    public void addVariable(KotlinSemanticPsiTreeVariable variable) {
        if (variables == null) {
            variables = new ArrayList<>();
        }
        variables.add(variable);
    }

    public List<KotlinSemanticPsiTreeVariable> getVariables() {
        return variables != null ? variables : new ArrayList<>();
    }

    public void addConstructor(KotlinSemanticPsiTreeConstructor constructor) {
        constructors.add(constructor);
    }

    public List<KotlinSemanticPsiTreeConstructor> getConstructors() {
        return constructors;
    }




    @Override
    public List<KotlinSemanticPsiTreeChildNode<?>> getChildren() {
        List<KotlinSemanticPsiTreeChildNode<?>> allChildren = new ArrayList<>();
        if (nestedSemanticClassesOrObjects != null) {
            allChildren.addAll(nestedSemanticClassesOrObjects);
        }
        allChildren.addAll(constructors);
        allChildren.addAll(kotlinSemanticPsiTreeMemberFunctions);
        if (variables != null) {
            allChildren.addAll(variables);
        }
        return allChildren;
    }

    @Override
    public String getPublicContext() {
        return encapsulateContextWithParentNodes(getPublicContextForParent(), false);
    }

    @Override
    public String getPublicContextForParent() {
        return getContextForParent(ElementVisibility.PUBLIC);
    }

    public String getContextForParent(ElementVisibility... allowedVisibilities) {
        List<ElementVisibility> visibilityList = Arrays.asList(allowedVisibilities);

        // Add package name at the beginning
//        context.append("// package\n");
//        context.append("package ").append(getPackageName()).append("\n\n");
        
        // Add header (class declaration) with opening brace


        if (ktClassOrObject.hasModifier(KtTokens.ENUM_KEYWORD)
        ) {
            return getPsiElement().getText();
        } else {



            StringBuilder context = new StringBuilder();


            // Only add section headers if there are items in that section
            if (!getVariables().isEmpty()) {
                //context.append("\n    // variables\n");
                getVariables().stream()
                        .filter(variable -> visibilityList.contains(variable.getElementVisibility()))
                        .map(variable -> "    " + variable.getPublicContextForParent())
                        .forEach(s -> context.append(s).append("\n"));
            }

            // Constructors
            if (!getConstructors().isEmpty()) {
                //context.append("\n    // constructors\n");
                getConstructors().stream()
                        .filter(constructor -> visibilityList.contains(constructor.getElementVisibility()))
                        .map(constructor -> "    " + constructor.getPublicContextForParent() + " {...}")
                        .forEach(s -> context.append(s).append("\n"));
            }


            //System.out.println("Adding member functions for "+this.getTypeName());
            // Functions
            if (!getKotlinSemanticPsiTreeMemberFunctions().isEmpty()) {

                //context.append("\n    // functions\n");
                getKotlinSemanticPsiTreeMemberFunctions().stream()
                        .filter(function -> visibilityList.contains(function.getElementVisibility()))
                        .map(function -> "    " + function.getPublicContextForParent())
                        .forEach(s -> context.append(s).append("\n"));
            }

            //System.out.println("Adding nested classes for "+this.getTypeName());
            // Nested classes
            if (!getNestedSemanticClassesOrObjects().isEmpty()) {

                //context.append("\n    // nested classes\n");
                getNestedSemanticClassesOrObjects().stream()
                        //.peek(c -> System.out.println("Adding nested class "+c.getTypeName()))
                        .filter(nestedClass -> visibilityList.contains(nestedClass.getElementVisibility()))
                        .map(nestedClass -> "    " + nestedClass.getPublicContextForParent().replace("\n", "\n    "))
                        .forEach(s -> context.append(s).append("\n"));
            }

            if (context.length() > 0) {
                return getHeader() +" {\n"+context.toString()+"\n}";
            } else {
                return "";
            }

        }
    }

    public String getPackageName() {
        return ktClassOrObject.getContainingKtFile().getPackageFqName().asString();
    }


    @Override
    public String encapsulateChildContext(String context) {

       // if (!getTypeName().equalsIgnoreCase("Companion")) {
            return getHeader() + " {\n"+ context+"\n}";
//        } else {
//            return context;
//        }

    }

    public KotlinSemanticPsiTreeClassOrObject getParentSemanticClassOrObject() {
        return parentSemanticClassOrObject;
    }

    public List<KotlinSemanticPsiTreeClassOrObject> findAllExtendedClasses() {
        List<KotlinSemanticPsiTreeClassOrObject> allSuperTypes = new ArrayList<>();
        allSuperTypes.addAll(findExtendedClasses());

        for (KotlinSemanticPsiTreeClassOrObject superClass : findExtendedClasses()) {
            // System.out.println("Found supertype " + superClass.getTypeName() + " of class " + this.getTypeName());
            allSuperTypes.addAll(superClass.findAllExtendedClasses());
        }

        return allSuperTypes;
    }


    public List<KotlinSemanticPsiTreeClassOrObject> findExtendedClasses() {
        // First check if this class extends any other class
        List<KtTypeReference> supertypes = getSignatureReferencedTypes();
        if (supertypes.isEmpty()) {
            return new ArrayList<>();
        }

        KotlinSemanticPsiTreeRoot root = getRoot();

        // Convert each supertype reference directly to a class lookup
        return supertypes.stream()
                .filter(Objects::nonNull)
                .flatMap(ktTypeReference -> root.findNodesByTypeAndName(
                        KotlinSemanticPsiTreeClassOrObject.class,
                        ktTypeReference.getText()
                ).stream())
                .collect(Collectors.toList());
    }



//    public Optional<KotlinSemanticPsiTreeClassOrObject> findExtendingClass() {
//        // First check if this class extends any other class
//        List<KtTypeReference> supertypes = getSignatureReferencedTypes();
//        if (supertypes.isEmpty()) {
//            // If this class doesn't extend anything, no need to search for classes extending it
//            return Optional.empty();
//        }
//
//        // Get all class/object nodes from root
//        List<KotlinSemanticPsiTreeClassOrObject> allClasses = findNodesOfTypeFromRoot(
//                List.of(KotlinSemanticPsiTreeClassOrObject.class)
//        );
//
//        // Find a class that has this class as a supertype
//        return allClasses.stream()
//                .filter(classOrObject -> {
//                    return classOrObject.ktClassOrObject.getSuperTypeListEntries().stream()
//                            .map(entry -> entry.getTypeReference())
//                            .filter(Objects::nonNull)
//                            .anyMatch(typeRef -> typeRef.getText().equals(getTypeName()));
//                })
//                .findFirst();
//    }
//
//    public List<KotlinSemanticPsiTreeClassOrObject> findAllExtendingClasses() {
//        List<KotlinSemanticPsiTreeClassOrObject> extendingClasses = new ArrayList<>();
//
//        // Start with the current class
//        Optional<KotlinSemanticPsiTreeClassOrObject> currentExtending = findExtendingClass();
//
//        // Keep finding extending classes until we reach a class that isn't extended
//        while (currentExtending.isPresent()) {
//            KotlinSemanticPsiTreeClassOrObject extendingClass = currentExtending.get();
//            System.out.println("Found Extending Class: " + extendingClass.getTypeName() +" of "+this.getTypeName());
//            extendingClasses.add(extendingClass);
//            currentExtending = extendingClass.findExtendingClass();
//        }
//
//        return extendingClasses;
//    }
}