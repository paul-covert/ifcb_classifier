# IFCB Classifier

This repository is a fork of the original ```WHOIGit/ibcb_classifier repo```, an image classifying program designed to be trained on plankton images from an IFCB datasource.  The ```legacy``` branch is identical to ```WHOIGit/ifcb_classifier v0.3.1```, with the exception of some dependency changes.  The ```main``` branch contains a migration of the original to the modern PyTorch packages (```pytorch>=2.5.0```, etc.).

For details on usage, please see [the WHOIGit repository wiki](https://github.com/WHOIGit/ifcb_classifier/wiki).  A comparison of models trained with both branches is given below, as well as a list of changes made to the original code.

* Added ```env.forge.yml``` in ```requirements```.  This environment file specifies that all dependencies come from the conda-forge channel, eliminating the licensing restrictions associated with Anaconda distributions.  Additionally, ```env.forge.yml``` includes the dependencies for the ```pyifcb``` package, ensuring that all packages are installed with conda/mamba.  Finally, ```env.forge.yml``` points to the local ```pyifcb``` package included in the ifcb_classifier:legacy repository.
