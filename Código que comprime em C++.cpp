/******************************************************************************

                              Online C++ Compiler.
               Code, Compile, Run and Debug C++ program online.
Write your code in this editor and press "Run" button to compile and execute it.

*******************************************************************************/

#include <iostream>

using namespace std;

int main()
{
    //Vetor: tamanho 3 
    //<3, 7, 12> 
    
    //Objetivo: Multiplicar pela matriz 
    //arr[] = {8, 11, 7, 9, 12, 4}
    int bernoulli[] = {8, 11, 7, 9, 12, 4};
    
    //bernoulli[0], [1] e [2]: Primeira linha
    //bernoulli[3], [4] e [5]: Segunda linha
    
    const int tamanhoMatrizEntrada = 3;
    //É o mesmo tamanho das linhas da matriz de Bernoulli
    
    const int tamanhoMatrizOutput = 2;
    //É o tamanho da matriz de output 
    
    //A seguinte equação deve ser verdade:
    //tamanhoMatrizEntrada*tamanhoMatrizOutput = bernoulli[].length
    
    	 
int arr[] = {3, 7, 12};
int j = 0;

int pointertoloopThrough = tamanhoMatrizEntrada;


while(j<tamanhoMatrizOutput){
	
	int looperThroughMatrix = 0;
	int resposta = 0;
	
	while(looperThroughMatrix<tamanhoMatrizEntrada){
		resposta += arr[looperThroughMatrix]*bernoulli[pointertoloopThrough+looperThroughMatrix-tamanhoMatrizEntrada];
		looperThroughMatrix++;
		
	}
	
	cout<<resposta; //resposta => Output
	cout<<"\n";
	
	j++;
	pointertoloopThrough+=tamanhoMatrizEntrada;
	
}
}
