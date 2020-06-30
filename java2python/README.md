# Java to Python translation services #

## Pre-requisites ##
* Anaconda

## Installation ##
**The following instructions are for a Linux environment:**

1. If you have not already done so, set up a conda environment required to run this Java to Python translation service.
(The same conda environment is used to support text2triples and other services under ModelsFromText).
To set up the conda environment, use the .yml file in the deps sub-folder:
```
conda env create -f deps/aske-ta1.yml
```

2. Run `source activate aske-ta1` to activate this conda environment, provided it has been created successfully.

3. Once this environment is activated, install the modified java2python translation package (provided as a whl file) within this environment, as follows:
```
python -m pip install deps/java2python-0.5.1-py3-none-any.whl
```

4. Start up the java to python translation services in the background as follows:
```
screen -S j2py python translate-new.py
```
Here, j2py is the screen name for the background process


## Validation and Example Usage ##

The services will be running if you are able to access its [API documentation](http://localhost:19092/darpa/aske/ui/)

(Tip: If you are behind a firewall, make sure your proxies are set correctly. Running into problems? Open an [issue](https://github.com/GEGlobalResearch/DARPA-ASKE-TA1/issues))
