import os
import statistics

path_of_the_directory= os. getcwd()
print("Results of Readings in Folder")

a = open("myfile.txt", "a")

with open("array.txt") as f:
    median = f.readlines();
    for line in median:

        for c in line:

            if c == "	":
                a.write(",")
    
            else:
                a.write(c)    
                

       


os.system("pause")