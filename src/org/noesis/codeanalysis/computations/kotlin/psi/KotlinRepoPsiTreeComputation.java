package org.noesis.codeanalysis.computations.kotlin.psi;

import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.com.intellij.psi.PsiManager;
import org.jetbrains.kotlin.psi.KtFile;
import org.noesis.codeanalysis.dataobjects.input.ContextCollectionInput;
import org.noesis.codeanalysis.dataobjects.returnobjects.KotlinPsiExtract;
import org.noesis.codeanalysis.interfaces.Computation;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinEnvironmentUtil;
import org.noesis.codeanalysis.util.kotlin.psi.KotlinPsiExtractor;

import java.util.List;

public class KotlinRepoPsiTreeComputation extends Computation {


     List<KtFile> originalPsiTree;
     KotlinCoreEnvironment kotlinCoreEnvironment;
     PsiManager psiManager;
     Project project;
     KotlinEnvironmentUtil kotlinEnvironmentUtil;

     public KotlinRepoPsiTreeComputation(ContextCollectionInput contextCollectionInput) {
          super(contextCollectionInput);

          System.out.println("Parsing Kotlin Repo: " + getContextCollectionInput().getRepoFolder());

          kotlinEnvironmentUtil = new KotlinEnvironmentUtil();
          kotlinCoreEnvironment = kotlinEnvironmentUtil.createEnvironment(getContextCollectionInput().getRepoFolder());
          KotlinPsiExtract kotlinPsiExtract = KotlinPsiExtractor.getPsiTree(kotlinCoreEnvironment);
          originalPsiTree = kotlinPsiExtract.ktFiles();
          //findAndPrintAppBarFiles();

          psiManager = kotlinPsiExtract.psiManager();
          project = kotlinPsiExtract.project();



     }

     public List<KtFile> getOriginalPsiTree() {
          return originalPsiTree;
     }

     public KotlinCoreEnvironment getKotlinCoreEnvironment() {
          return kotlinCoreEnvironment;
     }

     public PsiManager getPsiManager() {
          return psiManager;
     }

     public Project getProject() {
          return project;
     }

     @Override
     public void close() throws Exception {
          originalPsiTree = null;
          kotlinEnvironmentUtil.close();
          kotlinCoreEnvironment= null;
          psiManager= null;
          project= null;
     }
}