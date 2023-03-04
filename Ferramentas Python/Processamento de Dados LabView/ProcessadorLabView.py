#Esse código consegue:
    #1. Ler o conteúdo de um arquivo oriundo do LabView, do código que foi feito no CEFET no laboratório presente do almoxarifado.
    #2. Ignorar todo o texto e números extras e extrair somente as leituras de consumo de corrente.
    #2.1 As leituras de consumo de corrente tem seu valor retornado pela função "def lerConteudodoArquivo"
    #3. Salvar o arquivo num arquivo .npy, mais leve e simples
    #4. Printar seu consumo de corrente médio


import os
import statistics
import numpy as np
path_of_the_directory= os. getcwd()

#função que, dado um filename que esteja no path_of_the_directory (definido por padrão por os.getcwd() => local onde o arquivo py se localiza) irá retornar uma
#lista com todas as leituras de consumo de corrente.

def lerConteudodoArquivo(filename):

    f = os.path.join(path_of_the_directory,filename)
    if os.path.isfile(f):
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

        return arraydeLeituras


#Este script abaixo lê todos os arquvios presentes na pasta e executa o código de leitura do conteúdo em todos. Note que o código nãoo funciona se
#tiver outros arquivos (fora py, txt ou npy na mesma pasta)

for filename in os.listdir(path_of_the_directory):

    arrayFileName = filename.split('.');
    if arrayFileName[-1] == 'py':
        continue;
    if arrayFileName[-1] == 'txt':
        continue
    if arrayFileName[-1] == 'npy':
        continue
    
    print("Arquivo sendo lido: " + filename)
    arrayColetada = lerConteudodoArquivo(filename)
    print("Média do Consumo: "+ str(statistics.mean(arrayColetada)))
    np.save("arrayNumpy " + filename, arrayColetada)



os.system("pause")