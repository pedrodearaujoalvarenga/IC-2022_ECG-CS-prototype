import matplotlib.pyplot as plt
import numpy as np
import os

x = np.array([20, 40, 80, 160, 240])
y = np.array([0.043126, 0.048715, 0.064825, 0.082251, 0.111512])
xticks = ['20mhz', '40mhz', '80mhz', '160mhz', '240mhz']
plt.xticks(x, xticks)
plt.plot(x, y, color ='red')
plt.title(label="Consumo de Corrente com Bluetooth a 160mhz",
          fontsize=20)

plt.show()

