# IFCB Classifier

This repo is a fork of an image classifying program designed to be trained on plankton images from an IFCB datasource.  It has been migrated to the modern PyTorch and Lightning packages.  This update allows for deployment on all modern operating systems (Linux, Windows, MacOS) and GPU architectures (CUDA, MPS).  The SLURM functionality has been removed, as we have no way to test it.  In all other respects, the code is the same as the original version.  This can be verified by comparing results of tests run with the current and ```legacy_pytorch_1.7.1``` branches.


For details on usage, please see [the WHOIGit repository wiki](https://github.com/WHOIGit/ifcb_classifier/wiki)

## Changes to original code

## Comparison of v2025.07a1 (not released as of 2025.06.24) with v0.3.1

To confirm successful migration to PyTorch 2.x, comparison between a model trained with the updated code and the ```legacy_pytorch_1.7.1``` was made.  


