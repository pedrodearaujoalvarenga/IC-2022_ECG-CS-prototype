arraydeLeituras = []

with open('ECG.txt') as f:
         median = f.readlines();
         positionArray = True;

         reading = ""

         for line in median:

             for c in line:
                 if c.isdigit():
                    reading = reading + c
                 else: 
                    if reading == '':
                        continue
                    else:
                        #print(reading)
                        arraydeLeituras.append(int(reading))
                        reading = ''

import numpy as np
np.save("ECG.npy", arraydeLeituras)