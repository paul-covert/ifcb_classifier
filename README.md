# IFCB Classifier (modern_pytorch)

This repo is a fork of an image classifying program designed to be trained on plankton images from an IFCB datasource.  There are two branches available, ```main``` and modern_pytorch.  The main branch is unchanged from the original WHOIGit main branch of ifcb_classifier, forked on 2025-06-20.  The modern_pytorch branch contains code that has been updated to work with the modern PyTorch and Lightning packages.  This update allows for deployment on all modern operating systems (Linux, Windows, MacOS) and GPU architectures (CUDA, MPS).  The SLURM functionality has been removed, as we have no way to test it.  In all other respects, the code is the same as the original version.  This can be verified by running the tests under both the main and modern_pytorch branches.


For details on usage, please see [this repository's wiki](https://github.com/WHOIGit/ifcb_classifier/wiki)

