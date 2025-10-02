package br.ufsm.poli.csi.redes.service;

import br.ufsm.poli.csi.redes.model.Mensagem;
import br.ufsm.poli.csi.redes.model.Usuario;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import netscape.javascript.JSObject;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.*;

/*
 * criar DatagramSocket
 * montar DatagramPacket
 * chamar socket.send()
 * receive()
 * broadcast para descobrir usuarios
 * mensagem de sonda afeta usuario listener - tem lista de usuarios na classe
 * */

//mensagem na interface --> chama essa classe
public class UDPServiceImpl implements UDPService {
    //atributos
    private DatagramSocket dtSocket;
    private final int porta = 8080;
    private Usuario usuario;
    //listeners para notificar a interface
    private UDPServiceMensagemListener mensagemListener;
    private UDPServiceUsuarioListener usuarioListener;


    //construtor
    public UDPServiceImpl() {
        try {
            //cria um socket
            this.dtSocket = new DatagramSocket(porta);
            System.out.println("UDPServiceImpl estabeleciado na porta: " + this.porta);

            //threads
            new Thread(new EnviaSonda()).start();
            new Thread(new EscutaSonda()).start();

        } catch (SocketException e) {
            throw new RuntimeException("ERRO ao estabelecer serviço UDP", e);
        }

    }


    //thread de enviar sonda
    private class EnviaSonda implements Runnable {
        @Override
        public void run() {
            while (true) {
                if (usuario == null) {
                    continue;
                }
                //manda sonda
                Mensagem mensagem = new Mensagem();
                mensagem.setTipoMensagem(Mensagem.TipoMensagem.sonda);
                mensagem.setUsuario(usuario.getNome());
                mensagem.setStatus(usuario.getStatus().toString());
                //converte para string
                ObjectMapper mapper = new ObjectMapper();
                String strMensagem = null;
                try {
                    strMensagem = mapper.writeValueAsString(mensagem);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                //converte para byte
                byte[] bMensagem = strMensagem.getBytes();

                //manda packet
                DatagramPacket pacote = null;
                for (int i = 1; i < 255; i++) {
                    System.out.println("entoru no for\n");
                    try {
                        pacote = new DatagramPacket(
                                bMensagem, bMensagem.length,
                                InetAddress.getByName("192.168.83." + i),
                                8080
                        );
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket();
                    } catch (SocketException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        socket.send(pacote);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(5000); //envia sonda a cada 5s
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    //thread de escutar
    private class EscutaSonda implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    //buffer vazio para receber dados da rede
                    byte[] buffer = new byte[1024]; //4096
                    DatagramPacket pacoteRecebido = new DatagramPacket(buffer, buffer.length);

                    //espera pacote chegar na 8080
                    dtSocket.receive(pacoteRecebido);

                    //converte bytes para string json
                    String jsonRecebido = new String(pacoteRecebido.getData(), 0, pacoteRecebido.getLength()); //pega apenas o conteudo da mensagem

                    //converte string json para mensagem de volta
                    ObjectMapper mapper = new ObjectMapper();
                    Mensagem msg = mapper.readValue(jsonRecebido, Mensagem.class);



                } catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    @Override
    public void enviarMensagem(String mensagem, Usuario destinatario, boolean chatGeral) {

    }

    private Usuario usuario = null; //referencia o usuario atual

    @Override
    public void usuarioAlterado(Usuario usuario) {
        this.usuario = usuario;
        //chama sempre que mandar uma osnda
    }

    private UDPServiceMensagemListener mensagemListener = null;

    @Override
    public void addListenerMensagem(UDPServiceMensagemListener listener) {
        //"esse eh o meu listener, quando receber mensagem me chama por aqui"
        this.mensagemListener = listener;
    }

    private UDPServiceUsuarioListener usuarioListener = null;

    @Override
    public void addListenerUsuario(UDPServiceUsuarioListener listener) {
        this.usuarioListener = listener;
    }
}
