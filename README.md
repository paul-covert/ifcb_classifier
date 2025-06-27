# IFCB Classifier

This repo is a fork of an image classifying program designed to be trained on plankton images from an IFCB datasource.  It has been migrated to the modern PyTorch and Lightning packages.  This update allows for deployment on all modern operating systems (Linux, Windows, MacOS) and GPU architectures (CUDA, MPS).  The SLURM functionality has been removed, as we have no way to test it.  In all other respects, the code is the same as the original version.  This can be verified by comparing results of tests run with the current and ```legacy_pytorch_1.7.1``` branches.


For details on usage, please see [the WHOIGit repository wiki](https://github.com/WHOIGit/ifcb_classifier/wiki)

## Changes to original code

- "ptl.callbacks.base.Callback" replaced with "ptl.callbacks.Callback".
- Loaders are now used as parameters for the NeustonModel object, as they don't work as parameters when the trainer fits the model anymore.
- 'input_classes', 'output_classes', 'input_srcs' and 'outputs' are now placed into a separate dictionary object ('unloggable_dict') instead of being logged, as logging lists doesn't work anymore.
- 'training_epoch_end' changed to 'on_train_epoch_end' as the lingo was out of date (ditto for validation and testing).
- 'steps' now stored in separate lists and cleared after their respective 'on_epoch_end' function is called, as said function no longer accepts 'steps' as a parameter.
- 'gpus' and 'checkpoint_callback' removed as parameters from the Trainers, as they are no longer valid for whatever reason.
- Added parameters to Trainers: accelerator='gpu', devices=1
- Added parameter to dataloaders: persistent_workers=True

## Comparison of v2025.07a2 with v0.3.1

To confirm successful migration to PyTorch 2.x, comparison between a model trained with the updated code and the ```legacy_pytorch_1.7.1``` was made. (TBD)


