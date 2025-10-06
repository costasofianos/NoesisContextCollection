# Noesis Context Collection  
*A Framework for Structural Code Relationship Exploration and Context Optimization for LLM-Assisted Code Completion*

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](LICENSE)

## Overview  
This repository contains the implementation and experimentation framework described in the paper:  
**“Exploration of Structural Code Relationship Space for Context Collection Optimization.”**  

The framework was developed as a solution to the **JetBrains + Mistral AI Context Collection Competition**, addressing the challenge of optimizing **LLM-assisted code completion** across diverse Kotlin codebases.  

It enables **dynamic experimentation** with multiple context-collection strategies, leveraging **semantic structural analysis** and **targeted pruning** to improve model performance while maintaining efficiency and transparency.  

---

## Core Idea  
Code completion quality depends heavily on the relevance of contextual code provided to the LLM.  
This project introduces a **semantic tree abstraction** built on Kotlin’s PSI, allowing structural and association-based pruning of irrelevant code.  

The key idea is to **balance signal and noise** in the provided context — capturing the *most semantically meaningful* code elements while pruning what’s less relevant.  

---

## Key Features  
- 🧩 **Composable Framework** — Combine and experiment with multiple context collection strategies.  
- ⚙️ **Dynamic Parameterization** — Tune thresholds and association weights to explore the parameter space.  
- 🌳 **Semantic Tree Layer** — A lightweight semantic abstraction built on top of the Kotlin PSI for efficient traversal.  
- 🔍 **Transparent Reporting** — Generate human-readable HTML reports showing selected snippets, weights, and reasoning.  
- 🚀 **Extensible Architecture** — Designed for easy integration of new strategies or additional language support.  

---

## Experimentation Highlights  
Experiments were conducted using three LLMs provided in the competition: **Codestral**, **Mellum**, and **Qwen-Coder**.  

### Key Findings
- **Concise structural associations**—particularly method signatures of elements referenced *from* the edit location—provided the strongest signal for code completion.  
- **Hierarchical context pruning** improved performance across all LLMs.  
- **Recently modified code** and **method name matching** strategies further enhanced results.  
- Performance varied by model: *Codestral benefited most from additional context*, while *Mellum* showed moderate gains and *Qwen-Coder* underperformed.  

These results validate the approach and highlight the need for automated, large-scale exploration of configuration parameters to identify optimal performance for each model.

---

## Framework Architecture  
The framework is composed of several core components:

| Component | Description |
|------------|-------------|
| **Strategy Combination Executor** | Runs multiple context selection strategies and merges their results with weighting and prioritization. |
| **Dynamic Computation Cache** | Caches PSI and semantic computations for reuse, reducing redundant parsing. |
| **Semantic Tree Parser** | Constructs an efficient semantic layer on top of the Kotlin PSI tree to represent classes, methods, and variables hierarchically. |
| **Reporting Engine** | Produces detailed HTML reports showing how each snippet was selected and weighted. |

See the following diagram from the paper for an overview of the architecture:  
*(Insert `StrategyCombiningArchitecture.png` here)*

---

## Future Work  
The current results were achieved through **manual exploration** of the parameter space.  
Future work aims to:
- Implement **automated large-scale evaluation** for systematic exploration.  
- Add **multi-model optimization** to adapt parameter sets for specific LLMs.  
- Integrate **additional context-collection strategies** to push the upper bounds of achievable performance.  

---

## Citation  
If you reference this work or build upon it, please cite:

```bibtex
@article{sofianos2025noesis,
  title={Exploration of Structural Code Relationship Space for Context Collection Optimization},
  author={Sofianos, Constantinos},
  year={2025},
  note={Independent Research}
}
