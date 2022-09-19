import matplotlib.pyplot as plt
import numpy as np
import os

x = np.array([20, 40, 80, 160, 240])
y = np.array([0.043126, 0.049044, 0.064825, 0.08258, 0.111841])
xticks = ['20mhz', '40mhz', '80mhz', '160mhz', '240mhz']
plt.xticks(x, xticks)
plt.plot(x, y, color='green')
plt.title(label="Consumo de Corrente com Bluetooth a 80mhz",
          fontsize=20)

plt.show()

