# IFCB Classifier

This repository is a fork of the original ```WHOIGit/ibcb_classifier repo```, an image classifying program designed to be trained on plankton images from an IFCB datasource.  The ```legacy``` branch is identical to ```WHOIGit/ifcb_classifier v0.3.1```, with the exception that the dependencies have been updated to rely on conda-forge channels only.  The ```main``` branch contains the code that has been migrated to the modern PyTorch packages (```pytorch>=2.5.0```, etc.).  In addition, he SLURM functionality has been removed.

For details on usage, please see [the WHOIGit repository wiki](https://github.com/WHOIGit/ifcb_classifier/wiki)
