# IFCB Classifier

This repository is a fork of the original WHOIGit ibcb_classifier repo, an image classifying program designed to be trained on plankton images from an IFCB datasource.  The ```legacy``` branch is identical to WHOIGit/ifcb_classifier v0.3.1, with the exception that the dependencies have been updated to rely on conda-forge channels only.  The ```main``` branch contains the code that has been migrated to the modern PyTorch packages (pytorch>=2.5.0, etc.).  In addition, he SLURM functionality has been removed.

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


