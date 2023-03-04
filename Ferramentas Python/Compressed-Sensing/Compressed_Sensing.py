#Este código:
# 1. Lê uma amostra de ECG contida no arquivo na mesma pasta deste "ECG.npy"
# 2. Executa o script da linha 189 em diante, que contém a seguinte sequência de ações:
    # 2.1: Extrai 7000 da totalidade de amostras do ECG.
    # 2.2: Remove amostras aleatórias, de forma semelhante à que ocorre na ESP32.
    # 2.3: Reconstrói o sinal.
    # 2.4: Calcula PRD e RMSE para avaliação de qualidade.
    # 2.5: Gera um gráfico contendo somente 300 amostras do sinal original, reconstruído e do sinal-diferença.
    # 2.6: Repete este procedimento 5x.




import os
import statistics
import numpy as np
import matplotlib.pyplot as plt
import scipy.fftpack as spfft
import cvxpy as cvx
from math import sqrt
import numpy
import math

path_of_the_directory= os. getcwd()
arraydeLeituras = numpy.load("ECG.npy")

#Solver é o algoritmo utilizado para a biblioteca cvxpy para resolver o problema de otimização convexa (problema que se trata de
# encontrar a matriz reconstruída)

#Existem vários tipos de solvers diferentes, como ECOS, SCS, OSQP.
#Existem também solvers pagos, melhores do que esses gratuitos.
#Nossos testes foram feitos utilizando a licença de teste do solver MOSEK. Foi necessário me registrar para a versão trial e durou apenas um mês, mas
#também é possível solicitar o uso do mesmo através do e-mail de uma instituição acadêmica.
#Solvers diferentes irão obter resultados melhores ou piores, e com maior ou menor velocidade. Obtive velocidades no MOSEK (chutando de cabeça) umas 6x mais rápidas do que
#com o ECOS, padrão.
#Testei também o SCS, que é bem rápido e não perdeu qualidade.

solver = 'ECOS'

arraydeLeituras = arraydeLeituras[50:7050] #Usar 7000 amostras para o ECG. Comecei a partir da amostra 50 pra ficar mais bonito no gráfico.


#Função que recebe sinal da ESP (10 bits) para normalizado (que varia de -1 até 1)
def normalizarSinal(listaECG):
    listaECG_float = [(x / 1023.0) * 2 - 1 for x in listaECG]
    return listaECG_float



#PRD e RMSE foram calculados ambos os sinais normalizados.
#Função que retorna o RMSE (métrica de qualidade de sinal. Segundo o Alexandre, de modo geral, muito bom é de menor que e-2.)
def RMSE(original, compressed):
    original = normalizarSinal(original)
    compressed = normalizarSinal(compressed)
    MSE = np.square(np.subtract(original,compressed)).mean() 
    return math.sqrt(MSE)

#Função que retorna o PRD (métrica de qualidade de sinal. Segundo Alexandre, de modo geral, bom é menor que 9%.)
def PRD(original, compressed):


    original = normalizarSinal(original)
    compressed = normalizarSinal(compressed)

    somatoria1 = 0
    somatoria2 = 0
    for idx, x in enumerate(original):
        somatoria1 = somatoria1 + pow((original[idx] - compressed[idx]), 2)
        somatoria2 = somatoria2 + pow((original[idx]), 2)
    return sqrt(somatoria1/somatoria2)*100    
#Lembrando que "bom" varia da aplicação e de cada caso. Mas essas métricas seriam o ideal.


#Função que pega uma array, remove amostras aleatórias e reconstrói. Retorna a array reconstruída (Compressed-Sensing)
def pegarArray_removerAmostras_reconstruir(arrayOriginal, compressRatio):
        
        #Pegar sinal 
        y = arrayOriginal
        y = np.array(y)
        n = len(y)


        # Extrair pedaços aleatórios do sinal.
        #Atenção: As posições dos pedaços removidos são armazenados no ri.
        #Numa implementação real, é necessário que o algoritmo daqui tenha a mesma seed de geração de dados aleatórios da ESP, rodando em C++.

        m = int((100-compressRatio)*n/100)
        ri = np.random.choice(n, m, replace=False)
        ri.sort() # sorting not strictly necessary, but convenient for plotting
        y2 = y[ri]


        #Geração da Transformada Discreta Inversa de Cosseno.
        #A Matriz da Transformada Discreta Inversa de Cosseno tem os mesmos índices da matriz comprimida.
        A = spfft.idct(np.identity(n), norm='ortho', axis=0)
        A = A[ri]

        #Criação do problema de otimização convexa.
        #Esse problema, basicamente, tem o objetivo de MINIMIZAR a norma 1 da matriz resultado
        #porém, simultaneamente, essa matriz deve preencher A*vx = y2.
        #Isso é melhor explicado no nosso artigo.

        vx = cvx.Variable(n)
        objective = cvx.Minimize(cvx.norm(vx, 1))
        constraints = [A*vx == y2]
        prob = cvx.Problem(objective, constraints)
        result = prob.solve(verbose=True, solver=solver)

        #Armazenar o resultado    
        x = np.array(vx.value)
        x = np.squeeze(x)
        sig = spfft.idct(x, norm='ortho', axis=0)


        #retornar
        arrayOriginal = np.array(arraydeLeituras)
        return np.array(sig)

#Função que pega a array de 10 bits da ESP e converte em leitura de corrente (de 0V a 3.3V)
def transformarSinalem3_3Voltz(array10bitsESP):
    arraycom3_3V = []
    for x in array10bitsESP:
        arraycom3_3V.append(x*3.3/1023)
    return arraycom3_3V    

#Função que pega calcula o Sinal-Diferença (diferença entre sinal original e reconstruído)
def calcularSinalDiferenca_deslocado0_5V(arraysemCS, arraycomCS):
    comCS = np.array(arraycomCS)
    semCS = np.array(arraysemCS)
    return comCS - semCS - 0.5


numerodeTestes = 5
#Função principal que, 5x (número de testes), gera um arquivo PNG, faz um gráfico com o sinal original, reconstruído e diferença.
#retorna uma string contendo o PRD médio das reconstruções, o desvio padrão, o RMSE médio e o desvio padrão. o \pm é para inserir diretamente no LaTeX, onde o
#artigo é escrito.

def gerarGraficocomCS(compressRatio, filename, arraydeLeituras):
    listadePRDs = [] #variável só pra armazenar o PRD pra printar depois
    listadeRMSEs = [] #variável só pra armazenar o RMSE pra printar depois

    for numerodosarquivos in range(numerodeTestes): #Fazer o teste 5x

        plt.figure(figsize=(12, 4))
        plt.xlabel("Tempo (s)")
        plt.ylabel("Amplitude (V)")
        plt.yticks([-1.1, 0, 1.1, 2.2, 3.3])
        plt.axhline(y = -1.7, linewidth=0.1, color=(0, 0, 0, 0.5), linestyle = '-')
        plt.axhline(y = 3.5, linewidth=0.1, color=(0, 0, 0, 0.5), linestyle = '-')
        plt.margins(x=0, y=0)
        plt.xticks([0, 50, 100, 150, 200, 250, 300]
        , ["0", "0.5", "1", "1.5", "2", "2.5", "3"])

        arrayPlotada = transformarSinalem3_3Voltz(arraydeLeituras)

        sinalReconstruido = pegarArray_removerAmostras_reconstruir(arraydeLeituras, compressRatio)
        arrayPlotadacomCS = sinalReconstruido
        arrayPlotadacomCS = transformarSinalem3_3Voltz(arrayPlotadacomCS)

        arrayPlotada = arrayPlotada[0:301]
        arrayPlotadacomCS = arrayPlotadacomCS[0:301]

        plt.plot(arrayPlotada, color = 'blue', linewidth=0.6)
        plt.plot(arrayPlotadacomCS, color = "red", linewidth=0.6)


        sinalDif = calcularSinalDiferenca_deslocado0_5V(arrayPlotada, arrayPlotadacomCS)
        plt.plot(sinalDif, linewidth =0.6, color = 'green')


        nomedosarquivos = str(numerodosarquivos) + filename
        plt.savefig(nomedosarquivos, dpi=800, bbox_inches='tight')
        plt.clf()

        listadePRDs.append(PRD(arraydeLeituras, sinalReconstruido))
        listadeRMSEs.append(RMSE(arraydeLeituras, sinalReconstruido))    


    return "PRD: $" + str(statistics.mean(listadePRDs)) + "\pm" + str(statistics.stdev(listadePRDs)) + "$" " RMSE: $" + str(statistics.mean(listadeRMSEs)) + "\pm" + str(statistics.stdev(listadeRMSEs)) + "$"    

listadeRespostas = []


#parte onde o código é oficialmente chamado.
#Este exemplo testa as compressões para 30, 50, 60 e 70%.
#As leituras de PRD e RMSE são escritas na listadeRespostas, que é printada posteriormente.


listadeRespostas.append(gerarGraficocomCS(30, "30%.png", arraydeLeituras))
listadeRespostas.append(gerarGraficocomCS(50, "50%.png", arraydeLeituras))
listadeRespostas.append(gerarGraficocomCS(60, "60%.png", arraydeLeituras))
listadeRespostas.append(gerarGraficocomCS(70, "70%.png", arraydeLeituras))


for x in listadeRespostas:
    print(x)

