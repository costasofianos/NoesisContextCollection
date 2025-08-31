ok, here is my main input, please could you help me structure this with the right headings, and see where there are gaos and I have not joined the concepts correctly. Or whwat I need to emphasise given the goals and audience of the paper.  Note that I have not gone into details in some areas, e.g. the design for making application dynamic and iterable, so you can tell me where you think more detail would be appropriate

These are the two major design criteria when building the application:

1.  Build it from the ground up to be able to easily create and combine different strategies due to the fact that this needed to be an iterative and experimental process. This is because the nature of the task is such and there is no proven correct approach to submitting to llms.

2. Performance.   The solution needed to be fast enough for the iterative process and to be within the parameters of the competition.   Also, it needed to be feasible for real life applications in terms of speed to make it useful.

3. Transparency.  Also, the application was built from the outset to show the context results, and show how each snippet was prioritised and chosen. This included a weighting system which showed different weights for each code snippet and how it was calculated. 

Once the framework was built, I created a simple strategy with simple token matching, and even though there was some positive results,  this approach did not offer much depth in terms of progressing further and improving the results.

Out of the various possible strategies, the main approach that emerged as the most likely to yield results and allow constant improvements was to parse the code into a syntax tree.

Initially tried an approach using the Kotlin Psi Tree, but when trying to work with it, it did not directly serve the purpose, because it was simply a parser and provided a syntax tree, but not a semantic tree.  

In order to continue the iterative design approach, it was first attempted to use the compiler to find these connection, but performance was an inssue.  After that, I built a semantic tree structure on top of the psi tree syntax tree.  This allowed me to find meaningful relationships in teh code. For example, I could lin a class instantiation with the actual class.

In addition, the sementic tree offered an additional feature to the process which was essential to the main approach.  It offered the ability to convert code into a format that would be favourable to the llm.  For example, I could choose to only show the method signatures of public methods inside a class.  This could also be dynamic depending on the reaosn for choosing the code or the weighting.   

It turns out that this is a known approach abd there is a paper on this (which you showed me and I hope you can help me find it again, that uses the approach of providing code skeletons)

The results from that paper proved that this was a promising approach, and combined with the dynamism of the application, I believe offers a good path towards getting better results.


Biggest Challenge

Each AI handled different approaches differently, and there was not enough iterations online to experiment and get the balance right. I found Qwen Coder especially diffult and dropped the score on iterations that did very well with Misral and Mellum.  Investment in setting up infrastructure for local testing at scale woule the obvious next step. Especially since the whole approach is designed for this.  This could even be automated to travsers the options and asjust weightings using an optimisation approach.  