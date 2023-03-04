# Protótipo de IoT Wereable para Eletrocardiograma com Bluetooth Low Energy e Compressive Sensing

Este repositório contém as principais ferramentas utilizadas para o projeto de Iniciação Científica realizado em 2022. Se tratou de um dispositivo IoT wereable que faz eletrocardiograma e transmite dados de forma incompleta e esparsa via BLE, com o objetivo de economizar energia no próprio ao enviar menos dados. Foi elaborado um artigo científico medindo a redução de consumo proporcionada por este método.

Em síntese, o projeto possuiu um aplicativo e um microcontrolador ESP32. O aplicativo faz upload dos dados recebidos via BLE do microcontrolador por um servidor e a reconstrução dos dados é feita utilizando Python. Neste repositório constam ambos os aplicativos utilizados para testes, ferramentas em Python para processamento de dados, análise de consumo de corrente, processamento de dados e Compressed-Sensing, bem como os códigos do microcontrolador ESP32. Há também documentos de texto explicando o que cada código faz e como usá-lo.

Basta navegar a cada um dos arquivos e explorar, cada um dos blocos de código está amplamento comentado.
