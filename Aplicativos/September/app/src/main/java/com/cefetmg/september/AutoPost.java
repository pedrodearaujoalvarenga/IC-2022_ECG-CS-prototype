package com.cefetmg.september;

import java.util.ArrayList;

public class AutoPost {

    private ArrayList<Integer> vetor = new ArrayList<Integer>();

    public boolean isNova_leitura() {
        return nova_leitura;
    }

    public void setNova_leitura(boolean nova_leitura) {
        this.nova_leitura = nova_leitura;
    }

    private boolean nova_leitura;

    public AutoPost(ArrayList<Integer> vetor, boolean nova_leitura){
        this.vetor = vetor;
        this.nova_leitura = nova_leitura;
    }
}
