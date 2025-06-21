# IFCB Classifier
## pytorch_2.x

This repo is a fork of an image classifying program designed to be trained on plankton images from an IFCB datasource.  It has been updated to work with the modern PyTorch and Lightning packages.  This update allows for deployment on all modern operating systems (Linux, Windows, MacOS) and GPU architectures (CUDA, MPS).  The SLURM functionality has been removed, as we have no way to test it.  In all other respects, the code is the same as the original version.  This can be verified by comparing results of tests run with the current and ```legacy_pytorch_1.7.1``` branches.


For details on usage, please see [this repository's wiki](https://github.com/WHOIGit/ifcb_classifier/wiki)

