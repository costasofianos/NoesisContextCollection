package org.noesis.codeanalysis.util.lmstudio;

public class CodeCompletionPromptGenerator {

    public static String createCodeCompletionPrompt(String prefix, String suffix, String context) {

       StringBuilder promptBuilder = new StringBuilder();
       promptBuilder.append("<|repo_separator|>Repo\n");
       promptBuilder.append(context);
       promptBuilder.append("\n<|fim_prefix|>\n");
       promptBuilder.append(prefix);
       promptBuilder.append("\n<|fim_suffix|>\n");
       promptBuilder.append(suffix);
       promptBuilder.append("\n<|fim_middle|>\n");



        return promptBuilder.toString();
    }

//    public static String createCodeCompletionPrompt(String prefix, String suffix, String context) {
//        StringBuilder promptBuilder = new StringBuilder();
//        promptBuilder.append("You are an expert Kotlin code completion model. You are given a prefix and suffix, and your job is to fill in the code between them using the provided context entries.\n\n");
//
//        // Add context entries
//        promptBuilder.append("### Context Entries:\n");
//        promptBuilder.append(context).append("\n\n");
//
//        // Add prefix and suffix
//        promptBuilder.append("### Prefix:\n")
//                .append(prefix)
//                .append("\n\n")
//                .append("### Suffix:\n")
//                .append(suffix)
//                .append("\n\n");
//
//        // Add tasks
//        promptBuilder.append("### Task 1: Code Completion\n")
//                .append("Write the Kotlin code that should appear between the prefix and suffix.\n\n")
//                .append("### Task 2: Reflection and Analysis\n")
//                .append("Now answer the following questions:\n")
//                .append("1. Give an estimation between 0 and 1 of what you consider the probability that you have found the right code intended by the user?\n")
//                .append("2. What in the context helped you decide on the code?\n")
//                .append("3. Is there anything in the context that is missing (e.g. parents of existing classes, missing imports) that would have been significantly helpful (name specific examples)?\n")
//                .append("4. Is there anything in the formatting that would make things more clear to you? For example, if I put '...' for missing code such as method bodies, would that make things more clear\n")
//                .append("5. What, if anything, in the context was unclear, noisy, or misleading?\n")
//                .append("6. Did the documentation assist you in any way?\n");
//
//        System.out.println("Estimated tokens: " + TokenEstimator.estimateTokens(promptBuilder.toString()));
//
//        return promptBuilder.toString();
//    }




}
