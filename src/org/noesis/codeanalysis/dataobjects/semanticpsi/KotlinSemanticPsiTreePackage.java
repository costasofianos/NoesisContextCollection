package org.noesis.codeanalysis.dataobjects.semanticpsi;

import org.jetbrains.kotlin.psi.KtPackageDirective;
import org.jetbrains.kotlin.psi.KtTypeReference;
import org.noesis.codeanalysis.dataobjects.enums.ElementVisibility;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeContextEncapsulatorNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticPart;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class KotlinSemanticPsiTreePackage extends KotlinSemanticPsiTreeChildNode<KtPackageDirective> implements KotlinSemanticPsiTreeContextEncapsulatorNode {

    private final KtPackageDirective ktPackageDirective;
    private final List<KotlinSemanticPsiTreeFile> files = new ArrayList<>();

    public KotlinSemanticPsiTreePackage(KtPackageDirective ktPackageDirective, KotlinSemanticPsiTreeNode parent) {
        super(ktPackageDirective, parent);
        this.ktPackageDirective = ktPackageDirective;
    }

    @Override
    public KotlinSemanticReference getSemanticReference() {
        return null;
    }

    @Override
    public List<KotlinSemanticPart<?>> getSemanticParts() {
        return List.of();
    }


    @Override
    public ElementVisibility getElementVisibility() {
        return ElementVisibility.PUBLIC;
    }

    @Override
    public String getTypeName() {
        return ktPackageDirective.getQualifiedName();
    }

    @Override
    public Set<String> getBodyReferences() {
        return Set.of(getTypeName());

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
        return getPublicContextForParent();
    }

    @Override
    public String getPublicContextForParent() {
        return getTypeName();
    }

    public void addFile(KotlinSemanticPsiTreeFile file) {
        //System.out.println("Adding file to package "+ktPackageDirective.getQualifiedName() + " : " +file.getTypeName() );
        files.add(file);
    }

    public List<KotlinSemanticPsiTreeFile> getFiles() {
        return files;
    }

    @Override
    public List<KotlinSemanticPsiTreeFile> getChildren() {
        return files;
    }

    @Override
    public String encapsulateChildContext(String context) {

        return context;
    }
}

