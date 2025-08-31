package org.noesis.codeanalysis.dataobjects.semanticpsi;

import org.jetbrains.kotlin.psi.KtNamedFunction;
import org.jetbrains.kotlin.psi.KtTypeReference;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeContextEncapsulatorNode;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementReferenceUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeUtil;
import org.noesis.codeanalysis.util.kotlin.psi.formatters.KotlinPsiTreeFormatUtil;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.function.KotlinSemanticFunctionBodyPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.function.KotlinSemanticFunctionInputParameterPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.function.KotlinSemanticFunctionReceiverPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.function.KotlinSemanticFunctionReturnTypePart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class KotlinSemanticPsiTreeFunction extends KotlinSemanticPsiTreeChildNode<KtNamedFunction> implements KotlinSemanticPsiTreeContextEncapsulatorNode {

    private final KtNamedFunction ktNamedFunction;
    private  KotlinSemanticFunctionInputParameterPart kotlinSemanticFunctionInputParameterPart;
    private  KotlinSemanticFunctionReturnTypePart kotlinSemanticFunctionReturnTypePart;
    private  KotlinSemanticFunctionReceiverPart kotlinSemanticFunctionReceiverPart;
    private  KotlinSemanticFunctionBodyPart kotlinSemanticFunctionBodyPart;

    public KotlinSemanticPsiTreeFunction(KtNamedFunction ktNamedFunction, KotlinSemanticPsiTreeChildNode<?> parent) {
        super(ktNamedFunction, parent);
        this.ktNamedFunction = ktNamedFunction;
        initializeParts();
    }

    protected void initializeParts() {
        KtNamedFunction ktNamedFunction = (KtNamedFunction) getPsiElement();
       // System.out.println("\nInitializing parts for function: " + ktNamedFunction.getName());

        kotlinSemanticFunctionInputParameterPart = new KotlinSemanticFunctionInputParameterPart(

                this
        );

        kotlinSemanticFunctionReturnTypePart = new KotlinSemanticFunctionReturnTypePart(

                this
        );

        kotlinSemanticFunctionReceiverPart = new KotlinSemanticFunctionReceiverPart(
                this
        );

        kotlinSemanticFunctionBodyPart = new KotlinSemanticFunctionBodyPart(
                this
        );

//        if (ktNamedFunction.getContainingKtFile().getName().equals("FlowExt.kt")) {
//        System.out.println("*************************************************************");
//        System.out.println("COLLECTING REFERENCES FOR FUNCTION: " + ktNamedFunction.getName() +" IN FILE "+ ktNamedFunction.getContainingKtFile().getName() );
//        System.out.println("*************************************************************");
//        System.out.println("#########################################");
//        System.out.println("Collecting References from Parameters");
//        System.out.println("#########################################");
//        }
        // Now collect references with the parts available and print counts and details
        KotlinPsiElementReferenceUtil.collectReferencesFromParameters(ktNamedFunction.getValueParameters(), kotlinSemanticFunctionInputParameterPart);
        //printPartReferences("Parameter", kotlinSemanticFunctionInputParameterPart);

//        if (ktNamedFunction.getContainingKtFile().getName().equals("FlowExt.kt")) {
//            System.out.println("#########################################");
//            System.out.println("Collecting References from Returned Type");
//            System.out.println("#########################################");
//        }
        KotlinPsiElementReferenceUtil.collectReferencesFromPsiElement(ktNamedFunction.getTypeReference(), kotlinSemanticFunctionReturnTypePart);
        //printPartReferences("Return type", kotlinSemanticFunctionReturnTypePart);

//        if (ktNamedFunction.getContainingKtFile().getName().equals("FlowExt.kt")) {
//            System.out.println("#########################################");
//            System.out.println("Collecting References from Receiver Type");
//            System.out.println("#########################################");
//        }
        KotlinPsiElementReferenceUtil.collectReferencesFromPsiElement(ktNamedFunction.getReceiverTypeReference(), kotlinSemanticFunctionReceiverPart);
        //printPartReferences("Receiver type", kotlinSemanticFunctionReceiverPart);

//        if (ktNamedFunction.getContainingKtFile().getName().equals("FlowExt.kt")) {
//            System.out.println("#########################################");
//            System.out.println("Collecting References from Body");
//            System.out.println("#########################################");
//        }
        KotlinPsiElementReferenceUtil.collectReferencesFromPsiElement(ktNamedFunction.getBodyExpression(), kotlinSemanticFunctionBodyPart);
//        if (ktNamedFunction.getContainingKtFile().getName().equals("FlowExt.kt")) {
//            printPartReferences("Body", kotlinSemanticFunctionBodyPart);
//        }

//        if (ktNamedFunction.getContainingKtFile().getName().equals("FlowExt.kt")) {
//            System.out.println("*************************************************************");
//        }
    }

    private void printPartReferences(String partName, KotlinSemanticPart<?> part) {
        System.out.println("\n" + partName + " references: " + part.getSemanticReferences().size());
        System.out.println(partName + " reference details: " +
            part.getSemanticReferences().stream()
                .map(ref -> ref.getReferenceName() + " (" + ref.getClass().getSimpleName() + ")")
                .collect(Collectors.joining(", ")));
    }


    @Override
    public List<KotlinSemanticPart<?>> getSemanticParts() {
        List<KotlinSemanticPart<?>> parts = new ArrayList<>();

        // Add all function parts
        if (kotlinSemanticFunctionInputParameterPart != null) {
            parts.add(kotlinSemanticFunctionInputParameterPart);
        }
        if (kotlinSemanticFunctionReturnTypePart != null) {
            parts.add(kotlinSemanticFunctionReturnTypePart);
        }
        if (kotlinSemanticFunctionReceiverPart != null) {
            parts.add(kotlinSemanticFunctionReceiverPart);
        }
        if (kotlinSemanticFunctionBodyPart != null) {
            parts.add(kotlinSemanticFunctionBodyPart);
        }

        return parts;
    }


    public KtNamedFunction getPsiElement() {
        return ktNamedFunction;
    }

    @Override
    public ElementVisibility getElementVisibility() {
        return KotlinPsiElementUtil.getElementVisibility(ktNamedFunction);
    }

    @Override
    public String getTypeName() {
        return ktNamedFunction.getName();
    }




    @Override
    public List<KtTypeReference> getSignatureReferencedTypes() {
        return Stream.of(
                        getInputParameterTypeReferences().stream(),
                        getReturnTypeReference().stream()
                )
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }


    @Override
    public List<KtTypeReference> getBodyReferencedTypes() {
        List<KtTypeReference> bodyReferences = KotlinPsiTreeUtil.getEligibleReferencedTypes(
                ktNamedFunction.getBodyExpression(),
                ktNamedFunction.getBodyBlockExpression());

        //System.out.println("Body References Found: " + bodyReferences.size());

        return bodyReferences;

    }

    @Override
    public List<String> getReferencedNames() {
        return KotlinPsiTreeUtil.findEligibleKtNameReferencedExpressionNames(getPsiElement());
    }

    @Override
    public String getPublicContextForParent() {
        return KotlinPsiTreeFormatUtil.getKtNamedFunctionSignature(ktNamedFunction);
    }

    @Override
    public List<KotlinSemanticPsiTreeChildNode<?>> getChildren() {
        return Collections.emptyList(); // Functions don’t have children in this structure
    }

    private List<KtTypeReference> getInputParameterTypeReferences() {
        return ktNamedFunction.getValueParameters().stream()
                .flatMap(param -> KotlinPsiTreeUtil.getEligibleReferencedTypes(param).stream())
                .collect(Collectors.toList());
    }


    private List<KtTypeReference> getReturnTypeReference() {
        KtTypeReference ktTypeReference = ktNamedFunction.getTypeReference();
        return ktTypeReference != null ? KotlinPsiTreeUtil.getEligibleReferencedTypes(ktTypeReference) : Collections.emptyList();
    }

//    @Override
//    public String encapsulateChildContext(String context) {
//        return context;
//    }

    public <T extends KotlinSemanticPsiTreeChildNode<?>> String getMatchingFunctionsFromParent(Class<T> parentType) {
        // Find parent class/object
        Optional<T> parentClass = findParentNodeOfType(parentType);
        if (parentClass.isPresent()) {
            // Get all member functions with the same name
            String currentMethodName = getTypeName();
            List<KotlinSemanticPsiTreeFunction> matchingMethods = parentClass.get()
                    .findDirectChildrenOfType(KotlinSemanticPsiTreeFunction.class)
                    .stream()
                    .filter(method -> method.getTypeName().equals(currentMethodName))
                    .toList();

            // Build string with all matching method signatures
            StringBuilder result = new StringBuilder();
            for (KotlinSemanticPsiTreeFunction method : matchingMethods) {
                if (result.length() > 0) {
                    result.append("\n");
                }
                result.append(KotlinPsiTreeFormatUtil.getKtNamedFunctionSignature(method.getPsiElement()));
            }
            return result.toString();
        }
        throw new IllegalStateException("No class or object parent node found for member function: " + getPsiElement().getName());
    }



}