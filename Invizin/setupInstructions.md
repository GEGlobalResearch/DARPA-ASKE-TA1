Steps to setup and launch this InVizIn service: 
* To setup:
    * open command prompt and use aske-ta1-kchain-env.yml file from K-CHAIN folder to create a new conda environment:
      ```
      conda env create -f aske-ta1-kchain-env.yml
      ```
      skip this step, if K-CHAIN environment has already been setup as they use the same environment.
* After environment is setup, to use this service:
    * open command prompt, navigate to Invizin folder, and activate environment:
      ```
      conda activate aske-ta1-kchain-py36
      ```
    * launch invizin_app.py file
      ```
      python invizin_app.py
      ```
  
