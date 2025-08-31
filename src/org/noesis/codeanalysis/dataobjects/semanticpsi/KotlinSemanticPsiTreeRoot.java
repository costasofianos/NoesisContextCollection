package org.noesis.codeanalysis.dataobjects.semanticpsi;

import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtPackageDirective;
import org.jetbrains.kotlin.psi.KtTypeReference;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;

import java.util.*;
import java.util.stream.Collectors;

public class KotlinSemanticPsiTreeRoot extends KotlinSemanticPsiTreeNode {
    private final List<KotlinSemanticPsiTreePackage> packages = new ArrayList<>();
    private Map<Class<? extends KotlinSemanticPsiTreeNode>, Set<KotlinSemanticPsiTreeNode>> childNodesByType;


    public KotlinSemanticPsiTreeRoot(List<KtFile> ktFiles) {
        super();
        processFiles(ktFiles);
        childNodesByType = indexChildNodesByType();

    }

    public <T extends KotlinSemanticPsiTreeNode> Set<T> findNodesByTypeAndName(
            Class<? extends T> nodeType,
            String nodeName
    ) {
        // Get nodes of the specified type from the index
        @SuppressWarnings("unchecked")
        Set<KotlinSemanticPsiTreeNode> nodesOfType = childNodesByType.get(nodeType);

        if (nodesOfType == null || nodeName == null) {
            return new HashSet<>();
        }

        // Filter nodes by name and cast to the correct type
        return nodesOfType.stream()
                .filter(node -> nodeName.equals(node.getTypeName()))
                .map(node -> (T) node)
                .collect(Collectors.toSet());
    }


    public List<KotlinSemanticPsiTreeFile> findFileNodesByName(String fileName) {
        if (fileName == null) {
            return new ArrayList<>();
        }

        return packages.stream()
                .flatMap(pkg -> pkg.getChildren().stream())
                .map(node -> (KotlinSemanticPsiTreeFile) node)
                .filter(file -> file.getContainingKtFile().getName().contains(fileName))
                .collect(Collectors.toList());
    }




    private void processFiles(List<KtFile> ktFiles) {
        // Group files by package name
        Map<String, List<KtFile>> filesByPackage = new HashMap<>();
        
        for (KtFile ktFile : ktFiles) {
            String packageFqName = ktFile.getPackageFqName().asString();
            filesByPackage.computeIfAbsent(packageFqName, k -> new ArrayList<>())
                         .add(ktFile);
        }
        
        // Create package nodes and add files to them
        filesByPackage.forEach((packageName, files) -> {
            // Use package directive from the first file in the package
            KtPackageDirective packageDirective = files.get(0).getPackageDirective();
            
            // Create or get the package node and its parent structure
            KotlinSemanticPsiTreePackage packageNode;
            if (packageName.isEmpty()) {
                // For default package, parent is root
                packageNode = new KotlinSemanticPsiTreePackage(packageDirective, this);
                addPackage(packageNode);
            } else {
                // For named packages, ensure the full package hierarchy exists
                String[] packageParts = packageName.split("\\.");
                StringBuilder currentPackage = new StringBuilder();
                
                // Start with root as the first parent
                KotlinSemanticPsiTreePackage lastParent = null;
                KotlinSemanticPsiTreeNode parent = this;
                
                for (String part : packageParts) {
                    if (currentPackage.length() > 0) {
                        currentPackage.append(".");
                    }
                    currentPackage.append(part);
                    
                    // Find or create package at this level
                    final String currentPackageName = currentPackage.toString();
                    final KotlinSemanticPsiTreeNode currentParent = parent;
                    
                    KotlinSemanticPsiTreePackage currentPackageNode = packages.stream()
                            .filter(p -> p.getTypeName().equals(currentPackageName))
                            .findFirst()
                            .orElseGet(() -> {
                                KotlinSemanticPsiTreePackage newPackage = 
                                    new KotlinSemanticPsiTreePackage(packageDirective, currentParent);
                            if (currentParent == this) {
                                    addPackage(newPackage);
                                }
                                return newPackage;
                            });
                    
                    parent = currentPackageNode;
                }
                packageNode = (KotlinSemanticPsiTreePackage) parent;
            }

            // Add files to package
            files.forEach(file -> {
                KotlinSemanticPsiTreeFile fileNode = new KotlinSemanticPsiTreeFile(file, packageNode);
                packageNode.addFile(fileNode);
            });
        });
    }




    public Map<Class<? extends KotlinSemanticPsiTreeNode>, Set<KotlinSemanticPsiTreeNode>> indexChildNodesByType() {
        // Create map to store the results
        Map<Class<? extends KotlinSemanticPsiTreeNode>, Set<KotlinSemanticPsiTreeNode>> typeIndex = new HashMap<>();

        // Get all nodes in the tree using getAllNodes()
        Set<KotlinSemanticPsiTreeNode> allNodes = getAllNodes();

        // Process each node
        for (KotlinSemanticPsiTreeNode node : allNodes) {
            // Get the actual class of the node
            @SuppressWarnings("unchecked")
            Class<? extends KotlinSemanticPsiTreeNode> nodeClass =
                    (Class<? extends KotlinSemanticPsiTreeNode>) node.getClass();

            // Add the node to the set for its class
            typeIndex.computeIfAbsent(nodeClass, k -> new HashSet<>()).add(node);

            // Also add the node to sets for all its superclasses that extend KotlinSemanticPsiTreeNode
            Class<?> currentClass = nodeClass.getSuperclass();
            while (currentClass != null && KotlinSemanticPsiTreeNode.class.isAssignableFrom(currentClass)) {
                @SuppressWarnings("unchecked")
                Class<? extends KotlinSemanticPsiTreeNode> superClass =
                        (Class<? extends KotlinSemanticPsiTreeNode>) currentClass;
                typeIndex.computeIfAbsent(superClass, k -> new HashSet<>()).add(node);
                currentClass = currentClass.getSuperclass();
            }
        }

        return typeIndex;
    }





    private KotlinSemanticPsiTreeNode getOrCreateParentPackage(String packageName, KtFile ktFile) {
    int lastDotIndex = packageName.lastIndexOf('.');
    if (lastDotIndex == -1) {
        // If no dots, parent is root
        return this;
    }
    
    String parentPackageName = packageName.substring(0, lastDotIndex);
    // Find existing package or create new one
    return packages.stream()
            .filter(p -> p.getTypeName().equals(parentPackageName))
            .findFirst()
            .orElseGet(() -> {
                KotlinSemanticPsiTreeNode parentOfParent = getOrCreateParentPackage(parentPackageName, ktFile);
                KtPackageDirective parentDirective = ktFile.getPackageDirective();
                KotlinSemanticPsiTreePackage parentPackage = new KotlinSemanticPsiTreePackage(parentDirective, parentOfParent);
                if (parentOfParent == this) {
                    addPackage(parentPackage);
                }
                return parentPackage;
            });
}

    public Optional<KotlinSemanticPsiTreeFile> findMatchingFile(KtFile ktFile) {
    String filePath = ktFile.getVirtualFilePath();
    return packages.stream()
            .flatMap(pkg -> pkg.getChildren().stream())
            .map(node -> (KotlinSemanticPsiTreeFile) node)
            .filter(kotlinSemanticPsiTreeFile -> 
                    kotlinSemanticPsiTreeFile.getPsiElement().getVirtualFilePath().equals(filePath))
            .findFirst();
}



    public void addPackage(KotlinSemanticPsiTreePackage pkg) {
    packages.add(pkg);
}

public List<KotlinSemanticPsiTreePackage> getPackages() {
    return packages;
}

    @Override
    public KotlinSemanticPsiTreeNode getParent() {
        return null;
    }

    @Override
    public KotlinSemanticReference getSemanticReference() {
        return null;
    }

    @Override
public List<KotlinSemanticPsiTreePackage> getChildren() {
    return packages;
}

    @Override
    public String getTypeName() {
        return "Source Root";
    }

    @Override
    public String getPublicContext() {
        return "Source Root";
    }

    @Override
    public String getPublicContextForParent() {
        return "Source Root";
    }

    @Override
    public Set<String> getSignatureReferencedNames() {
        return Set.of();
    }

    @Override
    public Set<String> getBodyReferences() {
        return Set.of();
    }

    @Override
    public ElementVisibility getElementVisibility() {
        return ElementVisibility.PUBLIC;
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
    public List<KotlinSemanticPart<?>> getSemanticParts() {
        return List.of();
    }

    @Override
    public KotlinSemanticPart<?> getSelfPart() {
        return null;
    }

    public Map<Class<? extends KotlinSemanticPsiTreeNode>, Set<KotlinSemanticPsiTreeNode>> getChildNodesByType() {
        return childNodesByType;
    }
}