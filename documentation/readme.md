# Batch Context Collector
This Docker container runs the Batch Context Collector application for analyzing code contexts.
## Prerequisites

### 1. Docker installed on your system
 
### 2. Create Workspace Directory

## Create Workspace Structure

Workspace directory, referred to as <workspace_directory>, stores the necessary test data and is the target folder to which predictions are placed.
Create the workspace directory with th necessary structure and permissions as defined below 

#### Data Folder

`<workspace_directory>/data/`

##### Repositories

`<workspace_directory>/data/repositories-<language>-<stage>`

Contains the repos for the test data in the standard format e.g. <workspace_directory>/data/repositories-kotlin-practice

##### Input Lines

`<workspace_directory>/data/<language>-<stage>.jsonl`

jsonl input lines e.g. <workspace_directory>/data/kotlin-practice.jsonl

#### Predictions Folder

`<workspace_directory>/predictions/`

This directory is where the predictions are stored. The program can create this directory if required.  
When running, the docker runtime must have the permissions to create this directory, or it must be created.  If it is created upfront, 
permissions must allow the docker runtime to create the prediction file in there.

#### Predictions File

`<workspace_directory>/predictions/<language>-<stage>-<strategy>.jsonl`

This file be created when running the docker file

## Download Docker Image

Download image from here:

https://drive.google.com/file/d/10YN9hsGGdgTngDEGgI0KZgdRmXRstdE6/view?usp=sharing

## Run the Container

The container requires a mounted volume for accessing the source code to analyze. The mount point inside the container is . `/mnt/ContextCollectionWorkspace`
### Basic Usage
``` bash
docker run -v <workspace_directory>:/mnt/ContextCollectionWorkspace batch-context-collector
```
This is the command to run the private phase. No other inputs are required. 
The optional parameters provided below are provided for reference in the case of wanting to re-run practice or public stages.

### Command Line Arguments
The following arguments are supported:
- `--stage=<value>`: Set the analysis stage (default: "private")
    - Valid values: "local", "practice", "public", "private"

- `--lang=<value>`: Set the programming language (default: "kotlin")
- `--workspace=<value>`: Set the workspace path (default: "/mnt/ContextCollectionWorkspace/")

### Example Commands
Run with default settings:
``` bash
docker run -v <workspace_directory>:/mnt/ContextCollectionWorkspace batch-context-collector
```
Run with specific stage and language:
``` bash
docker run -v <workspace_directory>:/mnt/ContextCollectionWorkspace batch-context-collector --stage=practice --lang=kotlin
```
Run with custom workspace path:
``` bash
docker run -v <workspace_directory>:/mnt/custom_workspace batch-context-collector --workspace=/mnt/custom_workspace
```
## Results

Output will be generated in the mounted volume directory in the 'predictions' folder described above 

