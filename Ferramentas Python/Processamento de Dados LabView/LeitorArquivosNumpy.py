#Este código é um exemplo de como se lê arquivos .npy

import numpy as np
data = np.load('arrayNumpy 80 continuo comCS50-1_001.npy')
print(np.mean(data))