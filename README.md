# SELF-ASSEMBLY-DRONES
Given a 2-dimensional drawing represented by a matrix of pixels, this program simulates a group of autonomous drones assembling themselves to replicate the provided drawing  as quickly as possible.

## PREREQUISITES
This program needs the newest version of Java (1.8 or above) installed before. You can get the newest version of Java [here](https://java.com/en/download/)

## INSTALLING
You can install the program by double click on file setup.jar or use the following command in Linux terminal of the current directory: 
```
java -jar setup.jar
```
Follow all the steps until the program is successfully installed on your computer.
For it to be run on CSE server, you need to give execution permission to the start_model.bat and start_model.command files. To do so, use the following commands in the program folder: 
```
chmod +x start_model.bat
chmod +x start_model.command
```
After that, you can start the model by double click on start_model.bat (for Windows) or use the following command (for Linux): 
```
./start_model.command
```

## RUNNING
- While the program is running, click on Parameters tab to set the initial parameters. 
- The edge of communicating square should be an odd integer starting from 5. A maximum of 13 is preferred because larger value may cause the program to slow down.
- The grid size will be a fixednteger of 50.
- The map sharing strategy will be an integer between 0 and 3 where 0 is sharing nothing, 1 is sharing drone's own explored map, 2 is sharing connected portions of the whole map, and 3 is sharing everything.
- The noise level can be any integer between 0 and 100. However, 0/10/20/30/40/50 are prefered because of the efficiency. Larger value of noise may cause the program to slow down.
- The type of shape should be an integer between 0 and 5 where 0 is a line, 1 is a square, 2 is a circle, 3 is a triangle, 4 is a complex triangle, and 5 is a star.
- When everything is set, click on the Initialize Run button to initialize the environment. Then click on either Start Run or Step Run to run the simulation. 
- When a run finishes, click on Reset Run to reset the setting to try a new one.

## BUILT WITH
- [Repast Simphony 2.5](https://repast.github.io/repast_simphony.html) - an interactive Java-based modeling system

## AUTHORS
- Trieu Hung Tran
- Minal Khatri 
