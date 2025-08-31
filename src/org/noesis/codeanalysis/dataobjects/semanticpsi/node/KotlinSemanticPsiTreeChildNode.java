package org.noesis.codeanalysis.dataobjects.semanticpsi.node;

import org.jetbrains.kotlin.com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.psi.KtImportDirective;
import org.jetbrains.kotlin.psi.KtPackageDirective;
import org.noesis.codeanalysis.dataobjects.semanticpsi.KotlinSemanticPsiTreeFile;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.self.KotlinSemanticSelfPart;
import org.noesis.codeanalysis.util.general.StringUtils;
import org.noesis.codeanalysis.util.kotlin.general.KotlinSourceCodeUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiElementUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiTreeUtil;

import java.io.File;
import java.util.List;
import java.util.Optional;

public abstract class KotlinSemanticPsiTreeChildNode<T extends PsiElement> extends KotlinSemanticPsiTreeNode {

    private final KotlinSemanticPsiTreeNode parent;
    private final T psiElement;
    private KotlinSemanticSelfPart<? extends KotlinSemanticPsiTreeChildNode<?>> kotlinSemanticSelfPart;


    protected KotlinSemanticPsiTreeChildNode(T psiElement, KotlinSemanticPsiTreeNode parent) {

        super();
        this.parent = parent;
        this.psiElement = psiElement;
        kotlinSemanticSelfPart = new KotlinSemanticSelfPart<>(this);

    }
    public String getFullTextOfElement() {
        System.out.println("Printing full text of element: " + getTypeName());
        return encapsulateContextWithParentNodes(getPsiElement().getText().trim(), true);
    }

    public void initialise() {
        if (getSemanticReference() != null) {
            kotlinSemanticSelfPart.getSemanticReferences().add(
                    getSemanticReference()
            );
        }
    }


    public boolean isInSameFile(KotlinSemanticPsiTreeChildNode<?> other) {
        return this.getContainingKtFile().getName().equals(other.getContainingKtFile().getName());
    }

  /**
   * Gets the package directive from the containing file
 * @return Optional containing the package directive if found, empty otherwise
 */
    private Optional<KtPackageDirective> getPackageDirective() {
        Optional<KotlinSemanticPsiTreeFile> containingFileOptional =
                this.findParentNodeOfType(KotlinSemanticPsiTreeFile.class);

        if (containingFileOptional.isEmpty()) {
            return Optional.empty();
        }

        KotlinSemanticPsiTreeFile containingFile = containingFileOptional.get();
        KtPackageDirective packageDirective = containingFile.getPackageDirective();
        return Optional.ofNullable(packageDirective);
    }

    /**
     * Gets the package name from the containing file
     * @return The package name or empty string if no package directive found
     */
    protected String getPackageName() {
        return getPackageDirective()
                .map(KtPackageDirective::getQualifiedName)
                .orElse("");
    }

    /**
     * Returns the fully qualified name of this node, combining package name with type name
     * @return The fully qualified name (e.g. "com.example.MyClass")
     */
    public String getFullyQualifiedName() {
        String packageName = getPackageName();
        return packageName.isEmpty() ? getTypeName() : packageName + "." + getTypeName();
    }


    public float getPackageDistance(KotlinSemanticPsiTreeChildNode<?> otherNode) {
        String thisPackage = this.getPackageName();
        String otherPackage = otherNode.getPackageName();
        return StringUtils.calculatePackageDistance(thisPackage, otherPackage);
    }



    public boolean canBeAccessedBy(KotlinSemanticPsiTreeChildNode<?> otherNode) {
        Optional<KotlinSemanticPsiTreeFile> thisFileOptional =
                this.findParentNodeOfType(KotlinSemanticPsiTreeFile.class);
        Optional<KotlinSemanticPsiTreeFile> otherFileOptional =
                otherNode.findParentNodeOfType(KotlinSemanticPsiTreeFile.class);

        if (thisFileOptional.isEmpty() || otherFileOptional.isEmpty()) {
            System.out.println("WARNING [canBeAccessedBy] Both nodes must be part of a file ("+this.getClass().getSimpleName()+" "+otherNode.getClass().getSimpleName()+")");
            //throw new IllegalArgumentException("Both nodes must be part of a file");
            return false;
        }

        String thisPackageName = getPackageName();
        String otherPackageName = otherNode.getPackageName();

        // If both have no package directive, access is allowed
        if (thisPackageName.isEmpty() && otherPackageName.isEmpty()) {
            return true;
        }

        // If one has a package directive and the other doesn't, access is not allowed
        if (thisPackageName.isEmpty() || otherPackageName.isEmpty()) {
            return false;
        }

        String thisFullyQualifiedName = getFullyQualifiedName();

        // Check if same package
        if (thisPackageName.equals(otherPackageName)) {
            return true;
        }

        // Check imports
        KotlinSemanticPsiTreeFile otherFile = otherFileOptional.get();
        List<KtImportDirective> otherFileImportDirectives = otherFile.getImportDirectives();

        return otherFileImportDirectives.stream()
                .filter(directive -> directive.getImportedFqName() != null)
                .anyMatch(directive -> {
                    String importPath = directive.getImportedFqName().asString();
                    return directive.isAllUnder() ?
                            thisPackageName.equals(importPath) :
                            importPath.equals(thisFullyQualifiedName);
                });
    }




    @Override
    public KotlinSemanticPsiTreeNode getParent() {
        return parent;
    }

    public T getPsiElement() {
        return psiElement;
    }

    public String getFullyQualifiedNameOfContainingFile() {
        return KotlinPsiTreeUtil.getFullyQualifiedFileName(getContainingKtFile());
    }

    public KtFile getContainingKtFile() {
        return KotlinPsiTreeUtil.findContainingKtFile(getPsiElement());
    }

    public File getContainingFile() {
        return KotlinPsiElementUtil.getContainingFile(getContainingKtFile());
    }

    public String getRelativePathOfContainingFile() {
        return KotlinSourceCodeUtil.getRelativePathFromFullyQualifiedName(getFullyQualifiedNameOfContainingFile());
    }

    public String encapsulateContextWithParentNodes(String context, boolean encapsulateSelf) {
        if (this instanceof KotlinSemanticPsiTreeChildNode<?> childNode) {
            return encapsulateContextWithParentNodes(context, childNode, encapsulateSelf);
        }
        return context;
    }

    private String encapsulateContextWithParentNodes(String context, KotlinSemanticPsiTreeChildNode<?> childNode, boolean encapsulateSelf) {
        KotlinSemanticPsiTreeNode current = childNode;
        String result = context;

        //first encapsulate itself
        if (encapsulateSelf && childNode instanceof KotlinSemanticPsiTreeContextEncapsulatorNode selfEncapsulator) {
            result = selfEncapsulator.encapsulateChildContext(result);
        }

        while (current instanceof KotlinSemanticPsiTreeChildNode<?> currentChild) {
            current = currentChild.getParent();
            if (current instanceof KotlinSemanticPsiTreeContextEncapsulatorNode encapsulator) {
                result = encapsulator.encapsulateChildContext(result);
            }
        }

        return result;
    }

    @Override
     public KotlinSemanticSelfPart<?> getSelfPart() {
        return kotlinSemanticSelfPart;
    }

    public KotlinSemanticPsiTreeFile getContainingKotlinSemanticPsiTreeFile() {
        if (this instanceof KotlinSemanticPsiTreeFile) {
            return (KotlinSemanticPsiTreeFile) this;
        }

        Optional<KotlinSemanticPsiTreeFile> fileOptional = findParentNodeOfType(KotlinSemanticPsiTreeFile.class);
        if (fileOptional.isEmpty()) {
            throw new IllegalStateException("Node must be part of a file: " + this.getClass().getSimpleName());
        }
        return fileOptional.get();
    }

}