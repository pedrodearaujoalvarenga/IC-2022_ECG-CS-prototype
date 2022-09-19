import matplotlib.pyplot as plt
import numpy as np
import os

grossuraLegenda = 1;

x = np.array([1, 2, 3, 4, 5])
y = np.array([0.042797, 0.048386, 0.065154, 0.082908, 0.111841])
xticks = ['20mhz', '40mhz', '80mhz', '160mhz', '240mhz']
plt.xticks(x, xticks)
plt.plot(x, y, color = 'blue', linewidth=grossuraLegenda )


x = np.array([1, 2, 3, 4, 5])
y = np.array([0.043126, 0.048715, 0.064825, 0.082251, 0.111512])
xticks = ['20mhz', '40mhz', '80mhz', '160mhz', '240mhz']
plt.xticks(x, xticks)
plt.plot(x, y, color ='red', linewidth=grossuraLegenda)


x = np.array([3, 4, 5])
y = np.array([0.066141, 0.074689, 0.084224])
xticks = ['80mhz', '160mhz', '240mhz']
plt.xticks(x, xticks)
plt.plot(x, y, color='purple', linewidth=grossuraLegenda)



x = np.array([1, 2, 3, 4, 5])
y = np.array([0.043126, 0.049044, 0.064825, 0.08258, 0.111841])
xticks = ['20mhz', '40mhz', '80mhz', '160mhz', '240mhz']
plt.xticks(x, xticks)
plt.plot(x, y, color='green', linewidth=grossuraLegenda)



plt.title(label="Consumo de Corrente Rajada x Contínuo")

plt.show()
#plt.savefig('image.png', dpi =800)

