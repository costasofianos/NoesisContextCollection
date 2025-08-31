package org.noesis.codeanalysis.util.kotlin.psi.formatters;

import org.noesis.codeanalysis.dataobjects.associations.SemanticNodeAssociation;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeFile;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeMemberFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeTopLevelFunction;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeContextEncapsulatorNode;
import org.noesis.codeanalysis.util.constants.KotlinContextCollectorConstants;
import org.noesis.codeanalysis.util.general.StringUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class KotlinSemanticNodeContextUtil {

//    static HashMap<Class<? extends KotlinSemanticPsiTreeChildNode<?>>, Integer> maxTokensPerContextEntry = new HashMap<>();
//    static {
//        // Add entries for different node types
//        maxTokensPerContextEntry.put(KotlinSemanticPsiTreeFile.class, 1000);
//        maxTokensPerContextEntry.put(KotlinSemanticPsiTreeClassOrObject.class, 2000);
//        maxTokensPerContextEntry.put(KotlinSemanticPsiTreeTopLevelFunction.class, 2000);
//        maxTokensPerContextEntry.put(KotlinSemanticPsiTreeMemberFunction.class, 300);
//        maxTokensPerContextEntry.put(KotlinSemanticPsiTreeVariable.class, 100);
//
//    }

    public static Map<KotlinSemanticPsiTreeFile, Set<SemanticNodeAssociation<?>>> groupAssociationsByFile(
            Set<SemanticNodeAssociation<?>> associations) {
        return associations.stream()
                .filter(association -> {
                    try {
                        association.associatedNode().getContainingKotlinSemanticPsiTreeFile();
                        return true;
                    } catch (Exception e) {
                        System.out.println("Warning: Error finding containing file");
                        e.printStackTrace();
                        return false; // skip this association
                    }
                }).
                collect(Collectors.groupingBy(
                        association -> association.associatedNode().getContainingKotlinSemanticPsiTreeFile(),
                        Collectors.toSet()
                ));
    }

    public static String getFileContextWithAssociations(KotlinSemanticPsiTreeFile file,
                                                           Set<SemanticNodeAssociation<?>> semanticNodeAssociations) {
        return processNodeContext(file, semanticNodeAssociations, true);
    }

    private static String processNodeContext(KotlinSemanticPsiTreeChildNode<?> node,
                                             Set<SemanticNodeAssociation<?>> semanticNodeAssociations,
                                             boolean firstRun) {

        Optional<SemanticNodeAssociation<?>> matchedAssociation =
                semanticNodeAssociations.stream().
                        filter( semanticNodeAssociation -> semanticNodeAssociation.associatedNode()==node).findFirst();


        if (matchedAssociation.isPresent()) {
            //return node.getPublicContextForParent();
            return getNodeContext(node, matchedAssociation.get());
        }

        ArrayList<String> childContexts = new ArrayList<>();
        for (KotlinSemanticPsiTreeChildNode<?> child : node.getChildren()) {
            String childContext = processNodeContext(child, semanticNodeAssociations, false);
            if (!childContext.isEmpty()) {
                childContexts.add(childContext);
            }
        }

        if (!childContexts.isEmpty()) {
            //System.out.println(childContexts);
            String context = StringUtils.normalizeNewLines(String.join("\n", childContexts));
            if (!firstRun) {
               context =  StringUtils.indentAllLines(context, 2);
            }
            if (node instanceof KotlinSemanticPsiTreeContextEncapsulatorNode encapsulator) {
                return encapsulator.encapsulateChildContext(context);
                //return "<* context-size="+context.length()+">"+context+"<*>";
            } else {
                return context;
            }
        } else {
            return "";
        }
    }

    private static String getNodeContext(KotlinSemanticPsiTreeChildNode<?> node, SemanticNodeAssociation<?> nodeAssociation) {
        if (
                nodeAssociation != null
                        &&
                        (
                           nodeAssociation.associatedNode().getClass() == KotlinSemanticPsiTreeMemberFunction.class
                           ||
                           nodeAssociation.associatedNode().getClass() == KotlinSemanticPsiTreeTopLevelFunction.class
                        )
                        &&
                        nodeAssociation.weight() >= KotlinContextCollectorConstants.METHOD_BODY_THRESHOLD

        ) {
            //System.out.println("Matched Method "+nodeAssociation.associatedNode().getTypeName());
            return node.getPsiElement().getText();
        } else {
            return node.getPublicContextForParent();
        }

    }
}
