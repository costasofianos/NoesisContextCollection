package org.noesis.codeanalysis.util.kotlin.psi.associations;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.noesis.codeanalysis.dataobjects.associations.AssociationWeightCalculator;
import org.noesis.codeanalysis.dataobjects.associations.CodeToken;
import org.noesis.codeanalysis.dataobjects.associations.SemanticNodeAssociation;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeClassOrObject;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementReferenceUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeUtil;

import java.util.*;

public class KotlinSemanticTreeCodeAssociationsUtil {


    @SafeVarargs
    public static List<CodeToken> collectCodeTokens(PsiElement psiElement, Class<? extends PsiElement>... elementClasses) {
        List<CodeToken> tokens = new ArrayList<>();

        // Process each element class type
        Arrays.stream(elementClasses).forEach(elementClass -> {
            List<? extends PsiElement> elements = KotlinPsiTreeUtil.findEligibleElements(elementClass, psiElement);

            // Convert each found element to a CodeToken
            elements.forEach(element -> {
                String token = element.getText();
                tokens.add(new CodeToken(token, element));
            });
        });

        return tokens;
    }

    public static Set<SemanticNodeAssociation<?>> createAllSemanticNodeAssociations(
            List<KotlinSemanticReference> semanticReferences,
            Map<String, Set<KotlinSemanticPsiTreeChildNode>> nodeMap,
            boolean includeExtendingClasses,
            AssociationWeightCalculator associationWeightCalculator,
            int numberOfAmbiguousMatchesToInclude
    ) {
        Set<SemanticNodeAssociation<?>> associations = new HashSet<>();

        for (KotlinSemanticReference reference : semanticReferences) {
            //System.out.println("Looking up reference "+reference.getReferenceName());
            String referenceName = reference.getReferenceName();
            if (referenceName != null && !referenceName.isBlank() && nodeMap.containsKey(referenceName)) {
                // Create temporary list of associations for this reference
                List<SemanticNodeAssociation<?>> referenceAssociations = new ArrayList<>();

                // Create and weigh associations for each node
                for (KotlinSemanticPsiTreeChildNode node : nodeMap.get(referenceName)) {
                    //System.out.println("Found node for reference "+referenceName+" "+node.getTypeName()+" "+node.getClass().getSimpleName()+" "+node.getContainingFile().getName());
//                    if (node.getTypeName().equals("AppTheme")) {
//                        System.out.println(node.getPsiElement().getText());
//                    }
                    SemanticNodeAssociation<?> association = new SemanticNodeAssociation<>(reference, node, 0.0f, true);
                    float weight = associationWeightCalculator.calculateWeightedAverage(association).weight();
                    //System.out.println("Associated added with weight "+weight);
                    referenceAssociations.add(new SemanticNodeAssociation<>(reference, node, weight, true));
                }
                //System.out.println("Found "+referenceAssociations.size()+" associations for reference "+referenceName);


                // Sort by weight and take top 3
              //  referenceAssociations.sort((a1, a2) -> Float.compare(a2.weight(), a1.weight()));
                referenceAssociations.sort((a1, a2) -> {
                    // First compare by weight
                    int weightComparison = Float.compare(a2.weight(), a1.weight());
                    if (weightComparison != 0) {
                        return weightComparison;
                    }

                    PsiElement referenceFile = a1.kotlinSemanticReference().getContainingPart().getContainingNode().getContainingKtFile();
                    // If weights are equal, compare by reference similarity
                    PsiElement associatedFile1 = a1.associatedNode().getContainingKtFile();
                    PsiElement associatedFile2 = a2.associatedNode().getContainingKtFile();

                    float similarity1 = KotlinPsiElementReferenceUtil.calculateReferenceOverlap(associatedFile1, referenceFile);
                    float similarity2 = KotlinPsiElementReferenceUtil.calculateReferenceOverlap(associatedFile2, referenceFile);
                    int compare = Float.compare(similarity2, similarity1);

//                    System.out.println("Similarity 1 ["+similarity1+"] "+associatedFile1.getContainingFile().getName()+" "+ KotlinPsiElementUtil.getSimpleNameOfContainingFile(referenceFile));
//                    System.out.println("Similarity 2 ["+similarity2+"] "+associatedFile2.getContainingFile().getName()+" "+KotlinPsiElementUtil.getSimpleNameOfContainingFile(referenceFile));

                    return Float.compare(similarity2, similarity1); // Compare the two similarities in descending order
                });

                if (referenceAssociations.size()>1) {

                    //if (referenceAssociations.get(0).associatedNode().getTypeName().equals("enabled")) {
                        //System.out.println("Found Multiple Association");
//                        referenceAssociations.stream().
//                                forEach(association -> System.out.println("Node: " + association.associatedNode().getTypeName() + " File : " + association.associatedNode().getContainingFile().getName() + " Weight: " + association.weight()
//                                        //+"\n\n"+association.associatedNode().getContainingKtFile().getText()
//                                        ));
                    //}
                }
                referenceAssociations.stream()
                        .limit(numberOfAmbiguousMatchesToInclude)
                        .forEach( referenceAssociation ->
                        {//System.out.println("Adding "+referenceAssociation.kotlinSemanticReference().getReferenceName());
                                associations.add(referenceAssociation);}
                                );



                // Add extending classes for the selected associations
                if (includeExtendingClasses) {
                    for (SemanticNodeAssociation<?> association : new ArrayList<>(referenceAssociations)) {
                        if (association.associatedNode() instanceof KotlinSemanticPsiTreeClassOrObject classNode) {
                            //System.out.println("Looking for extending classes for "+classNode.getTypeName());
                            for (KotlinSemanticPsiTreeClassOrObject extendingClass : classNode.findAllExtendedClasses()) {

                                associations.add(new SemanticNodeAssociation<>(
                                        reference,
                                        extendingClass,
                                        association.weight(),
                                        true
                                ));
                            }
                        }
                    }
                }
            }
        }
        return associations;
    }



}
