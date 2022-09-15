import os
import statistics

path_of_the_directory= os. getcwd()
print("Results of Readings in Folder")
for filename in os.listdir(path_of_the_directory):

    
    arrayFileName = filename.split('.');
    if arrayFileName[-1] == 'py':
        continue;

    f = os.path.join(path_of_the_directory,filename)
    if os.path.isfile(f):
        print(filename)
        with open(filename) as f:
         median = f.readlines();
         positionArray = True;
         arraydeLeituras = [];
         reading = ""
         i = 0;

         for line in median:
             i+=1;

             if i <= 24:
                 continue;

             for c in line:
                 if c == "	":
                     positionArray = False;

                 if positionArray == True:
                    continue;

                 reading = reading + c;    

             reading = reading.replace("\t", "").replace("\n", "").replace(",", ".");
             number = float(reading)
             arraydeLeituras.append(number)
             reading = "";
             positionArray = True; 

        print(statistics.median(arraydeLeituras))
os.system("pause")