clear all,close all, clc;





N=1024;   
load('Bernoulli.mat'); % ajuste CR entre 1 a 19 para ajustar a taxa de compress?o

y=[1, 2, 3, 4, 5]

y=y';

M=length(y);
CR = (N-M)/N;

Phi=BernoulliSample(1:M,:);            

   
ecgstr=['ecg1'];                 
x=cell2mat(struct2cell(load(ecgstr)));     



[ww]=dwt( N,'db2',5);
Psi=[ww];                                  
T=Phi*Psi'; 



hat_s=Alg_bp(y,T,N);    
hat_x=real(Psi'*hat_s); 
                

PRD=norm(x-hat_x)/norm(x)*100;    

fprintf( 'PRD = %.2f \n', PRD); % ideal < 9

fprintf( 'Compressao = %.0f \n', round(M/N*100));

%fprintf('CR',round(M/N*100,0));

%,'% ','......',' PRD ',num2str(round(PRD,1)) );
clf;

plot(x+1,'k','linewidth',1)
hold on

plot((hat_x-0.15),'r','linewidth',1)
plot((x-hat_x-0.9),'b', 'LineWidth',1)
set(legend('$\ {x(t)}$ ','$\hat{x}(t)$','Differenca'),'Interpreter','Latex','FontSize', 10)
set(gca,'xlim',[0 1024])
xlabel('Samples');
ylabel('Volts');
wav = 'db2'
suptitle(['Wavelet ' wav,'......', ' CR ',num2str(round(M/N*100,0)),'% ','......',' PRD ',num2str(round(PRD,1))] );