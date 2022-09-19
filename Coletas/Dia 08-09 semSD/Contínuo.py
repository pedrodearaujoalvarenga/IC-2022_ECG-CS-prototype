import matplotlib.pyplot as plt
import numpy as np
import os

x = np.array([20, 40, 80, 160, 240])
y = np.array([0, 0, 0.066141, 0.074689, 0.084224])
xticks = ['20mhz', '40mhz', '80mhz', '160mhz', '240mhz']
plt.xticks(x, xticks)
plt.plot(x, y, color='purple')
plt.title(label="Consumo de Corrente com Bluetooth Contínuo",
          fontsize=20)

plt.show()

