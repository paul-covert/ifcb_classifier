# Java GUI for the IFCB Classifier

This is an implementation of the IFCB Classifier (https://github.com/WHOIGit/ifcb_classifier) with an added Java graphical user interface (GUI) for ease of use. The original Python script has been modified to work with the most recent version of PyTorch (2.8.0 as of September 2025), and some experimental additions were added to the code.

As a disclaimer, I am an employee of Fisheries and Oceans Canada, and am not affliated with the Woods Hole Oceanographic Institution, nor the developers of the original IFCB Classifier code.

New additions include:
- Visuals showing epoch scores as a model is being trained, as well as a confusion matrix once it is finished.
- Visuals showing results charts for unlabelled data being tested on a pre-made training model.
- New options for how the image is modified to fit the image size required for the model (in addition to simply resizing them as in the original):
  - Padding images that are smaller than the required size and panning to the centre on images that are larger.
  - The same as above, but randomly panning the image instead.
  - Padding each image into a square and then resizing, in order to preserve the aspect ratio.
  - Padding images that are smaller than the required size, but simply resizing larger images.
- 'Class minimum' can now be higher than the 'class maximum', as the former refers to the number of images a class in the dataset must contain in order to be used, whereas the latter is the limit on how many are actually used.
- An option to add a small red square to the top left corner of the image, in order to provide recognizable metric of aspect ratio changes to the images.

<p align="center">
  <img src=https://github.com/htleblond/icfb_classifier_gui/blob/main/ifcb_gui_training_example.png />
</p>
<p align="center">
  <em>The classifier after training a model.</em>
</p>


<p align="center">
  <img src=https://github.com/htleblond/icfb_classifier_gui/blob/main/ifcb_gui_testing_example.png />
</p>
<p align="center">
  <em>The classifier after testing unlabelled data on a pre-made model file.</em>
</p>

