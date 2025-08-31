package org.noesis.codeanalysis.dataobjects.semanticpsi.node;

import org.jetbrains.kotlin.psi.KtTypeReference;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeRoot;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPsiCallableReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPsiTypeReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.self.KotlinSemanticSelfPart;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinKtTypeReferenceUtil;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class KotlinSemanticPsiTreeNode {


    private KotlinSemanticSelfPart<? extends KotlinSemanticPsiTreeChildNode<?>> kotlinSemanticSelfPart;


    public KotlinSemanticPsiTreeNode() {

    }

    public List<KotlinSemanticPart<?>> getAllSemanticParts() {
        List<KotlinSemanticPart<?>> parts = new ArrayList<>(getSemanticParts());
           parts.add(getSelfPart());
           return parts;
    }

    abstract public KotlinSemanticPsiTreeNode getParent();
    public abstract KotlinSemanticReference getSemanticReference();
    public abstract List<? extends KotlinSemanticPsiTreeChildNode<?>> getChildren();

    public abstract String getTypeName();

    public abstract String getPublicContext();



    public abstract String getPublicContextForParent();

    public abstract ElementVisibility getElementVisibility();
    public abstract List<KtTypeReference> getSignatureReferencedTypes();
    public abstract List<KtTypeReference> getBodyReferencedTypes();
    public abstract List<String> getReferencedNames();
    public abstract List<KotlinSemanticPart<?>> getSemanticParts();
    public abstract KotlinSemanticPart<?> getSelfPart();


    public List<KotlinSemanticPsiTypeReference> getAllTypeReferences() {
        return getAllReferencesOfType(KotlinSemanticPsiTypeReference.class);
    }

    public List<KotlinSemanticPsiCallableReference> getAllFunctionReferences() {
        return getAllReferencesOfType(KotlinSemanticPsiCallableReference.class);
    }

    private <T extends KotlinSemanticReference> List<T> getAllReferencesOfType(Class<T> referenceType) {
        return getAllSemanticParts().stream()
                .flatMap(part -> part.getSemanticReferences().stream())
                .filter(referenceType::isInstance)
                .map(referenceType::cast)
                .collect(Collectors.toList());
    }

    public boolean hasParent(Set<? extends KotlinSemanticPsiTreeNode> nodes) {
        KotlinSemanticPsiTreeNode current = this;

        while (current != null) {
            // Check if current node's parent is in the input set
            if (nodes.contains(current)) {
                return true;
            }
            // Move up the tree
            current = current.getParent();
        }

        return false;
    }



    public <T extends KotlinSemanticReference> List<T> getAllReferencesOfTypeInSubtree(Class<T> referenceType) {
        List<T> references = new ArrayList<>();

        // Get references from current node
        references.addAll(getAllReferencesOfType(referenceType));

        // Recursively collect references from all children
        for (KotlinSemanticPsiTreeChildNode<?> child : getChildren()) {
            references.addAll(child.getAllReferencesOfTypeInSubtree(referenceType));
        }

        return references;
    }



    public Set<String> getAllReferencedTypeNamesInSubtree(boolean includeBodyTypes) {
        Set<String> typeNames = new HashSet<>(getSignatureReferencedNames());

        if (includeBodyTypes) {
            typeNames.addAll(getBodyReferences());
        }

        // Recursively collect type names from all children
        for (KotlinSemanticPsiTreeChildNode<?> child : getChildren()) {
            typeNames.addAll(child.getAllReferencedTypeNamesInSubtree(includeBodyTypes));
        }

        return typeNames;
    }

    public List<KtTypeReference> getAllReferencedTypesInSubtree(boolean includeBodyTypes) {
        List<KtTypeReference> referencedTypes = new ArrayList<>();

        // If this is a child node that can have referenced types, collect them
        if (this instanceof KotlinSemanticPsiTreeChildNode<?> childNode) {
            referencedTypes.addAll(childNode.getSignatureReferencedTypes());
            if (includeBodyTypes) {
                referencedTypes.addAll(childNode.getBodyReferencedTypes());
            }
        }

        // Recursively collect types from all children
        for (KotlinSemanticPsiTreeChildNode<?> child : getChildren()) {
            referencedTypes.addAll(child.getAllReferencedTypesInSubtree(includeBodyTypes));
        }

        return referencedTypes;
    }








    public <T extends KotlinSemanticPsiTreeNode> Map<String, Set<String>> collectNodePublicContextMap(
            Class<T> nodeType,
            Predicate<KotlinSemanticPsiTreeNode> filter,
            ElementVisibility... visibilities) {
        return collectNodeMapForType(nodeType, filter, visibilities).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(KotlinSemanticPsiTreeNode::getPublicContext)
                                .collect(Collectors.toSet())
                ));
    }


    public <T extends KotlinSemanticPsiTreeNode> Map<String, Set<String>> collectNodePublicContextMap(
            Class<T> nodeType,
            ElementVisibility... visibilities) {
        return collectNodePublicContextMap(nodeType, null, visibilities);
    }


    public <T extends KotlinSemanticPsiTreeNode> Map<String, Set<T>> collectNodeMapForType(
            Class<T> nodeType,
            Predicate<KotlinSemanticPsiTreeNode> filter,
            ElementVisibility... visibilities) {
        return findNodesOfType(nodeType, filter, visibilities).stream()
                .filter(node -> {
                    if (node.getTypeName() == null) {
                        System.out.println("WARNING: Node of class " + node.getClass().getSimpleName() +
                                " returned null type name");
                        return false;
                    }
                    return true;
                })
                .collect(Collectors.groupingBy(
                        KotlinSemanticPsiTreeNode::getTypeName,
                        Collectors.toSet()
                ));
    }







//    public <T extends KotlinSemanticPsiTreeNode> Map<String, T> collectNodeMapForType(
//            Class<T> nodeType,
//            Predicate<KotlinSemanticPsiTreeNode> filter,
//            ElementVisibility... visibilities) {
//        return findNodesOfType(nodeType, filter, visibilities).stream()
//                .collect(Collectors.toMap(
//                        KotlinSemanticPsiTreeNode::getTypeName,
//                        node -> node,
//                        (existing, replacement) -> existing // Keep first occurrence in case of duplicates
//                ));
//    }

    public <T extends KotlinSemanticPsiTreeNode> Map<String, Set<T>> collectNodeMapForType(
            Class<T> nodeType,
            ElementVisibility... visibilities) {
        return collectNodeMapForType(nodeType, null, visibilities);
    }





    // Base method with lambda
public <T extends KotlinSemanticPsiTreeNode> List<T> findNodesOfType(
        Class<T> nodeType,
        Predicate<KotlinSemanticPsiTreeNode> filter,
        ElementVisibility... visibilities) {

    List<T> results = new ArrayList<>();

    // Check if current node matches the type
    if (nodeType.isInstance(this)) {
        @SuppressWarnings("unchecked")
        T castedNode = (T) this;
        if ((visibilities.length == 0 || Arrays.asList(visibilities).contains(getElementVisibility()))
                && (filter == null || filter.test(this))) {
            results.add(castedNode);
        }
    }

    // Recursively search through all children
    for (KotlinSemanticPsiTreeChildNode<?> child : getChildren()) {
        results.addAll(child.findNodesOfType(nodeType, filter, visibilities));
    }

    return results;
}



    public <T extends KotlinSemanticPsiTreeChildNode<?>> List<T> findDirectChildrenOfType(Class<T> nodeType) {
        return getChildren().stream()
                .filter(nodeType::isInstance)
                .map(nodeType::cast)
                .toList();
    }

    public <T extends KotlinSemanticPsiTreeNode> List<T> findNodesOfTypes(
            List<Class<? extends T>> nodeTypes,
            ElementVisibility... visibilities) {
        List<T> results = new ArrayList<>();
        for (Class<? extends T> nodeType : nodeTypes) {
            results.addAll(findNodesOfType(nodeType, visibilities));
        }
        return results;
    }


    // Original method delegates to new one
public <T extends KotlinSemanticPsiTreeNode> List<T> findNodesOfType(
        Class<T> nodeType, 
        ElementVisibility... visibilities) {
    return findNodesOfType(nodeType, null, visibilities);
}

    public Set<String> getSignatureReferencedNames() {
        Set<String> typeNames = new HashSet<String>(KotlinKtTypeReferenceUtil.extractTypeNames(getSignatureReferencedTypes()));

        return typeNames;
    }



    public  Set<String> getBodyReferences() {

        Set<String> typeNames =  new HashSet<String>(KotlinKtTypeReferenceUtil.extractTypeNames(getBodyReferencedTypes()));
        typeNames.addAll(getReferencedNames());
        return typeNames;

    }

    public <T extends KotlinSemanticPsiTreeNode> Optional<T> findTopParentNodeOfType(Class<T> nodeType) {
        T topMatch = null;
        KotlinSemanticPsiTreeNode current = this;

        // First check if current node matches
        if (nodeType.isInstance(current)) {
            topMatch = nodeType.cast(current);
        }

        // Keep traversing up while we can
        while (current != null && current instanceof KotlinSemanticPsiTreeChildNode<?> childNode) {
            current = childNode.getParent();
            if (nodeType.isInstance(current)) {
                topMatch = nodeType.cast(current);
            }
        }

        return Optional.ofNullable(topMatch);
    }



    public <T extends KotlinSemanticPsiTreeNode> Optional<T> findParentNodeOfType(Class<T> nodeType) {
        KotlinSemanticPsiTreeNode current = this;

        // First check if current node matches
        if (nodeType.isInstance(current)) {
            return Optional.of(nodeType.cast(current));
        }


        while (current != null && current instanceof KotlinSemanticPsiTreeChildNode<?> childNode) {
            current = childNode.getParent();
            if (nodeType.isInstance(current)) {
                return Optional.of(nodeType.cast(current));
            }
        }
        return Optional.empty();
    }

    public Optional<KotlinSemanticPsiTreeNode> findParentNodeOfTypes(List<Class<? extends KotlinSemanticPsiTreeNode>> nodeTypes) {

        KotlinSemanticPsiTreeNode current = this;

//        // First check if current node matches any of the types
//        for (Class<? extends KotlinSemanticPsiTreeNode> nodeType : nodeTypes) {
//            if (nodeType.isInstance(current)) {
//                return Optional.of(current);
//            }
//        }

        // Keep traversing up while we can
        while (current instanceof KotlinSemanticPsiTreeChildNode<?> childNode) {
            current = childNode.getParent();
            // Check each type against the current node
            for (Class<? extends KotlinSemanticPsiTreeNode> nodeType : nodeTypes) {
                if (nodeType.isInstance(current)) {
                    return Optional.of(current);
                }
            }
        }
        return Optional.empty();

    }


    public KotlinSemanticPsiTreeRoot getRoot() {
        KotlinSemanticPsiTreeNode currentNode = this;
        while (currentNode.getParent() != null) {
            currentNode = currentNode.getParent();
        }

        if (!(currentNode instanceof KotlinSemanticPsiTreeRoot rootNode)) {
            throw new RuntimeException("Root node must be of type KotlinSemanticPsiTreeRoot but was: "
                    + currentNode.getClass().getSimpleName());
        }

        return rootNode;
    }


    public <T extends KotlinSemanticPsiTreeNode> List<T> findNodesOfTypeFromRoot(
            List<Class<? extends T>> nodeTypes,
            ElementVisibility... visibilities
    ) {
        KotlinSemanticPsiTreeRoot root = getRoot();

        // Get nodes from the index for each type and combine them
        List<T> result = new ArrayList<>();
        for (Class<? extends T> nodeType : nodeTypes) {
            @SuppressWarnings("unchecked")
            Set<KotlinSemanticPsiTreeNode> nodesOfType = root.getChildNodesByType().get(nodeType);
            if (nodesOfType != null) {
                nodesOfType.stream()
                        .map(node -> (T) node)
                        .filter(node -> visibilities == null ||
                                visibilities.length == 0 ||
                                Arrays.asList(visibilities).contains(node.getElementVisibility()))
                        .forEach(result::add);
            }
        }

        return result;
    }


    public Set<KotlinSemanticPsiTreeNode> getAllNodes() {
        Set<KotlinSemanticPsiTreeNode> allNodes = new HashSet<>();
        // Add this node
        allNodes.add(this);

        // Recursively add all children and their descendants
        for (KotlinSemanticPsiTreeChildNode<?> child : getChildren()) {
            allNodes.addAll(child.getAllNodes());
        }

        return allNodes;
    }

    public Map<String, Set<KotlinSemanticPsiTreeChildNode>> collectReferencesFromParts(List<Class<? extends KotlinSemanticPart<?>>> partTypes) {
        Map<String, Set<KotlinSemanticPsiTreeChildNode>> referenceMap = new HashMap<>();

        // Get all nodes in the tree starting from this node
        Set<KotlinSemanticPsiTreeNode> allNodes = getAllNodes();

        // Go through each node
        for (KotlinSemanticPsiTreeNode node : allNodes) {
            if (node instanceof KotlinSemanticPsiTreeChildNode<?> childNode) {
                // Get all semantic parts for the current node
                List<KotlinSemanticPart<?>> nodeParts = node.getAllSemanticParts();

                // Filter parts to only include those of the specified types
                List<KotlinSemanticPart<?>> matchedParts = nodeParts.stream()
                        .filter(part -> partTypes.stream()
                                .anyMatch(partType -> partType.isInstance(part)))
                        .toList();

                // For each matched part, collect its references
                for (KotlinSemanticPart<?> part : matchedParts) {
                    for (KotlinSemanticReference reference : part.getSemanticReferences()) {
                        String referenceName = reference.getReferenceName();
                        // Add the current node to the list of nodes for this reference
                        referenceMap.computeIfAbsent(referenceName, k -> new LinkedHashSet<>())
                                .add(childNode);
                    }
                }
            }
        }

        return referenceMap;
    }



}