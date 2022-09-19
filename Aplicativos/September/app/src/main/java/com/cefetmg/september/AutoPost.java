package com.cefetmg.september;

import java.util.ArrayList;

public class AutoPost {

    private ArrayList<Integer> vetor = new ArrayList<Integer>();
    private boolean nova_leitura;

    public AutoPost(ArrayList<Integer> vetor, boolean nova_leitura){
        this.vetor = vetor;
        this.nova_leitura = nova_leitura;
    }


}
