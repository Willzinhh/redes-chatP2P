package br.ufsm.poli.csi.redes;

import br.ufsm.poli.csi.redes.swing.ChatClientSwing;

import java.net.UnknownHostException;

public class Main {
    public static void main(String[] args) throws UnknownHostException {
        if(args.length != 2) {
            System.err.println("Argumentos incorretos");
            System.exit(1);
        }
        try {
            int portaOrigem = Integer.parseInt(args[0]);
            int portaDestino = Integer.parseInt(args[1]);
            new ChatClientSwing(portaOrigem, portaDestino);
        } catch (NumberFormatException e) {
            System.err.println("Argumentos incorretos");
        }
    }
}