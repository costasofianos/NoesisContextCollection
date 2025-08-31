package org.noesis.codeanalysis.dataobjects.associations;

import org.noesis.codeanalysis.dataobjects.semanticpsi.node.KotlinSemanticPsiTreeChildNode;
import org.noesis.codeanalysis.dataobjects.semanticpsi.nodecomponents.general.KotlinSemanticReference;

import java.util.Objects;

public record SemanticNodeAssociation<T extends KotlinSemanticPsiTreeChildNode<?>>
        (
                KotlinSemanticReference kotlinSemanticReference,
                T associatedNode,
                float weight,
                boolean incomingAssociation
        )
{
        public String toString() {
                return  "-------------------------\n"+
                        "SEMANTIC REFERENCE\n\n" + kotlinSemanticReference +
                        "\n\n-------------------------\n" +
                        "ASSOCIATED NODE\n\n" +
                        "\nNAME\n" +associatedNode.getTypeName()+
                        "\nTYPE\n" +associatedNode.getClass().getSimpleName()+
                        "\nWEIGHT" + weight
                        ;
        }

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || !SemanticNodeAssociation.class.equals(o.getClass())) return false;

                SemanticNodeAssociation<?> that = (SemanticNodeAssociation<?>) o;

                return Objects.equals(kotlinSemanticReference.getReferenceName(), that.kotlinSemanticReference.getReferenceName()) &&
                        Objects.equals(associatedNode.getPsiElement().getText(), that.associatedNode.getPsiElement().getText());
        }

        @Override
        public int hashCode() {
                return Objects.hash(
                        kotlinSemanticReference.getReferenceName(),
                        associatedNode.getPsiElement().getText()
                );
        }
        public SemanticNodeAssociation<T> clone() {
                return new SemanticNodeAssociation<>(
                        this.kotlinSemanticReference,
                        this.associatedNode,
                        this.weight,
                        this.incomingAssociation
                );
        }





}

